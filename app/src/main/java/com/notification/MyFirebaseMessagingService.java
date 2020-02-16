package com.notification;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private int id = 0;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String body = data.get("body");
            if (title.equals("status")) {
                writeToDisk(body);
                showMessage("onReceived");
            }
            else {
                appendToDisk(body + "\n");
                raiseNotification(title, body);
                showMessage("onReceived");
            }
        }
    }

    private void showMessage(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }

    private void raiseNotification(String title, String body){
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        String channelId  = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(id, builder.build());
        id++;
    }

    private void appendToDisk(String message){
        try (FileOutputStream fos = this.openFileOutput(
                getString(R.string.filenameStream),
                MODE_PRIVATE | MODE_APPEND
        )) {
            fos.write(message.getBytes());
        } catch (FileNotFoundException e) {

        } catch (IOException e){

        }
    }

    private void writeToDisk(String message){
        try (FileOutputStream fos = this.openFileOutput(
                getString(R.string.filenameStatus),
                MODE_PRIVATE
        )) {
            fos.write(message.getBytes());
        } catch (FileNotFoundException e) {

        } catch (IOException e){

        }
    }
}