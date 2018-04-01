package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Steven on 31/03/2018.
 */

public class SquawkMessagingService extends FirebaseMessagingService {

    public static final String LOG_TAG = SquawkMessagingService.class.getSimpleName();
    private static final int MAIN_ACTIVITY_PENDING_INTENT_REQUEST_CODE = 1;
    int max = 30;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        sendNotification(data);
        insertData(data);
    }

    private void insertData(final Map<String, String> data) {
        AsyncTask<Void, Void, Void> insertSquawkTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues cv = new ContentValues();
                cv.put(SquawkContract.COLUMN_AUTHOR, data.get("author"));
                cv.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get("authorKey"));
                cv.put(SquawkContract.COLUMN_MESSAGE, data.get("message"));
                cv.put(SquawkContract.COLUMN_DATE, data.get("date"));
                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, cv);
                return null;
            }
        };
        insertSquawkTask.execute();
    }

    private void sendNotification(Map<String, String> data){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                MAIN_ACTIVITY_PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);
        String author = data.get("author");
        String message = data.get("message");

        if (message.length() > 30){
            message = message.substring(0, max);
        }

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(String.format(getString(R.string.notification_message), author))
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        notificationManager.notify(0, mBuilder.build());
    }
}
