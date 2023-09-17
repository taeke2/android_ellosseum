package com.gbsoft.ellosseum;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivityRemedyBinding;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemedyActivity extends AppCompatActivity {
    private static final String TAG = "RemedyActivityLog";
    private ActivityRemedyBinding mBinding;
    private SharedPreferences mSharedPreferences;
    private int mSiteCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRemedyBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    private void initialSet() {
        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        mSiteCode = mSharedPreferences.getInt("site", -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        editCheck();
        mBinding.btnOk.setOnClickListener(okClick);
        mBinding.editTitle.addTextChangedListener(mTextWatcher);
        mBinding.editContent.addTextChangedListener(mTextWatcher);
        mBinding.btnBack.setOnClickListener(backClick);
    }

    // 뒤로가기 클릭
    View.OnClickListener backClick = v -> onBackPressed();

    TextWatcher mTextWatcher = new TextWatcher() {
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

    // EditText 공백 체크
    private void editCheck() {
        if (mBinding.editTitle.getText().toString().equals("") || mBinding.editContent.getText().toString().equals("")) {
            mBinding.btnOk.setEnabled(false);
            mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        } else {
            mBinding.btnOk.setEnabled(true);
            mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
        }
    }

    // 확인 버튼 클릭
    View.OnClickListener okClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckDialog dialog = new CheckDialog(RemedyActivity.this, mCheckDialogClickListener, getString(R.string.dialog_remedy_title), getString(R.string.register_complaint));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(true); // 다이얼로그 밖에 터치 시 종료
            dialog.setCancelable(true); // 다이얼로그 취소 가능 (back key)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 다이얼로그 background 적용하기 위한 코드 (radius 적용)
            dialog.show();
        }
    };
    // 다이얼로그 확인, 취소 클릭 리스너
    CheckDialogClickListener mCheckDialogClickListener = new CheckDialogClickListener() {
        @Override
        public void onPositiveClick() {
            String title = mBinding.editTitle.getText().toString();
            String content = mBinding.editContent.getText().toString();
            inputData(title, content);
            finish();
        }
    };

    // 신문고 내용 등록
    private void inputData(String title, String content) {
        String currentTime = Common.getCurrentTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));
        Log.v(TAG, "inputData 실행 >> " + title + "/" + content + "/" + currentTime + "/" + mSiteCode);
        Call<ResponseBody> call = Common.sService_site.inputRemedy(title, content, currentTime, mSiteCode, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    Common.showToast(getApplicationContext(), getString(R.string.registered));
                } else if (code == 401) {
                    if (!RemedyActivity.this.isFinishing())
                        Common.appRestart(RemedyActivity.this);
                } else {
                    if (!RemedyActivity.this.isFinishing())
                        Common.showDialog(RemedyActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                if (!RemedyActivity.this.isFinishing())
                    Common.showDialog(RemedyActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }
}
