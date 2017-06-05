/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.hsm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.dcm4che.util.Executer;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.mbean.TimerSupport;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2461 $ $Date: 2006-05-05 21:23:21 +0800 (周五, 05 5月 2006) $
 * @since Nov 22, 2005
 */
public class SyncFileStatusService extends ServiceMBeanSupport {

    private final TimerSupport timer = new TimerSupport(this);

    private static final String NONE = "NONE";
	private static final String INFO_PARAM = "%i";
	private static final String FILE_PARAM = "%f";
	private static final String DIR_PARAM = "%d";
	private static final String TIMER_ID = "SyncFileStatus";
	private static final String[] EMPTY_STRINGS = {};

	private long minFileAge = 0L;
	private long taskInterval = 0L;
    private int disabledStartHour;
    private int disabledEndHour;
    private int limitNumberOfFilesPerTask;
    
    private int checkFileStatus;
    private int commandFailedFileStatus;
    private int nonZeroExitFileStatus;
    private int matchFileStatus;
    private int noMatchFileStatus;
    private String[] fileSystems = {};
    private String[] command = { "mmls ", INFO_PARAM, "/", FILE_PARAM};
    private Pattern pattern;

    private Integer listenerID;
    
    private final NotificationListener timerListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (isDisabled(hour)) {
                if (log.isDebugEnabled())
                    log.debug("SyncFileStatus ignored in time between "
                            + disabledStartHour + " and " + disabledEndHour
                            + " !");
            } else {
                try {
                	check();
                } catch (Exception e) {
                    log.error("check file status failed!", e);
                }
            }
        }

    };

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final String getDirectoryPathList() {
		return fileSystems.length == 0 ? NONE 
				: StringUtils.toString(fileSystems, File.pathSeparatorChar);
	}

	public final void setDirectoryPathList(String str) {
		if (NONE.equalsIgnoreCase(str)) {
			this.fileSystems = EMPTY_STRINGS;
		} else {
			StringTokenizer tk = new StringTokenizer(" ,:;");
			this.fileSystems = new String[tk.countTokens()];
			for (int i = 0; i < fileSystems.length; i++) {
				fileSystems[i] = tk.nextToken();
			}
		}
	}

	public final String getCheckFileStatus() {
		return  FileStatus.toString(checkFileStatus);
	}

	public final void setCheckFileStatus(String status) {
		this.checkFileStatus = FileStatus.toInt(status);
	}

	public final String getNonZeroExitFileStatus() {
		return FileStatus.toString(nonZeroExitFileStatus);
	}

	public final void setNonZeroExitFileStatus(String status) {
		this.nonZeroExitFileStatus = FileStatus.toInt(status);
	}

	public final String getMatchFileStatus() {
		return FileStatus.toString(matchFileStatus);
	}

	public final void setMatchFileStatus(String status) {
		this.matchFileStatus = FileStatus.toInt(status);
	}

	public final String getNoMatchFileStatus() {
		return FileStatus.toString(noMatchFileStatus);
	}

	public final void setNoMatchFileStatus(String status) {
		this.noMatchFileStatus = FileStatus.toInt(status);
	}

	public final String getCommandFailedFileStatus() {
		return FileStatus.toString(commandFailedFileStatus);
	}

	public final void setCommandFailedFileStatus(String status) {
		this.commandFailedFileStatus = FileStatus.toInt(status);
	}

	public final String getCommand() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < command.length; i++) {
			sb.append(command[i]);
		}
		return sb.toString();
	}

	private String makeCommand(String dirParam, String fileParam, String infoParam) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < command.length; i++) {
			sb.append(command[i] == DIR_PARAM ? dirParam
					: command[i] == FILE_PARAM ? fileParam
					: command[i] == INFO_PARAM ? infoParam
					: command[i]);
		}
		return sb.toString();
	}
	
	public final void setCommand(String command) {
		String[] a = StringUtils.split(command, '%');
		try {
			String[] b = new String[a.length + a.length - 1];
			b[0] = a[0];
			for (int i = 1; i < a.length; i++) {
				String s = a[i];
				b[2 * i - 1] = ("%" + s.charAt(0)).intern();
				b[2 * i] = s.substring(1);
			}
			this.command = b;
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException(command);
		}
	}

	public final String getPattern() {
		return pattern.pattern();
	}

	public final void setPattern(String pattern) {
		this.pattern = Pattern.compile(pattern, Pattern.DOTALL);
	}
	
	public final String getTaskInterval() {
        String s = RetryIntervalls.formatIntervalZeroAsNever(taskInterval);
        return (disabledEndHour == -1) ? s : s + "!" + disabledStartHour + "-"
                + disabledEndHour;
    }

    public void setTaskInterval(String interval) {
        long oldInterval = taskInterval;
        int pos = interval.indexOf('!');
        if (pos == -1) {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval);
            disabledEndHour = -1;
        } else {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval
                    .substring(0, pos));
            int pos1 = interval.indexOf('-', pos);
            disabledStartHour = Integer.parseInt(interval.substring(pos + 1, pos1));
            disabledEndHour = Integer.parseInt(interval.substring(pos1 + 1));
        }
        if (getState() == STARTED && oldInterval != taskInterval) {
            timer.stopScheduler(TIMER_ID, listenerID, timerListener);
            listenerID = timer.startScheduler(TIMER_ID, taskInterval,
            		timerListener);
        }
    }

    public final String getMinimumFileAge() {
		return RetryIntervalls.formatInterval(minFileAge);
	}

	public final void setMinimumFileAge(String intervall) {
		this.minFileAge = RetryIntervalls.parseInterval(intervall);
	}

	public int getLimitNumberOfFilesPerTask() {
        return limitNumberOfFilesPerTask;
    }

    public void setLimitNumberOfFilesPerTask(int limit) {
        this.limitNumberOfFilesPerTask = limit;
    }
    
	private boolean isDisabled(int hour) {
        if (disabledEndHour == -1) return false;
        boolean sameday = disabledStartHour <= disabledEndHour;
        boolean inside = hour >= disabledStartHour && hour < disabledEndHour; 
        return sameday ? inside : !inside;
    }

    protected void startService() throws Exception {
        timer.init();
        listenerID = timer.startScheduler(TIMER_ID, taskInterval, timerListener);
    }

    protected void stopService() throws Exception {
        timer.stopScheduler(TIMER_ID, listenerID, timerListener);
        super.stopService();
    }
    

	public int check() throws Exception {
		FileSystemMgt fsmgt = newFileSystemMgt();
		int limit = limitNumberOfFilesPerTask;
		Timestamp before = new Timestamp(System.currentTimeMillis() - minFileAge);
		int count = 0;
		for (int i = 0; limit > 0 && i < fileSystems.length; i++) {
			FileDTO[] c = fsmgt.findFilesByStatusAndFileSystem(
					fileSystems[i].replace(File.separatorChar, '/'),
					checkFileStatus, before, limit);
			for (int j = 0; j < c.length; j++) {
				if (check(fsmgt, c[i]))
					++count; 
			}
			limit -= c.length;
		}
		return count;
	}

	private boolean check(FileSystemMgt fsmgt, FileDTO fileDTO) {
		int status = fileDTO.getFileStatus(); 
		String cmd = makeCommand(
				fileDTO.getDirectoryPath().replace('/', File.separatorChar), 
				fileDTO.getFilePath().replace('/', File.separatorChar),
				fileDTO.getUserInfo());
		try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(cmd, stdout, null);
            int exit = ex.waitFor();
            if (exit != 0) {
    			log.info("Non-zero exit code(" + exit + ") of " + cmd);
            	status = nonZeroExitFileStatus;
            } else {
            	String result = stdout.toString();
            	status = pattern.matcher(result).matches() 
            			? matchFileStatus : noMatchFileStatus;
            }
		} catch (Exception e) {
			log.error("Failed to execute " + cmd, e);
			status = commandFailedFileStatus;
		}
		if (fileDTO.getFileStatus() != status)
		{
			try {
				fsmgt.setFileStatus(fileDTO.getPk(), status);
				return true;
			} catch (Exception e) {
				log.error("Failed to update status of file " + fileDTO, e);
			}
		}
		return false;
	}

	private FileSystemMgt newFileSystemMgt() {
        try {
            FileSystemMgtHome home = (FileSystemMgtHome) EJBHomeFactory
                    .getFactory().lookup(FileSystemMgtHome.class,
                            FileSystemMgtHome.JNDI_NAME);
            return home.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access File System Mgt EJB:",
                    e);
        }
    }
}
