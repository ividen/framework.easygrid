package com.kwanza.easygrid.map;

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


/**
 * @author Alexander Guzanov
 */
public class ClusterTest {
    final static int ITEM_COUNT = 30;

    private static final String[] OP_NAMES = {"Put", "Remove", "Replace", "Clear"};

    public static void main(String args[]) throws Exception {
//        Properties p = new Properties();
//        p.put("log4j.rootLogger", "DEBUG,A1");
//        p.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
//        p.put("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
//        p.put("log4j.appender.A1.layout.ConversionPattern", "%d %-5p [%c{1}] %m%n");
//        p.put("log4j.appender.A1.Threshold", "DEBUG");
//        PropertyConfigurator.configure(p);
//        ClusterTest ct = new ClusterTest();
//        ct.work();
//        //ct.testTTLTimeOut();
    }

    private void testTTLTimeOut() throws InterruptedException {
//        MapBuilder<String, BigObject> builder = new MapBuilder<String, BigObject>();
//        final ConcurrentMap<String, BigObject> map = builder.strong().ttl().setTtlTimeout(1000).setTtlTimeUnit(TimeUnit.MILLISECONDS).newMap();
//
//        final String key = "1";
//        map.put(key, new BigObject());
//        BigObject bo = map.get(key);
//        Thread.sleep(500L);
//        bo = map.get(key);
//        assert bo!=null;
//        Thread.sleep(1000L);
//        bo = map.get(key);
//        assert bo==null;
    }

    private void work() throws Exception {
//        //final Map<String, String> map = new HashMap<String, String>();
//        MapBuilder<String, BigObject> builder = new MapBuilder<String, BigObject>();
//
//        //builder.strong().setConcurrencyLevel(8).setInitialCapacity(5).ttl().newMap();
//
//        //final ConcurrentMap<String, String> map = builder.strong().setConcurrencyLevel(8).setInitialCapacity(5).ttl().setChannelName("Test").newReplicatedMap();
//        final ConcurrentMap<String, BigObject> map = builder.strong().setConcurrencyLevel(8).setInitialCapacity(5).setChannelName("Test").newReplicatedMap();
//        final Random rnd = new Random();
//        Runnable r = new Runnable() {
//            public void run() {
//                logger.info("== Start ==");
//
//                for (int i = 0; i < ITEM_COUNT; i++) {
//                    final String key = "Sample_" + (Math.abs(rnd.nextInt()) % 10);
//                    //final String value = "Test_" + (Math.abs(rnd.nextInt()) % 1000);
//
//                    int operationCount = 2;//(i > ITEM_COUNT / 2) ? 4 : 3;
//                    int opCode = rnd.nextInt(operationCount);
//                    if (opCode == 3) opCode = rnd.nextInt(operationCount);
//                    logger.info("Operation: " + getOperationName(opCode));
//                    switch (opCode) {
//                        case 0:
//                            map.put(key,new BigObject());
//                            break;
//                        case 1:
//                            map.remove(key);
//                            break;
//                        case 2:
//                            map.replace(key, new BigObject());
//                            break;
//                        case 3:
//                            map.clear();
//                    }
//                    try {
//                        Thread.sleep(2000L);
//                    } catch (InterruptedException e) {
//                        logger.error("Fail", e);
//                    }
//                }
//                logger.info("== Stop ==");
//                try {
//                    Thread.sleep(10000L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//                for (Map.Entry<String, BigObject> entry : map.entrySet()) {
//                    logger.info(entry.getKey() + "->" + entry.getValue());
//                }
//                DistributedMapManager.getInstance().shutdownAll();
//            }
//
//            private String getOperationName(int opCode) {
//                return OP_NAMES[opCode];
//            }
//        };
//        (new Thread(r)).start();

    }
}
