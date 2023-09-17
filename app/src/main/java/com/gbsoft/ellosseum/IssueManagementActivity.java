package com.gbsoft.ellosseum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.gbsoft.ellosseum.databinding.ActivityIsuemanagementBinding;
import com.gbsoft.ellosseum.dto.IssueDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IssueManagementActivity extends AppCompatActivity {
    private final String TAG = "IssueManagementActivity";

    private ActivityIsuemanagementBinding mBinding;

    private int issue_id; // issue add 용

    private IssueAdapter mIssueaAapter;

    private int hashtag_flag = 0; // flag == 0 이면 이슈 검색, flag == 1이면 해시태그로 검색

    /**
     * onActivityResult
     */
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "MainActivity로 돌아왔다. ");

                        // hashtag 결과 값이 있을 경우 flag = 1로 설정
                        if(result.getData().hasExtra("hashtag")){
                            hideResultView();
                            gethashtag_issueList(result.getData().getStringExtra("hashtag"));
                            hashtag_flag = 1;
                        }
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityIsuemanagementBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        hideResultView();
    }

    // 이슈 글들을 불러오는 동안 recyclerview 숨기고 loading progress 활성화
    private void hideResultView(){
        Glide.with(this)
                .load(R.drawable.ic_plus)
                .into(mBinding.imgIssueAdd);

        mBinding.recyclerView.setVisibility(View.GONE);
        mBinding.loading.setVisibility(View.VISIBLE);
        mBinding.emptyLayout.setVisibility(View.GONE);
    }

    // 이슈 글들을 불러왔으면 recyclerview 활성화하고 loading progress는 숨김
    private void visibleResultView(){
        mBinding.recyclerView.setVisibility(View.VISIBLE);
        mBinding.loading.setVisibility(View.GONE);
        mBinding.emptyLayout.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // if (Common.sIssueStates.size() <= 0) Common.getCommonData(8, getApplicationContext());    // 이슈 상태 저장

        // 해시태그가 선택되어서 flag가 변경되었는지 확인
        if(hashtag_flag == 0) {
            hideResultView();
            getIssueList(mBinding.editTitle.getText().toString());
        }

        mBinding.btnSearch.setOnClickListener(searchClick);
        mBinding.btnBack.setOnClickListener(backClick);
        if (Common.sAuthority == Common.EMP)
            mBinding.imgIssueAdd.setVisibility(View.INVISIBLE);
        else {
            mBinding.imgIssueAdd.setVisibility(View.VISIBLE);
            mBinding.imgIssueAdd.setOnClickListener(issueAddClick);
        }
    }

    // 뒤로가기 클릭
    View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hashtag_flag = 0;
            onBackPressed();
        }
    };

    // 검색 클릭
    View.OnClickListener searchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(mBinding.editTitle.getWindowToken(), 0);
            hashtag_flag = 0;
            hideResultView();
            getIssueList(mBinding.editTitle.getText().toString());
        }
    };

    // + 클릭
    View.OnClickListener issueAddClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hashtag_flag = 0;
            setIssue();
        }
    };

    /**
     * issue 글 저장 index 확보
     */
    private void setIssue(){
        Call<ResponseBody> call = Common.sService_site.setissue(Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "addIssue result : " + result);
                        JSONObject jsonObject = new JSONObject(result);
                        if(jsonObject.has("insertId")){
                            issue_id = jsonObject.getInt("insertId");

                            Intent intent = new Intent(getApplicationContext(), IssueManagementAddActivity.class);
                            intent.putExtra("id", issue_id);
                            startActivity(intent);
                        } else {
                            //error
                            Log.e(Common.TAG_ERR, "ERROR: jsonObject.has error - setIssue");
                            if (!IssueManagementActivity.this.isFinishing())
                                Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                        }
                    } catch (JSONException e){
                        Log.e(Common.TAG_ERR, "ERROR: JSONException - setIssue");
                        if (!IssueManagementActivity.this.isFinishing())
                            Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - setIssue");
                        if (!IssueManagementActivity.this.isFinishing())
                            Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else if (code == 401) {
                    if (!IssueManagementActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementActivity.this, checkDialogClickListener);
                } else {
                    if (!IssueManagementActivity.this.isFinishing())
                        Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementActivity.this.isFinishing())
                    Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
            }
        });
    }


    /**
     * 선택한 해시태그로 해당하는 issue 리스트들 검색
     */
    private void gethashtag_issueList(String hashTag) {
        Common.sIssueDTOS.clear();
//        Call<ResponseBody> call = Common.sService_site.getissue(searchText.equals("") ? "" : searchText, Common.sToken);
        String temp = hashTag.equals("") ? "" : hashTag;
        String[] text = temp.split(" ");
        Call<ResponseBody> call = Common.sService_site.gethashtag_issue(text[0], Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        JSONArray jsonArray = new JSONArray(result);
                        Log.v(TAG, "gethashtag_issue result : " + result);
                        int jsonArr_len = jsonArray.length();
                        if (jsonArr_len == 0) {
//                            Common.showToast(getApplicationContext(), "관련 게시물이 없습니다.");
//                            mIssueaAapter = new IssueAdapter();
//                            mIssueaAapter.notifyDataSetChanged();
//                            mIssueaAapter.setOnItemClickListener(issueOnClick);
//                            mBinding.recyclerView.setAdapter(mIssueaAapter);
//                            mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            mBinding.loading.setVisibility(View.GONE);
                            mBinding.emptyLayout.setVisibility(View.VISIBLE);
                            mBinding.emptyText.setText(getString(R.string.none_related_issue));
                        } else {
                            for (int i = 0; i < jsonArr_len; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                IssueDTO dto = new IssueDTO();
                                dto.setId(jsonObject.getInt("id"));
                                dto.setEmployee_id(jsonObject.getInt("employee_id"));
                                dto.setName(jsonObject.getString("name"));
                                dto.setTitle(jsonObject.getString("title"));
                                dto.setContent(jsonObject.getString("content"));
                                dto.setState(jsonObject.getInt("state"));
                                dto.setImageName(jsonObject.getString("imageName"));
                                dto.setCreateAt(jsonObject.getString("createAt"));
                                dto.setUpdateAt(jsonObject.getString("updateAt"));
                                dto.setTag(jsonObject.getString("tag"));
                                Common.sIssueDTOS.add(dto);
                            }
                            mIssueaAapter = new IssueAdapter();
                            mIssueaAapter.notifyDataSetChanged();
                            mIssueaAapter.setOnItemClickListener(issueOnClick);
                            mBinding.recyclerView.setAdapter(mIssueaAapter);
                            mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                            visibleResultView();
                        }
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - gethashtag_issue");
                        if (!IssueManagementActivity.this.isFinishing())
                            Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    } catch (IOException e) {
                        if (!IssueManagementActivity.this.isFinishing())
                            Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                        Log.e(Common.TAG_ERR, "ERROR: IOException - gethashtag_issue");
                    }
                } else if (code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementActivity.this, checkDialogClickListener);
                } else {
                    if (!IssueManagementActivity.this.isFinishing())
                        Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementActivity.this.isFinishing())
                    Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
            }
        });
    }


    /**
     * issue 리스트들 검색 (제목으로 필터링)
     */
    private void getIssueList(String searchText) {
        Common.sIssueDTOS.clear();
//        Call<ResponseBody> call = Common.sService_site.getissue(searchText.equals("") ? "" : searchText, Common.sToken);
        Call<ResponseBody> call = Common.sService_site.getissue(searchText.equals("") ? "" : searchText, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        JSONArray jsonArray = new JSONArray(result);
                        Log.v(TAG, "getIssueList result : " + result);
                        int jsonArr_len = jsonArray.length();
                        if (jsonArr_len == 0) {
                            mBinding.loading.setVisibility(View.GONE);
                            mBinding.emptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            for (int i = 0; i < jsonArr_len; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                IssueDTO dto = new IssueDTO();
                                dto.setId(jsonObject.getInt("id"));
                                dto.setEmployee_id(jsonObject.getInt("employee_id"));
                                dto.setName(jsonObject.getString("name"));
                                dto.setTitle(jsonObject.getString("title"));
                                dto.setContent(jsonObject.getString("content"));
                                dto.setState(jsonObject.getInt("state"));
                                dto.setImageName(jsonObject.getString("imageName"));
                                dto.setCreateAt(jsonObject.getString("createAt"));
                                dto.setUpdateAt(jsonObject.getString("updateAt"));
                                dto.setTag(jsonObject.getString("tag"));
                                Common.sIssueDTOS.add(dto);
                            }
                            mIssueaAapter = new IssueAdapter();
                            mIssueaAapter.notifyDataSetChanged();
                            mIssueaAapter.setOnItemClickListener(issueOnClick);
                            mBinding.recyclerView.setAdapter(mIssueaAapter);
                            mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                            visibleResultView();
                        }
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getIssueList");
                        if (!IssueManagementActivity.this.isFinishing())
                            Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getIssueList");
                        if (!IssueManagementActivity.this.isFinishing())
                            Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else if (code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementActivity.this, checkDialogClickListener);
                } else {
                    if (!IssueManagementActivity.this.isFinishing())
                        Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementActivity.this.isFinishing())
                    Common.showDialog(IssueManagementActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
            }
        });
    }

    CheckDialogClickListener checkDialogClickListener = new CheckDialogClickListener() {
        @Override
        public void onPositiveClick() {
            finishAffinity();

            Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
            startActivity(intent);

            System.runFinalization();
            System.exit(0);
        }
    };

    IssueAdapter.OnItemClickListener issueOnClick = new IssueAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int pos) {
            hashtag_flag = 0;
            // 예외 조치
            if(Common.sIssueDTOS.size() > 0){
                Intent intent = new Intent(getApplicationContext(), IssueManagementDetailActivity.class);
                intent.putExtra("pos", pos);
                startActivityResult.launch(intent);
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
