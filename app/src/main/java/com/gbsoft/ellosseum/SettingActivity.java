package com.gbsoft.ellosseum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivitySettingBinding;
import com.gbsoft.ellosseum.dto.SiteDTO;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivityLog";
    private ActivitySettingBinding mBinding;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences mSharedPreferencesRent;
    private int mRentId = -1;
//    private final String[] mLanguage = {"한국어", "English"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialSet() {
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");

        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);

        mSharedPreferencesRent = getSharedPreferences("Rent", MODE_PRIVATE);
        mRentId = mSharedPreferencesRent.getInt("rentId", -1);

        mBinding.txtId.setText(Common.sUserId);
        mBinding.rentResetLayout.setVisibility(View.GONE);

        if (type.equals("auth")) {
            mBinding.layoutProfile.setVisibility(View.GONE);
            mBinding.layoutSite.setVisibility(View.GONE);
        } else {
            mBinding.layoutProfile.setVisibility(View.VISIBLE);
        }
        mBinding.btnBack.setOnClickListener(v -> onBackPressed());

        if (mRentId != -1) {
            editCheck();
            mBinding.txtRentReset.setVisibility(View.VISIBLE);
            mBinding.txtRentReset.setOnClickListener(rentResetClick);
            mBinding.editRentSerial.addTextChangedListener(mTextWatcher);
            mBinding.txtCancel.setOnClickListener(rentResetCancelClick);
            mBinding.txtOk.setOnClickListener(rentResetOkClick);
        } else
            mBinding.txtRentReset.setVisibility(View.GONE);

//        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, mLanguage);
//        mBinding.listViewLanguage.setAdapter(adapter);

        mBinding.txtLogout.setOnTouchListener(logoutTouch);
    }


    private View.OnTouchListener logoutTouch = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP :
                    mBinding.txtLogout.setTextColor(getColor(R.color.gray));
                    break;
                case MotionEvent.ACTION_DOWN :
                    mBinding.txtLogout.setTextColor(getColor(R.color.darker_red));
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        mBinding.layoutSite.setOnClickListener(siteClick);
//        mBinding.layoutLanguage.setOnClickListener(languageClick);
//        mBinding.listViewLanguage.setOnItemClickListener(itemClick);
        // 로그아웃
        mBinding.txtLogout.setOnClickListener(logoutClick);
        this.setSite();
    }

    private void setSite() {
        int siteCode = mSharedPreferences.getInt("site", -1);
        if (siteCode != -1) {
            for (SiteDTO dto : Common.sSiteDTOS) {
                if (dto.getId() == siteCode) {
                    mBinding.txtSelectedSite.setText(dto.getName());
                    break;
                }
            }
        }
    }

    // 현장 선택 데이터 받기
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                String name = intent.getExtras().getString("name");
                mBinding.txtSelectedSite.setText(name);
            }
        }
    });

    View.OnClickListener siteClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Common.sAuthority <= 3) {
                Intent intent = new Intent(getApplicationContext(), SiteSelectActivity.class);
                startActivityResult.launch(intent);
            }
        }
    };

    // 로그아웃 버튼 클릭
    View.OnClickListener logoutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Common.sAuthority == Common.EMP) {  // 로그인 유저가 근로자일 경우
                getEmployeeName();
            } else {
                getAdminName();
            }
        }
    };

    // 대여폰 초기화 클릭
    View.OnClickListener rentResetClick = v -> {
        if (mBinding.rentResetLayout.getVisibility() == View.GONE) {
            mBinding.rentResetLayout.setVisibility(View.VISIBLE);
            mBinding.txtPwError.setVisibility(View.GONE);
        } else
            mBinding.rentResetLayout.setVisibility(View.GONE);
        mBinding.txtRentReset.setVisibility(View.GONE);
    };

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
        if (mBinding.editRentSerial.getText().toString().length() < 6) {
            mBinding.txtOk.setEnabled(false);
            mBinding.txtOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        } else {
            mBinding.txtOk.setEnabled(true);
            mBinding.txtOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
        }
    }

    View.OnClickListener rentResetCancelClick = v -> {
        mBinding.editRentSerial.setText("");
        mBinding.rentResetLayout.setVisibility(View.GONE);
        mBinding.txtRentReset.setVisibility(View.VISIBLE);
        InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mBinding.editRentSerial.getWindowToken(), 0);
    };

    View.OnClickListener rentResetOkClick = v -> {
        Call<ResponseBody> call = Common.sService_site.serialNumberCheck(mBinding.editRentSerial.getText().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        JSONObject jsonObject = new JSONObject(result);
                        if (mRentId == jsonObject.getInt("id")) {
                            SharedPreferences.Editor rentEditor = mSharedPreferencesRent.edit();
                            rentEditor.clear();
                            rentEditor.commit();
                            if (Common.sAuthority < Common.EMP) logout();
                            else    setRentPhoneUsingN();
                        } else {
                            mBinding.txtPwError.setVisibility(View.VISIBLE);
                            mBinding.editRentSerial.setText("");
                        }
                    } catch (IOException e) {
                        if (!SettingActivity.this.isFinishing())
                            Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (JSONException e) {
                        if (!SettingActivity.this.isFinishing())
                            Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if (code == 204) {
                    mBinding.txtPwError.setVisibility(View.VISIBLE);
                    mBinding.editRentSerial.setText("");
                } else {
                    if (!SettingActivity.this.isFinishing())
                        Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!SettingActivity.this.isFinishing())
                    Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    };

    private void showLogoutDialog(String name) {
        LogoutBottomSheetDialog dialog = new LogoutBottomSheetDialog(mLogoutDialogClickListener, name);
        dialog.show(getSupportFragmentManager(), dialog.getTag());

//        LogoutDialog dialog = new LogoutDialog(SettingActivity.this, mLogoutDialogClickListener, name);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCanceledOnTouchOutside(true); // 다이얼로그 밖에 터치 시 종료
//        dialog.setCancelable(true); // 다이얼로그 취소 가능 (back key)
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 다이얼로그 background 적용하기 위한 코드 (radius 적용)
//        dialog.show();
    }

    // 관리자 이름 조회
    private void getAdminName() {
        Call<ResponseBody> call = Common.sService_master.getAdminName_(Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getAdminName result = " + result);
                        JSONObject jsonObject = new JSONObject(result);
                        if (!SettingActivity.this.isFinishing())
                            showLogoutDialog(jsonObject.getString("name") + " ");
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getAdminName");
                        if (!SettingActivity.this.isFinishing())
                            Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getAdminName");
                        if (!SettingActivity.this.isFinishing())
                            Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if (code == 401) {
                    if (!SettingActivity.this.isFinishing())
                        Common.appRestart(SettingActivity.this);
                } else {
                    if (!SettingActivity.this.isFinishing())
                        Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!SettingActivity.this.isFinishing())
                    Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    // 근로자 이름 조회
    private void getEmployeeName() {
        Call<ResponseBody> call = Common.sService_site.getEmployeeName_(Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getEmployeeName result = " + result);
                        JSONObject jsonObject = new JSONObject(result);
                        if (!SettingActivity.this.isFinishing())
                            showLogoutDialog(jsonObject.getString("name") + " ");
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getEmployeeName");
                        if (!SettingActivity.this.isFinishing())
                            Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getEmployeeName");
                        if (!SettingActivity.this.isFinishing())
                            Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if (code == 401) {
                    if (!SettingActivity.this.isFinishing())
                        Common.appRestart(SettingActivity.this);
                } else {
                    if (!SettingActivity.this.isFinishing())
                        Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!SettingActivity.this.isFinishing())
                    Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    // 로그아웃 다이얼로그 버튼클릭 리스너 설정
    LogoutDialogClickListener mLogoutDialogClickListener = new LogoutDialogClickListener() {
        @Override
        public void onPositiveClick() {
            if (mRentId != -1) {
                if (Common.sAuthority < Common.EMP) logout();
                else    setRentPhoneUsingN();
            } else
                logout();
        }

        @Override
        public void onNegativeClick() {

        }
    };

    public void setRentPhoneUsingN() {
        Call<ResponseBody> call = Common.sService_site.rentPhoneLogout(mRentId, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200)
                    logout();
                else if (code == 401) {
                    if (!SettingActivity.this.isFinishing())
                        Common.appRestart(SettingActivity.this);
                } else {
                    if (!SettingActivity.this.isFinishing())
                        Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!SettingActivity.this.isFinishing())
                    Common.showDialog(SettingActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    // 로그아웃
    public void logout() {
        FirebaseMessaging.getInstance().deleteToken();
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        int siteCode = mSharedPreferences.getInt("site", -1);
        String siteLink = mSharedPreferences.getString("siteLink", "");
        editor.clear();
        editor.commit();
        editor.putInt("site", siteCode);
        editor.putString("siteLink", siteLink);
        editor.commit();
        Common.getService2(siteLink);
        Common.stopLocationService(getApplicationContext(), Common.isLocationServiceRunning(SettingActivity.this));
        Intent intent = new Intent(SettingActivity.this, AuthorizationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /*    View.OnClickListener languageClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBinding.listViewLanguage.setVisibility(View.VISIBLE);
        }
    };

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO : 언어 설정
            mBinding.txtSelectedLanguage.setText(mLanguage[position]);
            mBinding.listViewLanguage.setVisibility(View.GONE);
        }
    };*/
}
