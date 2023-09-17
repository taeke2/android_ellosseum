package com.gbsoft.ellosseum;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gbsoft.ellosseum.databinding.DialogLogoutBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

public class LogoutBottomSheetDialog extends BottomSheetDialogFragment {
    DialogLogoutBottomSheetBinding mBinding;
    private LogoutDialogClickListener mLogoutDialogClickListener;
    private String mName;

    LogoutBottomSheetDialog(LogoutDialogClickListener logoutDialogClickListener, String name) {
        this.mLogoutDialogClickListener = logoutDialogClickListener;
        this.mName = name;
    }

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setStyle(LogoutBottomSheetDialog.STYLE_NORMAL, getTheme());
        mBinding = DialogLogoutBottomSheetBinding.inflate(inflater, container, false);

        mBinding.txtName.setText(mName);

        mBinding.btnOk.setOnClickListener(v -> {
            mLogoutDialogClickListener.onPositiveClick();
            dismiss();
        });

        mBinding.btnCancel1.setOnClickListener(v -> dismiss());

        mBinding.btnCancel2.setOnClickListener(v -> {
            mLogoutDialogClickListener.onNegativeClick();
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