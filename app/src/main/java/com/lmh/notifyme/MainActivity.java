package com.lmh.notifyme;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends Activity {

    private int notificationTextColor = Color.BLUE;
    private int notificationLightColor = Color.RED;
    private String notificationTitle = "My notification";
    private String notificationChannelId;
    private String notificationText = "Hello World!";
    private int notificationAutoTimeoutInterval = 60000;
    private String chosenRingtone = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationChannelId = getChannelId();
        initializeItems();
    }

    private void initializeItems() {
        notificationBuilderSetup();

        textColorBuilderSetup();

        lightColorBuilderSetup();

        soundPickerSetup();

        ((EditText) findViewById(R.id.titleText)).setText(notificationTitle);
        ((EditText) findViewById(R.id.descText)).setText(notificationText);
    }

    private void soundPickerSetup() {
        findViewById(R.id.soundPicker).setOnClickListener((View v) -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            this.startActivityForResult(intent, 5);
        });

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null) {
                this.chosenRingtone = uri.toString();
            } else {
                this.chosenRingtone = null;
            }
        }
    }

    private void lightColorBuilderSetup() {
        findViewById(R.id.lightColorPicker).setBackgroundColor(notificationLightColor);
        AmbilWarnaDialog lightColorPickerDialog = new AmbilWarnaDialog(this, notificationTextColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                notificationLightColor = color;
                findViewById(R.id.lightColorPicker).setBackgroundColor(notificationLightColor);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        findViewById(R.id.lightColorPicker).setOnClickListener((View v) -> {
            lightColorPickerDialog.show();
        });
    }

    private void textColorBuilderSetup() {
        findViewById(R.id.textColorPicker).setBackgroundColor(notificationTextColor);
        AmbilWarnaDialog textColorPickerDialog = new AmbilWarnaDialog(this, notificationTextColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                notificationTextColor = color;
                findViewById(R.id.textColorPicker).setBackgroundColor(notificationTextColor);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        findViewById(R.id.textColorPicker).setOnClickListener((View v) -> {
            textColorPickerDialog.show();
        });
    }

    private void notificationBuilderSetup() {
        findViewById(R.id.button).setOnClickListener((View v) -> {
            notificationTitle = ((EditText) findViewById(R.id.titleText)).getText().toString();
            notificationText = ((EditText) findViewById(R.id.descText)).getText().toString();
            Toast.makeText(this, "Button Clicked", Toast.LENGTH_LONG).show();
            Notification.Builder mBuilder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText);
            long mNotificationId = new Date().getTime();
            Intent resultIntent = new Intent(this, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                if (this.chosenRingtone != null) {
                    //Update channel ID if required
                    mNotificationManager.deleteNotificationChannel(notificationChannelId);
                    notificationChannelId = new Date().getTime() + "";
                }

                mBuilder.setChannelId(notificationChannelId).setTimeoutAfter(notificationAutoTimeoutInterval)
                        .setAutoCancel(true).setColor(notificationTextColor);
                NotificationChannel channel = new NotificationChannel(notificationChannelId,
                        "Entertainment",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Test Notifications");
                channel.enableLights(true);
                if (this.chosenRingtone != null) {
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .build();

                    channel.setSound(Uri.parse(this.chosenRingtone), audioAttributes);
                    this.chosenRingtone = null;
                }
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                channel.setLightColor(notificationLightColor);
                channel.enableVibration(true);
                saveNotificationChannelId(notificationChannelId);

                mNotificationManager.createNotificationChannel(channel);
            }
            mNotificationManager.notify(Long.valueOf(mNotificationId).intValue(), mBuilder.build());

        });
    }

    private void saveNotificationChannelId(String notificationChannelId) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("notificationChannelId", notificationChannelId);
        editor.commit();
    }

    public String getChannelId() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String defaultValue = new Date().getTime() + "";
        return sharedPref.getString("notificationChannelId", defaultValue);
    }
}
