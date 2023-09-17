package com.gbsoft.ellosseum;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.gbsoft.ellosseum.databinding.DialogCheckBinding;

public class CheckDialog extends Dialog {

    private DialogCheckBinding mBinding;

    private Context mContext;
    private CheckDialogClickListener mCheckDialogClickListener;
    private String mTitle;
    private String mContent;

    public CheckDialog(@NonNull Context context, CheckDialogClickListener checkDialogClickListener, String title, String content) {
        super(context);
        this.mContext = context;
        this.mCheckDialogClickListener = checkDialogClickListener;
        this.mTitle = title;
        this.mContent = content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DialogCheckBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.txtTitle.setText(mTitle);
        mBinding.txtContent.setText(mContent);

        mBinding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckDialogClickListener.onPositiveClick();
                dismiss();
            }
        });

        mBinding.btnCancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

//        mBinding.btnCancel2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCheckDialogClickListener.onNegativeClick();
//                dismiss();
//            }
//        });
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
