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


package org.apache.james.protocols.smtp.core;

import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.handler.ConnectHandler;
import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPRetCode;
import org.apache.james.protocols.smtp.SMTPSession;

/**
 * This ConnectHandler print the greeting on connecting
 */
public class WelcomeMessageHandler implements ConnectHandler<SMTPSession> {

    private static final String SERVICE_TYPE = "SMTP";
    
    @Override
    public Response onConnect(SMTPSession session) {
       return computeGreetings(session, session.getConfiguration().getGreeting());
    }

    private SMTPResponse computeGreetings(SMTPSession session, String smtpGreeting) {
        // if no greeting was configured use a default
        if (smtpGreeting == null) {
            // Initially greet the connector
            return new SMTPResponse(SMTPRetCode.SERVICE_READY,
                new StringBuilder(256)
                    .append(session.getConfiguration().getHelloName())
                    .append(" ").append(getServiceType()).append(" Server (")
                    .append(session.getConfiguration().getSoftwareName())
                    .append(") ready"));
        } else {
            return new SMTPResponse(SMTPRetCode.SERVICE_READY, smtpGreeting);
        }
    }

    protected String getServiceType() {
        return SERVICE_TYPE;
    }
}
