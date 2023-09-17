package com.gbsoft.ellosseum;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GpsService extends Service {
    private final String TAG = "GpsServiceLog";
    private final int REPEAT_TIME = 10000;

    private String mUserId = "";

    private SharedPreferences mSharedPreferences;
    private boolean mIsGpsOn = false;


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            locationResult.getLastLocation();
            double latitude = locationResult.getLastLocation().getLatitude();
            double longitude = locationResult.getLastLocation().getLongitude();
            Log.d(TAG, latitude + ", " + longitude);
            if (rangeCheck(latitude, longitude)) { // 현재 위도, 경도 체크
                if (Common.sAuthority == Common.EMP)    // 근로자
                    employeeUpdateLocation(String.valueOf(latitude), String.valueOf(longitude));
                else {  // 관리자
                    SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("lat", String.valueOf(latitude));
                    editor.putString("lon", String.valueOf(longitude));
                    editor.apply();
                    if (mIsGpsOn) {
                        if (Common.sGpsHandler != null)
                            Common.sGpsHandler.sendEmptyMessage(1);
                        if (Common.sIssueGpsHandler != null)
                            Common.sIssueGpsHandler.sendEmptyMessage(2);
                    }
                }
            }
        }
    };

    private boolean rangeCheck(Double latitude, Double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180; // 위도 -90 ~ 90, 경도 -180 ~ 180 넘을시 db에 값 저장x
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        String[] userId_arr = mSharedPreferences.getString("userID", "").split("@", -1);
        mUserId = userId_arr[1];
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.icon_gps_1x)
                .setContentTitle("Location Service")
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS).setVibrate(new long[]{0})
                .setContentText("Running")
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_MIN);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(REPEAT_TIME);  // Set the interval in which you want to get locations
        locationRequest.setFastestInterval(REPEAT_TIME);   // If a location is available sooner you can get it early.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(GpsConstants.LOCATION_SERVICE_ID, builder.build());
        Log.v(TAG, "service start");
    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        Log.v(TAG, "service stop");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(GpsConstants.ACTION_START_LOCATION_SERVICE)) {
                    mIsGpsOn = true;
                    startLocationService();
                } else if (action.equals(GpsConstants.ACTION_STOP_LOCATION_SERVICE)) {
                    mIsGpsOn = false;
                    stopLocationService();
                    stopSelf();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void employeeUpdateLocation(String latitude, String longitude) {
        String currentTime = Common.getCurrentTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));
        int site_id = mSharedPreferences.getInt("site", -1);
        Call<ResponseBody> call = Common.sService_site.employeeUpdateLocation(Integer.parseInt(mUserId), latitude, longitude, currentTime, site_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    Log.v(TAG, "employeeUpdateLocation Success");
                } else {
                    Log.v(TAG, "employeeUpdateLocation fail");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v(TAG, "employeeUpdateLocation connect fail");
            }
        });
    }
}

