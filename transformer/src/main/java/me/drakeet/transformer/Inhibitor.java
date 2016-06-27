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
import android.util.Log;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;
import me.drakeet.agera.eventbus.AgeraBus;
import me.drakeet.timemachine.SimpleMessage;
import me.drakeet.timemachine.TimeKey;

import static me.drakeet.transformer.MessageService.YIN;
import static me.drakeet.transformer.SimpleMessagesStore.messagesStore;

/**
 * Created by drakeet on 16/6/13.
 */
public class Inhibitor extends IntentService implements Updatable {

    private final static String TAG = "Inhibitor";

    private Repository<Result<String>> repository;


    public Inhibitor() {
        super(TAG);
    }


    @Override protected void onHandleIntent(Intent intent) {
        repository = Requests.requestYinSync();
        repository.addUpdatable(this);
    }


    @Override public void update() {
        if (repository.get().succeeded()) {
            SimpleMessage in = new SimpleMessage.Builder()
                .setContent(repository.get().get())
                .setFromUserId(YIN)
                .setToUserId(TimeKey.userId)
                .thenCreateAtNow();
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


    private void notify(SimpleMessage message) {
        String title = message.getFromUserId();
        String content = message.getContent().toString();
        if (Objects.equals(message.getFromUserId(), YIN)) {
            String[] messageContents = message.getContent().toString().split("\n");
            title = messageContents[0];
            content = messageContents[1];
        }
        Notifications.simple(this, title, content,
            R.drawable.ic_notification, MainActivity.class);
    }
}


