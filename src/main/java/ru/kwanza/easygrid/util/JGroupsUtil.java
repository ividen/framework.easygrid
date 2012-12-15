package ru.kwanza.easygrid.util;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Guzanov
 */
public class JGroupsUtil {
    private static final Logger logger = LoggerFactory.getLogger("com.intervale.datacache");

    public static void printViewInfo(JChannel channel, Class component, View view) {
        logger.info(getViewDumpInfo(channel, component.getSimpleName(), view).toString());
    }

    public static StringBuffer getViewDumpInfo(JChannel channel, String componentName, View view) {

        StringBuffer buffer =
                new StringBuffer(componentName).append("(").append(channel.getClusterName()).append(")[")
                        .append(view.getMembers().size()).append("]\n\t{");
        for (Address a : view.getMembers()) {
            buffer.append("\n\t\t").append(a);
            if (a.equals(view.getVid().getCoordAddress())) {
                buffer.append(" - coordinator");
            }
            if (a.equals(channel.getLocalAddress())) {
                buffer.append(" - this");
            }
        }
        buffer.append("\n\t}");
        return buffer;
    }
}
