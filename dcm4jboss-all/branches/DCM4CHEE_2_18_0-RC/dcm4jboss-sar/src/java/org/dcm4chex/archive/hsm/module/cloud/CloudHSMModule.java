/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * itMD, LLC
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Damien Evans <damien.evans@itmd.net>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.hsm.module.cloud;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.hsm.module.AbstractHSMModule;
import org.dcm4chex.archive.hsm.module.HSMException;
import org.dcm4chex.archive.util.FileUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Rule;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class CloudHSMModule extends AbstractHSMModule {
    private static final int KB = 1024;

    private static final int MB = KB * KB;

    private File outgoingDir;

    private File absOutgoingDir;

    private File incomingDir;

    private File absIncomingDir;

    private String endPoint;
    
    private String bucketName;

    private String accessKey;

    private String secretKey;

    // the S3 client
    private AmazonS3 s3;

	private int connectionTimeout;

	private int readTimeout;

    public final String getOutgoingDir() {
        return outgoingDir.getPath();
    }

    public final void setOutgoingDir(String dir) {
        this.outgoingDir = new File(dir);
        this.absOutgoingDir = FileUtils.resolve(this.outgoingDir);
    }

    public final String getIncomingDir() {
        return incomingDir.getPath();
    }

    public final void setIncomingDir(String dir) {
        this.incomingDir = new File(dir);
        this.absIncomingDir = FileUtils.resolve(this.incomingDir);
    }

    @Override
    public void failedHSMFile(File file, String fsID, String filePath)
            throws HSMException {
        log.debug("failedHSMFile called with file=" + file + ", fsID=" + fsID
                + ", filePath=" + filePath);
        log.warn("failedHSMFile is not yet implemented for S3 integration");

    }

    @Override
    public File fetchHSMFile(String fsID, String filePath) throws HSMException {
        log.debug("fetchHSMFile called with fsID=" + fsID + ", filePath="
                + filePath);

        if (absIncomingDir.mkdirs()) {
            log.info("M-WRITE " + absIncomingDir);
        }

        File tarFile;
        try {
            tarFile = File.createTempFile("hsm_", ".tar", absIncomingDir);
        } catch (IOException x) {
            throw new HSMException("Failed to create temp file in "
                    + absIncomingDir, x);
        }

        log.info("Fetching " + filePath + " from cloud storage");

        FileOutputStream fos = null;
        try {
            if (s3 == null)
                createClient();

            S3Object object = s3.getObject(new GetObjectRequest(bucketName,
                    filePath));
            fos = new FileOutputStream(tarFile);
            IOUtils.copy(object.getObjectContent(), fos);
        } catch (AmazonClientException ace) {
            s3 = null;
            throw new HSMException("Could not list objects for: " + filePath,
                    ace);
        } catch (Exception x) {
            throw new HSMException("Failed to retrieve " + filePath, x);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Couldn't close output stream for: " + tarFile);
                }
            }
        }
        return tarFile;
    }

    @Override
    public void fetchHSMFileFinished(String fsID, String filePath, File file)
            throws HSMException {
        log.debug("fetchHSMFileFinished called with fsID=" + fsID
                + ", filePath=" + filePath + ", file=" + file);
        log.info("M-DELETE " + file);
        file.delete();
    }

    @Override
    public File prepareHSMFile(String fsID, String filePath)
            throws HSMException {
        log.debug("prepareHSMFile called with fsID=" + fsID + ", filePath="
                + filePath);
        return new File(absOutgoingDir, new File(filePath).getName());
    }

    @Override
    public Integer queryStatus(String fsID, String filePath, String userInfo)
            throws HSMException {
        log.debug("queryStatus called with fsID=" + fsID + ", filePath="
                + filePath + ", userInfo=" + userInfo);

        try {
            if (s3 == null)
                createClient();

            ObjectListing objectListing = s3.listObjects(bucketName, filePath);
            if (objectListing != null) {
                for (S3ObjectSummary objectSummary : objectListing
                        .getObjectSummaries()) {
                    log.debug("Listed " + objectSummary.getKey() + "  "
                            + "(size = " + objectSummary.getSize() + ")");
                    if (filePath.equals(objectSummary.getKey())) {
                        return FileStatus.ARCHIVED;
                    }
                }
            }
        } catch (AmazonClientException ace) {
            s3 = null;
            throw new HSMException("Could not list objects for: " + filePath,
                    ace);
        } catch (Exception e) {
            throw new HSMException("Could not list objects for: " + filePath, e);
        }

        return FileStatus.DEFAULT;
    }

    @Override
    public String storeHSMFile(File file, String fsID, String filePath)
            throws HSMException {
        log.debug("storeHSMFile called with file=" + file + ", fsID=" + fsID
                + ", filePath=" + filePath);

        String awsFileKey = filePath;
        log.info("Uploading " + file + " to S3 with key of " + awsFileKey);
        try {
            if (s3 == null)
                createClient();

            s3.putObject(new PutObjectRequest(bucketName, awsFileKey, file));
        } catch (AmazonClientException ace) {
            s3 = null;
            throw new HSMException("Could not store: " + filePath, ace);
        } catch (Exception e) {
            throw new HSMException("Could not store: " + filePath, e);
        }
        return filePath;
    }

    private synchronized void createClient() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey,
                secretKey);
        ClientConfiguration config = new ClientConfiguration();
        config.setConnectionTimeout(connectionTimeout);
        config.setSocketTimeout(readTimeout);
        s3 = new AmazonS3Client(credentials, config);
        if (endPoint != null)
        	s3.setEndpoint(endPoint);
        
    }

    public String getEndPoint() {
		return endPoint == null ? "DEFAULT" : endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = "DEFAULT".equalsIgnoreCase(endPoint) ? null : endPoint;
	}

	public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    
    public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		if (connectionTimeout != this.connectionTimeout) {
			s3 = null;
			this.connectionTimeout = connectionTimeout;
		}
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		if (readTimeout != this.readTimeout) {
			s3 = null;
			this.readTimeout = readTimeout;
		}
	}

	public String listBuckets() {
    	if (s3 == null)
    		this.createClient();
    	List<Bucket> buckets = s3.listBuckets();
    	StringBuilder sb = new StringBuilder();
    	for (Bucket b : buckets) {
    		sb.append("Name:").append(b.getName()).append(" (").append(b.getCreationDate()).append(") owner:").append(b.getOwner()).append("\n");
    	}
    	return sb.toString();
    }
    
    public void createBucket(String name) {
    	if (s3 == null)
    		this.createClient();
    	s3.createBucket(name);
    }
    
    public String showExpirationRules() {
    	if (bucketName == null)
    		return "No bucket name configured!";
    	if (s3 == null)
    		this.createClient();
    	BucketLifecycleConfiguration cfg = s3.getBucketLifecycleConfiguration(bucketName);
    	if (cfg == null)
    		return "Bucket has no LifecycleConfiguration";
    	StringBuilder sb = new StringBuilder();
    	sb.append("Lifecycle Rules:\n");
    	for (Rule r : cfg.getRules()) {
    		sb.append("Rule ").append(r.getId())
    		.append(": prefix:").append(r.getPrefix())
    		.append(", expirationInDays:").append(r.getExpirationInDays())
    		.append(", expirationDate:").append(r.getExpirationDate())
    		.append(", status:").append(r.getStatus()).append("\n");
    	}
    	return sb.toString();
    }
    
    public String addExpirationRule(String id, String prefix, int expirationInDays) {
    	if (s3 == null)
    		this.createClient();
    	BucketLifecycleConfiguration cfg = s3.getBucketLifecycleConfiguration(bucketName);
    	if (cfg == null) {
    		cfg = new BucketLifecycleConfiguration().withRules(new ArrayList<Rule>());
    	}
    	BucketLifecycleConfiguration.Rule rule = new BucketLifecycleConfiguration.Rule()
                .withId(id)
                .withPrefix(prefix)
                .withExpirationInDays(expirationInDays)
                .withStatus(BucketLifecycleConfiguration.ENABLED.toString());
    	cfg.getRules().add(rule);
    	s3.setBucketLifecycleConfiguration(bucketName, cfg);
    	return showExpirationRules();
    }
    
    public boolean removeExpirationRule(String id) {
    	if (s3 == null)
    		this.createClient();
    	BucketLifecycleConfiguration cfg = s3.getBucketLifecycleConfiguration(bucketName);
    	if (cfg == null) {
    		return false;
    	}
    	Rule rule = null;
    	for (Rule r : cfg.getRules()) {
    		if (r.getId().equals(id)) {
    			rule = r;
    			break;
    		}
    	}
    	if (rule != null) {
			cfg.getRules().remove(rule);
			if (cfg.getRules().isEmpty()) {
				s3.deleteBucketLifecycleConfiguration(bucketName);
			} else {
				s3.setBucketLifecycleConfiguration(bucketName, cfg);
			}
    	}
    	return rule != null;
    }
    
    public boolean removeBucketLifecycleConfiguration() {
    	if (bucketName == null)
    		return false;
    	if (s3 == null)
    		this.createClient();
    	s3.deleteBucketLifecycleConfiguration(bucketName);
    	
    	return s3.getBucketLifecycleConfiguration(bucketName) == null;
    }

}
