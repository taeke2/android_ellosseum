package com.gbsoft.ellosseum;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.gbsoft.ellosseum.databinding.DialogFindBinding;

public class FindDialog extends Dialog {

    private DialogFindBinding mBinding;

    private Context mContext;
    private FindDialogClickListener mFindDialogClickListener;
    private String mName;
    private String mId;

    public FindDialog(@NonNull Context context, String name, String id, FindDialogClickListener findDialogClickListener) {
        super(context);
        this.mContext = context;
        this.mFindDialogClickListener = findDialogClickListener;
        this.mName = name;
        this.mId = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DialogFindBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.txtName.setText(mName);
        mBinding.txtId.setText(mId);

        mBinding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFindDialogClickListener.onPositiveClick();
                dismiss();
            }
        });
    }
}
