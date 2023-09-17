package com.gbsoft.ellosseum;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessageService extends FirebaseMessagingService {
    private final String TAG = "MessageServiceLog";

    private final Uri mUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // 로그아웃 시 알람받지않도록
        if (getSharedPreferences("Info", MODE_PRIVATE).getString("userID", "").equals(""))
            return;

        Log.v(TAG, "getData : " + remoteMessage.getData());
        if (remoteMessage.getData().size() > 0) {
            String type = remoteMessage.getData().get("type");
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");

            switch (type) {
                case "sos":
                    Common.sNotificationSosId++;
                    showNotificationSos(type, body);
//                    setCustomAlarm(new long[]{300, 100, 100, 100, 100, 100, 300, 100, 300, 100, 300, 100, 300, 100, 100, 100, 100, 100});
                    break;
                case "gps":
                    Common.sNotificationGpsId++;
                    showNotificationGps(type, title, body);
                    break;
                case "restrictedIn":
                case "restrictedOut":
                    Common.sNotificationWarningId++;
                    showNotificationWarning(type, body);
                    break;
                case "siteIn" :
                case "siteOut":
                    Common.sNotificationSiteId++;
                    showNotificationSite(type, body);
                    break;
                case "areaIn" :
                case "areaOut" :
                    Common.sNotificationAreaId++;
                    showNotificationArea(type, body);
                    break;
                case "work":
                case "workOff":
                case "workOut":
                    Common.sNotificationWorkId++;
                    showNotificationWork(type);
                    break;
                case "logout":
                    Common.sNotificationLogoutId++;
                    showNotificationLogout(type);
                    break;
                default:
                    break;
            }
        }
    }

    private void setCustomAlarm(long[] vibratorPattern) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(vibratorPattern, 0);
        try {
            Thread.sleep(10000);
            vibrator.cancel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private PendingIntent getPendingIntent(String type, String message) {
        Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("empId", message);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent;
    }

    public void showNotificationSos(String type, String message) {
        String channel_id = "CHN_SOS";
        String channel_name = "SOS";
        String group_key = "groupKey_sos";

//        buildNotification(
//                type, title, message, message,
//                "CHN_SOS", "SOS 요청", "groupKey_sos",
//                Common.sNotificationSosId, R.drawable.icon_emergency_1x,
//                "구조 알람", "구조",10);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 알림 채널이 필요한 안드로이드 버전을 위한 코드
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(mUri, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Notification builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSound(mUri)             // 기본 사운드로 알림음 설정. 커스텀하려면 소리 파일의 uri 입력
                .setVibrate(new long[]{300, 100, 100, 100, 100, 100, 300, 100, 100, 100, 300, 100, 300, 100, 100, 100, 100, 100})
                .setAutoCancel(true)        // 알람 터치시 알람 삭제 유무
                .setContentTitle(getString(R.string.sos_send))
                .setContentText(message)
                .setContentIntent(getPendingIntent(type, message))  // 팝업 터치시 이동할 액티비티를 지정합니다.
                .setSmallIcon(R.drawable.icon_emergency_1x) // 알람 아이콘 (상단에 작은 아이콘 설정)
                .setGroup(group_key)
                .build();

//                        .setOnlyAlertOnce(false)     // 동일한 알림은 한번만.. : 확인 하면 다시 울림

        builder.flags = Notification.FLAG_INSISTENT;

        notificationManager.notify(Common.sNotificationSosId, builder);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setContentTitle("SOS")
                .setContentText("new messages")
                .setSmallIcon(R.drawable.icon_emergency_1x)
                .setStyle(new NotificationCompat.InboxStyle()
                        .setSummaryText("SOS"))
                .setGroup(group_key)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .build();

//        notification.flags = Notification.FLAG_INSISTENT;

        notificationManager.notify(10, notification);
    }

    public void showNotificationGps(String type, String title, String message) {
        String channel_id = "CHN_GPS";
        String channel_name = "앱 실행 알림";
        String group_key = "groupKey_gps";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(mUri, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSound(mUri)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(getPendingIntent(type, message))
                .setSmallIcon(R.drawable.icon_gps_1x)
                .setGroup(group_key);

        notificationManager.notify(Common.sNotificationGpsId, builder.build());

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setContentTitle("앱 실행")
                .setContentText("new messages")
                .setSmallIcon(R.drawable.icon_gps_1x)
                .setStyle(new NotificationCompat.InboxStyle()
                        .setSummaryText("앱 실행"))
                .setGroup(group_key)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .build();
        notificationManager.notify(20, notification);
    }

    public void showNotificationWarning(String type, String message) {
        String channel_id = "CHN_WARNING";
        String channel_name = getString(R.string.warning);
        String group_key = "groupKey_warning";

        String title = "";
        switch (type) {
            case "restrictedIn":
                title = getString(R.string.restricted_in);
                break;
            case "restrictedOut":
                title = getString(R.string.restricted_out);
                break;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(mUri, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSound(mUri)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(getPendingIntent(type, message))
                .setSmallIcon(R.drawable.icon_warning_1x)
                .setGroup(group_key);

        notificationManager.notify(Common.sNotificationWarningId, builder.build());

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setContentTitle(getString(R.string.warning))
                .setContentText("new messages")
                .setSmallIcon(R.drawable.icon_warning_1x)
                .setStyle(new NotificationCompat.InboxStyle()
                        .setSummaryText(getString(R.string.warning)))
                .setGroup(group_key)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .build();
        notificationManager.notify(30, notification);
    }

    public void showNotificationSite(String type, String message) {
//        String channel_id = "CHN_SITE";
//        String channel_name = "현장출입";
//        String group_key = "groupKey_site";
        String title = "";
        switch (type) {
            case "siteIn" :
                title = getString(R.string.site_in);
                break;
            case "siteOut" :
                title = getString(R.string.site_out);
                break;
        }
        buildNotification(
                type, title, message, message,
                "CHN_SITE", getString(R.string.site_in_out), "groupKey_site",
                Common.sNotificationSiteId, R.drawable.icon_user_out,
                getString(R.string.site_in_out), getString(R.string.site_in_out),40);

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
//            notificationChannel.setSound(mUri, null);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
//                .setSound(mUri)
//                .setAutoCancel(true)
//                .setOnlyAlertOnce(true)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setContentIntent(getPendingIntent(type, message))
//                .setSmallIcon(R.drawable.icon_user_out)
//                .setGroup(group_key);
//
//        notificationManager.notify(Common.sNotificationSiteId, builder.build());
//
//        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channel_id)
//                .setContentTitle("현장출입")
//                .setContentText("new messages")
//                .setSmallIcon(R.drawable.icon_user_out)
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .setSummaryText("현장출입"))
//                .setGroup(group_key)
//                .setAutoCancel(true)
//                .setGroupSummary(true)
//                .build();
//        notificationManager.notify(40, notification);
    }

    public void showNotificationArea(String type, String message) {
//        String channel_id = "CHN_AREA";
//        String channel_name = "구역출입";
//        String group_key = "groupKey_area";
        String title = "";

        switch (type) {
            case "areaIn" :
                title = getString(R.string.area_in);
                break;
            case "areaOut" :
                title = getString(R.string.area_out);
                break;
        }

        buildNotification(
                type, title, message, message,
                "CHN_AREA", getString(R.string.area_in_out), "groupKey_area",
                Common.sNotificationSiteId, R.drawable.icon_user_out,
                getString(R.string.area_in_out), getString(R.string.area_in_out),50);

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
//            notificationChannel.setSound(mUri, null);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
//                .setSound(mUri)
//                .setAutoCancel(true)
//                .setOnlyAlertOnce(true)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setContentIntent(getPendingIntent(type, message))
//                .setSmallIcon(R.drawable.icon_user_out)
//                .setGroup(group_key);
//
//        notificationManager.notify(Common.sNotificationAreaId, builder.build());
//
//        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channel_id)
//                .setContentTitle(title)
//                .setContentText("구역출입 알림")
//                .setSmallIcon(R.drawable.icon_user_out)
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .setSummaryText(title))
//                .setGroup(group_key)
//                .setAutoCancel(true)
//                .setGroupSummary(true)
//                .build();
//        notificationManager.notify(50, notification);
    }

    public void showNotificationWork(String type) {
        String title = "";
        String message = "";
        switch (type) {
            case "work":
                Common.startLocationService(MessageService.this, Common.isLocationServiceRunning(MessageService.this));
                title = getString(R.string.work);
                message = getString(R.string.work_msg);
                break;
            case "workOff":
                title = getString(R.string.work_off);
                message = getString(R.string.work_off_msg);
                break;
            case "workOut":    // 근무지 이탈 && 퇴근
                title = getString(R.string.work_out);
                message = getString(R.string.work_out_msg);
                Common.stopLocationService(MessageService.this, Common.isLocationServiceRunning(MessageService.this));
                break;
        }

        buildNotification(
                type, title, message, Common.sUserId,
                "CHN_WORK", getString(R.string.clock_in_out), "groupKey_work",
                Common.sNotificationWorkId, R.drawable.icon_user,
                getString(R.string.clock_in_out), getString(R.string.clock_in_out),60);
    }



    public void showNotificationLogout(String type) {
        buildNotification(
                type, getString(R.string.detect_login), getString(R.string.logout_this), Common.sUserId,
                "CHN_LOGOUT", getString(R.string.logout), "groupKey_logout",
                Common.sNotificationLogoutId, R.drawable.icon_warning_1x,
                getString(R.string.logout), getString(R.string.logout), 70);

        FirebaseMessaging.getInstance().deleteToken();
        SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int siteCode = sharedPreferences.getInt("site", -1);
        String siteLink = sharedPreferences.getString("siteLink", "");
        editor.clear();
        editor.commit();
        editor.putInt("site", siteCode);
        editor.putString("siteLink", siteLink);
        editor.commit();
        Common.getService2(siteLink);
        Common.stopLocationService(getApplicationContext(), Common.isLocationServiceRunning(MessageService.this));
        Intent intent = new Intent(MessageService.this, AuthorizationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void buildNotification(
            String type, String title, String message, String userId,
            String channel_id, String channel_name, String group_key,
            int buildId, int icon, String contentText, String summaryText, int notificationId) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(mUri, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSound(mUri)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(getPendingIntent(type, userId))
                .setSmallIcon(icon)
                .setGroup(group_key);

        notificationManager.notify(buildId, builder.build());

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(icon)
                .setStyle(new NotificationCompat.InboxStyle()
                        .setSummaryText(summaryText))
                .setGroup(group_key)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .build();
        notificationManager.notify(notificationId, notification);
    }
}