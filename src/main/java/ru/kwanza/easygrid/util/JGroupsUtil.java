package ru.kwanza.easygrid.util;

/*
 * #%L
 * easygrid
 * %%
 * Copyright (C) 2015 Kwanza
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
