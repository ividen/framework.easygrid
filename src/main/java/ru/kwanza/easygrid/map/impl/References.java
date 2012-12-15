package ru.kwanza.easygrid.map.impl;

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
