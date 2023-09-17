package com.gbsoft.ellosseum;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivityLoadingBinding;
import com.gbsoft.ellosseum.dto.SiteDTO;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity implements AutoPermissionsListener {
    private static final String TAG = "LoadingActivityLog";
    private static final int SITE_OK = 1;
    private static final int TOKEN_OK = 2;
    private static final int NO = 0;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private ActivityLoadingBinding mBinding;

    private SharedPreferences mSharedPreferences;
    private Intent mIntent;

    private int mCounter = 0;

    private boolean mIsPermissionAccess;
    private boolean mIsSiteList = false;
    private boolean mIsToken = false;
    private String mType = "";
    private String mSosEmpId = "";
    private boolean mIsLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoadingBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // 위치 서비스 권한 승인
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoadingActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        }
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        this.initialSet();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.runFinalization();
        System.exit(0);
    }

    // 초기 설정
    private void initialSet() {
        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mBinding.pbLoading.getProgressDrawable().setColorFilter(new BlendModeColorFilter(Color.RED, BlendMode.SRC_ATOP));
        else
            mBinding.pbLoading.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN); // progress bar 설정
        Common.getService1();   // DB_master api service 생성

        if (Common.sSiteDTOS.size() <= 0) getSiteList();
        else mIsSiteList = true;

        // 토큰 정보 및 site, siteLink 정보 가지고 있는지 체크
        Common.sToken = mSharedPreferences.getString("jwt", "");
        String url = mSharedPreferences.getString("siteLink", "");
        int siteCode = mSharedPreferences.getInt("site", -1);
        Common.getUserId(mSharedPreferences);

        if (!url.equals("")) Common.getService2(url);   // DB 현장 api service 생성

//        boolean isLogin = !Common.sToken.equals("") && !url.equals("") && siteCode != -1 && !Common.sUserId.equals("") && Common.sAuthority != 0;
        boolean isLogin = !Common.sToken.equals("") && !Common.sUserId.equals("") && Common.sAuthority != 0;
        if (isLogin) getNewToken();
        else mIsToken = true;
        mIntent = new Intent(getApplicationContext(), isLogin ? MainActivity.class : AuthorizationActivity.class);

        getIntentData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent.getStringExtra("type") == null) {    // type 이 없을 때 - 토큰 갱신만 동작
            mType = "N";
        } else {
            mType = intent.getStringExtra("type");  // type 이 있을 때
            mSosEmpId = intent.getStringExtra("empId");
        }
        Log.v(TAG, "mType = " + mType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int i, String[] strings) {
    }

    @Override
    public void onGranted(int i, String[] strings) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionCheck();
    }

    public void permissionCheck() {
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                mIsPermissionAccess = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Log.v(TAG, "permission = " + mIsPermissionAccess);
                if (mIsPermissionAccess && mIsSiteList && mIsToken) {
                    t.cancel();
                    Log.v(TAG, "permission t.cancel()");
                    loading();
                }
            }
        };

        t.schedule(tt, 0, 100);
    }

    public void loading() {
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                mCounter++;
                mBinding.pbLoading.setProgress(mCounter);
                if (mCounter >= 100 && !mIsLoading) {
                    mIsLoading = true;
                    try {
                        t.cancel();
                        Log.v(TAG, "loading t.cancel()");
                        if (!mType.equals("N")) {
                            mIntent.putExtra("type", mType);
                            mIntent.putExtra("empId", mSosEmpId);
                        } else
                            Thread.sleep(500);
                        startActivity(mIntent);
                        finish();
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        t.schedule(tt, 0, 5);
    }

    private final Handler loadingHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case NO:
                    mIsSiteList = false;
                    mIsToken = false;
                    break;
                case SITE_OK:
                    mIsSiteList = true;
                    break;
                case TOKEN_OK:
                    mIsToken = true;
                    break;
            }
        }
    };

    public void getNewToken() {
        Call<ResponseBody> call = Common.sService_site.getNewToken_(Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getNewToken result = " + result);
                        SharedPreferences sharedPreferences = getSharedPreferences("Info", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("jwt", result);
                        editor.apply();
                        Common.sToken = result;
                        loadingHandler.sendEmptyMessage(TOKEN_OK);
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "IOException - getNewToken");
                        if (!LoadingActivity.this.isFinishing())
                            Common.showDialog(LoadingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else if (code == 401) {
                    // 토큰 만료
                    if (!LoadingActivity.this.isFinishing())
                        Common.appRestart(LoadingActivity.this);
                    loadingHandler.sendEmptyMessage(NO);
                } else {
                    if (!LoadingActivity.this.isFinishing())
                        Common.showDialog(LoadingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                    loadingHandler.sendEmptyMessage(NO);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!LoadingActivity.this.isFinishing())
                    Common.showDialog(LoadingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
                loadingHandler.sendEmptyMessage(NO);
            }
        });
    }

    // 현장 리스트 조회
    public void getSiteList() {
        Common.sSiteDTOS.clear();
        Call<ResponseBody> call = Common.sService_master.getSiteList_();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getSiteList result = " + result);
                        JSONArray jsonArray = new JSONArray(result);
                        int jsonLen = jsonArray.length();
                        for (int i = 0; i < jsonLen; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            SiteDTO dto = new SiteDTO();
                            dto.setId(jsonObject.getInt("code"));
                            dto.setName(jsonObject.getString("siteName"));
                            dto.setDbName(jsonObject.getString("dName"));
                            dto.setState(jsonObject.getInt("state"));
                            dto.setLatitude(jsonObject.getString("lat"));
                            dto.setLongitude(jsonObject.getString("lon"));
                            dto.setRemark(jsonObject.getString("remark"));
                            dto.setLink(jsonObject.getString("link"));
                            Common.sSiteDTOS.add(dto);
                        }
                        loadingHandler.sendEmptyMessage(SITE_OK);
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getSiteList");
                        if (!LoadingActivity.this.isFinishing())
                            Common.showDialog(LoadingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getSiteList");
                        if (!LoadingActivity.this.isFinishing())
                            Common.showDialog(LoadingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else {
                    if (!LoadingActivity.this.isFinishing())
                        Common.showDialog(LoadingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                    loadingHandler.sendEmptyMessage(NO);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!LoadingActivity.this.isFinishing())
                    Common.showDialog(LoadingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
                loadingHandler.sendEmptyMessage(NO);
            }
        });
    }
}
