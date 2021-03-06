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

package org.apache.james.user.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.james.core.Username;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DelegationStoreContract {

    Username ALICE = Username.of("alice");
    Username BOB = Username.of("bob");
    Username CEDRIC = Username.of("cedic");
    Username DAMIEN = Username.of("damien");
    Username EDGARD = Username.of("edgard");

    DelegationStore testee();

    @Test
    default void authorizedUsersShouldReturnEmptyByDefult() {
        assertThat(Flux.from(testee().authorizedUsers(ALICE)).collectList().block())
            .isEmpty();
    }

    @Test
    default void authorizedUsersShouldReturnAddedUsers() {
        Mono.from(testee().addAuthorizedUser(ALICE, BOB)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, CEDRIC)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, DAMIEN)).block();

        assertThat(Flux.from(testee().authorizedUsers(ALICE)).collectList().block())
            .containsOnly(BOB, CEDRIC, DAMIEN);
    }

    @Test
    default void authorizedUsersShouldReturnEmptyAfterClear() {
        Mono.from(testee().addAuthorizedUser(ALICE, BOB)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, CEDRIC)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, DAMIEN)).block();

        Mono.from(testee().clear(ALICE)).block();

        assertThat(Flux.from(testee().authorizedUsers(ALICE)).collectList().block())
            .isEmpty();
    }

    @Test
    default void clearShouldBeIdempotent() {
        Mono.from(testee().addAuthorizedUser(ALICE, BOB)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, CEDRIC)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, DAMIEN)).block();

        Mono.from(testee().clear(ALICE)).block();
        Mono.from(testee().clear(ALICE)).block();

        assertThat(Flux.from(testee().authorizedUsers(ALICE)).collectList().block())
            .isEmpty();
    }

    @Test
    default void authorizedUsersShouldNotReturnDeletedUsers() {
        Mono.from(testee().addAuthorizedUser(ALICE, BOB)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, CEDRIC)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, DAMIEN)).block();

        Mono.from(testee().removeAuthorizedUser(ALICE, CEDRIC)).block();

        assertThat(Flux.from(testee().authorizedUsers(ALICE)).collectList().block())
            .containsOnly(BOB, DAMIEN);
    }

    @Test
    default void removeAuthorizedUserShouldBeIdempotent() {
        Mono.from(testee().addAuthorizedUser(ALICE, BOB)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, CEDRIC)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, DAMIEN)).block();

        Mono.from(testee().removeAuthorizedUser(ALICE, CEDRIC)).block();
        Mono.from(testee().removeAuthorizedUser(ALICE, CEDRIC)).block();

        assertThat(Flux.from(testee().authorizedUsers(ALICE)).collectList().block())
            .containsOnly(BOB, DAMIEN);
    }

    @Test
    default void authorizedUsersShouldNotReturnDuplicates() {
        Mono.from(testee().addAuthorizedUser(ALICE, BOB)).block();
        Mono.from(testee().addAuthorizedUser(ALICE, BOB)).block();

        assertThat(Flux.from(testee().authorizedUsers(ALICE)).collectList().block())
            .containsExactly(BOB);
    }

    @Test
    default void authorizedUsersShouldNotReturnUnrelatedUsers() {
        Mono.from(testee().addAuthorizedUser(BOB, ALICE)).block();
        Mono.from(testee().addAuthorizedUser(BOB, CEDRIC)).block();

        assertThat(Flux.from(testee().authorizedUsers(ALICE)).collectList().block())
            .isEmpty();
    }

}
