package com.gbsoft.ellosseum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gbsoft.ellosseum.databinding.ActivityTermsBinding;

public class TermsActivity extends AppCompatActivity {

    private ActivityTermsBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTermsBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        initialSet();
    }

    private void initialSet() {
        mBinding.btnOk.setOnClickListener(nextClick);
        mBinding.btnCancel.setOnClickListener(cancelClick);
        mBinding.chkTermsAllAgree.setOnClickListener(allAgreeClick);
        mBinding.chkTerms1Agree.setOnClickListener(agree1Click);
        mBinding.chkTerms2Agree.setOnClickListener(agree2Click);
        mBinding.chkTerms3Agree.setOnClickListener(agree3Click);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Context context = TermsActivity.this;
//        if (Common.sSubcontractorDTOS.size() <= 0) Common.getSubcontractorList(context);   // 파트너사 리스트 저장
//        if (Common.sStates.size() <= 0) Common.getCommonData(1, context);    // 상태 리스트 저장
//        if (Common.sOccupations.size() <= 0) Common.getCommonData(2, context);   // 직종 리스트 저장
//        if (Common.sEquipmentTypes.size() <= 0) Common.getCommonData(4, context);   // 장비 종류 리스트 저장
//        if (Common.sBloodTypes.size() <= 0) Common.getCommonData(5, context);    // 혈액형 리스트 저장
        // 파트너사 리스트가 변동될 수 있으니...
        Common.getSubcontractorList(context);   // 파트너사 리스트 저장
        Common.getCommonData(1, context);    // 상태 리스트 저장
        Common.getCommonData(2, context);   // 직종 리스트 저장
        Common.getCommonData(4, context);   // 장비 종류 리스트 저장
        Common.getCommonData(5, context);    // 혈액형 리스트 저장

        // 장비 이름 리스트 저장(추가)

        if (mBinding.chkTermsAllAgree.isChecked()) {
            mBinding.btnOk.setEnabled(true);
            mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
        } else {
            mBinding.btnOk.setEnabled(false);
            mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
        }
    }

    // 전체동의
    View.OnClickListener allAgreeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBinding.chkTermsAllAgree.isChecked()) {
                mBinding.chkTerms1Agree.setChecked(true);
                mBinding.chkTerms2Agree.setChecked(true);
                mBinding.chkTerms3Agree.setChecked(true);
                mBinding.btnOk.setEnabled(true);
                mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
            } else {
                mBinding.chkTerms1Agree.setChecked(false);
                mBinding.chkTerms2Agree.setChecked(false);
                mBinding.chkTerms3Agree.setChecked(false);
                mBinding.btnOk.setEnabled(false);
                mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
            }
        }
    };

    View.OnClickListener agree1Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBinding.chkTerms2Agree.isChecked() && mBinding.chkTerms3Agree.isChecked()) {
                if (mBinding.chkTerms1Agree.isChecked()) {
                    mBinding.chkTermsAllAgree.setChecked(true);
                    mBinding.btnOk.setEnabled(true);
                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                } else {
                    mBinding.chkTermsAllAgree.setChecked(false);
                    mBinding.btnOk.setEnabled(false);
                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
                }
            }
        }
    };

    View.OnClickListener agree2Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBinding.chkTerms1Agree.isChecked() && mBinding.chkTerms3Agree.isChecked()) {
                if (mBinding.chkTerms2Agree.isChecked()) {
                    mBinding.chkTermsAllAgree.setChecked(true);
                    mBinding.btnOk.setEnabled(true);
                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                } else {
                    mBinding.chkTermsAllAgree.setChecked(false);
                    mBinding.btnOk.setEnabled(false);
                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
                }
            }
        }
    };

    View.OnClickListener agree3Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBinding.chkTerms1Agree.isChecked() && mBinding.chkTerms2Agree.isChecked()) {
                if (mBinding.chkTerms3Agree.isChecked()) {
                    mBinding.chkTermsAllAgree.setChecked(true);
                    mBinding.btnOk.setEnabled(true);
                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_enable));
                } else {
                    mBinding.chkTermsAllAgree.setChecked(false);
                    mBinding.btnOk.setEnabled(false);
                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_access_disable));
                }
            }
        }
    };

    // '확인' 클릭
    View.OnClickListener nextClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Common.sSubcontractorDTOS.size() <= 0 || Common.sStates.size() <= 0 || Common.sOccupations.size() <= 0 || Common.sBloodTypes.size() <= 0 || Common.sEquipmentTypes.size() <= 0) {
                // Common.showToast(getApplicationContext(), "Server Error");
                Common.showDialog(TermsActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
            } else {
                Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        }
    };

    // '취소' 클릭
    View.OnClickListener cancelClick = v -> finish();

    @Override
    public void onBackPressed() {
        finish();
    }
}
