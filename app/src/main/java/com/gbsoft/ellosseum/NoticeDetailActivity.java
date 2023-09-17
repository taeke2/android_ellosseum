package com.gbsoft.ellosseum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.gbsoft.ellosseum.databinding.ActivityNoticeDetailBinding;

public class NoticeDetailActivity extends AppCompatActivity {

    private ActivityNoticeDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityNoticeDetailBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    private void initialSet() {
        Intent intent = getIntent();
        int i = intent.getIntExtra("pos", -1);
        if (i != -1) {
            mBinding.txtTitle.setText(Common.sNoticeDTOs.get(i).getTitle());
            mBinding.txtDate.setText(Common.sNoticeDTOs.get(i).getUpdateDate());
            mBinding.txtWriter.setText(Common.sNoticeDTOs.get(i).getWriter());
        }

        WebSetting setting = new WebSetting(NoticeDetailActivity.this);
        WebView webView = setting.setWebSettings(mBinding.webView);
        webView.loadData(Common.sNoticeDTOs.get(i).getContent(), "text/html; charset=utf-8", "UTF-8");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.btnBack.setOnClickListener(backClick);
    }

    View.OnClickListener backClick = v -> onBackPressed();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
