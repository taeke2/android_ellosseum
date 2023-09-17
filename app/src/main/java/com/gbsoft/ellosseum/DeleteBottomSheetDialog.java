package com.gbsoft.ellosseum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gbsoft.ellosseum.databinding.DialogDeleteBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeleteBottomSheetDialog extends BottomSheetDialogFragment {
    private DialogDeleteBinding mBinding;
    private DeleteBottomSheetClickListener issue_deleteClickListener;
    private String content, title;

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }

    public DeleteBottomSheetDialog(DeleteBottomSheetClickListener issue_deleteClickListener, String title, String content){
        this.issue_deleteClickListener = issue_deleteClickListener;
        this.title = title;
        this.content = content;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogDeleteBinding.inflate(inflater, container, false);
        setStyle(DeleteBottomSheetDialog.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);

        mBinding.txtTitle.setText(this.title);
        mBinding.txtContent1.setText(this.content);

        mBinding.btnOk.setOnClickListener(v -> {
            issue_deleteClickListener.onDeleteClick();
            dismiss();
        });

        mBinding.btnCancel1.setOnClickListener(v -> {
            dismiss();
        });

        mBinding.btnCancel2.setOnClickListener(v -> {
            issue_deleteClickListener.onCancelClick();
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
