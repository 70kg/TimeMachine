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

package me.drakeet.timemachine.store;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.agera.Function;
import com.google.android.agera.Merger;
import com.google.android.agera.Observable;
import com.google.android.agera.Receiver;
import com.google.android.agera.Repository;
import com.google.android.agera.Reservoir;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.Updatable;
import com.google.android.agera.database.SqlDeleteRequest;
import com.google.android.agera.database.SqlInsertRequest;
import com.google.android.agera.database.SqlRequest;
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
import static com.google.android.agera.Result.absent;
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
import static me.drakeet.timemachine.store.DatabaseSupplier.CONTENT_COLUMN;
import static me.drakeet.timemachine.store.DatabaseSupplier.CONTENT_DESC_COLUMN;
import static me.drakeet.timemachine.store.DatabaseSupplier.CREATED_AT_COLUMN;
import static me.drakeet.timemachine.store.DatabaseSupplier.FROM_USER_ID_COLUMN;
import static me.drakeet.timemachine.store.DatabaseSupplier.ID_COLUMN;
import static me.drakeet.timemachine.store.DatabaseSupplier.TABLE;
import static me.drakeet.timemachine.store.DatabaseSupplier.TO_USER_ID_COLUMN;
import static me.drakeet.timemachine.store.DatabaseSupplier.databaseSupplier;

public final class MessageStore implements Store<Message> {

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
    private final Receiver<StoreRequest> writeRequestReceiver;
    @NonNull
    private final Repository<List<Message>> messagesRepository;


    private MessageStore(@NonNull final Repository<List<Message>> messagesRepository,
                         @NonNull final Receiver<StoreRequest> writeRequestReceiver) {
        this.messagesRepository = messagesRepository;
        this.writeRequestReceiver = writeRequestReceiver;
    }


    @NonNull
    public synchronized static MessageStore messagesStore(
        @NonNull final Context applicationContext) {
        if (messageStore != null) {
            return messageStore;
        }
        final Executor executor = newSingleThreadExecutor();
        final DatabaseSupplier databaseSupplier = databaseSupplier(applicationContext);

        final Function<SqlInsertRequest, Result<Long>> insertMessageFunction =
            databaseInsertFunction(databaseSupplier);
        final Function<SqlUpdateRequest, Result<Integer>> updateMessageFunction =
            databaseUpdateFunction(databaseSupplier);
        final Function<SqlDeleteRequest, Result<Integer>> deleteMessageFunction =
            databaseDeleteFunction(databaseSupplier);

        final Reservoir<StoreRequest> writeRequestReservoir = reservoir();

        final Number unimportantValue = 0;
        final Merger<Number, Number, Boolean> alwaysNotify = staticMerger(true);
        final Observable writeReaction = repositoryWithInitialValue(unimportantValue)
            .observe(writeRequestReservoir)
            .onUpdatesPerLoop()
            .goTo(executor)
            .attemptGetFrom(writeRequestReservoir).orSkip()
            .thenAttemptTransform(new Function<StoreRequest, Result<? extends Number>>() {
                @NonNull @Override
                public Result<? extends Number> apply(@NonNull StoreRequest input) {
                    Object request = input.request;
                    Result<? extends Number> result = absent();
                    if (request instanceof SqlInsertRequest) {
                        result = insertMessageFunction.apply((SqlInsertRequest) request);
                    } else if (request instanceof SqlUpdateRequest) {
                        result = updateMessageFunction.apply((SqlUpdateRequest) request);
                    } else if (request instanceof SqlDeleteRequest) {
                        result = deleteMessageFunction.apply((SqlDeleteRequest) request);
                    }
                    if (input.observer != null) {
                        input.observer.onReturn(result.succeeded());
                    }
                    return result;
                }
            }).orSkip()
            .notifyIf(alwaysNotify)
            .compile();

        // Keep the reacting repository in this lazy singleton activated for the full app life cycle.
        // This is optional -- it allows the write requests submitted when the messages repository is not
        // active to still be processed asap.
        writeReaction.addUpdatable(new Updatable() {
            @Override public void update() {}
        });

        messageStore = new MessageStore(repositoryWithInitialValue(INITIAL_VALUE)
            .observe(writeReaction)
            .onUpdatesPerLoop()
            .goTo(executor)
            .goLazy()
            .getFrom(new Supplier<SqlRequest>() {
                @NonNull @Override public SqlRequest get() {
                    return sqlRequest().sql(GET_MESSAGES_FROM_TABLE).compile();
                }
            })
            .thenAttemptTransform(databaseQueryFunction(databaseSupplier,
                new Function<Cursor, Message>() {
                    @NonNull @Override public Message apply(@NonNull Cursor cursor) {
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
                }
                )
            )
            .orEnd(staticFunction(INITIAL_VALUE))
            .onConcurrentUpdate(SEND_INTERRUPT)
            .onDeactivation(SEND_INTERRUPT)
            .compile(), writeRequestReservoir);
        return messageStore;
    }


    @NonNull
    private static ItemContent searchContent(
        @Nullable String contentDesc, @NonNull final byte[] blob) {
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


    @Override
    public void insert(@NonNull final Message message, @NonNull final ResultObserver observer) {
        requireNonNull(message);
        requireNonNull(observer);
        StoreRequest request = new StoreRequest(getInsertRequest(message), observer);
        writeRequestReceiver.accept(request);
    }


    @Override public void insert(@NonNull final Message message) {
        requireNonNull(message);
        writeRequestReceiver.accept(new StoreRequest(getInsertRequest(message)));
    }


    @Override public void delete(@NonNull final Message message) {
        requireNonNull(message);
        StoreRequest request = new StoreRequest(sqlDeleteRequest()
            .table(TABLE)
            .where(MODIFY_WHERE)
            .arguments(String.valueOf(message.id))
            .compile());
        writeRequestReceiver.accept(request);
    }


    @Override public void clear() {
        StoreRequest request = new StoreRequest(sqlDeleteRequest()
            .table(TABLE)
            .compile());
        writeRequestReceiver.accept(request);
    }
}
