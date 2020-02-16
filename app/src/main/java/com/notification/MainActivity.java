package com.notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    private class StreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String message = extras.getString("message");
            showMessage(message);
        }
    }

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String message = extras.getString("message");
            updateMessage(message);
        }
    }

    private void showMessage(String message){
        TextView textView = (TextView) findViewById(R.id.streamTxtView);
        textView.append(message);
    }

    private void updateMessage(String message){
        TextView textView = (TextView) findViewById(R.id.statusTxtView);
        textView.setText(message);
    }

    private void loadView(){
        TextView textView = findViewById(R.id.streamTxtView);
        textView.setText(loadFromDisk(getString(R.string.filenameStream)));
        textView = findViewById(R.id.statusTxtView);
        textView.setText(loadFromDisk(getString(R.string.filenameStatus)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        printToken();
        createNotificationChannel();
        loadView();
        registerStreamNotification();
        registerStatusNotification();
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadView();
    }

    private void registerStreamNotification(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("onStreamReceived");
        StreamReceiver receiver = new StreamReceiver();
        registerReceiver(receiver, intentFilter);
    }

    private void registerStatusNotification(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("onStatusReceived");
        StatusReceiver receiver = new StatusReceiver();
        registerReceiver(receiver, intentFilter);
    }

    private void printToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();

                // Log
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                }
            });
    }

    private void createNotificationChannel(){
        String channelId  = getString(R.string.default_notification_channel_id);
        String channelName = getString(R.string.default_notification_channel_name);
        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH));
    }

    private String loadFromDisk(String filename){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fis = this.openFileInput(filename);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
        } catch (FileNotFoundException e){

        } catch (IOException e){

        } finally {
            return stringBuilder.toString();
        }
    }
}
