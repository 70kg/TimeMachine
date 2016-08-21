/*
 * Copyright 2016 drakeet. All Rights Reserved.
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

package me.drakeet.transformer.store;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import me.drakeet.multitype.ItemContent;
import me.drakeet.multitype.Savable;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.message.InTextContent;
import me.drakeet.timemachine.message.OutTextContent;

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
import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.transformer.store.DatabaseSupplier.CONTENT_COLUMN;
import static me.drakeet.transformer.store.DatabaseSupplier.CONTENT_DESC_COLUMN;
import static me.drakeet.transformer.store.DatabaseSupplier.CREATED_AT_COLUMN;
import static me.drakeet.transformer.store.DatabaseSupplier.FROM_USER_ID_COLUMN;
import static me.drakeet.transformer.store.DatabaseSupplier.ID_COLUMN;
import static me.drakeet.transformer.store.DatabaseSupplier.TABLE;
import static me.drakeet.transformer.store.DatabaseSupplier.TO_USER_ID_COLUMN;
import static me.drakeet.transformer.store.DatabaseSupplier.databaseSupplier;

public final class MessageStore {

    private static final String MODIFY_WHERE = ID_COLUMN + "=?";
    private static final String GET_MESSAGES_FROM_TABLE =
        "SELECT * FROM " + TABLE + " ORDER BY " + CREATED_AT_COLUMN;
    private static final int ID_COLUMN_INDEX = 0;
    private static final int CONTENT_COLUMN_INDEX = 1;
    private static final int CONTENT_DESC_COLUMN_INDEX = 2;
    private static final int FROM_USER_ID_COLUMN_INDEX = 3;
    private static final int TO_USER_ID_COLUMN_INDEX = 4;
    private static final int CREATED_AT_COLUMN_INDEX = 5;
    private static final List<Message> INITIAL_VALUE = emptyList();

    private static MessageStore messageStore;

    @NonNull
    private final Receiver<Object> writeRequestReceiver;
    @NonNull
    private final Repository<List<Message>> messagesRepository;


    private MessageStore(@NonNull final Repository<List<Message>> messagesRepository,
                         @NonNull final Receiver<Object> writeRequestReceiver) {
        this.messagesRepository = messagesRepository;
        this.writeRequestReceiver = writeRequestReceiver;
    }


    @NonNull
    public synchronized static MessageStore messagesStore(
        @NonNull final Context applicationContext) {
        if (messageStore != null) {
            return messageStore;
        }
        // Create a thread executor to execute all database operations on.
        final Executor executor = newSingleThreadExecutor();

        // Create a database supplier that initializes the database. This is also used to supply the
        // database in all database operations.
        final DatabaseSupplier databaseSupplier = databaseSupplier(
            applicationContext);

        // Create a function that processes database write operations.
        final Function<SqlInsertRequest, Result<Long>> insertSimpleMessageFunction =
            databaseInsertFunction(databaseSupplier);
        final Function<SqlUpdateRequest, Result<Integer>> updateSimpleMessageFunction =
            databaseUpdateFunction(databaseSupplier);
        final Function<SqlDeleteRequest, Result<Integer>> deleteSimpleMessageFunction =
            databaseDeleteFunction(databaseSupplier);

        // Create a reservoir of database write requests. This will be used as the receiver of write
        // requests submitted to the MessageStore, and the event/data source of the reacting repository.
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
                if (input instanceof EchoRequest) {
                    Result result = insertSimpleMessageFunction.apply(
                        (SqlInsertRequest) ((EchoRequest) input).request);
                    ((EchoRequest) input).observer.onResult(result.succeeded());
                    return result;
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
        messageStore = new MessageStore(repositoryWithInitialValue(INITIAL_VALUE)
            .observe(writeReaction)
            .onUpdatesPerLoop()
            .goTo(executor)
            .goLazy()
            .getFrom(() -> sqlRequest().sql(GET_MESSAGES_FROM_TABLE).compile())
            .thenAttemptTransform(databaseQueryFunction(databaseSupplier,
                cursor -> {
                    Message message = new Message();
                    message.id = cursor.getString(ID_COLUMN_INDEX);
                    message.createdTime = cursor.getLong(CREATED_AT_COLUMN_INDEX);
                    message.fromUserId = cursor.getString(FROM_USER_ID_COLUMN_INDEX);
                    message.toUserId = cursor.getString(TO_USER_ID_COLUMN_INDEX);
                    String contentDesc = cursor.getString(CONTENT_DESC_COLUMN_INDEX);
                    message.content = searchContent(contentDesc,
                        cursor.getBlob(CONTENT_COLUMN_INDEX));
                    return message;
                }
            ))
            .orEnd(staticFunction(INITIAL_VALUE))
            .onConcurrentUpdate(SEND_INTERRUPT)
            .onDeactivation(SEND_INTERRUPT)
            .compile(), writeRequestReservoir);
        return messageStore;
    }


    @NonNull
    private static ItemContent searchContent(@Nullable String contentDesc, @NonNull byte[] blob) {
        if (contentDesc == null) {
            contentDesc = "";
        }
        final ItemContent content;
        switch (contentDesc) {
            default:
            case "InText":
                content = new InTextContent(blob);
                break;
            case "OutText":
                content = new OutTextContent(blob);
                break;
        }
        return content;
    }


    @NonNull
    public Repository<List<Message>> getSimpleMessagesRepository() {
        return messagesRepository;
    }


    private SqlInsertRequest getInsertRequest(@NonNull Message message) {
        return sqlInsertRequest()
            .table(TABLE)
            .column(ID_COLUMN, message.id)
            .column(CONTENT_COLUMN, ((Savable) message.content).toBytes())
            .column(CONTENT_DESC_COLUMN, ((Savable) message.content).describe())
            .column(FROM_USER_ID_COLUMN, message.fromUserId)
            .column(TO_USER_ID_COLUMN, message.toUserId)
            .column(CREATED_AT_COLUMN, message.createdTime)
            .compile();
    }


    public void insert(@NonNull final Message message, @NonNull final ResultObserver observer) {
        requireNonNull(message);
        requireNonNull(observer);
        EchoRequest request = new EchoRequest(getInsertRequest(message), observer);
        writeRequestReceiver.accept(request);
    }


    public void insert(@NonNull final Message message) {
        requireNonNull(message);
        writeRequestReceiver.accept(getInsertRequest(message));
    }


    public boolean delete(@NonNull final Message message) {
        requireNonNull(message);
        writeRequestReceiver.accept(sqlDeleteRequest()
            .table(TABLE)
            .where(MODIFY_WHERE)
            .arguments(String.valueOf(message.id))
            .compile());
        return true;
    }


    public void clear() {
        writeRequestReceiver.accept(sqlDeleteRequest()
            .table(TABLE)
            .compile());
    }
}
