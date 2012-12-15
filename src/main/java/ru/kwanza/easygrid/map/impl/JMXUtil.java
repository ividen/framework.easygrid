package ru.kwanza.easygrid.map.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @author Alexander Guzanov
 */
public class JMXUtil {
    private static final Logger logger = LoggerFactory.getLogger(JMXUtil.class);

    public static boolean forcedRegister(Object obj, String name) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = null;
        try {
            objectName = new ObjectName(name);
            mbs.registerMBean(obj, objectName);
        } catch (InstanceAlreadyExistsException e) {
            logger.warn("Register new object for name " + name);
            try {
                mbs.unregisterMBean(objectName);
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }

            try {
                mbs.registerMBean(obj, objectName);
            } catch (Exception ex) {
                throw new RuntimeException(e);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
