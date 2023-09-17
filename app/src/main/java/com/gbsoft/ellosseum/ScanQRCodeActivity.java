package com.gbsoft.ellosseum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRCodeActivity extends AppCompatActivity {
    private static final String TAG = "ScanQRCodeActivityLog";
    private IntentIntegrator mIntentIntegrator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    public void initialSet() {
        mIntentIntegrator = new IntentIntegrator(this);
        mIntentIntegrator.setOrientationLocked(false);
        mIntentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                onBackPressed();
            } else {
                if (Common.sSubcontractorDTOS.size() <= 0 || Common.sStates.size() <= 0 || Common.sOccupations.size() <= 0 || Common.sBloodTypes.size() <= 0 || Common.sEquipmentTypes.size() <= 0) {
                    Common.showDialog(ScanQRCodeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                } else {
                    int empNum = Integer.parseInt(result.getContents());
                    Log.v(TAG, "scan employee code = " + empNum);
                    Intent intent = new Intent(ScanQRCodeActivity.this, UserInfoActivity.class);
                    intent.putExtra("empNum", empNum);
                    startActivity(intent);
                    finish();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Context context = ScanQRCodeActivity.this;
//        if (Common.sSubcontractorDTOS.size() <= 0) Common.getSubcontractorList(context);    // 파트너사 리스트 저장
//        if (Common.sStates.size() <= 0) Common.getCommonData(1, context);    // 상태 리스트 저장
//        if (Common.sOccupations.size() <= 0) Common.getCommonData(2, context);   // 직종 리스트 저장
//        if (Common.sEquipmentTypes.size() <= 0) Common.getCommonData(4, context);   // 장비 종류 리스트 저장
//        if (Common.sBloodTypes.size() <= 0) Common.getCommonData(5, context);    // 혈액형 리스트 저장

        // 파트너사 변경될 수 있으니..
        Common.getSubcontractorList(context);    // 파트너사 리스트 저장
        Common.getCommonData(1, context);    // 상태 리스트 저장
        Common.getCommonData(2, context);   // 직종 리스트 저장
        Common.getCommonData(4, context);   // 장비 종류 리스트 저장
        Common.getCommonData(5, context);    // 혈액형 리스트 저장
    }
}