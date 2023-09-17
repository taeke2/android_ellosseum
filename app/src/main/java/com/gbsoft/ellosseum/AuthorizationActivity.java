package com.gbsoft.ellosseum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivityAuthorizationBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorizationActivity extends AppCompatActivity {
    private static final String TAG = "AuthorizationActivityLog";

    private static final int MAX_COUNT = 5;    // 승인 코드 입력 최대 횟수
    private static final int HOLD_SECOND = 30;  // 오류횟수 초과 시 비활성화 시간 ( 단위 : 초 )

    private ActivityAuthorizationBinding mBinding;

    private SimpleDateFormat mSimpleDateFormat;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private SharedPreferences mSharedPreferencesRent; // 공유변수 Rent

    private long mBackKeyPressedTime = 0;
    private int mCount = 1; // 입력 오류 횟수

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAuthorizationBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        this.initialSet();
    }

    // 초기 세팅
    @SuppressLint("ClickableViewAccessibility")
    public void initialSet() {
        mBinding.txtError.setVisibility(View.GONE);
        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // 공유변수 생성
        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        mSharedPreferencesRent = getSharedPreferences("Rent", MODE_PRIVATE);
        int rentId = mSharedPreferencesRent.getInt("rentId", -1);
        if (rentId != -1) {
            Intent intent = new Intent(AuthorizationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        this.editCheck();

        mBinding.btnSetting.setVisibility(View.GONE);
        // 리스너 설정
        mBinding.btnSetting.setOnClickListener(settingClick);
        mBinding.txtLogin.setOnClickListener(toLoginClick);
        mBinding.btnCertification.setOnClickListener(toJoinClick);
        mBinding.editAuthCode.addTextChangedListener(codeChange);
        mBinding.txtLogin.setOnTouchListener(loginTouch);
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener loginTouch = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP :
                mBinding.txtLogin.setTextColor(getColor(R.color.default_gray));
                break;
            case MotionEvent.ACTION_DOWN :
                mBinding.txtLogin.setTextColor(getColor(R.color.darker_red));
                break;
        }
        return false;
    };

    private void editCheck() {
        if (mBinding.editAuthCode.getText().toString().equals("")) {
            mBinding.btnCertification.setEnabled(false);
            mBinding.btnCertification.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        } else {
            mBinding.btnCertification.setEnabled(true);
            mBinding.btnCertification.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
        }
    }

    TextWatcher codeChange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            editCheck();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            editCheck();
        }

        @Override
        public void afterTextChanged(Editable s) {
            editCheck();
        }
    };
    // 설정 클릭
    View.OnClickListener settingClick = v -> {
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        intent.putExtra("type", "auth");
        startActivity(intent);
    };
    // '이미 회원정보가 있으신가요?' 클릭
    View.OnClickListener toLoginClick = v -> {
        Intent intent = new Intent(AuthorizationActivity.this, LoginActivity.class);
        startActivity(intent);
        mBinding.editAuthCode.setText("");
        mBinding.txtError.setVisibility(View.GONE);
    };
    // 승인코드 입력 후 '확인' 클릭
    View.OnClickListener toJoinClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // 승인코드는 6자리, 만약 6자리가 넘어갈 경우 시리얼번호로 등록하게 됨.
            // 현재는 그냥 바로 공유변수에 저장하지만 추후에 데이터베이스에서 시리얼코드가 있는지 체크해서 진행할 예정
            // sitecode와 site 또한 시리얼넘버를 통해 바로 알 수 있어야함

            if (mBinding.editAuthCode.length() > 6) {
                int siteCode = mSharedPreferences.getInt("site", -1);
                String link = mSharedPreferences.getString("siteLink", "");
                if (siteCode == -1 || link.equals("")) {
                    Intent intent = new Intent(AuthorizationActivity.this, SiteSelectActivity.class);
                    startActivityResult.launch(intent);
                } else {
                    serialNumberCheck();
                }
            } else {
                String holdTime = mSharedPreferences.getString("hold", "");
                Log.v(TAG, "holdTime = " + holdTime);
                boolean isHold = !holdTime.equals("") && timeCheck(holdTime, HOLD_SECOND);
                if (isHold) {   // 승인코드 입력 비활성화
                    if (!AuthorizationActivity.this.isFinishing())
                        Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_auth_warning_title), getString(R.string.dialog_auth_warning_content) + holdTime, () -> { });
                } else {    // 승인코드 입력 활성화
                    mEditor.remove("hold");
                    mEditor.commit();
                    if (mCount <= MAX_COUNT) {  // 승인코드 확인
                        authCheck();
                    } else {    // 최대 유효 횟수 초과일 때
                        String currentTime = Common.getCurrentTime(mSimpleDateFormat);
                        mEditor.putString("hold", currentTime);
                        mEditor.commit();
                        mCount = 1;
                        if (!AuthorizationActivity.this.isFinishing())
                            Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_auth_warning_title), getString(R.string.dialog_auth_warning_content) + currentTime, () -> { });
                    }
                }
            }
        }
    };
    // 현장 선택 데이터 받기
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                serialNumberCheck();
            }
        }
    });

    // 유효시간 체크 (유효시작시각, 유효시간)
    private boolean timeCheck(String date, int time) {
        Date nowDate = new Date();
        Date startDate = null;
        try {
            startDate = mSimpleDateFormat.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.SECOND, time);

            Date endDate = cal.getTime();
            return nowDate.after(startDate) && nowDate.before(endDate);
        } catch (ParseException e) {
            Log.e(Common.TAG_ERR, "ERROR: Parse exception - timeCheck");
        }
        return false;
    }

    // 대여폰 serialNumber 체크
    public void serialNumberCheck() {
        mBinding.btnCertification.setEnabled(false);
//        mBinding.btnCertification.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));

        String serialNumber = mBinding.editAuthCode.getText().toString();
//        Call<ResponseBody> call = Common.sService_master.serialNumberCheck(serialNumber.equals("") ? "0" : serialNumber);
        Call<ResponseBody> call = Common.sService_site.serialNumberCheck(serialNumber.equals("") ? "0" : serialNumber);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "serialNumber result = " + result);
                        JSONObject jsonObject = new JSONObject(result);
                        SharedPreferences.Editor editorRent = mSharedPreferencesRent.edit();
                        editorRent.putInt("rentId", jsonObject.getInt("id"));
                        editorRent.apply();

                        Intent intent = new Intent(AuthorizationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - authCheck");
                        if (!AuthorizationActivity.this.isFinishing())
                            Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - authCheck");
                        if (!AuthorizationActivity.this.isFinishing())
                            Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
//                    finally {
////                        mBinding.btnCertification.setEnabled(true);
////                        mBinding.btnCertification.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
//                    }
                } else if (code == 204) {
                    mBinding.txtError.setVisibility(View.VISIBLE);
                    mBinding.editAuthCode.setText("");
                } else {
                    if (!AuthorizationActivity.this.isFinishing())
                        Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(Common.TAG_ERR, "Connect fail - authCheck");
                if (!AuthorizationActivity.this.isFinishing())
                    Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }


    // 입력한 승인코드 확인
    public void authCheck() {
        mBinding.btnCertification.setEnabled(false);
        mBinding.btnCertification.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        String inputCode = mBinding.editAuthCode.getText().toString();
        Call<ResponseBody> call = Common.sService_master.authCheck(inputCode.equals("") ? "0" : inputCode);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        JSONObject jsonObject = new JSONObject(result);
                        int siteCode = jsonObject.getInt("siteCode");
                        String url = jsonObject.getString("link");
                        mEditor.putString("siteLink", url);
                        mEditor.putInt("site", siteCode);
                        mEditor.commit();
                        Common.getService2(url);
                        Intent intent = new Intent(AuthorizationActivity.this, TermsActivity.class);
                        startActivity(intent);

                        mBinding.editAuthCode.setText("");
                        mBinding.txtError.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - authCheck");
                        if (!AuthorizationActivity.this.isFinishing())
                            Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - authCheck");
                        if (!AuthorizationActivity.this.isFinishing())
                            Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                    finally {
                        mBinding.btnCertification.setEnabled(true);
                        mBinding.btnCertification.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                    }
                } else if (code == 204) {
                    mBinding.txtError.setVisibility(View.VISIBLE);
                    mBinding.editAuthCode.setText("");
                    mCount++;
                } else {
                    if (!AuthorizationActivity.this.isFinishing())
                        Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> {
                            mBinding.btnCertification.setEnabled(true);
                            mBinding.btnCertification.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                        });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(Common.TAG_ERR, "Connect fail - authCheck");
                if (!AuthorizationActivity.this.isFinishing())
                    Common.showDialog(AuthorizationActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> {
                        mBinding.btnCertification.setEnabled(true);
                        mBinding.btnCertification.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                    });
            }
        });
    }

    // 뒤로가기 두번 클릭 시 종료
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mBackKeyPressedTime < 1500) {
            finishAffinity();
            System.runFinalization();
            System.exit(0);
        }
        mBackKeyPressedTime = System.currentTimeMillis();
        Common.showToast(getApplicationContext(), getString(R.string.exit_app));
    }
}
