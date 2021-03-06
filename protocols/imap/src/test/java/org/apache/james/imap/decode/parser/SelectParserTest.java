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

package org.apache.james.imap.decode.parser;

import static org.apache.james.imap.ImapFixture.TAG;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.james.imap.api.message.response.StatusResponseFactory;
import org.apache.james.imap.decode.DecodingException;
import org.apache.james.imap.decode.ImapRequestStreamLineReader;
import org.junit.jupiter.api.Test;

class SelectParserTest {
    @Test
    void emptyLiteralShouldThrow() {
        SelectCommandParser parser = new SelectCommandParser(mock(StatusResponseFactory.class));
        String commandString = "{}";
        InputStream inputStream = new ByteArrayInputStream(commandString.getBytes());
        ImapRequestStreamLineReader lineReader = new ImapRequestStreamLineReader(inputStream, null);

        assertThatThrownBy(() -> parser.decode(lineReader, TAG, null))
            .isInstanceOf(DecodingException.class);
    }

    @Test
    void negativeLiteralShouldThrow() {
        SelectCommandParser parser = new SelectCommandParser(mock(StatusResponseFactory.class));
        String commandString = "{-1}";
        InputStream inputStream = new ByteArrayInputStream(commandString.getBytes());
        ImapRequestStreamLineReader lineReader = new ImapRequestStreamLineReader(inputStream, null);

        assertThatThrownBy(() -> parser.decode(lineReader, TAG, null))
            .isInstanceOf(DecodingException.class);
    }

    @Test
    void invalidLiteralShouldThrow() {
        SelectCommandParser parser = new SelectCommandParser(mock(StatusResponseFactory.class));
        String commandString = "{invalid}";
        InputStream inputStream = new ByteArrayInputStream(commandString.getBytes());
        ImapRequestStreamLineReader lineReader = new ImapRequestStreamLineReader(inputStream, null);

        assertThatThrownBy(() -> parser.decode(lineReader, TAG, null))
            .isInstanceOf(DecodingException.class);
    }
}
