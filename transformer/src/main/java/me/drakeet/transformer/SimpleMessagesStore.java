/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.drakeet.transformer;

import android.content.Context;
import android.support.annotation.NonNull;
import com.google.android.agera.Function;
import com.google.android.agera.Merger;
import com.google.android.agera.Observable;
import com.google.android.agera.Receiver;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import com.google.android.agera.database.SqlDeleteRequest;
import com.google.android.agera.database.SqlInsertRequest;
import com.google.android.agera.database.SqlUpdateRequest;
import java.util.List;
import java.util.concurrent.Executor;
import me.drakeet.timemachine.SimpleMessage;

import static com.google.android.agera.Functions.staticFunction;
import static com.google.android.agera.Mergers.staticMerger;
import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.RepositoryConfig.SEND_INTERRUPT;
import static com.google.android.agera.Reservoirs.reservoir;
import static com.google.android.agera.Result.failure;
import static com.google.android.agera.database.SqlDatabaseFunctions.databaseDeleteFunction;
import static com.google.android.agera.database.SqlDatabaseFunctions.databaseInsertFunction;
import static com.google.android.agera.database.SqlDatabaseFunctions.databaseQueryFunction;
import static com.google.android.agera.database.SqlDatabaseFunctions.databaseUpdateFunction;
import static com.google.android.agera.database.SqlRequests.sqlDeleteRequest;
import static com.google.android.agera.database.SqlRequests.sqlInsertRequest;
import static com.google.android.agera.database.SqlRequests.sqlRequest;
import static java.util.Collections.emptyList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static me.drakeet.transformer.SimpleMessagesSqlDatabaseSupplier.CONTENT_COLUMN;
import static me.drakeet.transformer.SimpleMessagesSqlDatabaseSupplier.CREATED_AT_COLUMN;
import static me.drakeet.transformer.SimpleMessagesSqlDatabaseSupplier.FROM_USER_ID_COLUMN;
import static me.drakeet.transformer.SimpleMessagesSqlDatabaseSupplier.ID_COLUMN;
import static me.drakeet.transformer.SimpleMessagesSqlDatabaseSupplier.TABLE;
import static me.drakeet.transformer.SimpleMessagesSqlDatabaseSupplier.TO_USER_ID_COLUMN;
import static me.drakeet.transformer.SimpleMessagesSqlDatabaseSupplier.databaseSupplier;

final class SimpleMessagesStore {

    private static final String MODIFY_WHERE = ID_COLUMN + "=?";
    private static final String GET_MESSAGES_FROM_TABLE =
        "SELECT * FROM " + TABLE + " ORDER BY " + CREATED_AT_COLUMN;
    private static final int ID_COLUMN_INDEX = 0;
    private static final int CONTENT_COLUMN_INDEX = 1;
    private static final int FROM_USER_ID_COLUMN_INDEX = 2;
    private static final int TO_USER_ID_COLUMN_INDEX = 3;
    private static final int CREATED_AT_COLUMN_INDEX = 4;
    private static final List<SimpleMessage> INITIAL_VALUE = emptyList();

    private static SimpleMessagesStore messagesStore;

    @NonNull
    private final Receiver<Object> writeRequestReceiver;
    @NonNull
    private final Repository<List<SimpleMessage>> messagesRepository;


    private SimpleMessagesStore(@NonNull final Repository<List<SimpleMessage>> messagesRepository,
                                @NonNull final Receiver<Object> writeRequestReceiver) {
        this.messagesRepository = messagesRepository;
        this.writeRequestReceiver = writeRequestReceiver;
    }


    @NonNull
    public synchronized static SimpleMessagesStore messagesStore(
        @NonNull final Context applicationContext) {
        if (messagesStore != null) {
            return messagesStore;
        }
        // Create a thread executor to execute all database operations on.
        final Executor executor = newSingleThreadExecutor();

        // Create a database supplier that initializes the database. This is also used to supply the
        // database in all database operations.
        final SimpleMessagesSqlDatabaseSupplier databaseSupplier = databaseSupplier(
            applicationContext);

        // Create a function that processes database write operations.
        final Function<SqlInsertRequest, Result<Long>> insertSimpleMessageFunction =
            databaseInsertFunction(databaseSupplier);
        final Function<SqlUpdateRequest, Result<Integer>> updateSimpleMessageFunction =
            databaseUpdateFunction(databaseSupplier);
        final Function<SqlDeleteRequest, Result<Integer>> deleteSimpleMessageFunction =
            databaseDeleteFunction(databaseSupplier);

        // Create a reservoir of database write requests. This will be used as the receiver of write
        // requests submitted to the SimpleMessagesStore, and the event/data source of the reacting repository.
        final Reservoir<Object> writeRequestReservoir = reservoir();

        // Create a reacting repository that processes all write requests. The value of the repository
        // is unimportant, but it must be able to notify the messages repository on completing each write
        // operation. The database thread executor is single-threaded to optimize for disk I/O, but if
        // the executor can be multi-threaded, then this is the ideal place to multiply the reacting
        // repository to achieve parallelism. The messages repository should observe all these instances.
        final Number unimportantValue = 0;
        final Merger<Number, Number, Boolean> alwaysNotify = staticMerger(true);
        final Observable writeReaction = repositoryWithInitialValue(unimportantValue)
            .observe(writeRequestReservoir)
            .onUpdatesPerLoop()
            .goTo(executor)
            .attemptGetFrom(writeRequestReservoir).orSkip()
            .thenAttemptTransform(input -> {
                if (input instanceof SqlInsertRequest) {
                    return insertSimpleMessageFunction.apply((SqlInsertRequest) input);
                }
                if (input instanceof SqlUpdateRequest) {
                    return updateSimpleMessageFunction.apply((SqlUpdateRequest) input);
                }
                if (input instanceof SqlDeleteRequest) {
                    return deleteSimpleMessageFunction.apply((SqlDeleteRequest) input);
                }
                return failure();
            }).orSkip()
            .notifyIf(alwaysNotify)
            .compile();

        // Keep the reacting repository in this lazy singleton activated for the full app life cycle.
        // This is optional -- it allows the write requests submitted when the messages repository is not
        // active to still be processed asap.
        writeReaction.addUpdatable(() -> {
        });

        // Create the repository of messages, wire it up to update on each database write, set it to fetch
        // messages from the database on the database thread executor.

        // Create the wired up messages store
        messagesStore = new SimpleMessagesStore(repositoryWithInitialValue(INITIAL_VALUE)
            .observe(writeReaction)
            .onUpdatesPerLoop()
            .goTo(executor)
            .goLazy() // todo: add go lazy to reload the same content when restart
            .getFrom(() -> sqlRequest().sql(GET_MESSAGES_FROM_TABLE).compile())
            .thenAttemptTransform(databaseQueryFunction(databaseSupplier,
                cursor -> new SimpleMessage.Builder()
                    .setId(cursor.getString(ID_COLUMN_INDEX))
                    .setContent(cursor.getString(CONTENT_COLUMN_INDEX))
                    .setFromUserId(cursor.getString(FROM_USER_ID_COLUMN_INDEX))
                    .setToUserId(cursor.getString(TO_USER_ID_COLUMN_INDEX))
                    .setCreatedAt(cursor.getLong(CREATED_AT_COLUMN_INDEX))
                    .build()
            ))
            .orEnd(staticFunction(INITIAL_VALUE))
            .onConcurrentUpdate(SEND_INTERRUPT)
            .onDeactivation(SEND_INTERRUPT)
            .compile(), writeRequestReservoir);
        return messagesStore;
    }


    @NonNull
    public Repository<List<SimpleMessage>> getSimpleMessagesRepository() {
        return messagesRepository;
    }


    public void insert(@NonNull final SimpleMessage message) {
        writeRequestReceiver.accept(sqlInsertRequest()
            .table(TABLE)
            .column(ID_COLUMN, message.getId())
            .column(CONTENT_COLUMN, message.getContent())
            .column(FROM_USER_ID_COLUMN, message.getFromUserId())
            .column(TO_USER_ID_COLUMN, message.getToUserId())
            .column(CREATED_AT_COLUMN, String.valueOf(message.getCreatedAt().getTime()))
            .compile());
    }


    public boolean delete(@NonNull final SimpleMessage message) {
        writeRequestReceiver.accept(sqlDeleteRequest()
            .table(TABLE)
            .where(MODIFY_WHERE)
            .arguments(String.valueOf(message.getId()))
            .compile());
        return true;
    }


    public void clean() {
        writeRequestReceiver.accept(sqlDeleteRequest()
            .table(TABLE)
            .compile());
    }
}
