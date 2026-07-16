package com.example.silentmodescheduler.core.scheduler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import com.example.silentmodescheduler.data.firebase.firestore.FirebaseFirestoreRepository;
import com.example.silentmodescheduler.data.model.TimePeriod;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

public class SilentModeReceiver extends BroadcastReceiver {
    public static final String ACTION_START_SILENT = "com.example.silentmodescheduler.ACTION_START_SILENT";
    public static final String ACTION_END_SILENT = "com.example.silentmodescheduler.ACTION_END_SILENT";

    public static final String EXTRA_PERIOD_ID = "period_id";
    public static final String EXTRA_PERIOD_NAME = "period_name";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_END_TIME = "end_time";

    private static final String CHANNEL_ID = "silent_mode_channel";
    private static final int NOTIFICATION_ID_START = 1001;
    private static final int NOTIFICATION_ID_END = 1002;
    private static final int NOTIFICATION_ID_ERROR = 1003;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                rescheduleAllAlarms(context);
                break;
            case ACTION_START_SILENT:
            case ACTION_END_SILENT:
                reschedulePeriodAlarms(context, intent);
                evaluateAndApplyCurrentState(context);
                break;
        }
    }

    private void evaluateAndApplyCurrentState(Context context) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) return;
        String userId = firebaseAuth.getCurrentUser().getUid();

        FirebaseFirestoreRepository repository = new FirebaseFirestoreRepository();
        repository.getCollection("users/" + userId + "/time_periods")
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    SilentModeScheduler scheduler = new SilentModeScheduler(context);
                    java.util.List<TimePeriod> periods = new java.util.ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString("name");
                        String start = doc.getString("startTime");
                        String end = doc.getString("endTime");
                        Boolean enabled = doc.getBoolean("enabled");
                        if (enabled == null) enabled = true;

                        if (enabled && name != null && !name.trim().isEmpty()
                                && start != null && !start.trim().isEmpty()
                                && end != null && !end.trim().isEmpty()) {
                            periods.add(new TimePeriod(doc.getId(), name, start, end, enabled));
                        }
                    }
                    scheduler.checkAndApplyCurrentState(periods);
                });
    }

    private void reschedulePeriodAlarms(Context context, Intent intent) {
        String periodId = intent.getStringExtra(EXTRA_PERIOD_ID);
        String periodName = intent.getStringExtra(EXTRA_PERIOD_NAME);
        String startTime = intent.getStringExtra(EXTRA_START_TIME);
        String endTime = intent.getStringExtra(EXTRA_END_TIME);

        if (periodId != null && !periodId.trim().isEmpty()
                && startTime != null && !startTime.trim().isEmpty()
                && endTime != null && !endTime.trim().isEmpty()) {
            TimePeriod period = new TimePeriod(periodId, periodName != null ? periodName : "", startTime, endTime, true);
            new SilentModeScheduler(context).scheduleAlarms(period);
        }
    }

    public static void enableSilentMode(Context context, String periodName) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                showNotification(context, "Silent Mode Enabled", "Activated for schedule: " + periodName, NOTIFICATION_ID_START);
            } else {
                showNotification(context, "Silent Mode Error", "DND access permission is missing. Tap to configure.", NOTIFICATION_ID_ERROR);
            }
        } else {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            showNotification(context, "Silent Mode Enabled", "Activated for schedule: " + periodName, NOTIFICATION_ID_START);
        }
    }

    public static void disableSilentMode(Context context, String periodName) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                showNotification(context, "Silent Mode Disabled", "Restored ring mode for schedule: " + periodName, NOTIFICATION_ID_END);
            }
        } else {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            showNotification(context, "Silent Mode Disabled", "Restored ring mode for schedule: " + periodName, NOTIFICATION_ID_END);
        }
    }

    private void rescheduleAllAlarms(Context context) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) return;
        String userId = firebaseAuth.getCurrentUser().getUid();

        new Thread(() -> {
            FirebaseFirestoreRepository repository = new FirebaseFirestoreRepository();
            repository.getCollection("users/" + userId + "/time_periods")
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        SilentModeScheduler scheduler = new SilentModeScheduler(context);
                        java.util.List<TimePeriod> periods = new java.util.ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String name = doc.getString("name");
                            String start = doc.getString("startTime");
                            String end = doc.getString("endTime");
                            Boolean enabled = doc.getBoolean("enabled");
                            if (enabled == null) enabled = true;

                            if (enabled && name != null && !name.trim().isEmpty()
                                    && start != null && !start.trim().isEmpty()
                                    && end != null && !end.trim().isEmpty()) {
                                TimePeriod period = new TimePeriod(doc.getId(), name, start, end, enabled);
                                scheduler.scheduleAlarms(period);
                                periods.add(period);
                            }
                        }
                        // Re-evaluate and apply correct ringer state immediately on boot
                        scheduler.checkAndApplyCurrentState(periods);
                    });
        }).start();
    }

    public static void showNotification(Context context, String title, String message, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Silent Mode Scheduler Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (notificationId == NOTIFICATION_ID_ERROR) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.setContentIntent(pendingIntent);
        }

        notificationManager.notify(notificationId, builder.build());
    }
}
