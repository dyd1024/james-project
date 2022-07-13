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

package org.apache.james.imap.processor;

import static org.apache.james.util.ReactorUtils.logOnError;

import java.util.Date;

import javax.mail.Flags;

import org.apache.james.imap.api.display.HumanReadableText;
import org.apache.james.imap.api.message.UidRange;
import org.apache.james.imap.api.message.response.StatusResponse;
import org.apache.james.imap.api.message.response.StatusResponse.ResponseCode;
import org.apache.james.imap.api.message.response.StatusResponseFactory;
import org.apache.james.imap.api.process.ImapSession;
import org.apache.james.imap.api.process.SelectedMailbox;
import org.apache.james.imap.main.PathConverter;
import org.apache.james.imap.message.request.AppendRequest;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.ComposedMessageId;
import org.apache.james.mailbox.model.Content;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.UidValidity;
import org.apache.james.metrics.api.MetricFactory;
import org.apache.james.util.MDCBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fge.lambdas.Throwing;

import reactor.core.publisher.Mono;

public class AppendProcessor extends AbstractMailboxProcessor<AppendRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppendProcessor.class);

    public AppendProcessor(MailboxManager mailboxManager, StatusResponseFactory statusResponseFactory,
            MetricFactory metricFactory) {
        super(AppendRequest.class, mailboxManager, statusResponseFactory, metricFactory);
    }

    @Override
    protected Mono<Void> processRequestReactive(AppendRequest request, ImapSession session, Responder responder) {
        final String mailboxName = request.getMailboxName();
        final Content messageIn = request.getMessage().asMailboxContent();
        final Date datetime = request.getDatetime();
        final Flags flags = request.getFlags();
        final MailboxPath mailboxPath = PathConverter.forSession(session).buildFullPath(mailboxName);
        final MailboxManager mailboxManager = getMailboxManager();

        session.stopDetectingCommandInjection();
        return Mono.from(mailboxManager.getMailboxReactive(mailboxPath, session.getMailboxSession()))
            .flatMap(mailbox -> appendToMailbox(messageIn, datetime, flags, session, request, mailbox, responder, mailboxPath))
            .doOnEach(logOnError(MailboxNotFoundException.class, e -> LOGGER.debug("Append failed for mailbox {}", mailboxPath, e)))
            .onErrorResume(MailboxNotFoundException.class, e -> {
                // Indicates that the mailbox does not exist
                // So TRY CREATE
                no(request, responder, HumanReadableText.FAILURE_NO_SUCH_MAILBOX, StatusResponse.ResponseCode.tryCreate());
                return Mono.empty();
            })
            .doOnEach(logOnError(MailboxException.class, e -> LOGGER.error("Append failed for mailbox {}", mailboxPath, e)))
            .onErrorResume(MailboxException.class, e -> {
                // Some other issue
                no(request, responder, HumanReadableText.GENERIC_FAILURE_DURING_PROCESSING);
                return Mono.empty();
            });
    }

    private Mono<Void> appendToMailbox(Content message, Date datetime, Flags flagsToBeSet, ImapSession session, AppendRequest request, MessageManager mailbox, Responder responder, MailboxPath mailboxPath) {
        final MailboxSession mailboxSession = session.getMailboxSession();
        final SelectedMailbox selectedMailbox = session.getSelected();
        final boolean isSelectedMailbox = selectedMailbox != null && selectedMailbox.getMailboxId().equals(mailbox.getId());

        return Mono.from(mailbox.appendMessageReactive(
            MessageManager.AppendCommand.builder()
                .withInternalDate(datetime)
                .withFlags(flagsToBeSet)
                .isRecent(!isSelectedMailbox)
                .build(message), mailboxSession))
            .map(MessageManager.AppendResult::getId)
            .map(Throwing.<ComposedMessageId, ComposedMessageId>function(messageId -> {
                    if (isSelectedMailbox) {
                        selectedMailbox.addRecent(messageId.getUid());
                    }
                    return messageId;
                }).sneakyThrow())
            .flatMap(messageId -> unsolicitedResponses(session, responder, false).thenReturn(messageId))
            .doOnNext(Throwing.consumer(messageId -> {
                // get folder UIDVALIDITY
                UidValidity uidValidity = mailbox
                    .getMailboxEntity()
                    .getUidValidity();
                okComplete(request, ResponseCode.appendUid(uidValidity, new UidRange[] { new UidRange(messageId.getUid()) }), responder);
            }))
            .then();
    }

    @Override
    protected MDCBuilder mdc(AppendRequest request) {
        return MDCBuilder.create()
            .addToContext(MDCBuilder.ACTION, "APPEND")
            .addToContext("mailbox", request.getMailboxName());
    }
}
