package org.dcm4chee.arr.util;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;

/**
 * EJB3 client utilities
 * 
 * @author fang.yang@agfa.com
 * @version $Id$
 * @since Aug 23, 2006 11:27:09 AM
 * 
 */
public class Ejb3Util {
	public static final Logger log = Logger.getLogger(Ejb3Util.class);

	private static Ejb3Util _instance = new Ejb3Util();

	private InitialContext ctx = null;

	private static Map<Class, String> remoteJNDIs = new HashMap<Class, String>();

	private static Map<Class, String> localJNDIs = new HashMap<Class, String>();

	private Ejb3Util() {
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			log.error("Couldn't initialize EJB3 context", e);
		}
	}

	/**
	 * Assume the JNDI name follow the following pattern: dcm4chee/remote/<Interface Class Name>
	 * 
	 * @param <T>
	 *            the remote interface class
	 * @param interfaceClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T> T getRemoteInterface2(Class<T> interfaceClass) {
		try {
			return (T) _instance.ctx.lookup("dcm4chee/remote/" + interfaceClass.getSimpleName());
		} catch (NamingException e) {
			log.error("Couldn't look up session bean: " + interfaceClass.getName(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Assume the JNDI name follow the following pattern: dcm4chee/remote/<Interface Class Name>
	 * 
	 * @param <T>
	 *            the local interface class
	 * @param interfaceClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T> T getLocalInterface2(Class<T> interfaceClass) {
		try {
			return (T) _instance.ctx.lookup("dcm4chee/local/" + interfaceClass.getSimpleName());
		} catch (NamingException e) {
			log.error("Couldn't look up SLSB interface: " + interfaceClass.getName(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the remote interface of the SLSB that implements the provided interface.
	 * <p>
	 * Instead of making assumption of its JNDI name pattern, this method assume the SLSB class is annotated by
	 * 
	 * @RemoteBinding, through which we can retrieve the JNDI binding names.
	 * 
	 * @param <T>
	 *            the generics for the interface class
	 * @param interfaceClass
	 *            the interface implemented by the SLSB
	 * @return the remote interface
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRemoteInterface(Class<T> interfaceClass) {
		try {
			if (!remoteJNDIs.containsKey(interfaceClass)) {
				// Get the Bean class first since we need the annotations
				String beanClzName = interfaceClass.getName() + "Bean";
				Class beanClz = Class.forName(beanClzName);
				RemoteBinding remoteBinding = (RemoteBinding)beanClz.getAnnotation(RemoteBinding.class);
				remoteJNDIs.put(interfaceClass, remoteBinding.jndiBinding());
			}

			return (T) _instance.ctx.lookup(remoteJNDIs.get(interfaceClass));
		} catch (Exception e) {
			log.error("Couldn't look up SLSB remote interface: " + interfaceClass.getName(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the local interface of the SLSB that implements the provided interface.
	 * <p>
	 * Instead of making assumption of its JNDI name pattern, this method assume the SLSB class is annotated by
	 * 
	 * @LocalBinding, through which we can retrieve the JNDI binding names.
	 * 
	 * @param <T>
	 *            the generics for the interface class
	 * @param interfaceClass
	 *            the interface implemented by the SLSB
	 * @return the remote interface
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getLocalInterface(Class<T> interfaceClass) {
		try {
			if (!localJNDIs.containsKey(interfaceClass)) {
				// Get the Bean class first since we need the annotations
				String beanClzName = interfaceClass.getName() + "Bean";
				Class beanClz = Class.forName(beanClzName);
				LocalBinding localBinding = (LocalBinding)beanClz.getAnnotation(LocalBinding.class);
				localJNDIs.put(interfaceClass, localBinding.jndiBinding());
			}

			return (T) _instance.ctx.lookup(localJNDIs.get(interfaceClass));
		} catch (Exception e) {
			log.error("Couldn't look up SLSB local interface: " + interfaceClass.getName(), e);
			throw new RuntimeException(e);
		}
	}
}
