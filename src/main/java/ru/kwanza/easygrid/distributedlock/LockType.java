package ru.kwanza.easygrid.distributedlock;

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
