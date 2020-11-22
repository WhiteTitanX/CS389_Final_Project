package com.cs389f20.diamonds;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OccupancyAlertManager {
    private static OccupancyAlertManager oam;
    private final int INTERVAL = MainActivity.REFRESH_INTERVAL;
    private List<Building> notificationBuildings;
    private Handler handler;
    private Runnable checker;
    private MainActivity ma;

    public OccupancyAlertManager() {
        oam = this;
        ma = MainActivity.getInstance();
        notificationBuildings = new ArrayList<>();
        setup();
    }

    private void setup() {
        HandlerThread checkerThread = new HandlerThread("OccupancyAlertThread");
        checkerThread.start();
        handler = new Handler(checkerThread.getLooper());
        check();
    }

    public static OccupancyAlertManager getInstance() {
        return oam;
    }

    public void check() {
        checker = new Runnable() {
            @Override
            public void run() {
                for (Building b : notificationBuildings) {
                    checkNotification(b);
                }
                if (handler != null) //just in case runnable is active just as we destroy the handler
                    handler.postDelayed(this, TimeUnit.MINUTES.toMillis(INTERVAL));
            }
        };
        handler.postDelayed(checker, TimeUnit.MINUTES.toMillis(INTERVAL));
    }

    public void add(Building building, NotificationType type) {
        if (type == null) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ma);
            notificationManager.cancel(building.notificationID);
            remove(building);
            return;
        }
        Log.d(OccupancyAlertManager.class.getSimpleName(), "adding notification for " + building.name + " type: " + type);
        if (type == NotificationType.ZERO)
            building.notificationType = OccupancyAlertManager.NotificationType.ZERO;
        else if (type == NotificationType.BELOW)
            building.notificationType = OccupancyAlertManager.NotificationType.BELOW;
        else if (type == NotificationType.ABOVE)
            building.notificationType = OccupancyAlertManager.NotificationType.ABOVE;
        else
            building.notificationType = OccupancyAlertManager.NotificationType.MAX;

        if (notificationBuildings.contains(building)) {
            notificationBuildings.add(building);
            notificationBuildings.remove(building); //delete the first copy after adding the new.
        } else
            notificationBuildings.add(building);

        if (handler == null)
            setup();
    }

    public void remove(Building b) {
        boolean removed = notificationBuildings.remove(b);
        b.notificationType = null;
        if (removed && notificationBuildings.size() == 0) {
            Log.d(OccupancyAlertManager.class.getSimpleName(), "Destroying handler as there aren't anymore notifications active");
            destroyHandler();
        }
    }

    private void checkNotification(final Building b) {
        //get notification for this building. if doesn't exist, return.
        //if the notification requirements is met, call notify.
        Log.d(OccupancyAlertManager.class.getSimpleName(), "Checking notifications for building " + b.name + ". target type: " + b.notificationType);
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ma);
                NotificationType type = b.notificationType;
                if ((type == NotificationType.ZERO && b.currentNumberOfPeople == 0) ||
                        (type == NotificationType.BELOW && b.currentNumberOfPeople <= b.maxOccupancy / 2) ||
                        (type == NotificationType.ABOVE && b.currentNumberOfPeople > b.maxOccupancy / 2) ||
                        (type == NotificationType.MAX && b.currentNumberOfPeople >= b.maxOccupancy)
                ) {
                    Intent intent = new Intent(ma, MainActivity.class);
                    //    intent.putExtra(BuildingSelectActivity.EXTRA_BUILDING, b); //for directly launching to BuildingActivity (but back button will exit out of app)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(ma, 0, intent, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ma, MainActivity.NOTIFICATION_CHANNEL)
                            .setSmallIcon(R.drawable.app_notification_icon)
                            .setLargeIcon(BitmapFactory.decodeResource(ma.getResources(),
                                    R.drawable.app_notification_icon))
                            .setContentTitle(b.name + " Occupancy Alert")
                            .setContentText("The building has reached " + getMessage(type) + " occupancy")
                            .setContentIntent(pendingIntent)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    notificationManager.notify(b.notificationID, builder.build());
                    remove(b); //once the notification is activated, we no longer need to keep track of it
                }
            }
        });
        t.start();
    }


    private String getMessage(NotificationType type) {
        switch (type) {
            case ZERO:
                return "zero";
            case BELOW:
                return "below 50%";
            case ABOVE:
                return "above 50%";
            case MAX:
                return "at or above maximum";
            default:
                return "your set";
        }
    }

    public void destroyHandler() {
        if (checker != null && handler != null) {
            handler.removeCallbacks(checker);
            handler.getLooper().quit();
            handler = null;
        }
    }

    public enum NotificationType implements java.io.Serializable {
        ZERO, BELOW, ABOVE, MAX
    }

}
