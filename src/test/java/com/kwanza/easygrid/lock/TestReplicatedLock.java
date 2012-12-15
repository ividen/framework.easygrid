package com.kwanza.easygrid.lock;

//import com.hazelcast.core.Hazelcast;

//import com.hazelcast.core.Hazelcast;

import ru.kwanza.easygrid.distributedlock.DistributedLockManager;
import org.jgroups.ChannelException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.Lock;

/**
 * @author: Alexander Guzanov
 */
public class TestReplicatedLock {

    public static void main(String[] args) throws InterruptedException, ChannelException, IOException {
        DistributedLockManager manager = new DistributedLockManager("Test");
        Lock[] locks = new Lock[1];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = manager.getLock("Test" + i);
        }
//        Lock lock = Hazelcast.getLock("Test");
//        for(int i = 0;i<100;i++){
//            DistributedLock.getLock("Test" + i);
//        }
        Thread.sleep(1000);
        RandomAccessFile randomAccessFile = new RandomAccessFile("test.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();

        FileLock fileLock = null;

        for (int i = 0; i < 100000000; i++) {
            for (int j = 0; j < locks.length; j++) {
                locks[j].lock();
            }
            try {
                fileLock = channel.tryLock();
                if (fileLock == null) {
                    throw new RuntimeException("Cant;t lock");
                }
                if (i % 1000 == 0) {
                    System.out.println(i);
                }

            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (fileLock != null) {
                    fileLock.release();
                }
                for (int j = 0; j < locks.length; j++) {
                    locks[j].unlock();
                }
            }
        }
        randomAccessFile.close();
        System.out.println("FINISHED!!!!");
        Thread.currentThread().join();
    }
}
