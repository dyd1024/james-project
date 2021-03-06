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

package org.apache.james.imap.api.message;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.util.Locale;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Capability {
    public static Capability of(String value) {
        Preconditions.checkNotNull(value, "'value' can not be null");
        Preconditions.checkArgument(!value.isEmpty(), "'value' can not be empty");

        return new Capability(value.toUpperCase(Locale.US));
    }

    private final String value;
    private final byte[] bytes;

    private Capability(String value) {
        this.value = value;
        this.bytes = value.getBytes(US_ASCII);
    }

    public String asString() {
        return value;
    }

    public byte[] asBytes() {
        return bytes;
    }

    @Override
    public final boolean equals(Object o) {
        if (o instanceof Capability) {
            Capability that = (Capability) o;

            return Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("value", value)
            .toString();
    }
}
