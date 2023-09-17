package com.gbsoft.ellosseum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gbsoft.ellosseum.databinding.DialogSelectBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class imgArSelectBottomSheetDialog extends BottomSheetDialogFragment {
    private DialogSelectBinding mBinding;
    private Img_ArDialogClickListener mImg_arDialogClickListener;

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }

    public imgArSelectBottomSheetDialog(Img_ArDialogClickListener img_arDialogClickListener){
        this.mImg_arDialogClickListener = img_arDialogClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogSelectBinding.inflate(inflater, container, false);
        setStyle(imgArSelectBottomSheetDialog.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);

        mBinding.btnImg.setOnClickListener(v -> {
            mImg_arDialogClickListener.onImgClick();
            dismiss();
        });

        mBinding.btnGallery.setOnClickListener(v -> {
            mImg_arDialogClickListener.onGalleryClick();
            dismiss();
        });

        mBinding.btnAR.setOnClickListener(v -> {
            // 현재 없음
        });

        mBinding.btnCancel1.setOnClickListener(v -> {
            dismiss();
        });


        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
