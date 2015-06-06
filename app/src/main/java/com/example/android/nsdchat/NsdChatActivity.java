/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.example.android.nsdchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class NsdChatActivity extends Activity {

    NsdHelper mNsdHelper;

    private static Context context;
    private TextView mStatusView;
    private Handler mUpdateHandler;
    public ArrayAdapter<String> servicesAdapter;

    public static final String TAG = "NsdChat";

    ChatConnection mConnection;

    public static Context getContext() {
        return NsdChatActivity.context;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NsdChatActivity.context = getApplicationContext();
        setContentView(R.layout.main);
        mStatusView = (TextView) findViewById(R.id.status);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

        mConnection = new ChatConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

        //Creating list of services
        //servicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mNsdHelper.getServices());
        //ListView serviceList = (ListView) findViewById(R.id.serviceList);
        //serviceList.setAdapter(servicesAdapter);

    }

    public void clickAdvertise(View v) {
        // Register service
        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    public void clickDiscover(View v) throws InterruptedException {
        mNsdHelper.discoverServices();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(NsdChatActivity.this, DiscoverActivity.class);
                startActivity(intent);
            }
        }, 1000);


    }

    public void clickConnect(View v) {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    public void clickSend(View v) {
        EditText messageView = (EditText) this.findViewById(R.id.chatInput);
        if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                mConnection.sendMessage(messageString);
            }
            messageView.setText("");
        }
    }

    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }

    @Override
    protected void onPause() {
//        if (mNsdHelper != null) {
//            mNsdHelper.stopDiscovery();
//        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mNsdHelper != null) {
//            mNsdHelper.discoverServices();
//        }
    }

    @Override
    protected void onDestroy() {
        //mNsdHelper.tearDown();  //close all services when app closed
        //mConnection.tearDown();
        super.onDestroy();
    }
}
