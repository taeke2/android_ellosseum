package com.gbsoft.ellosseum;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.gbsoft.ellosseum.databinding.DialogSelectBinding;

public class Img_ArDialog extends Dialog {
    private DialogSelectBinding mBinding;

    private Context mContext;
    private Img_ArDialogClickListener mimg_arDialogClickListener;

    public Img_ArDialog(@NonNull Context context, Img_ArDialogClickListener img_arDialogClickListener) {
        super(context);
        this.mContext = context;
        this.mimg_arDialogClickListener = img_arDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DialogSelectBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.btnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mimg_arDialogClickListener.onImgClick();
                dismiss();
            }
        });

        mBinding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mimg_arDialogClickListener.onGalleryClick();
                dismiss();
            }
        });

        mBinding.btnAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mimg_arDialogClickListener.onARClick();
                dismiss();
            }
        });

        mBinding.btnCancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
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
