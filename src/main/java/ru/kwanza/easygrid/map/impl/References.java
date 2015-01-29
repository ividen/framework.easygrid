package ru.kwanza.easygrid.map.impl;

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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

/**
 * @author Alexander Guzanov
 */
class References {
    static final ReferenceQueue<StrongEntry> REFERENCE_QUEUE = new ReferenceQueue<StrongEntry>();

    static {
        final Thread thread = new Thread(new RejectHandler());
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    private static final class RejectHandler implements Runnable {
        public void run() {
            for (; ;) {
                try {
                    final Reference<? extends StrongEntry> reference = REFERENCE_QUEUE.remove();
                    if (reference instanceof SoftEntry) {
                        SoftEntry e = (SoftEntry) reference;
                        e.segment.removeEntryReference(e);
                    } else if (reference instanceof WeakEntry) {
                        WeakEntry e = (WeakEntry) reference;
                        e.segment.removeEntryReference(e);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
