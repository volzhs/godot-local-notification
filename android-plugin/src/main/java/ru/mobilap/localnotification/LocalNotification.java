package ru.mobilap.localnotification;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class LocalNotification extends GodotPlugin {

    private Godot godot = null;
    private Dictionary notificationData = new Dictionary();
    private String action = null;
    private String uri = null;

    public LocalNotification(Godot godot) 
    {
        super(godot);
        this.godot = godot;
        checkIntent();
    }

    @Override
    public String getPluginName() {
        return "LocalNotification";
    }

    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList(
                "init",
                "showLocalNotification",
                "isInited",
                "isEnabled",
                "register_remote_notification",
                "get_device_token",
                "get_notification_data",
                "get_deeplink_action",
                "get_deeplink_uri"
        );
    }

    /*
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(loggedInSignal);
    }
    */

    @Override
    public View onMainCreate(Activity activity) {
        return null;
    }

    // Public methods

    public void init() {
    }

    public boolean isInited() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public void showLocalNotification(String message, String title, int interval, int tag) {
        if(interval <= 0) return;
        Log.i("godot", "showLocalNotification: "+message+", "+Integer.toString(interval)+", "+Integer.toString(tag));
        PendingIntent sender = getPendingIntent(message, title, tag);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, interval);
               
        AlarmManager am = (AlarmManager) godot.getActivity().getSystemService(godot.getActivity().ALARM_SERVICE);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
    }

    public void register_remote_notification() {
    }

    public String get_device_token() {
        return "";
    }

    // Internal methods

    private PendingIntent getPendingIntent(String message, String title, int tag) {
        Intent i = new Intent(godot.getActivity().getApplicationContext(), LocalNotificationReceiver.class);
        i.putExtra("notification_id", tag);
        i.putExtra("message", message);
        i.putExtra("title", title);
        PendingIntent sender = PendingIntent.getBroadcast(godot.getActivity(), tag, i, PendingIntent.FLAG_UPDATE_CURRENT);
        return sender;
    }

    @Override public void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
    }

    @Override public void onMainResume() {
        checkIntent();
    } 

    private void checkIntent() {
        Intent intent = Godot.getCurrentIntent();
        if(intent == null) {
            Log.e("godot", "RN: No intent in app activity!");
            return;
        }
        if(intent.getExtras() != null) {
            Bundle extras = Godot.getCurrentIntent().getExtras();
            Log.e("godot", "RN: Extras:" + extras.toString());
            notificationData = new Dictionary();
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                try {
                    notificationData.put(key, value);
                } catch(Exception e) {
                    Log.e("godot", "RN: Conversion error: " + e.toString());
                    e.printStackTrace();
                }
                Log.e("godot", "RN: Extras content: " + notificationData.toString());
            }
        } else {
            Log.e("godot", "RN: No extra bundle in app activity!");
        }
        if(intent.getAction() != null) {
            action = intent.getAction();
        }
        if(intent.getData() != null) {
            uri = intent.getData().toString();
        }
    }

    public Dictionary get_notification_data() {
        return notificationData;
    }

    public String get_deeplink_action() {
        return action;
    }

    public String get_deeplink_uri() {
        return uri;
    }
}
