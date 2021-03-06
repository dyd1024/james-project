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

package org.apache.james.mpt.imapmailbox.inmemory;

import java.util.Locale;

import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mpt.api.ImapHostSystem;
import org.apache.james.mpt.imapmailbox.inmemory.host.InMemoryHostSystem;
import org.apache.james.mpt.imapmailbox.suite.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryListingTest extends Listing {
    private ImapHostSystem system;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        system = new InMemoryHostSystem();
        system.beforeTest();
        super.setUp();
    }
    
    @Override
    protected ImapHostSystem createImapHostSystem() {
        return system;
    }


    @Test
    public void listShouldUTF7EscapeSpecialChar() throws Exception {
        system.createMailbox(MailboxPath.forUser(USER, "projects & abc"));
        system.createMailbox(MailboxPath.forUser(USER, "mailbox #17"));
        system.createMailbox(MailboxPath.forUser(USER, "??valuations"));

        simpleScriptedTestProtocol
            .withLocale(Locale.KOREA)
            .run("ListSpecialChar");
    }
}
