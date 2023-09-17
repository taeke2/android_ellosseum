package com.gbsoft.ellosseum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gbsoft.ellosseum.databinding.ActivityQrcodeBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateQRCodeActivity extends AppCompatActivity {
    private static final String TAG = "CreateQRCodeActivityLog";

    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_APPROVE_REPEAT = 102;
    private static final int MESSAGE_TIMER_STOP = 103;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    CreateQRCodeActivity.TimerHandler mTimerHandler = null;
    private static final int MAX_SECOND = 59;
    private static int sCount = MAX_SECOND;

    private ActivityQrcodeBinding mBinding;

    private boolean mIsFinish = false;

    private int mJoinCode;
    private int mRentId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityQrcodeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    public void initialSet() {
        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mJoinCode = mSharedPreferences.getInt("joinCode", -1);
        Log.v(TAG, "joinCode = " + mJoinCode);

        Intent intent = getIntent();
        mRentId = intent.getIntExtra("rentId", -1);

        if (mJoinCode != -1) {
            // QR 코드 만드는 부분
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(Integer.toString(mJoinCode), BarcodeFormat.QR_CODE, 200, 200);    //encode(만들 string, 형식, width, height)
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                mBinding.imgViewQrCode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } else {
            // TODO : 가입자 정보가 없을 때 by 제이
        }

        mBinding.imgViewQrCode.setOnClickListener(extendClick);
        mBinding.imgViewRefresh.setOnClickListener(extendClick);
        mBinding.btnCancel.setOnClickListener(v -> { onBackPressed(); });
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsFinish = false;
        mTimerHandler = new CreateQRCodeActivity.TimerHandler();
        sCount = MAX_SECOND;
        mBinding.txtTimer.setText("01:00");
        mBinding.txtTimer.setTextColor(Color.rgb(0, 0, 0));
        mTimerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
    }

    View.OnClickListener extendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sCount = MAX_SECOND;
            mBinding.txtTimer.setText("01:00");
            mBinding.txtTimer.setTextColor(Color.rgb(0, 0, 0));
        }
    };

    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_TIMER_START:
                    // 타이머 초기화 기능
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.removeMessages(MESSAGE_APPROVE_REPEAT);
                    this.sendEmptyMessage(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessage(MESSAGE_APPROVE_REPEAT);
                    break;
                case MESSAGE_TIMER_REPEAT:
                    if (mIsFinish)
                        this.sendEmptyMessage(MESSAGE_TIMER_STOP);
                    // 타이머 반복 기능
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 1000);
                    mBinding.txtTimer.setText("00:" + String.format("%02d", sCount--));
                    if (sCount < 5)
                        mBinding.txtTimer.setTextColor(Color.RED); //5초 남으면 빨간색으로 표시
//                    getApproveYn();
                    if (sCount < 0) {
                        this.sendEmptyMessage(MESSAGE_TIMER_STOP);
                        finish();
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                    break;
                case MESSAGE_APPROVE_REPEAT:
                    getApproveYn();
                    break;
                case MESSAGE_TIMER_STOP:
                    // 타이머 종료 기능
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.removeMessages(MESSAGE_APPROVE_REPEAT);
                    break;
            }
        }
    }

    // 가입 근로자 승인 여부 파악
    public void getApproveYn() {
        Call<ResponseBody> call = Common.sService_site.getApproveYn_(mJoinCode);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getApproveYn result = " + result + " / " + sCount);
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getString("yn").equals("Y")) {
                            if (!mIsFinish) {
                                mEditor.putString("userID", "emp@" + mJoinCode);
                                mEditor.remove("joinCode");
                                mEditor.apply();
                                if (mRentId != -1)
                                    rentAccept(mJoinCode, mRentId);
                                else
                                    getEmployeeToken();
                                mIsFinish = true;
                            }
                        }
                        mTimerHandler.sendEmptyMessageDelayed(MESSAGE_APPROVE_REPEAT, 1000);
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getApproveYn");
                        mTimerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
                        if (!CreateQRCodeActivity.this.isFinishing())
                            Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getApproveYn");
                        mTimerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
                        if (!CreateQRCodeActivity.this.isFinishing())
                            Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else {
                    mTimerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
                    if (!CreateQRCodeActivity.this.isFinishing())
                        Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mTimerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
                if (!CreateQRCodeActivity.this.isFinishing())
                    Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
            }
        });
    }

    public void rentAccept(int employeeId, int rentId) {
        Call<ResponseBody> call;
        call = Common.sService_site.rentAccept(employeeId, rentId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    getEmployeeToken();
                } else {
                    if (!CreateQRCodeActivity.this.isFinishing())
                        Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!CreateQRCodeActivity.this.isFinishing())
                    Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
            }
        });
    }

    private void getEmployeeToken() {
        Call<ResponseBody> call = Common.sService_site.getEmployeeToken(mJoinCode, Common.EMP);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getEmployeeToken result = " + result);
                        mEditor.putString("jwt", result);
                        mEditor.apply();
                        Common.sToken = result;
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getEmployeeToken");
                        if (!CreateQRCodeActivity.this.isFinishing())
                            Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else {
                    if (!CreateQRCodeActivity.this.isFinishing())
                        Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!CreateQRCodeActivity.this.isFinishing())
                    Common.showDialog(CreateQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIsFinish = true;
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
