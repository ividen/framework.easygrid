package ru.kwanza.easygrid.distributedlock;

import org.jgroups.Address;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Alexander Guzanov
 */
class LockCommand {
    private static final String UTF_8 = "UTF-8";
    private LockType type;
    private String lockName;
    private Address address;

    public LockCommand(Address address, byte[] buffer) {
        this.type = LockType.find(buffer[0]);
        try {
            this.lockName = new String(buffer, 1, buffer.length - 1, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        this.address = address;
    }

    public LockCommand(LockType type, String lockName, Address address) {
        this.type = type;
        this.lockName = lockName;
        this.address = address;
    }

    public LockCommand(LockType type, String lockName) {
        this.type = type;
        this.lockName = lockName;
    }

    public LockType getType() {
        return type;
    }

    public String getLockName() {
        return lockName;
    }

    public Address getAddress() {
        return address;
    }

    public byte[] asBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(type.getCode());
        try {
            stream.write(lockName.getBytes(UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }
}
