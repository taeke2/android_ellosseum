package com.gbsoft.ellosseum;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gbsoft.ellosseum.dto.EmployeeAttendanceDTO;
import com.gbsoft.ellosseum.dto.IssueDTO;
import com.gbsoft.ellosseum.dto.NoticeDTO;
import com.gbsoft.ellosseum.dto.SiteDTO;
import com.gbsoft.ellosseum.dto.SubcontractorDTO;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Common {
    private static final String TAG = "CommonLog";
    // 서버 주소
//    public static final String URL = "http://192.168.0.173:3000";
//    public static final String URL = "http://192.168.0.101:3000";
//    public static final String URL = "http://192.168.0.27:3000";
    public static final String URL = "http://115.85.183.231:3000";

    public static ApiService sService_master = null;
    public static ApiService sService_site = null;

    // Retrofit2_master
    public static void getService1() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        sService_master = retrofit.create(ApiService.class);
    }

    // Retrofit2_site
    public static void getService2(String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        sService_site = retrofit.create(ApiService.class);
    }

    // gps handler
    public static Handler sGpsHandler = null;
    public static Handler sIssueGpsHandler = null;

    // 알람 그룹 아이디
    public static int sNotificationSosId = 1000;
    public static int sNotificationGpsId = 2000;
    public static int sNotificationWarningId = 3000;
    public static int sNotificationSiteId = 4000;
    public static int sNotificationAreaId = 5000;
    public static int sNotificationWorkId = 6000;
    public static int sNotificationLogoutId = 7000;

    public static String sUserId = "";
    public static int sAuthority = 0;
    public static String sToken = "";

    // 회원 아이디 가져오기
    public static void getUserId(SharedPreferences sharedPreferences) {
        String[] id = sharedPreferences.getString("userID", "").split("@", -1);
        if (id.length != 1) {
            userGradeCheck(id[0]);
            sUserId = id[1];
        }
    }

    public static final int EMP = 100; // 근로자 토큰 권한

    // 회원 권한 체크
    public static void userGradeCheck(String id) {
        if (id.equals("emp")) sAuthority = EMP;
        else sAuthority = Integer.parseInt(id);
    }

    // 현재시간 포맷
    public static String getCurrentTime(SimpleDateFormat format) {
        Calendar date = Calendar.getInstance();
        return format.format(date.getTime());
    }

    // Toast 메세지 보이기
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    // 서비스 시작
    public static void startLocationService(Context context, boolean isLocationServiceRunning) {
        if (!isLocationServiceRunning) {
            Intent intent = new Intent(context, GpsService.class);
            intent.setAction(GpsConstants.ACTION_START_LOCATION_SERVICE);
            context.startService(intent);
//            try {
//                showToast(context, "Location service started");
//            } catch (Exception e) {
//                Log.d(Common.TAG_ERR, "toast error");
//            }
        }
    }

    // 서비스 종료
    public static void stopLocationService(Context context, boolean isLocationServiceRunning) {
        if (isLocationServiceRunning) {
            Intent intent = new Intent(context, GpsService.class);
            intent.setAction(GpsConstants.ACTION_STOP_LOCATION_SERVICE);
            context.startService(intent);
//            try {
//                showToast(context, "Location service stopped");
//            } catch (Exception e) {
//                Log.d(Common.TAG_ERR, "toast error");
//            }
        }
    }

    // 임시토큰 발급
    public static String sTempToken = "";

    public static void showDialog(Context context, String title, String content, CheckDialogClickListener listener) {
        try {
            CheckDialog dialog = new CheckDialog(context, listener, title, content);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false); // 다이얼로그 밖에 터치 시 종료
            dialog.setCancelable(false); // 다이얼로그 취소 가능 (back key)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 다이얼로그 background 적용하기 위한 코드 (radius 적용)
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG_ERR, "Common showDialog error. maybe BadTokenException");
        }
    }

    public static void tokencheckDialog(Context context, CheckDialogClickListener listener) {
        try {
            CheckDialog dialog = new CheckDialog(context, listener, context.getResources().getString(R.string.dialog_token_title), context.getResources().getString(R.string.dialog_token_content));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false); // 다이얼로그 밖에 터치 시 종료
            dialog.setCancelable(false); // 다이얼로그 취소 가능 (back key)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 다이얼로그 background 적용하기 위한 코드 (radius 적용)
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG_ERR, "Common tokencheckDialog error. maybe BadTokenException");
        }
    }

    public static void appRestart(Context context) {
        CheckDialog dialog = new CheckDialog(context, () -> {
            SharedPreferences mSharedPreferences = context.getSharedPreferences("Info", AppCompatActivity.MODE_PRIVATE);
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();

            int siteCode = mSharedPreferences.getInt("site", -1);
            String siteLink = mSharedPreferences.getString("siteLink", "");
            mEditor.clear();
            mEditor.commit();
            mEditor.putInt("site", siteCode);
            mEditor.putString("siteLink", siteLink);
            mEditor.commit();
            Common.sToken = "";
            Common.sAuthority = 0;
            Common.sUserId = "";

            if (!siteLink.equals("")) Common.getService2(siteLink);
            Common.stopLocationService(context, isLocationServiceRunning(context));

            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
            context.startActivity(mainIntent);

            System.runFinalization();
            System.exit(0);
        }, context.getResources().getString(R.string.dialog_token_title), context.getResources().getString(R.string.dialog_token_content));

        try {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false); // 다이얼로그 밖에 터치 시 종료
            dialog.setCancelable(false); // 다이얼로그 취소 가능 (back key)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 다이얼로그 background 적용하기 위한 코드 (radius 적용)
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG_ERR, "Common appRestart error. maybe BadTokenException");
        }
    }


    public static boolean isLocationServiceRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (GpsService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------  getList
    // DTO 리스트
    public static ArrayList<SiteDTO> sSiteDTOS = new ArrayList<>(); // 현장 리스트
    public static ArrayList<SubcontractorDTO> sSubcontractorDTOS = new ArrayList<>();   // 파트너사 리스트
    public static ArrayList<NoticeDTO> sNoticeDTOs = new ArrayList<>(); // 공지사항 리스트
    public static ArrayList<EmployeeAttendanceDTO> sEmployeeAttendanceDTOS = new ArrayList<>(); // 근태현황 리스트
    public static ArrayList<IssueDTO> sIssueDTOS = new ArrayList<>(); // 이슈 리스트
    public static LinkedHashMap<Integer, String> sStates = new LinkedHashMap<>(); // 상태 리스트
    public static LinkedHashMap<Integer, String> sOccupations = new LinkedHashMap<>(); // 직종 리스트
    public static LinkedHashMap<Integer, String> sEquipmentTypes = new LinkedHashMap<>(); // 장비 종류 리스트
    public static LinkedHashMap<Integer, String> sIssueStates = new LinkedHashMap<>(); // 장비 종류 리스트
    public static LinkedHashMap<Integer, String> sBloodTypes = new LinkedHashMap<>(); // 혈액형 리스트
    public static String[] sTels = {"010", "011", "016", "017", "018", "019"}; // 연락처 앞자리

    // Error Tag
    public static final String TAG_ERR = "ErrorMessage";

    // 소속 파트너사 리스트 조회
    public static void getSubcontractorList(Context context) {
        sSubcontractorDTOS.clear();
        Call<ResponseBody> call = Common.sService_site.getSubcontractorList();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        JSONArray jsonArray = new JSONArray(result);

                        int jsonLen = jsonArray.length();
                        for (int i = 0; i < jsonLen; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            SubcontractorDTO dto = new SubcontractorDTO();
                            dto.setId(jsonObject.getInt("subCode"));
                            String subName = jsonObject.getString("subName");
                            dto.setName(subName);
                            dto.setTel(jsonObject.getString("subTel"));
                            dto.setWorkType(jsonObject.getString("subType"));
                            dto.setManagerName(jsonObject.getString("subManager"));
                            dto.setRemark(jsonObject.getString("remark"));
                            sSubcontractorDTOS.add(dto);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG_ERR, "ERROR: JSON Parsing Error - getSubcontractorList");
                        showDialog(context, context.getResources().getString(R.string.dialog_error_title), context.getResources().getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(TAG_ERR, "ERROR: IOException - getSubcontractorList");
                        showDialog(context, context.getResources().getString(R.string.dialog_error_title), context.getResources().getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else {
                    showDialog(context, context.getResources().getString(R.string.dialog_error_title), context.getResources().getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // showDialog(context, context.getResources().getString(R.string.dialog_error_title), context.getResources().getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    // 상태 조회
    public static void getCommonData(int id, Context context) {
        sStates.clear();
        Call<ResponseBody> call = Common.sService_site.getCommonData(id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "getCommonData" + id + " = " + result);
                        JSONArray jsonArray = new JSONArray(result);
                        int jsonLen = jsonArray.length();
                        for (int i = 0; i < jsonLen; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int column_id = jsonObject.getInt("id");
                            String data = jsonObject.getString("name");
                            if (id == 1) {  // 상태
                                sStates.put(column_id, data);
                            } else if (id == 2) {   // 직종
                                sOccupations.put(column_id, data);
                            } else if (id == 4) {   // 장비 종류
                                sEquipmentTypes.put(column_id, data);
                            } else if (id == 5) {   // 혈액형
                                sBloodTypes.put(column_id, data);
                            } else if (id == 8) {
                                sIssueStates.put(column_id, data);
                            } else{
                                // 리스트 추가
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG_ERR, "ERROR: JSON Parsing Error - getStateList");
                        showDialog(context, context.getResources().getString(R.string.dialog_error_title), context.getResources().getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(TAG_ERR, "ERROR: IOException - getStateList");
                        showDialog(context, context.getResources().getString(R.string.dialog_error_title), context.getResources().getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else {
                    Log.v(TAG, "Response fail " + id);
                    showDialog(context, context.getResources().getString(R.string.dialog_error_title), context.getResources().getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v(TAG, "Connect fail " + id);
                // showDialog(context, context.getResources().getString(R.string.dialog_error_title), context.getResources().getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }
}
