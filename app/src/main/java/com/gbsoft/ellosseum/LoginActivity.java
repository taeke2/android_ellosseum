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

import com.gbsoft.ellosseum.databinding.ActivityLoginBinding;
import com.gbsoft.ellosseum.dto.SiteDTO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivityLog";

    private ActivityLoginBinding mBinding;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences mSharedPreferencesRent;
    private SharedPreferences.Editor mEditor;
    private int mRentId = -1;
    private long mBackKeyPressedTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initialSet() {
        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        mSharedPreferencesRent = getSharedPreferences("Rent", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mRentId = mSharedPreferencesRent.getInt("rentId", -1);
        mBinding.btnLogin.setOnClickListener(loginClick);
        mBinding.editId.addTextChangedListener(mTextWatcher);
        mBinding.editPw.addTextChangedListener(mTextWatcher);
        mBinding.txtJoin.setOnClickListener(joinClick);
        mBinding.txtFindId.setOnClickListener(findIdClick);
        mBinding.txtFindPw.setOnClickListener(findPwClick);

        mBinding.txtFindId.setOnTouchListener(findIdTouch);
        mBinding.txtFindPw.setOnTouchListener(findPwTouch);
        mBinding.txtJoin.setOnTouchListener(joinTouch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        editCheck();
    }

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

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener joinTouch = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP :
                mBinding.txtJoin.setTextColor(getColor(R.color.default_gray));
                break;
            case MotionEvent.ACTION_DOWN :
                mBinding.txtJoin.setTextColor(getColor(R.color.darker_red));
                break;
        }
        return false;
    };

    // 아이디가 근로자인지 관리자인지 확인
    private boolean adminCheck(String id) {
        boolean result = false;
        for (int i = 0; i < id.length(); i++) {
            if (48 > id.charAt(i) || id.charAt(i) > 57) {
                result = true;
                break;
            }
        }
        return result;
    }

    // 로그인 클릭
    View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String id = mBinding.editId.getText().toString();
            String pw = mBinding.editPw.getText().toString();
            if (id.equals("")) id = "null";

            boolean isAdmin = adminCheck(id);

            String url = mSharedPreferences.getString("siteLink", "");
            int siteCode = mSharedPreferences.getInt("site", -1);

//            if (url.equals("") || siteCode == -1) {   // 현장 정보가 없을 때
//                if (isAdmin) {
//                    adminLogin2(id, pw);
//                } else {
//                    Intent intent = new Intent(getApplicationContext(), SiteSelectActivity.class);
//                    intent.putExtra("type", "emp");
//                    startActivityResult.launch(intent);
//                }
//            } else {
//                if (isAdmin) adminLogin(id, pw);
//                else employeeLogin(id, pw);
//            }

            if (isAdmin) {
                adminLogin2(id, pw);
            } else {
                if (url.equals("") || siteCode == -1) {   // 현장 정보가 없을 때
                    Intent intent = new Intent(getApplicationContext(), SiteSelectActivity.class);
                    intent.putExtra("type", "emp");
                    startActivityResult.launch(intent);
                } else
                    employeeLogin(id, pw);
            }
        }
    };

    // 현장 선택 데이터 받기
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                String id = mBinding.editId.getText().toString();
                String pw = mBinding.editPw.getText().toString();
                if (id.equals("")) id = "null";

                Intent intent = result.getData();
                String type = intent.getExtras().getString("type");
                if (type.equals("emp")) {
                    employeeLogin(id, pw);
                } else if (type.equals("adm")) {
                    // adminLogin(id, pw);
                    Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
                    intentMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentMain);
                    finish();
                }
            }
        }
    });

    // 아이디 찾기 클릭
    View.OnClickListener findIdClick = v -> {
        Intent intent = new Intent(getApplicationContext(), FindIdActivity.class);
        startActivity(intent);
    };

    // 패스워드 찾기 클릭
    View.OnClickListener findPwClick = v -> {
        Intent intent = new Intent(getApplicationContext(), FindPwActivity.class);
        startActivity(intent);
    };

    // 회원가입 클릭
    View.OnClickListener joinClick = v -> {
        if (mRentId != -1) {
            Intent intent = new Intent(LoginActivity.this, TermsActivity.class);
            startActivity(intent);
        } else {
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    };

    // id EditText 체인지 리스너
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { editCheck(); }

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
        if (mBinding.editId.getText().toString().equals("") || mBinding.editPw.getText().toString().equals("")) {
            mBinding.btnLogin.setEnabled(false);
            mBinding.btnLogin.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        } else {
            mBinding.btnLogin.setEnabled(true);
            mBinding.btnLogin.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
        }
    }

    // 근로자 로그인
    private void employeeLogin(String id, String pw) {
        // mRentId = mSharedPreferencesRent.getInt("rentId", -1);
        Call<ResponseBody> call = null;
        if (mRentId != -1) {
            call = Common.sService_site.rentEmployeeLogin(id, pw, mRentId);
        } else {
            call = Common.sService_site.employeeLogin(id, pw, Common.EMP);
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();

                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "employee login result = " + result);
                        Common.sToken = result;
                        mEditor.putString("jwt", result);
                        mEditor.putString("userID", "emp@" + id);
                        mEditor.commit();
                        setLastLoginTime(id);
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - employeeLogin");
                        if (!LoginActivity.this.isFinishing())
                            Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if (code == 204) {
                    mBinding.txtError.setVisibility(View.VISIBLE);
                    mBinding.editPw.setText("");
                    mBinding.editPw.requestFocus();
                } else if (code == 401) {
                    if (!LoginActivity.this.isFinishing())
                        Common.showDialog(LoginActivity.this, getString(R.string.dialog_auth_error_title), getString(R.string.dialog_auth_error_content), () -> { });
                } else {
                    if (!LoginActivity.this.isFinishing())
                        Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!LoginActivity.this.isFinishing())
                    Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    // 근로자 로그인 시간 업데이트
    private void setLastLoginTime(String id) {
        Call<ResponseBody> call = Common.sService_site.setLastLoginTime_(id, Common.getCurrentTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")), Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                } else if (code == 401) {
                    if (!LoginActivity.this.isFinishing())
                        Common.appRestart(LoginActivity.this);
                } else {
                    if (!LoginActivity.this.isFinishing())
                        Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!LoginActivity.this.isFinishing())
                    Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

//    // 관리자 로그인
//    private void adminLogin(String id, String pw) {
//        Call<ResponseBody> call = Common.sService_master.adminLogin(id, pw);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                int code = response.code();
//                if (code == 200) {
//                    try {
//                        String result = response.body().string();
//                        Log.v(TAG, "adminLogin result = " + result);
//                        JSONObject jsonObject = new JSONObject(result);
//                        int auth = jsonObject.getInt("auth");
//                        mEditor.putString("userID", auth + "@" + id);
//                        String jwt = jsonObject.getString("token");
//                        Common.sToken = jwt;
//                        mEditor.putString("jwt", jwt);
//                        mEditor.commit();
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                        finish();
//                    } catch (JSONException e) {
//                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - adminLogin");
//                        Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
//                    } catch (IOException e) {
//                        Log.e(Common.TAG_ERR, "ERROR: IOException - adminLogin");
//                        Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
//                    }
//                } else if (code == 204) {
//                    mBinding.txtError.setVisibility(View.VISIBLE);
//                    mBinding.editPw.setText("");
//                    mBinding.editPw.requestFocus();
//                } else if (code == 401) {
//                    Common.appRestart(LoginActivity.this);
//                } else {
//                    Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
//            }
//        });
//    }

    // 관리자 로그인(site id return)
    private void adminLogin2(String id, String pw) {
        Call<ResponseBody> call = Common.sService_master.adminSignIn(id, pw);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "adminSignIn result = " + result);
                        JSONObject jsonObject = new JSONObject(result);
                        int auth = jsonObject.getInt("auth");
                        mEditor.putString("userID", auth + "@" + id);
                        String jwt = jsonObject.getString("token");
                        int siteId = jsonObject.getInt("siteId");
                        mEditor.putInt("siteId", siteId);
                        Common.sToken = jwt;
                        mEditor.putString("jwt", jwt);
                        mEditor.apply();
                        if (siteId < 0) {
                            Intent intent = new Intent(getApplicationContext(), SiteSelectActivity.class);
                            intent.putExtra("type", "adm");
                            startActivityResult.launch(intent);
                        } else {
                            int pos = 0;
                            for (SiteDTO dto : Common.sSiteDTOS) {
                                if (dto.getId() == siteId)
                                    break;
                                pos++;
                            }

                            int site = Common.sSiteDTOS.get(pos).getId();
                            String link = Common.sSiteDTOS.get(pos).getLink();
                            String name = Common.sSiteDTOS.get(pos).getName();

                            Log.v(TAG, "on item info site / link / name = " + site + " / " + link + " / " + name);

                            mEditor.putInt("site", site);
                            mEditor.putString("siteLink", link);
                            mEditor.apply();

                            Common.getService2(link);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        }
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - adminLogin2");
                        if (!LoginActivity.this.isFinishing())
                            Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - adminLogin2");
                        if (!LoginActivity.this.isFinishing())
                            Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if (code == 204) {
                    mBinding.txtError.setVisibility(View.VISIBLE);
                    mBinding.editPw.setText("");
                    mBinding.editPw.requestFocus();
                } else if (code == 401) {
                    if (!LoginActivity.this.isFinishing())
                        Common.showDialog(LoginActivity.this, getString(R.string.dialog_auth_error_title), getString(R.string.dialog_auth_error_content), () -> { });
                } else {
                    if (!LoginActivity.this.isFinishing())
                        Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!LoginActivity.this.isFinishing())
                    Common.showDialog(LoginActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mRentId == -1) {
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        } else {
            if (System.currentTimeMillis() - mBackKeyPressedTime < 1500) {
                finishAffinity();
                System.runFinalization();
                System.exit(0);
            }
            mBackKeyPressedTime = System.currentTimeMillis();
            Common.showToast(getApplicationContext(), getString(R.string.exit_app));
        }
    }
}

