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

package org.apache.james.mailrepository.cassandra;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.update;
import static com.datastax.oss.driver.api.querybuilder.relation.Relation.column;
import static org.apache.james.mailrepository.cassandra.MailRepositoryTable.COUNT;
import static org.apache.james.mailrepository.cassandra.MailRepositoryTable.COUNT_TABLE;
import static org.apache.james.mailrepository.cassandra.MailRepositoryTable.REPOSITORY_NAME;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.james.backends.cassandra.utils.CassandraAsyncExecutor;
import org.apache.james.mailrepository.api.MailRepositoryUrl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;

import reactor.core.publisher.Mono;

public class CassandraMailRepositoryCountDAO {

    private final CassandraAsyncExecutor executor;
    private final PreparedStatement increment;
    private final PreparedStatement decrement;
    private final PreparedStatement select;

    @Inject
    public CassandraMailRepositoryCountDAO(CqlSession session) {
        this.executor = new CassandraAsyncExecutor(session);

        this.increment = prepareIncrement(session);
        this.decrement = prepareDecrement(session);
        this.select = prepareSelect(session);
    }

    private PreparedStatement prepareDecrement(CqlSession session) {
        return session.prepare(update(COUNT_TABLE)
            .decrement(COUNT)
            .where(column(REPOSITORY_NAME).isEqualTo(bindMarker(REPOSITORY_NAME)))
            .build());
    }

    private PreparedStatement prepareIncrement(CqlSession session) {
        return session.prepare(update(COUNT_TABLE)
            .increment(COUNT)
            .where(column(REPOSITORY_NAME).isEqualTo(bindMarker(REPOSITORY_NAME)))
            .build());
    }

    private PreparedStatement prepareSelect(CqlSession session) {
        return session.prepare(selectFrom(COUNT_TABLE)
            .column(COUNT)
            .where(column(REPOSITORY_NAME).isEqualTo(bindMarker(REPOSITORY_NAME)))
            .build());
    }

    public Mono<Void> increment(MailRepositoryUrl url) {
        return executor.executeVoid(increment.bind()
            .setString(REPOSITORY_NAME, url.asString()));
    }

    public Mono<Void> decrement(MailRepositoryUrl url) {
        return executor.executeVoid(decrement.bind()
            .setString(REPOSITORY_NAME, url.asString()));
    }

    public Mono<Long> getCount(MailRepositoryUrl url) {
        return executor.executeSingleRowOptional(select.bind()
                .setString(REPOSITORY_NAME, url.asString()))
            .map(this::toCount);
    }

    private Long toCount(Optional<Row> rowOptional) {
        return rowOptional
            .map(row -> row.getLong(COUNT))
            .orElse(0L);
    }
}
