package com.gbsoft.ellosseum;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivityFindIdBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindIdActivity extends AppCompatActivity {
    private static final String TAG = "FindIdActivityLog";

    private ActivityFindIdBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityFindIdBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        this.initialSet();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialSet() {
        mBinding.btnFindId.setEnabled(false);
        mBinding.btnFindId.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        mBinding.editName.addTextChangedListener(nameChange);
        mBinding.editEmail.addTextChangedListener(emailChange);
        mBinding.txtLogin.setOnClickListener(loginClick);
        mBinding.txtFindPw.setOnClickListener(findPwClick);
        mBinding.btnFindId.setOnClickListener(findIdClick);

        mBinding.txtLogin.setOnTouchListener(loginTouch);
        mBinding.txtFindPw.setOnTouchListener(findPwTouch);
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
    private View.OnTouchListener findPwTouch = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP :
                mBinding.txtFindPw.setTextColor(getColor(R.color.default_gray));
                break;
            case MotionEvent.ACTION_DOWN :
                mBinding.txtFindPw.setTextColor(getColor(R.color.darker_red));
                break;
        }
        return false;
    };

    // '아이디 찾기' 버튼 클릭
    View.OnClickListener findIdClick = v -> findId();

    // 아이디 찾기
    private void findId() {
        String name = mBinding.editName.getText().toString();
        String email = mBinding.editEmail.getText().toString();
        Call<ResponseBody> call = Common.sService_master.findId(name, email);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "findId result = " + result);
                        JSONArray jsonArray = new JSONArray(result);
                        int jsonArray_len = jsonArray.length();
                        if (jsonArray_len == 0) {
                            mBinding.txtError.setVisibility(View.VISIBLE);
                            mBinding.editEmail.setText("");
                            mBinding.editEmail.requestFocus();
                        } else {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String id = jsonObject.getString("id");
                            FindDialog dialog = new FindDialog(FindIdActivity.this, name, id, () -> finish());
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(true);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                        }
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - findId");
                        if (!FindIdActivity.this.isFinishing())
                            Common.showDialog(FindIdActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - findId");
                        if (!FindIdActivity.this.isFinishing())
                            Common.showDialog(FindIdActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }

                } else {
                    if (!FindIdActivity.this.isFinishing())
                        Common.showDialog(FindIdActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!FindIdActivity.this.isFinishing())
                    Common.showDialog(FindIdActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    // '로그인 화면' 클릭
    View.OnClickListener loginClick = v -> {
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    };

    // '패스워드 찾기' 클릭
    View.OnClickListener findPwClick = v -> {
        Intent intent = new Intent(getApplicationContext(), FindPwActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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
        if (mBinding.editName.getText().toString().equals("") || mBinding.editEmail.getText().toString().equals("")) {
            mBinding.btnFindId.setEnabled(false);
            mBinding.btnFindId.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        } else {
            mBinding.btnFindId.setEnabled(true);
            mBinding.btnFindId.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
