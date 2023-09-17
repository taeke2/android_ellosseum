package com.gbsoft.ellosseum;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DynamicNewLine {

        public DynamicNewLine() {}

        public void getNewLineLayout(final String[] str, final int fontSize, final Context context, final ViewGroup parent, final View.OnClickListener onClickListener, float scale) {


            //부모뷰가 초기화가 된 다음 메소드를 실행해주지 않으면 contents의 getWidth 값은 0을 반환한다.
            parent.post(new Runnable() {
                @Override
                public void run() {
//                    String[] strArr = str.split(" ");
                    String[] strArr = str;

//                    int space = fontSize / 2; // 폰트크기의 절반만큼을 공백으로 사용함.
                    int space = 8; // 폰트크기의 절반만큼을 공백으로 사용함.
                    int textViewTotalWidth = 20; //TextView의 길이 합을 알기 위해 사용합니다.
                    int linearCount = 0;

                    int padding = 16;

                    int mImageWidth = (int) (20 * scale);
                    int mImageHeight = (int) (20 * scale);

                    int mImageFlag = 0;
                    ImageView imageView = new ImageView(context);


                    Glide.with(context)
                            .load(R.drawable.icon_hashtag)
                            .override(80)
                            .into(imageView);

                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                            mImageWidth,
                            mImageHeight);

                    imageView.setLayoutParams(imageParams);

                    imageParams.setMargins(0,0,space,0);
                    imageView.requestLayout();


                    //분할된 문자열을 저장한 TextView 동적 생성
                    TextView[] textViewArr = new TextView[strArr.length];

                    //TextView 초기화
                    for (int i = 0; i < textViewArr.length; i++) {
                        if(strArr[i].equals(" ") || strArr[i].equals("")){
                            continue;
                        }

                        //동적 레이아웃 생성
                        textViewArr[i] = new TextView(context);
                        Log.v("DynamicNewLine: ", strArr[i]);
                        textViewArr[i].setText("#"+strArr[i]);
//                        textViewArr[i].setTextSize(fontSize);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        textViewArr[i].setLayoutParams(params);
//                        textViewArr[i].setTextColor(Color.BLACK);
                        textViewArr[i].setTextColor(Color.parseColor("#9EA4AE"));
                        textViewArr[i].setTypeface(Typeface.DEFAULT_BOLD);

                        textViewArr[i].setSingleLine();

                        textViewArr[i].setBackground(context.getDrawable(R.drawable.txt_hashtag_sizefree));

                        textViewArr[i].setOnClickListener(onClickListener);

                        //부모 뷰에서 onDraw 하기 전에 부모뷰에 크기를 위임하여 길이를 알아오기 위함
                        textViewArr[i].measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        textViewTotalWidth += textViewArr[i].getMeasuredWidth();

                        //마지막 TextView가 아닐 경우 공백 추가
                        if(i != textViewArr.length-1) {
                            params.setMargins(0, 0, space, 0);
                            textViewTotalWidth += space + 16;
                        }

                    }

                    //TextViewTotalWidth를 이용하여 동적 생성할 Linear Layout의 개수를 알 수 있다.
                    //텍스트뷰 길이의 합을 부모뷰의 가로길이로 나눈다.
                    LinearLayout[] linearBox;
                    if(parent.getWidth() != 0) {

                        //LinearLayout 초기화
                        linearBox = new LinearLayout[textViewTotalWidth / parent.getWidth() + 1];
                        for (int i = 0; i < linearBox.length; i++) {
                            linearBox[i] = new LinearLayout(context);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            if(i > 0){ // 해시태그 이미지 때문에 간격 맞추기 위해 추가
                                params.setMargins(80 + space, space, 0, space);
                            } else {
                                params.setMargins(0, space, 0, space);
                            }
                            linearBox[i].setLayoutParams(params);
                            linearBox[i].setOrientation(LinearLayout.HORIZONTAL);
                        }

                        textViewTotalWidth = 20;

                        for(int i=0; i<textViewArr.length; i++) {
                            if(textViewArr[i] == null) continue;

                            textViewArr[i].measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                            textViewTotalWidth += textViewArr[i].getMeasuredWidth();

                            //마지막 TextView가 아닐 경우 공백 추가
                            if(i != textViewArr.length-1) {
                                textViewTotalWidth += space + padding;
                            }

                            //만약 가로뷰보다 추가될 텍스트뷰 가로합이 더 작다면 레이아웃에 추가해줌.
                            if(linearCount < linearBox.length && textViewTotalWidth < parent.getWidth()) {
                                if(mImageFlag == 0) {
                                    linearBox[linearCount].addView(imageView);
                                    mImageFlag = 1;
                                }
                                linearBox[linearCount].addView(textViewArr[i]);
                            } else {
                                //그렇지 않으면 다음 레이아웃으로 넘거가고 같은 작업을 다시 실시
                                textViewTotalWidth = 20;
                                linearCount++;
                                i--; // 같은 작업을 한번 더 실시하게 함.
                            }

                        }

                        //마지막으로 부모뷰에 레이아웃을 추가해준다.
                        for(int i=0; i<linearBox.length; i++) {
                            if(i > 0){

                            }
                            parent.addView(linearBox[i]);
                        }
                    }

                    else Log.d("Error", "부모뷰의 getWidth를 구할 수 없습니다.");
                }
            });

        }
}
