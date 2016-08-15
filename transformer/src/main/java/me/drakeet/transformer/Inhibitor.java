/*
 * Copyright (C) 2016 drakeet.
 *      http://drakeet.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.drakeet.transformer;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;
import me.drakeet.agera.eventbus.AgeraBus;
import me.drakeet.timemachine.Message;
import me.drakeet.timemachine.MessageFactory;
import me.drakeet.timemachine.Objects;
import me.drakeet.timemachine.TimeKey;
import me.drakeet.timemachine.message.InTextContent;
import me.drakeet.timemachine.message.TextContent;
import me.drakeet.transformer.request.YinRequests;

import static me.drakeet.timemachine.Objects.requireNonNull;
import static me.drakeet.transformer.MessageService.YIN;
import static me.drakeet.transformer.MessageStore.messagesStore;

/**
 * Created by drakeet on 16/6/13.
 */
public class Inhibitor extends IntentService implements Updatable {

    private final static String TAG = Inhibitor.class.getSimpleName();

    private Repository<Result<String>> repository;


    public Inhibitor() {
        super(TAG);
    }


    @Override protected void onHandleIntent(Intent intent) {
        repository = YinRequests.sync();
        repository.addUpdatable(this);
    }


    @Override public void update() {
        if (repository.get().succeeded()) {
            MessageFactory factory = new MessageFactory.Builder()
                .setFromUserId(YIN)
                .setToUserId(TimeKey.userId)
                .build();
            Message in = factory.newMessage(new InTextContent(repository.get().get()));
            if (AgeraBus.repository().hasObservers()) {
                AgeraBus.repository().accept(new NewInEvent(in));
            } else {
                Log.d(YIN, "DeadEvent");
                notify(in);
            }
            messagesStore(getApplicationContext()).insert(in);
        }
        repository.removeUpdatable(this);
    }


    private void notify(@NonNull final Message message) {
        requireNonNull(message);
        String title = message.fromUserId;
        String content = ((TextContent) message.content).text;
        if (Objects.equals(message.fromUserId, YIN)) {
            String[] messageContents = ((TextContent) message.content).text.split("\n");
            title = messageContents[0];
            content = messageContents[1];
        }
        Notifications.simple(this, title, content,
            R.drawable.ic_notification, MainActivity.class);
    }
}


