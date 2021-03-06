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

package org.apache.james.mailbox.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class AttachmentMetadata {
    public static class Builder {
        private AttachmentId attachmentId;
        private Long size;
        private ContentType type;
        private MessageId messageId;

        public Builder messageId(MessageId messageId) {
            Preconditions.checkArgument(messageId != null);
            this.messageId = messageId;
            return this;
        }

        public Builder attachmentId(AttachmentId attachmentId) {
            Preconditions.checkArgument(attachmentId != null);
            this.attachmentId = attachmentId;
            return this;
        }

        public Builder type(ContentType type) {
            this.type = type;
            return this;
        }

        public Builder type(String type) {
            this.type = ContentType.of(type);
            return this;
        }

        public Builder size(long size) {
            Preconditions.checkArgument(size >= 0, "'size' must be positive");
            this.size = size;
            return this;
        }

        public AttachmentMetadata build() {
            Preconditions.checkState(type != null, "'type' is mandatory");
            Preconditions.checkState(size != null, "'size' is mandatory");
            Preconditions.checkState(attachmentId != null, "'attachmentId' is mandatory");
            Preconditions.checkState(messageId != null, "'messageId' is mandatory");

            return new AttachmentMetadata(attachmentId, type, size, messageId);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final AttachmentId attachmentId;
    private final ContentType type;
    private final long size;
    private final MessageId messageId;

    private AttachmentMetadata(AttachmentId attachmentId, ContentType type, long size, MessageId messageId) {
        this.attachmentId = attachmentId;
        this.type = type;
        this.size = size;
        this.messageId = messageId;
    }

    public AttachmentId getAttachmentId() {
        return attachmentId;
    }

    public ContentType getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public MessageId getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttachmentMetadata) {
            AttachmentMetadata other = (AttachmentMetadata) obj;
            return Objects.equal(attachmentId, other.attachmentId)
                && Objects.equal(type, other.type)
                && Objects.equal(size, other.size)
                && Objects.equal(messageId, other.messageId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attachmentId, type, size, messageId);
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("attachmentId", attachmentId)
                .add("messageId", messageId)
                .add("type", type)
                .add("size", size)
                .toString();
    }
}
