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

package org.apache.james.mailbox.cassandra.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.james.backends.cassandra.CassandraCluster;
import org.apache.james.backends.cassandra.CassandraClusterExtension;
import org.apache.james.backends.cassandra.components.CassandraModule;
import org.apache.james.backends.cassandra.versions.CassandraSchemaVersionModule;
import org.apache.james.mailbox.cassandra.ids.CassandraMessageId;
import org.apache.james.mailbox.cassandra.modules.CassandraAttachmentModule;
import org.apache.james.mailbox.model.AttachmentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class CassandraAttachmentMessageIdDAOTest {

    private static final CassandraModule MODULE = CassandraModule.aggregateModules(
            CassandraAttachmentModule.MODULE,
            CassandraSchemaVersionModule.MODULE
    );

    @RegisterExtension
    static CassandraClusterExtension cassandraCluster = new CassandraClusterExtension(MODULE);

    private CassandraAttachmentMessageIdDAO testee;

    @BeforeEach
    void setUp(CassandraCluster cassandra) {
        testee = new CassandraAttachmentMessageIdDAO(cassandra.getConf(), new CassandraMessageId.Factory());
    }

    @Test
    void getOwnerMessageIdsShouldReturnDistinctValues() {
        CassandraMessageId messageId1 = new CassandraMessageId.Factory().generate();
        CassandraMessageId messageId2 = new CassandraMessageId.Factory().generate();
        AttachmentId attachmentId = AttachmentId.random();

        testee.storeAttachmentForMessageId(attachmentId, messageId1).block();
        testee.storeAttachmentForMessageId(attachmentId, messageId2).block();
        testee.storeAttachmentForMessageId(attachmentId, messageId1).block();

        assertThat(testee.getOwnerMessageIds(attachmentId).collectList().block())
            .containsExactlyInAnyOrder(messageId1, messageId2)
            .hasSize(2);
    }

    @Test
    void getOwnerMessageIdsShouldNotReturnDeletedValues() {
        CassandraMessageId messageId1 = new CassandraMessageId.Factory().generate();
        AttachmentId attachmentId = AttachmentId.random();

        testee.storeAttachmentForMessageId(attachmentId, messageId1).block();

        testee.delete(attachmentId, messageId1).block();
        assertThat(testee.getOwnerMessageIds(attachmentId).collectList().block())
            .isEmpty();
    }

    @Test
    void deleteShouldOnlyDeleteSuppliedValues() {
        CassandraMessageId messageId1 = new CassandraMessageId.Factory().generate();
        CassandraMessageId messageId2 = new CassandraMessageId.Factory().generate();
        AttachmentId attachmentId = AttachmentId.random();

        testee.storeAttachmentForMessageId(attachmentId, messageId1).block();
        testee.storeAttachmentForMessageId(attachmentId, messageId2).block();

        testee.delete(attachmentId, messageId1).block();

        assertThat(testee.getOwnerMessageIds(attachmentId).collectList().block())
            .containsExactly(messageId2);
    }

    @Test
    void deleteShouldNotThrowWhenNotExisting() {
        CassandraMessageId messageId1 = new CassandraMessageId.Factory().generate();
        AttachmentId attachmentId = AttachmentId.random();

        assertThatCode(() -> testee.delete(attachmentId, messageId1).block())
            .doesNotThrowAnyException();
    }

    @Test
    void listAllShouldReturnEmptyWhenNone() {
        assertThat(testee.listAll().collectList().block())
            .isEmpty();
    }

    @Test
    void listAllShouldReturnAddedValues() {
        CassandraMessageId messageId1 = new CassandraMessageId.Factory().generate();
        CassandraMessageId messageId2 = new CassandraMessageId.Factory().generate();
        AttachmentId attachmentId1 = AttachmentId.random();
        AttachmentId attachmentId2 = AttachmentId.random();

        testee.storeAttachmentForMessageId(attachmentId1, messageId1).block();
        testee.storeAttachmentForMessageId(attachmentId2, messageId2).block();

        assertThat(testee.listAll().collectList().block())
            .containsOnly(
                Pair.of(attachmentId1, messageId1),
                Pair.of(attachmentId2, messageId2));
    }

    @Test
    void listAllShouldNotReturnDeletedValues() {
        CassandraMessageId messageId1 = new CassandraMessageId.Factory().generate();
        CassandraMessageId messageId2 = new CassandraMessageId.Factory().generate();
        AttachmentId attachmentId1 = AttachmentId.random();
        AttachmentId attachmentId2 = AttachmentId.random();

        testee.storeAttachmentForMessageId(attachmentId1, messageId1).block();
        testee.storeAttachmentForMessageId(attachmentId2, messageId2).block();

        testee.delete(attachmentId1, messageId1).block();

        assertThat(testee.listAll().collectList().block())
            .containsOnly(Pair.of(attachmentId2, messageId2));
    }
}