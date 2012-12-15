package ru.kwanza.easygrid.map.impl.distributed;

import ru.kwanza.easygrid.map.IConcurrentMap;
import ru.kwanza.easygrid.map.IMapObserver;
import ru.kwanza.easygrid.map.impl.AbstractConcurrentHashMap;
import ru.kwanza.easygrid.map.impl.HashEntry;
import ru.kwanza.easygrid.map.impl.IMapConfig;
import ru.kwanza.easygrid.map.impl.JMXUtil;
import org.jgroups.*;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.jmx.JmxConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.easygrid.map.impl.HashEntry;
import ru.kwanza.easygrid.map.impl.JMXUtil;

import javax.management.MBeanServer;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DistributedMap<K extends Serializable, V extends Serializable>
        implements ExtendedMembershipListener, ExtendedMessageListener, IMapObserver<K, V>, RequestHandler,
        IConcurrentMap<K, V>, DistributedMapMBean {
    private MessageDispatcher dispatcher = null;
    private static final Logger logger = LoggerFactory.getLogger(DistributedMap.class);
    private JChannel channel = null;
    private AbstractConcurrentHashMap<K, V,?> map = null;
    private String clusterName;
    private Type type;
    private IMapObserver<K, V> mapNotifier;
    private long updateStateTimeout;
    private long castMessageTimeout;

    public DistributedMap(String clusterName, ProtocolStackConfigurator configurator,
                          AbstractConcurrentHashMap<K, V, ?> map, Type type,
                          long updateStateTimeout, long castMessageTimeout, String jmxName) throws ChannelException {
        this.clusterName = clusterName;
        this.map = map;
        this.mapNotifier = map.getMapNotifier();
        this.map.setMapNotifier(this);
        this.type = type;
        this.updateStateTimeout = updateStateTimeout;
        this.castMessageTimeout = castMessageTimeout;
        channel = configurator == null ? new JChannel() : new JChannel(configurator);
        channel.setOpt(Channel.LOCAL, false);

        dispatcher = new MessageDispatcher();
        dispatcher.setChannel(channel);
        dispatcher.setMembershipListener(this);
        dispatcher.setMessageListener(this);
        dispatcher.setRequestHandler(this);
        DistributedMapManager.getInstance().addMap(this);
        start(jmxName);
    }

    private void start(String jmxName) throws ChannelException {
        logger.info("Start");
        dispatcher.start();
        logger.info("Group name: {}", clusterName);
        channel.connect(clusterName);
        final boolean state = channel.getState(null, updateStateTimeout);
        logger.info("Get state: {}", state);
        logger.info("==> Local address: {}", channel.getLocalAddress());

        if (jmxName != null) {
            try {
                String groupsName = jmxName + ", channel=JGroups";
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                JMXUtil.forcedRegister(this, jmxName);
                JmxConfigurator.registerChannel(channel, mbs, groupsName);
                JmxConfigurator.registerProtocols(mbs, channel, jmxName + ", channel=JGroups");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void initJMX(IMapConfig config) {

    }

    public void receive(Message message) {
    }

    public Object handle(Message message) {
        final Object obj = message.getObject();
        if (obj instanceof PutMessage) {
            PutMessage putMsg = (PutMessage) obj;
            K k = (K) putMsg.getKey();
            V v = (V) putMsg.getValue();
            map.put(k, v, false);
            if (mapNotifier != null) {
                mapNotifier.notifyPut(k, v);
            }
        } else if (obj instanceof RemoveMessage) {
            RemoveMessage removeMsg = (RemoveMessage) obj;
            K k = (K) removeMsg.getKey();
            map.remove(k, false);
            if (mapNotifier != null) {
                mapNotifier.notifyRemove(k);
            }
        } else if (obj instanceof ClearMessage) {
            map.clear(false);
            if (mapNotifier != null) {
                mapNotifier.notifyClearAll();
            }
        } else if (obj instanceof UpdateMessage) {
            UpdateMessage updateMsg = (UpdateMessage) obj;
            final HashEntry<K, V> entry = (HashEntry<K, V>) updateMsg.getEntry();
            ((AbstractConcurrentHashMap<K, V, HashEntry<K, V>>) map).updateEntry(entry, false);
            if (mapNotifier != null) {
                mapNotifier.notifyUpdate(entry);
            }
        }
        return null;
    }

    public void viewAccepted(View view) {
        logger.info("== Accepted" + view);
    }

    public void suspect(Address suspected_mbr) {

    }

    public void block() {
        logger.info("== BLOCK ==");
    }

    public void unblock() {
        logger.info("== UNBLOCK ==");
    }

    public void notifyPut(K k, V v) {
        Message msg = new Message(null, null, new PutMessage(k, v));
        // todo aguzanov ����� ������, ��� ������, ���� �� ������� ��������� ���������
        dispatcher.castMessage(null, msg, type.getCode(), castMessageTimeout);
        if (mapNotifier != null) {
            mapNotifier.notifyPut(k, v);
        }
    }

    public void notifyRemove(K k) {
        Message msg = new Message(null, null, new RemoveMessage(k));
        // todo aguzanov ����� ������, ��� ������, ���� �� ������� ��������� ���������
        dispatcher.castMessage(null, msg, type.getCode(), castMessageTimeout);
        if (mapNotifier != null) {
            mapNotifier.notifyRemove(k);
        }
    }

    public void notifyClearAll() {
        Message msg = new Message(null, null, new ClearMessage());
        // todo aguzanov ����� ������, ��� ������, ���� �� ������� ��������� ���������
        dispatcher.castMessage(null, msg, type.getCode(), castMessageTimeout);
        if (mapNotifier != null) {
            mapNotifier.notifyClearAll();
        }
    }

    public void notifyUpdate(Entry<K, V> entry) {
        Message msg = new Message(null, null, new UpdateMessage((HashEntry<Serializable, Serializable>) entry));
        // todo aguzanov ����� ������, ��� ������, ���� �� ������� ��������� ���������
        dispatcher.castMessage(null, msg, type.getCode(), castMessageTimeout);

        if (mapNotifier != null) {
            mapNotifier.notifyUpdate(entry);
        }
    }

    public byte[] getState(String state_id) {
        throw new UnsupportedOperationException("Only streaming state transferring is supported!");
    }

    public void setState(String state_id, byte[] state) {
        throw new UnsupportedOperationException("Only streaming state transferring is supported!");
    }

    public void getState(OutputStream ostream) {
        map.lockMap();

        logger.info("getState start");
        try {
            ObjectOutputStream os = new ObjectOutputStream(ostream);
            map.writeEntries(os);
            os.flush();
            os.close();
        } catch (IOException e) {
            logger.warn("Error on get state", e);
            //throw new RuntimeException(e);
        } finally {
            map.unlockMap();
        }
        logger.info("getState end");

    }

    public void getState(String state_id, OutputStream ostream) {
        logger.info("getState(String state_id, OutputStream ostream)");
    }

    public void setState(InputStream istream) {
        logger.info("Set state start");
        map.lockMap();
        try {
            final ObjectInputStream s = new ObjectInputStream(istream);
            for (; ;) {
                K key = (K) s.readObject();
                V value = (V) s.readObject();
                if (key == null) {
                    break;
                }
                map.put(key, value, false);
                if (mapNotifier != null) {
                    mapNotifier.notifyPut(key, value);
                }
            }
        } catch (Exception e) {
            logger.error("Error on setState", e);
            /*throw new RuntimeException(e);*/
        } finally {
            map.unlockMap();
        }
        logger.info("Set state end");
    }

    public void setState(String state_id, InputStream istream) {

    }

    public byte[] getState() {
        throw new UnsupportedOperationException("Support only streaming state transfer!");
    }

    public void setState(byte[] state) {
        throw new UnsupportedOperationException("Support only streaming state transfer!");
    }

    public V put(K key, V value) {
        return map.put(key, value);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    public void clear() {
        map.clear();
    }

    public V replace(K key, V value) {
        return replace(key, value, true);
    }

    public boolean replace(K key, V oldValue, V newValue) {
        return replace(key, oldValue, newValue, true);
    }

    public V remove(Object key) {
        return remove(key, true);
    }

    public boolean remove(Object key, Object value) {
        return remove(key, value, true);
    }

    public V putIfAbsent(K key, V value) {
        return putIfAbsent(key, value, true);
    }

    public int size() {
        return map.size();
    }

    public Collection<V> values() {
        return map.values();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    public V get(Object key) {
        return map.get(key);
    }

    public V put(K key, V value, boolean notify) {
        V v = map.put(key, value);
        if (notify && mapNotifier != null && !value.equals(v)) {
            mapNotifier.notifyPut(key, value);
        }
        return v;
    }

    public V putIfAbsent(K key, V value, boolean notify) {
        V v = map.putIfAbsent(key, value);
        if (notify && mapNotifier != null && !value.equals(v)) {
            mapNotifier.notifyPut(key, value);
        }
        return v;
    }

    public boolean remove(Object key, Object value, boolean notify) {
        boolean result = map.remove(key, value);
        if (notify && mapNotifier != null && result) {
            mapNotifier.notifyRemove((K) key);
        }
        return result;
    }

    public V remove(Object key, boolean notify) {
        V v = map.remove(key);
        if (notify && mapNotifier != null && v != null) {
            mapNotifier.notifyRemove((K) key);
        }
        return v;
    }

    public boolean replace(K key, V oldValue, V newValue, boolean notify) {
        boolean result = map.replace(key, oldValue, newValue);
        if (notify && mapNotifier != null && result) {
            mapNotifier.notifyUpdate(map.getEntry(key));
        }
        return result;
    }

    public V replace(K key, V oldValue, boolean notify) {
        V value = map.replace(key, oldValue);
        if (notify && mapNotifier != null && !oldValue.equals(value)) {
            mapNotifier.notifyUpdate(map.getEntry(key));
        }
        return value;
    }

    public void clear(boolean notify) {
        map.clear();
        if (notify && mapNotifier != null) {
            mapNotifier.notifyClearAll();
        }
    }

    public void copyToMap(Map<K, V> values) {
        map.copyToMap(values);
    }

    public int estimatedCount() {
        return map.estimatedCount();
    }

    public void drainToMap(Map<K, V> values) {
        map.drainToMap(values);
    }

    public void lockMap() {
        // todo aguzanov �������������� ����������?
        map.lockMap();
    }

    public void unlockMap() {
        // todo aguzanov �������������� ����������?
        map.unlockMap();
    }

    public void writeEntries(ObjectOutputStream os) throws IOException {
        map.writeEntries(os);
    }

    public void readEntries(ObjectInputStream is) throws IOException, ClassNotFoundException {
        map.readEntries(is);
    }

    public void setMapNotifier(IMapObserver<K, V> mapNotifier) {
        this.mapNotifier = mapNotifier;
    }

    public IMapObserver<K, V> getMapNotifier() {
        return mapNotifier;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getReplicationTypeName() {
        return type.name();
    }

    public long getUpdateStateTimeout() {
        return updateStateTimeout;
    }

    public long getCastMessageTimeout() {
        return castMessageTimeout;
    }

    public void close() {
        channel.close();
    }
}
