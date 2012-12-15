package ru.kwanza.easygrid.distributedlock;

/**
 * @author Alexander Guzanov
 */
enum LockType {

    LOCK_COMMAND((byte) 1),
    RELEASE_COMMAND((byte) 2),
    WAKE_UP_COMMAND((byte) 3),
    AWAIT_CONDITION((byte) 4),
    SIGNAL_CONDITION((byte) 5),
    SIGNAL_ALL_CONDITION((byte) 5);

    LockType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static LockType find(byte code) {
        for (LockType lc : values()) {
            if (lc.code == code) {
                return lc;
            }
        }

        return null;
    }

    private byte code;

}
