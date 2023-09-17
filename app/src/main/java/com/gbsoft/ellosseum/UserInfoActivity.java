package com.gbsoft.ellosseum;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivityEmployeeInfoBinding;
import com.gbsoft.ellosseum.dto.SubcontractorDTO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends AppCompatActivity {
    private static final String TAG = "UserInfoActivityLog";

    private ActivityEmployeeInfoBinding mBinding;

    private int mId;
    private int mType;
    private String mName;
    private String mBirth;
    private int mBlood;
    private String mTel;
    private String mAddress;
    private int mSubcontractor;
    private int mOccupation;
    private int mState;
    private String mCreateDate;
    private String mRemark;
    private String mVaccineYn;
    private String mEquipmentName;
    private int mEquipmentType;
    private String mEquipmentUse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityEmployeeInfoBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    public void initialSet() {
        Intent intent = getIntent();
        mId = intent.getIntExtra("empNum", -1);
        Log.v(TAG, "mId = " + mId);

        mBinding.layoutEquipmentName.setVisibility(View.GONE);
        mBinding.layoutEquipmentType.setVisibility(View.GONE);
        mBinding.layoutEquipmentUse.setVisibility(View.GONE);
        mBinding.btnFinish.setOnClickListener(accessClick);
        mBinding.btnCancel.setOnClickListener(cancelClick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getEmployeeInfo();
    }

    // 정보 입력
    private void employeeInfoSetText() {
        mBinding.txtEmpCode.setText(String.valueOf(mId));
        mBinding.txtVaccine.setText(mVaccineYn);
        mBinding.txtName.setText(mName);
        mBinding.txtBirth.setText(mBirth);
        mBinding.txtBloodType.setText(Common.sBloodTypes.get(mBlood));

        mBinding.txtTel.setText(mTel);
        String[] address_arr = mAddress.split(",", -1);
        mBinding.txtAddress1.setText(address_arr[0]);
        mBinding.txtAddress2.setText(address_arr[1]);
        mBinding.txtAddress3.setText(address_arr[2]);
        mBinding.txtSubcontractor.setText(findSubcontractor());
        mBinding.txtOccupation.setText(Common.sOccupations.get(mOccupation));

        if (isEquipment()) {
            mBinding.textEquipmentName.setText(mEquipmentName);
            mBinding.textEquipmentType.setText(Common.sEquipmentTypes.get(mEquipmentType));
            mBinding.textEquipmentUse.setText(mEquipmentUse);
        }

        mBinding.txtRemark.setText(mRemark.equals("") ? "-" : mRemark);
    }

    // 장비정보 가져오기
    private boolean isEquipment() {
        boolean isEquip = !mEquipmentName.equals("") && !mEquipmentUse.equals("") && !(mEquipmentType == -1) && !mEquipmentName.equals("없음") && !mEquipmentUse.equals("없음") && !(mEquipmentType == -1);
        if (isEquip) {
            // 사원(장비) 정보 로 변경
            mBinding.txtTitle.setText(R.string.employee_equipment_info);
            // 글자색 변경
//            mBinding.txtTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
            // view 배경색 변경
            mBinding.constraintlayout.setBackgroundResource(R.color.light_gray);
            mBinding.srollview.setBackground(ContextCompat.getDrawable(this, R.drawable.scrollview_shape));

            mBinding.layoutEquipmentName.setVisibility(View.VISIBLE);
            mBinding.layoutEquipmentType.setVisibility(View.VISIBLE);
            mBinding.layoutEquipmentUse.setVisibility(View.VISIBLE);
        }
        return isEquip;
    }

    // 사원정보 조회
    private void getEmployeeInfo() {
        Call<ResponseBody> call = Common.sService_site.getEmployeeInfo(mId, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getEmployeeInfo result = " + result);

                        JSONObject jsonObject = new JSONObject(result);

                        mType = jsonObject.getInt("empType");

                        mName = jsonObject.getString("empName");
                        mBirth = jsonObject.getString("empBirth");

                        mBlood = jsonObject.getInt("blood");

                        mTel = jsonObject.getString("empTel");
                        mAddress = jsonObject.getString("address");

                        mSubcontractor = jsonObject.getInt("subNum");
                        mOccupation = jsonObject.getInt("empOcc");
                        mState = jsonObject.getInt("empState");

                        mRemark = jsonObject.getString("remark");
                        mVaccineYn = jsonObject.getString("vac");

                        mEquipmentName = jsonObject.getString("employee_name");
                        mEquipmentUse = jsonObject.getString("employee_use");
                        mEquipmentType = jsonObject.getInt("employee_type");

                        employeeInfoSetText();
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getEmployeeInfo");
                        if (!UserInfoActivity.this.isFinishing())
                            Common.showDialog(UserInfoActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getEmployeeInfo");
                        if (!UserInfoActivity.this.isFinishing())
                            Common.showDialog(UserInfoActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else if (code == 401) {
                    if (!UserInfoActivity.this.isFinishing())
                        Common.appRestart(UserInfoActivity.this);
                } else {
                    if (!UserInfoActivity.this.isFinishing())
                        Common.showDialog(UserInfoActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!UserInfoActivity.this.isFinishing())
                    Common.showDialog(UserInfoActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
            }
        });
    }

    // 승인
    View.OnClickListener accessClick = v -> employeeAccess();
    // 취소
    View.OnClickListener cancelClick = v -> onBackPressed();

    private String findSubcontractor() {
        String result = "";
        for (SubcontractorDTO dto : Common.sSubcontractorDTOS) {
            if (dto.getId() == mSubcontractor) {
                result = dto.getName();
                break;
            }
        }
        return result;
    }

    // 가입 승인
    private void employeeAccess() {
        Call<ResponseBody> call = Common.sService_site.setEmployeeAccess(mId, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    finish();
                } else if (code == 401) {
                    if (!UserInfoActivity.this.isFinishing())
                        Common.appRestart(UserInfoActivity.this);
                } else {
                    if (!UserInfoActivity.this.isFinishing())
                        Common.showDialog(UserInfoActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!UserInfoActivity.this.isFinishing())
                    Common.showDialog(UserInfoActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ScanQRCodeActivity.class);
        startActivity(intent);
        finish();
    }
}
