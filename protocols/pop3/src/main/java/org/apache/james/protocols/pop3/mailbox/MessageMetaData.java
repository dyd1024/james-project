/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.protocols.pop3.mailbox;

import com.google.common.base.CharMatcher;

/**
 * Hold meta data for a message
 */
public class MessageMetaData {

    private static final CharMatcher IS_RFC1939_COMPATIBLE = CharMatcher.inRange((char) 0x21, (char) 0x7E).precomputed();

    private final String uid;
    private final long size;

    public MessageMetaData(String uid, long size) {
        this.uid = uid;
        this.size = size;

        if (uid == null || !IS_RFC1939_COMPATIBLE.matchesAllOf(uid)) {
            throw new IllegalArgumentException("UID is not RFC1939 compatible");
        }
    }

    /**
     * Return the uid of the message
     */
    public String getUid() {
        return uid;
    }

    /**
     * Return the uid of the message. This method uses extra Mailbox ID argument to make
     * UID unique when it is not globally unique. By default assuming UID globally unique.
     */
    public String getUid(String mailboxId) {
        return uid;
    }

    /**
     * Return the size of a message
     */
    public long getSize() {
        return size;
    }
    
}
