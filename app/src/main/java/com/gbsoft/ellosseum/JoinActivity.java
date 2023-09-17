package com.gbsoft.ellosseum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivityJoinBinding;
import com.gbsoft.ellosseum.dto.SubcontractorDTO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinActivity extends AppCompatActivity {
    private static final String TAG = "JoinActivityLog";

    private SharedPreferences mSharedPreferences;
    private SharedPreferences mSharedPreferencesRent;
    private SharedPreferences.Editor mEditor;

    private ActivityJoinBinding mBinding;
    private ArrayList<String> mSubcontractorNames;
    private InputMethodManager mInputMethodManager;
    private int mType = -1;
    private int mRentId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityJoinBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    public void initialSet() {
        // 장비 정보 입력창 숨기기
        mBinding.layoutEquipmentName.setVisibility(View.GONE);
        mBinding.layoutEquipmentType.setVisibility(View.GONE);
        mBinding.layoutEquipmentUse.setVisibility(View.GONE);

        // 생년월일 완료 버튼 숨기기
        mBinding.btnFinish.setVisibility(View.GONE);
        mBinding.datePicker.setVisibility(View.GONE);
        // ArrayList 초기화
        mSubcontractorNames = new ArrayList<>();

        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        mSharedPreferencesRent = getSharedPreferences("Rent", MODE_PRIVATE);

        this.setSpinnerList();
        this.setInitData();
    }

    // TODO: 공통 코드 DB 생성 시 각 리스트별로 Spinner 어댑터 생성하기
    private void setSpinnerList() {
/*        // 사원 유형 리스트
        ArrayAdapter<CharSequence> adapter_empType = ArrayAdapter.createFromResource(getContext(), R.array.empType_array, R.layout.support_simple_spinner_dropdown_item);
        mBinding.spinnerEmpType.setAdapter(adapter_empType);*/
        // 연락처 앞자리 리스트
        ArrayAdapter<CharSequence> adapter_tel = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, Common.sTels);
        mBinding.spinnerTel1.setAdapter(adapter_tel);

//        data = new ArrayList<HashMap<String, Integer>>();
//        data.add(Common.sEquipmentTypes);
//
//        String[] mEquipmentKeys = Common.sEquipmentTypes.keySet().toArray(new String[Common.sEquipmentTypes.size()]);
        String[] mEquipmentValues = Common.sEquipmentTypes.values().toArray(new String[Common.sEquipmentTypes.size()]);

        // 장비 종류 리스트
        ArrayAdapter<String> adapter_equipmentTypes = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, mEquipmentValues);
        mBinding.spinnerEquipmentType.setAdapter(adapter_equipmentTypes);
        // 혈액형 리스트

//        String[] mBloodKeys = Common.sBloodTypes.keySet().toArray(new String[Common.sBloodTypes.size()]);
        String[] mBloodValues = Common.sBloodTypes.values().toArray(new String[Common.sBloodTypes.size()]);

        ArrayAdapter<String> adapter_blood = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, mBloodValues);
        mBinding.spinnerBloodType.setAdapter(adapter_blood);
        // 파트너사 리스트 조회
        for (SubcontractorDTO dto : Common.sSubcontractorDTOS)
            mSubcontractorNames.add(dto.getName());
        ArrayAdapter<String> adapter_subcontractor = new ArrayAdapter(getApplicationContext(), R.layout.item_spinner, mSubcontractorNames);
        mBinding.spinnerAgency.setAdapter(adapter_subcontractor);

//        String[] mOuccpationKeys = Common.sOccupations.keySet().toArray(new String[Common.sOccupations.size()]);
        String[] mOuccpationValues = Common.sOccupations.values().toArray(new String[Common.sOccupations.size()]);

        // 직종 리스트
        ArrayAdapter<String> adapter_occupation = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, mOuccpationValues);
        mBinding.spinnerOccupation.setAdapter(adapter_occupation);
//        // 상태 리스트
//        ArrayAdapter<CharSequence> adapter_state = ArrayAdapter.createFromResource(getContext(), R.array.state_array, R.layout.support_simple_spinner_dropdown_item);
//        mBinding.spinnerState.setAdapter(adapter_state);
    }

    // 입력란 초기화
    private void setInitData() {
        mBinding.editEquipmentName.setText(""); // 장비명
        mBinding.spinnerEquipmentType.setSelection(0); // 장비종류
        mBinding.editEquipmentUse.setText(""); // 장비용도
        mBinding.editName.setText("");  // 이름
        mBinding.txtBirth.setText("");  // 생년월일
        mBinding.spinnerBloodType.setSelection(0);  // 혈액형
        mBinding.spinnerTel1.setSelection(0);   // 연락처1
        mBinding.editTel2.setText("");  // 연락처2
        mBinding.editTel3.setText("");  // 연락처3
        mBinding.editZipCode.setText("");   // 우편번호
        mBinding.editAddress1.setText("");  // 주소검색
        mBinding.editAddress2.setText("");  // 상세주소
        mBinding.spinnerAgency.setSelection(0); // 소속사
        mBinding.spinnerOccupation.setSelection(0); // 직종
        mBinding.editUniqueness.setText("");    // 특이사항
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBinding.chkEquipment.setOnClickListener(chbEquipment); // 현장 장비 보유 체크
//        mBinding.editName.setFilters(new InputFilter[]{inputFilter});
        mBinding.btnFinish.setOnClickListener(finishClick); // 생년월일 완료 버튼
        mBinding.btnSearchZipCode.setOnClickListener(addressWebView);   // 주소 찾기 버튼
        mBinding.txtBirth.setOnClickListener(birthClick);   // 생년월일 텍스트뷰
        mBinding.btnJoin.setOnClickListener(joinClick); // 회원가입 버튼
    }

    View.OnClickListener chbEquipment = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked(); // 체크가 되어있는지 아닌지 판단

            // 어떤 박스가 체크되었는지 확인
            if (view.getId() == R.id.chk_equipment) {
                if (checked) {
                    mBinding.layoutEquipmentName.setVisibility(View.VISIBLE);
                    mBinding.layoutEquipmentType.setVisibility(View.VISIBLE);
                    mBinding.layoutEquipmentUse.setVisibility(View.VISIBLE);
                } else {
                    mBinding.layoutEquipmentName.setVisibility(View.GONE);
                    mBinding.layoutEquipmentType.setVisibility(View.GONE);
                    mBinding.layoutEquipmentUse.setVisibility(View.GONE);
                }
            }
        }
    };

    // 이름 한글, 영어만 입력 (천지인적용) 테스트땐 사용 안하기
//    InputFilter inputFilter = new InputFilter() {
//        @Override
//        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
//            Pattern ps = Pattern.compile("^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]*$");
//            if (!ps.matcher(charSequence).matches()) {
//                return "";
//            }
//            return null;
//        }
//    };

    // 주소 찾기 버튼 클릭
    View.OnClickListener addressWebView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), AddressWebView.class);
            startActivityResult.launch(intent);
        }
    };
    // 주소 webView 데이터 가져오기
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        String data = intent.getExtras().getString("data");

                        if (data != null) {
                            String[] address = data.split(",", -1);
                            mBinding.editZipCode.setText(address[0]);
                            mBinding.editAddress1.setText(address[1]);
                        }
                        mBinding.editAddress2.requestFocus();
                    }
                }
            }
    );
    // 생년월일 텍스트뷰 클릭
    View.OnClickListener birthClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 키보드 내리기
            mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(mBinding.editName.getWindowToken(), 0);
            // 완료버튼, 데이트피커 보이기
            mBinding.btnFinish.setVisibility(View.VISIBLE);
            mBinding.datePicker.setVisibility(View.VISIBLE);
            // 생년월일 초기화 (공백 시 현재로부터 20년전)
            String birth = mBinding.txtBirth.getText().toString();
            boolean birthIsNull = birth.equals("");
            birth = birthIsNull ? Common.getCurrentTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())) : birth;
            int year = birthIsNull ? Integer.parseInt(birth.substring(0, 4)) - 20 : Integer.parseInt(birth.substring(0, 4));
            int month = Integer.parseInt(birth.substring(5, 7));
            int day = Integer.parseInt(birth.substring(8, 10));
            mBinding.txtBirth.setText(year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day));
            mBinding.datePicker.init(year, month - 1, day, changedDate);
        }
    };
    // 생년월일 완료 클릭
    View.OnClickListener finishClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBinding.datePicker.setVisibility(View.GONE);
            mBinding.btnFinish.setVisibility(View.GONE);
        }
    };
    // DatePicker change listener
    private DatePicker.OnDateChangedListener changedDate = new DatePicker.OnDateChangedListener() {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            @SuppressLint("DefaultLocale") String date = year + "-" + String.format("%02d", monthOfYear + 1) + "-" + String.format("%02d", dayOfMonth);
            mBinding.txtBirth.setText(date);
        }
    };
    // 회원가입 클릭
    View.OnClickListener joinClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            employeeDataCheck();
        }
    };

    private void employeeDataCheck() {
        // 장비 보유 선택시
        if (mBinding.chkEquipment.isChecked()) {
            if (mBinding.editEquipmentName.getText().toString().equals("")) {
                Common.showToast(getApplicationContext(), getString(R.string.enter_equipment_name));
                mBinding.editEquipmentUse.requestFocus();
            } else if (mBinding.editEquipmentUse.getText().toString().equals("")) {
                Common.showToast(getApplicationContext(), getString(R.string.enter_equipment_use));
                mBinding.editEquipmentUse.requestFocus();
            } else {
                mType = 1;
                nextDataCheck();
            }
        } else {
            mType = 0;
            nextDataCheck();
        }
    }

    public void nextDataCheck(){
        if (mBinding.editName.getText().toString().equals("")) {
            Common.showToast(getApplicationContext(), getString(R.string.enter_name));
            mBinding.editName.requestFocus();
        } else if (mBinding.txtBirth.getText().toString().equals("")) {
            Common.showToast(getApplicationContext(), getString(R.string.enter_birthday));
            mBinding.txtBirth.requestFocus();
        } else if (mBinding.editTel2.getText().toString().equals("") || mBinding.editTel3.getText().toString().equals("")) {
            Common.showToast(getApplicationContext(), getString(R.string.enter_tel));
            mBinding.editTel2.requestFocus();
//            } else if (mBinding.spinnerBloodType.getSelectedItemPosition() == 0) {
//                Common.showToast(getContext(), "혈액형을 선택하세요.");
//                mBinding.spinnerBloodType.requestFocus();
//            } else if (mBinding.editZipCode.getText().toString().equals("") || mBinding.editAddress1.getText().toString().equals("")) {
//                Common.showToast(getContext(), "주소를 입력하세요.");
//                mBinding.editZipCode.requestFocus();
//            } else if (mBinding.editAddress2.getText().toString().equals("")) {
//                Common.showToast(getContext(), "상세주소를 입력하세요.");
//                mBinding.editAddress2.requestFocus();
//            } else if (mBinding.spinnerAgency.getSelectedItemPosition() == 0) {
//                Common.showToast(getContext(), "소속 회사를 선택하세요.");
//                mBinding.spinnerAgency.requestFocus();
//            } else if (mBinding.editOccupation.getText().toString().equals("")) {
//                Common.showToast(getContext(), "직종을 입력하세요.");
//                mBinding.editOccupation.requestFocus();
        } else {
            mRentId = mSharedPreferencesRent.getInt("rentId", -1);
            if (mRentId != -1) {
                checkRentEmployeeName(mRentId);
            } else {
                employeeJoin(); // 근로자 승인 관리에 추가
            }
        }
    }

    private void checkRentEmployeeName(int id) {
        String name = mBinding.editName.getText().toString();

        Call<ResponseBody> call = Common.sService_site.checkRentEmployeeName(id, name);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    employeeJoin();
                } else if (code == 204) {
                    if (!JoinActivity.this.isFinishing())
                        Common.showDialog(JoinActivity.this, getString(R.string.dialog_join_fail), getString(R.string.dialog_join_fail_content), () -> { });
                } else {
                    if (!JoinActivity.this.isFinishing())
                        Common.showDialog(JoinActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!JoinActivity.this.isFinishing())
                    Common.showDialog(JoinActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    // 근로자 승인 관리에 저장
    public void employeeJoin() {
        mBinding.btnJoin.setEnabled(false);
        mBinding.btnJoin.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));

//        int userType = mBinding.spinnerEmpType.getSelectedItemPosition();
        String userName = mBinding.editName.getText().toString();
        String userBirth = mBinding.txtBirth.getText().toString();
        int bloodType = mBinding.spinnerBloodType.getSelectedItemPosition() + 1;


        String userTel = mBinding.spinnerTel1.getSelectedItem().toString() + "-" + mBinding.editTel2.getText().toString() + "-" + mBinding.editTel3.getText().toString();
        String address = mBinding.editZipCode.getText().toString() + "," + mBinding.editAddress1.getText().toString() + "," + mBinding.editAddress2.getText().toString();

        int subcontractor = Common.sSubcontractorDTOS.get(mBinding.spinnerAgency.getSelectedItemPosition()).getId();

        int occupation = mBinding.spinnerOccupation.getSelectedItemPosition() + 1;

//        int state = mBinding.spinnerState.getSelectedItemPosition();
        int state = 1;
        String currentTime = Common.getCurrentTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));
        String remark = mBinding.editUniqueness.getText().toString();
        String vaccineYn = mBinding.chkVaccinated.isChecked() ? "Y" : "N";
        String equipmentName = mBinding.chkEquipment.isChecked() ? mBinding.editEquipmentName.getText().toString() : "";

        int equipmentType = mBinding.chkEquipment.isChecked() ? mBinding.spinnerEquipmentType.getSelectedItemPosition() + 1 : -1;

        String equipmentUse = mBinding.chkEquipment.isChecked() ? mBinding.editEquipmentUse.getText().toString() : "";

        Call<ResponseBody> call;
        int siteCode = mSharedPreferences.getInt("site", -1);
        int joinCode = mSharedPreferences.getInt("joinCode", -1);
        Log.v(TAG, "employeeJoin siteCode / joinCode = " + siteCode + " / " + joinCode);

        if (joinCode == -1) {
            Log.v(TAG, "employeeJoin()");
            call = Common.sService_site.employeeJoin_(mType, userName, userBirth, bloodType, userTel, address, subcontractor, occupation, state, remark, currentTime, vaccineYn, equipmentName, equipmentType, equipmentUse, siteCode);
        } else {
            Log.v(TAG, "employeeUpdate()");
            call = Common.sService_site.employeeUpdate_(joinCode, mType, userName, userBirth, bloodType, userTel, address, subcontractor, occupation, state, remark, currentTime, vaccineYn, equipmentName, equipmentType, equipmentUse);
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "employeeJoin result = " + result);
                        JSONObject jsonObject = new JSONObject(result);

                        int joinCode = jsonObject.getInt("insertId");
                        mEditor.putInt("joinCode", joinCode);
                        mEditor.commit();

                        Intent intent = new Intent(getApplicationContext(), CreateQRCodeActivity.class);
                        intent.putExtra("rentId", mRentId);
                        startActivity(intent);
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - employeeJoin");
                        if (!JoinActivity.this.isFinishing())
                            Common.showDialog(JoinActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - employeeJoin");
                        if (!JoinActivity.this.isFinishing())
                            Common.showDialog(JoinActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                    finally {
                        mBinding.btnJoin.setEnabled(true);
                        mBinding.btnJoin.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                    }
                } else {
//                    setInitData();
                    mBinding.editName.requestFocus();
                    if (!JoinActivity.this.isFinishing())
                        Common.showDialog(JoinActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> {
                            mBinding.btnJoin.setEnabled(true);
                            mBinding.btnJoin.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                        });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!JoinActivity.this.isFinishing())
                    Common.showDialog(JoinActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> {
                        mBinding.btnJoin.setEnabled(true);
                        mBinding.btnJoin.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                    });
            }
        });
    }
}
