package com.gbsoft.ellosseum;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gbsoft.ellosseum.databinding.DialogIssueStateBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class IssueStateBottomSheetDialog extends BottomSheetDialogFragment {
    private DialogIssueStateBottomSheetBinding mBinding;
    private int mState;
    private IssueStateBottomSheetClickListener mBottomSheetClickListener;

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }

    public IssueStateBottomSheetDialog(IssueStateBottomSheetClickListener bottomSheetClickListener, int state){
        this.mBottomSheetClickListener = bottomSheetClickListener;
        this.mState = state;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogIssueStateBottomSheetBinding.inflate(inflater, container, false);
        setStyle(IssueStateBottomSheetDialog.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);

        switch(mState){
            case 1:
                mBinding.btnPre.setTextColor(Color.parseColor("#ED1B23"));
                mBinding.imgPreCheck.setVisibility(View.VISIBLE);
                mBinding.imgPreCheck.setColorFilter(Color.parseColor("#ED1B23"), PorterDuff.Mode.SRC_IN);
                break;
            case 2:
                mBinding.btnHold.setTextColor(Color.parseColor("#ED1B23"));
                mBinding.imgHoldCheck.setVisibility(View.VISIBLE);
                mBinding.imgHoldCheck.setColorFilter(Color.parseColor("#ED1B23"), PorterDuff.Mode.SRC_IN);
                break;
            case 3:
                mBinding.btnIng.setTextColor(Color.parseColor("#ED1B23"));
                mBinding.imgIngCheck.setVisibility(View.VISIBLE);
                mBinding.imgIngCheck.setColorFilter(Color.parseColor("#ED1B23"), PorterDuff.Mode.SRC_IN);
                break;
            case 4:
                mBinding.btnPost.setTextColor(Color.parseColor("#ED1B23"));
                mBinding.imgPostCheck.setVisibility(View.VISIBLE);
                mBinding.imgPostCheck.setColorFilter(Color.parseColor("#ED1B23"), PorterDuff.Mode.SRC_IN);
                break;

          default:
            break;
        }

        mBinding.btnPre.setOnClickListener(v -> {
            mBottomSheetClickListener.onPreClick();
            mBinding.imgPreCheck.setVisibility(View.VISIBLE);
            mBinding.imgPreCheck.setColorFilter(Color.parseColor("#ED1B23"), PorterDuff.Mode.SRC_IN);
            mBinding.imgHoldCheck.setVisibility(View.GONE);
            mBinding.imgIngCheck.setVisibility(View.GONE);
            mBinding.imgPostCheck.setVisibility(View.GONE);

            mBinding.btnPre.setTextColor(Color.parseColor("#ED1B23"));
            mBinding.btnHold.setTextColor(Color.parseColor("#000000"));
            mBinding.btnIng.setTextColor(Color.parseColor("#000000"));
            mBinding.btnPost.setTextColor(Color.parseColor("#000000"));
        });

        mBinding.btnHold.setOnClickListener(v -> {
            mBottomSheetClickListener.onHoldClick();
            mBinding.imgPreCheck.setVisibility(View.GONE);
            mBinding.imgHoldCheck.setVisibility(View.VISIBLE);
            mBinding.imgHoldCheck.setColorFilter(Color.parseColor("#ED1B23"), PorterDuff.Mode.SRC_IN);
            mBinding.imgIngCheck.setVisibility(View.GONE);
            mBinding.imgPostCheck.setVisibility(View.GONE);

            mBinding.btnPre.setTextColor(Color.parseColor("#000000"));
            mBinding.btnHold.setTextColor(Color.parseColor("#ED1B23"));
            mBinding.btnIng.setTextColor(Color.parseColor("#000000"));
            mBinding.btnPost.setTextColor(Color.parseColor("#000000"));
        });

        mBinding.btnIng.setOnClickListener(v -> {
            mBottomSheetClickListener.onIngClick();
            mBinding.imgPreCheck.setVisibility(View.GONE);
            mBinding.imgHoldCheck.setVisibility(View.GONE);
            mBinding.imgIngCheck.setVisibility(View.VISIBLE);
            mBinding.imgIngCheck.setColorFilter(Color.parseColor("#ED1B23"), PorterDuff.Mode.SRC_IN);
            mBinding.imgPostCheck.setVisibility(View.GONE);

            mBinding.btnPre.setTextColor(Color.parseColor("#000000"));
            mBinding.btnHold.setTextColor(Color.parseColor("#000000"));
            mBinding.btnIng.setTextColor(Color.parseColor("#ED1B23"));
            mBinding.btnPost.setTextColor(Color.parseColor("#000000"));
        });

        mBinding.btnPost.setOnClickListener(v -> {
            mBottomSheetClickListener.onPostClick();
            mBinding.imgPreCheck.setVisibility(View.GONE);
            mBinding.imgHoldCheck.setVisibility(View.GONE);
            mBinding.imgIngCheck.setVisibility(View.GONE);
            mBinding.imgPostCheck.setVisibility(View.VISIBLE);
            mBinding.imgPostCheck.setColorFilter(Color.parseColor("#ED1B23"), PorterDuff.Mode.SRC_IN);

            mBinding.btnPre.setTextColor(Color.parseColor("#000000"));
            mBinding.btnHold.setTextColor(Color.parseColor("#000000"));
            mBinding.btnIng.setTextColor(Color.parseColor("#000000"));
            mBinding.btnPost.setTextColor(Color.parseColor("#ED1B23"));
        });

        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
