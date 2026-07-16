package com.example.silentmodescheduler.core.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.silentmodescheduler.data.model.TimePeriod;
import java.util.Calendar;

public class SilentModeScheduler {
    private final Context context;
    private final AlarmManager alarmManager;

    public SilentModeScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleAlarms(TimePeriod period) {
        scheduleAlarm(period, true);
        scheduleAlarm(period, false);
    }

    public void cancelAlarms(String periodId) {
        cancelAlarm(periodId, true);
        cancelAlarm(periodId, false);
    }

    public int parseTimeToSeconds(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) {
            return -1;
        }
        String[] parts = timeStr.trim().split(":");
        if (parts.length < 2) return -1;

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = 0;
            if (parts.length >= 3) {
                second = Integer.parseInt(parts[2]);
            }
            return hour * 3600 + minute * 60 + second;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int getCurrentTimeInSeconds(Calendar cal) {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        return hour * 3600 + minute * 60 + second;
    }

    public boolean isCurrentTimeWithinPeriod(TimePeriod period) {
        String start = period.getStartTime();
        String end = period.getEndTime();

        int startSeconds = parseTimeToSeconds(start);
        int endSeconds = parseTimeToSeconds(end);

        if (startSeconds == -1 || endSeconds == -1) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        int currentSeconds = getCurrentTimeInSeconds(now);

        if (endSeconds > startSeconds) {
            // Same-day period (e.g. 14:00:00 to 16:00:00)
            return currentSeconds >= startSeconds && currentSeconds < endSeconds;
        } else {
            // Overnight period (e.g. 22:00:00 to 06:00:00)
            return currentSeconds >= startSeconds || currentSeconds < endSeconds;
        }
    }

    public void checkAndApplyCurrentState(java.util.List<TimePeriod> periods) {
        boolean shouldBeSilent = false;
        String activePeriodName = "";

        for (TimePeriod period : periods) {
            if (period.isEnabled() && isCurrentTimeWithinPeriod(period)) {
                shouldBeSilent = true;
                activePeriodName = period.getName();
                break;
            }
        }

        if (shouldBeSilent) {
            SilentModeReceiver.enableSilentMode(context, activePeriodName);
        } else {
            SilentModeReceiver.disableSilentMode(context, "Restored Mode");
        }
    }

    private void scheduleAlarm(TimePeriod period, boolean isStart) {
        String timeStr = isStart ? period.getStartTime() : period.getEndTime();
        if (timeStr == null || !timeStr.contains(":")) return;

        String[] parts = timeStr.trim().split(":");
        if (parts.length < 2) return;

        int hour, minute, second = 0;
        try {
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
            if (parts.length >= 3) {
                second = Integer.parseInt(parts[2]);
            }
        } catch (NumberFormatException e) {
            return;
        }

        // Cancel existing alarm of this type before setting a new one
        cancelAlarm(period.getId(), isStart);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);

        // If alarm time is in past, schedule for next day
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, SilentModeReceiver.class);
        intent.setAction(isStart ? SilentModeReceiver.ACTION_START_SILENT : SilentModeReceiver.ACTION_END_SILENT);
        intent.putExtra(SilentModeReceiver.EXTRA_PERIOD_ID, period.getId());
        intent.putExtra(SilentModeReceiver.EXTRA_PERIOD_NAME, period.getName());
        intent.putExtra(SilentModeReceiver.EXTRA_START_TIME, period.getStartTime());
        intent.putExtra(SilentModeReceiver.EXTRA_END_TIME, period.getEndTime());

        int requestCode = getRequestCode(period.getId(), isStart);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }

    private void cancelAlarm(String periodId, boolean isStart) {
        Intent intent = new Intent(context, SilentModeReceiver.class);
        intent.setAction(isStart ? SilentModeReceiver.ACTION_START_SILENT : SilentModeReceiver.ACTION_END_SILENT);
        
        int requestCode = getRequestCode(periodId, isStart);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private int getRequestCode(String periodId, boolean isStart) {
        int base = periodId != null ? periodId.hashCode() : 0;
        return isStart ? base : base + 1000003;
    }
}
