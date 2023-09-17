//package com.gbsoft.ellosseum;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.activity.OnBackPressedCallback;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//
//import com.gbsoft.ellosseum.databinding.FragmentTermsBinding;
//
//public class TermsFragment extends Fragment {
//    private FragmentTermsBinding mBinding;
//    private OnBackPressedCallback callback;
//
//    public static TermsFragment newInstance(int num) {
//        TermsFragment termsFragment = new TermsFragment();
//        return termsFragment;
//    }
//
//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        // JoinActivity의 onBackPressed() 적용
//        callback = new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                ((JoinActivity) getActivity()).beforeFragment();
//            }
//        };
//        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        mBinding = FragmentTermsBinding.inflate(inflater, container, false);
//        return mBinding.getRoot();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (mBinding.chkTermsAllAgree.isChecked()) {
//            mBinding.btnOk.setEnabled(true);
//            mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_enable));
//        } else {
//            mBinding.btnOk.setEnabled(false);
//            mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_disable));
//        }
//        mBinding.btnOk.setOnClickListener(nextClick);
//        mBinding.btnCancel.setOnClickListener(cancelClick);
//        mBinding.chkTermsAllAgree.setOnClickListener(allAgreeClick);
//        mBinding.chkTerms1Agree.setOnClickListener(agree1Click);
//        mBinding.chkTerms2Agree.setOnClickListener(agree2Click);
//        mBinding.chkTerms3Agree.setOnClickListener(agree3Click);
//    }
//
//    // 전체동의
//    View.OnClickListener allAgreeClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (mBinding.chkTermsAllAgree.isChecked()) {
//                mBinding.chkTerms1Agree.setChecked(true);
//                mBinding.chkTerms2Agree.setChecked(true);
//                mBinding.chkTerms3Agree.setChecked(true);
//                mBinding.btnOk.setEnabled(true);
//                mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_enable));
//            } else {
//                mBinding.chkTerms1Agree.setChecked(false);
//                mBinding.chkTerms2Agree.setChecked(false);
//                mBinding.chkTerms3Agree.setChecked(false);
//                mBinding.btnOk.setEnabled(false);
//                mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_disable));
//            }
//        }
//    };
//
//    View.OnClickListener agree1Click = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (mBinding.chkTerms2Agree.isChecked() && mBinding.chkTerms3Agree.isChecked()) {
//                if (mBinding.chkTerms1Agree.isChecked()) {
//                    mBinding.chkTermsAllAgree.setChecked(true);
//                    mBinding.btnOk.setEnabled(true);
//                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_enable));
//                } else {
//                    mBinding.chkTermsAllAgree.setChecked(false);
//                    mBinding.btnOk.setEnabled(false);
//                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_disable));
//                }
//            }
//        }
//    };
//
//    View.OnClickListener agree2Click = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (mBinding.chkTerms1Agree.isChecked() && mBinding.chkTerms3Agree.isChecked()) {
//                if (mBinding.chkTerms2Agree.isChecked()) {
//                    mBinding.chkTermsAllAgree.setChecked(true);
//                    mBinding.btnOk.setEnabled(true);
//                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_enable));
//                } else {
//                    mBinding.chkTermsAllAgree.setChecked(false);
//                    mBinding.btnOk.setEnabled(false);
//                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_disable));
//                }
//            }
//        }
//    };
//
//    View.OnClickListener agree3Click = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (mBinding.chkTerms1Agree.isChecked() && mBinding.chkTerms2Agree.isChecked()) {
//                if (mBinding.chkTerms3Agree.isChecked()) {
//                    mBinding.chkTermsAllAgree.setChecked(true);
//                    mBinding.btnOk.setEnabled(true);
//                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_enable));
//                } else {
//                    mBinding.chkTermsAllAgree.setChecked(false);
//                    mBinding.btnOk.setEnabled(false);
//                    mBinding.btnOk.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.btn_access_disable));
//                }
//            }
//        }
//    };
//
//    // '확인' 클릭
//    View.OnClickListener nextClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            ((JoinActivity) getActivity()).nextFragment();
//        }
//    };
//
//    // '취소' 클릭
//    View.OnClickListener cancelClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            ((JoinActivity) getActivity()).beforeFragment();
//        }
//    };
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        callback.remove();
//    }
//}
