# Description

This framework implements associative maps with additional functionality and distributed network lock that is compatible with **java.util.concurrent.locks.Lock** interface.


Extended associative maps provide following functionality:

* Time-to-live strategy, life time of each element is restricted to configured value
* Tenaciouse-Of-life, life time of each element is prolonged if it is used often
* Weak/Soft References, it helps to clean garbage
* Replication, elements stored in cluster node with redundancy, supports SYNC/ASYNC mode of replication
* Listen events about removing/addition/updating of  element in the map
* Motinor&manage map throw jmx

Deistributed lock:

* uses "coordinator-slave" approach to manage lock acquiring and releasing queues
* uses cluster-networking framework **jgroups**
* correctly process reentrant mode


## Examples


Create hash-map with soft-reference values:
```java
IConcurrentMap<String, String> map = MapBuilder.simple().soft().<String, String>newMap();

```

Create hash-map with 4 seconds to time-to-live:
```java
IConcurrentMap<String, String> map = MapBuilder.ttl(4, TimeUnit.SECONDS).<String, String>newMap();

```
Create hash-карты with 40 seconds of time-to-live , prolonging life-time by 10 seconds for each hit, with weak reference and jmx-monitoring:
```java
 IConcurrentMap<String, String> map = MapBuilder
                .tol(40, TimeUnit.SECONDS, 10, TimeUnit.SECONDS)
                .weak()
                .jmx("myapp/")
                .<String, String>newMap();

```

Create distributed hash-map with synchronous replication , with  5 hours for time-to-live, with jmx-moтinoring and custom event listener:
```java
Map<String, Entry> map = MapBuilder.ttl(5, TimeUnit.HOURS)
                .distributed("StressTestReplicatedMap")
                .replicationType(Type.SYNC)
                .configurator("file:~/jgoups_tcp.xml")
                .mapNotifier(new IMapObserver() {
                    public void notifyPut(Object o, Object o1) {
                        System.out.println("Put " + o);
                    }

                    public void notifyRemove(Object o) {
                        System.out.println("Remove " + o);
                    }

                    public void notifyClearAll() {
                    }

                    public void notifyUpdate(Map.Entry entry) {
                        System.out.println("Update " + entry.getKey() + " value=" + entry.getValue());
                    }
                })
                .newMap();
```
Create distributed lock
```java
        DistributedLockManager manager = new DistributedLockManager("Test");  //cluster name in terms of jgroups
        final Lock lock = manager.getLock("TestLock");
        try{
            lock.lock();
            doWork();
        }finally {
            lock.unlock();
        }

        manager.close();
```

# Compare performance of DistributedLock with Hazelcast

There are two test cases:

* Fix number of nodes:  8
* Fix number of locks: 100

![938029180-Untitled4.png](https://bitbucket.org/repo/MXxRdA/images/3396160520-938029180-Untitled4.png)
