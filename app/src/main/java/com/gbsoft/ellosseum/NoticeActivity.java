package com.gbsoft.ellosseum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gbsoft.ellosseum.databinding.ActivityNoticeBinding;
import com.gbsoft.ellosseum.dto.NoticeDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeActivity extends AppCompatActivity {
    private ActivityNoticeBinding mBinding;
    private NoticeAdapter mNoticeAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityNoticeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        hideResultView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideResultView();
        getNoticeList(mBinding.editTitle.getText().toString());
        mBinding.btnSearch.setOnClickListener(searchClick);
        mBinding.btnBack.setOnClickListener(backClick);
    }

    // 뒤로가기 클릭
    View.OnClickListener backClick = v -> onBackPressed();

    // 검색 클릭
    View.OnClickListener searchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(mBinding.editTitle.getWindowToken(), 0);
            hideResultView();
            getNoticeList(mBinding.editTitle.getText().toString());
        }
    };

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

    // 공지사항 리스트 조회
    private void getNoticeList(String searchText) {
        Common.sNoticeDTOs.clear();
        Call<ResponseBody> call = Common.sService_site.noticeList(searchText.equals("") ? "" : searchText, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        JSONArray jsonArray = new JSONArray(result);
                        // Log.v(TAG, "getNoticeList result : " + result);
                        int jsonArr_len = jsonArray.length();
                        if (jsonArr_len == 0) {
                            mBinding.loading.setVisibility(View.GONE);
                            mBinding.emptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            for (int i = 0; i < jsonArr_len; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                NoticeDTO dto = new NoticeDTO();
                                dto.setId(jsonObject.getInt("id"));
                                dto.setWriterId(jsonObject.getString("writerId"));
                                dto.setWriter(jsonObject.getString("writer"));
                                dto.setPublicScope(jsonObject.getInt("pScope"));
                                dto.setTitle(jsonObject.getString("title"));
                                dto.setContent(jsonObject.getString("content"));
                                dto.setImportantYn(jsonObject.getInt("impYn"));
                                dto.setCreateDate(jsonObject.getString("cDate"));
                                dto.setUpdateDate(jsonObject.getString("uDate"));
                                Common.sNoticeDTOs.add(dto);
                            }
                            mNoticeAdapter = new NoticeAdapter();
                            mNoticeAdapter.notifyDataSetChanged();
                            mNoticeAdapter.setOnItemClickListener(noticeOnClick);
                            mBinding.recyclerView.setAdapter(mNoticeAdapter);
                            mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                            visibleResultView();
                        }
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getNoticeList");
                        if (!NoticeActivity.this.isFinishing())
                            Common.showDialog(NoticeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getNoticeList");
                        if (!NoticeActivity.this.isFinishing())
                            Common.showDialog(NoticeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { finish(); });
                    }
                } else if (code == 401) {
//                    Common.appRestart(NoticeActivity.this);
                    if (!NoticeActivity.this.isFinishing())
                        Common.tokencheckDialog(NoticeActivity.this, checkDialogClickListener);

                } else {
                    if (!NoticeActivity.this.isFinishing())
                        Common.showDialog(NoticeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { finish(); });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!NoticeActivity.this.isFinishing())
                    Common.showDialog(NoticeActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { finish(); });
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

    NoticeAdapter.OnItemClickListener noticeOnClick = new NoticeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int pos) {
            Intent intent = new Intent(getApplicationContext(), NoticeDetailActivity.class);
            intent.putExtra("pos", pos);
            startActivity(intent);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
