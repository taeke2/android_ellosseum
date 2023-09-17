package com.gbsoft.ellosseum;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivityFindPwBinding;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPwActivity extends AppCompatActivity {

    private static final String TAG = "FindPwActivityLog";

    private ActivityFindPwBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityFindPwBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialSet() {
        mBinding.btnFindPw.setEnabled(false);
        mBinding.btnFindPw.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        mBinding.editId.addTextChangedListener(idChange);
        mBinding.editName.addTextChangedListener(nameChange);
        mBinding.editEmail.addTextChangedListener(emailChange);
        mBinding.txtLogin.setOnClickListener(loginClick);
        mBinding.txtFindId.setOnClickListener(findIdClick);
        mBinding.btnFindPw.setOnClickListener(findPwClick);

        mBinding.txtLogin.setOnTouchListener(loginTouch);
        mBinding.txtFindId.setOnTouchListener(findIdTouch);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener findIdTouch = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP :
                mBinding.txtFindId.setTextColor(getColor(R.color.default_gray));
                break;
            case MotionEvent.ACTION_DOWN :
                mBinding.txtFindId.setTextColor(getColor(R.color.darker_red));
                break;
        }
        return false;
    };

    View.OnClickListener loginClick = v -> {
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    };

    View.OnClickListener findIdClick = v -> {
        Intent intent = new Intent(getApplicationContext(), FindIdActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    };

    TextWatcher idChange = new TextWatcher() {
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

    TextWatcher nameChange = new TextWatcher() {
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

    TextWatcher emailChange = new TextWatcher() {
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

    private void editCheck() {
        if (mBinding.editId.getText().toString().equals("") || mBinding.editName.getText().toString().equals("") || mBinding.editEmail.getText().toString().equals("")) {
            mBinding.btnFindPw.setEnabled(false);
            mBinding.btnFindPw.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        } else {
            mBinding.btnFindPw.setEnabled(true);
            mBinding.btnFindPw.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
        }
    }

    View.OnClickListener findPwClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findPw();
        }
    };

    private void findPw() {
        String id = mBinding.editId.getText().toString();
        String name = mBinding.editName.getText().toString();
        String email = mBinding.editEmail.getText().toString();
        Call<ResponseBody> call = Common.sService_master.findPw(name, email, id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "find id result = " + result);
                        if (result.equals("success")) {
                            if (!FindPwActivity.this.isFinishing())
                                Common.showDialog(FindPwActivity.this, getString(R.string.find_pw_dialog), getString(R.string.find_pw_dialog_content), () -> { finish(); });
                        } else {
                            mBinding.txtError.setVisibility(View.VISIBLE);
                            mBinding.editId.requestFocus();
                        }
                    } catch (IOException e) {
                        if (!FindPwActivity.this.isFinishing())
                            Common.showDialog(FindPwActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }

                } else {
                    if (!FindPwActivity.this.isFinishing())
                        Common.showDialog(FindPwActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!FindPwActivity.this.isFinishing())
                    Common.showDialog(FindPwActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
