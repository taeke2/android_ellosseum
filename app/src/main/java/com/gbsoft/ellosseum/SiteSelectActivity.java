package com.gbsoft.ellosseum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.gbsoft.ellosseum.databinding.ActivitySiteSelectBinding;

public class SiteSelectActivity extends AppCompatActivity {
    private static final String TAG = "SiteSelectActivityLog";

    private ActivitySiteSelectBinding mBinding;

    private String mType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySiteSelectBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    private void initialSet() {
        Intent intent = getIntent();
        mType = intent.getStringExtra("type");

        mBinding.btnBack.setOnClickListener(v -> onBackPressed());

        SiteSelectAdapter siteSelectAdapter = new SiteSelectAdapter();
        siteSelectAdapter.notifyDataSetChanged();
        siteSelectAdapter.setOnItemClickListener(siteClick);
        mBinding.recyclerView.setAdapter(siteSelectAdapter);
        mBinding.recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),2));
    }


    SiteSelectAdapter.OnItemClickListener siteClick = new SiteSelectAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int pos) {
            int site = Common.sSiteDTOS.get(pos).getId();
            String link = Common.sSiteDTOS.get(pos).getLink();
            String name = Common.sSiteDTOS.get(pos).getName();

            Log.v(TAG, "on item info site / link / name = " + site + " / " + link + " / " + name);

            SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("site", site);
            editor.putString("siteLink", link);
            editor.apply();

            Common.getService2(link);

            Intent intent = new Intent();
            intent.putExtra("type", mType);
            intent.putExtra("name", name);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
