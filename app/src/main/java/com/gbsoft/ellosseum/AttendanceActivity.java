package com.gbsoft.ellosseum;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gbsoft.ellosseum.databinding.ActivityAttendanceBinding;
import com.gbsoft.ellosseum.dto.EmployeeAttendanceDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity extends AppCompatActivity {
    private final String TAG = "AttendanceActivityLog";

    private ActivityAttendanceBinding mBinding;

    private SharedPreferences mSharedPreferences;
    private String mUserId;

    private AttendanceAdapter mAttendanceAdapter;

    private DatePickerDialog mStartDatePickerDialog;
    private DatePickerDialog mEndDatePickerDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAttendanceBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    private void initialSet() {
        mSharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        String userId = mSharedPreferences.getString("userID", "");
        String[] id = userId.split("@");
        mUserId = id[1];

        this.setDatePickerDialog();

        mBinding.txtStartDate.setOnClickListener(v -> mStartDatePickerDialog.show());
        mBinding.txtEndDate.setOnClickListener(v -> mEndDatePickerDialog.show());
        mBinding.btnSearch.setOnClickListener(v -> getAttendanceList());
        mBinding.btnBack.setOnClickListener(backClick);
    }

    private void hideResultView(){
        mBinding.recyclerView.setVisibility(View.GONE);
        mBinding.loading.setVisibility(View.VISIBLE);
        mBinding.emptyLayout.setVisibility(View.GONE);
    }

    private void visibleResultView(){
        mBinding.recyclerView.setVisibility(View.VISIBLE);
        mBinding.loading.setVisibility(View.GONE);
        mBinding.emptyLayout.setVisibility(View.GONE);
    }

    // 뒤로가기 클릭
    View.OnClickListener backClick = v -> onBackPressed();

    private void setDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        mStartDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mBinding.txtStartDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }
        }, mYear, mMonth, mDay);

        mEndDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mBinding.txtEndDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }
        }, mYear, mMonth, mDay);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideResultView();
        getAttendanceList();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    private void getAttendanceList() {
        hideResultView();
        Common.sEmployeeAttendanceDTOS.clear();
        String startDate = mBinding.txtStartDate.getText().toString();
        startDate = startDate.equals("") ? "2020-01-01" : startDate;
        String endDate = mBinding.txtEndDate.getText().toString();
        endDate = endDate.equals("") ? Common.getCurrentTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())) : endDate;

        Call<ResponseBody> call = Common.sService_site.attendanceList(startDate, endDate, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getAttendanceList result = " + result);
                        JSONArray jsonArray = new JSONArray(result);
                        int jsonArr_len = jsonArray.length();
                        if (jsonArr_len == 0) {
                            mBinding.recyclerView.setVisibility(View.GONE);
                            mBinding.loading.setVisibility(View.GONE);
                            mBinding.emptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            for (int i = 0; i < jsonArr_len; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Common.sEmployeeAttendanceDTOS.add(new EmployeeAttendanceDTO(Integer.parseInt(mUserId), jsonObject.getString("date"), jsonObject.getString("aTime"), jsonObject.getString("lTime")));
                            }
                            mAttendanceAdapter = new AttendanceAdapter();
                            mAttendanceAdapter.notifyDataSetChanged();
                            mBinding.recyclerView.setAdapter(mAttendanceAdapter);
                            mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            visibleResultView();
                        }
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getAttendanceList");
                        if (!AttendanceActivity.this.isFinishing())
                            Common.showDialog(AttendanceActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getAttendanceList");
                        if (!AttendanceActivity.this.isFinishing())
                            Common.showDialog(AttendanceActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else if (code == 401) {
                    if (!AttendanceActivity.this.isFinishing())
                        Common.appRestart(AttendanceActivity.this);
                } else {
                    if (!AttendanceActivity.this.isFinishing())
                        Common.showDialog(AttendanceActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!AttendanceActivity.this.isFinishing())
                    Common.showDialog(AttendanceActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
            }
        });


    }
}
