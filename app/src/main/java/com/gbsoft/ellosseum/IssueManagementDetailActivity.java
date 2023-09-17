package com.gbsoft.ellosseum;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.gbsoft.ellosseum.databinding.ActivityIsuemanagementDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IssueManagementDetailActivity extends AppCompatActivity {

//    private ActivityIsuemanagementDetailBinding mBinding;
    private ActivityIsuemanagementDetailBinding mBinding;

    private static final String TAG = "IssueManagementDetailActivity";

    private static final String TAG_ERR = "ErrorMessage";    // LOGTAG : 로그 태그
    private static final String LOG_TAG2 = "SuccessMessage";    // LOGTAG : 로그 태그
//    private static final int MAX = 10;   // 사진 최대 갯수
    private static final int MAX = 10;   // 사진 최대 갯수, 단말기 메모리 한계 문제로 조정
    private static final int MAX_GALLERY = 10;   // 갤러리 최대 선택 갯수


    private static final int REQUEST_CODE = 100;  // 갤러리 Intent 반환 코드
    private static final int REQUEST_TAKE_PHOTO = 200;    // 카메라 촬영 Intent 반환 코드

    // UI 객체
//    PhotoView photo_detail;
    RecyclerView recyclerView;

    // Adapter
    private RequestImageListAdapter adapter;    // recyclerView 어뎁터

    private String mNewImageList = "";  // 새로운 이미지 리스트
    private String mCurrentPhotoPath;   // 촬영한 이미지 경로
    private String mSavedImageList = "";  // 저장된 이미지 리스트
    private File mImage; // 카메라 촬영 이미지 저장

    // 미리 저장합시다 인적정보
    int issue_id; // 해당 게시물의 index
    private int state; // 해당 게시물을 불러왔을 때 상태
    int employee_id; // 이 근로자는 현재 디바이스에 접속한 사람이어야해요 Common.sUserId 사용하면됨
    String content = ""; // 게시물의 내용
    String hashtag_ = ""; // 게시물의 해시태그

    private static final int ISSUE_STATE_PRE = 1;
    private static final int ISSUE_STATE_HOLD = 2;
    private static final int ISSUE_STATE_ING = 3;
    private static final int ISSUE_STATE_POST = 4;

    private int ISSUE_CURRENT_STATE = ISSUE_STATE_PRE;
    private int ISSUE_SAVE_STATE = 0;

    private String[] stateArr;

    int popupflag = 0; // popupflag

    PopupMenu popupMenu;

    private Boolean btnMenu_status = false;
    private Boolean fabSave_Edit_status = false;

    private Boolean popupMenu_activity = false;

    private Boolean mUrls_TempSavedFlag = true;

    private ArrayList<String> mUploadFilePath;   // 업로드할 파일 실제 경로, 파일명 리스트
    private String mUrls_TempSaved = "";   // 저장된 이미지 임시 이름 리스트
    private ArrayList<String> mUrls_saved; // 저장된 이미지 이름 리스트
    private ArrayList<String> mUrls_new; // 등록할 이미지 이름 리스트
    private ArrayList<Bitmap> mBitmaps_new; // 등록할 이미지 리스트

    private Bitmap new_img;

    private String mServerPath = "";

    private RequestManager mGlide; // 카메라 glide

    private RecyclerView.LayoutManager layoutManager;

    private Animation myAnim;


    // 화면 해상도
    int standardSize_X, standardSize_Y;
    float density;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mBinding = ActivityIsuemanagementDetailBinding.inflate(getLayoutInflater());
        mBinding = ActivityIsuemanagementDetailBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    /**
     * Activity 초기설정 (onCreate에서 호출)
     */
    private void initialSet() {
        Intent intent = getIntent();
        int pos = intent.getIntExtra("pos", -1);
        SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        mServerPath = sharedPreferences.getString("siteLink", "");

        // 예외처리
        if(Common.sIssueDTOS.size() == 0) finish();
        if (pos == -1) finish();

        // value 초기 설정
        valueSettingInit(pos);

        // view 초기 설정
        viewSettingInit(pos);

        // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG2, "권한 설정 완료");
            } else {
                Log.v(LOG_TAG2, "권한 설정 요청");
                ActivityCompat.requestPermissions(IssueManagementDetailActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        // 현재 해당 이슈 글에 포함된 이미지 이름들 데이터베이스에서 가져옴
        getImgName();
    }

    /**
     * view 초기설정 (onCreate -> initialSet에서 호출)
     */
    private void viewSettingInit(int pos){
        mBinding.txtTitle.setText(Common.sIssueDTOS.get(pos).getTitle()); // 제목 설정
        mBinding.txtDate.setText(Common.sIssueDTOS.get(pos).getUpdateAt()); // 날짜 설정
        mBinding.txtWriter.setText(Common.sIssueDTOS.get(pos).getName()); // 작성자 설정
        mBinding.txtIsueContent.setText(Common.sIssueDTOS.get(pos).getContent()); // 내용 설정

        //floating action button image memory low setting
        mGlide
                .load(R.drawable.icon_more_vert)
                .into(mBinding.btnMenu);

        mGlide
                .load(R.drawable.icon_state)
                .into(mBinding.fabState);

        mGlide
                .load(R.drawable.save_icon_verylow)
                .into(mBinding.fabSave);

        mGlide
                .load(R.drawable.icon_edit)
                .into(mBinding.fabEdit);

        mGlide
                .load(R.drawable.icon_trash)
                .into(mBinding.fabDelete);

//        // hashTag icon load
//        mGlide.load(R.drawable.icon_hashtag)
//                .into(mBinding.imgHashtag);


        //imgview 추가 버튼 image momory low setting
        mGlide
                .load(R.drawable.ic_plus)
                .into(mBinding.imgviewAdd);


        // editview 숨기기
        mBinding.linearLayoutHashview.setVisibility(View.GONE);
        mBinding.imgviewAddbtn.setVisibility(View.GONE);

        // 해시태그 설정
        String[] text = Common.sIssueDTOS.get(pos).getTag().split(",");
        String temp = "";

        // 화면 크기 가져오기
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float scale = outMetrics.density;

        DynamicNewLine dynamicNewLine = new DynamicNewLine();
        dynamicNewLine.getNewLineLayout(text, 20, IssueManagementDetailActivity.this, mBinding.linearLayoutHashTextview, textViewOnclickListener, scale);

        for(int i = 0; i < text.length; i++){
            if(i == text.length - 1){
                temp += "#" + text[i];
                continue;
            }
            temp += "#" + text[i] + " ";
//            TextView textview = new TextView(getApplicationContext());
//            textview.setText("#"+text[i]);
//            textview.setBackground(getDrawable(R.drawable.txt_hashtag_sizefree));
////            textview.setTextColor(Color.parseColor("#55A8FD"));
//            textview.setTextColor(Color.parseColor("#9EA4AE"));
//            textview.setLayoutParams(mLayoutParams);
//            textview.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent();
//                    String search_hashtag = textview.getText().toString().substring(1, textview.getText().toString().length());
//                    intent.putExtra("hashtag", search_hashtag);
//
//                    setResult(Activity.RESULT_OK, intent);
//                    finish();
//                }
//            });
//            mBinding.linearLayoutHashTextview.addView(textview);
        }
        mBinding.editMemoImage.setText(temp);

        mBinding.txtIsueContent.setEnabled(false);

        // recyclerview에 image view가 순차적으로 들어감
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//        mBinding.fabsMenu.attachToRecyclerView(recyclerView);

        // animation 추가
        myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);

        // 기기 해상도 비례 값 가져오기
        getStandardSize();

    }


    /**
     * value 초기 설정 (onCreate -> initialSet에서 호출)
     */
    private void valueSettingInit(int pos){
        // 해당 게시물의 index
        issue_id = Common.sIssueDTOS.get(pos).getId();

        // 해당 게시물의 state
        state = Common.sIssueDTOS.get(pos).getState();

        // state 배열
        stateArr = new String[]{getString(R.string.state_pre), getString(R.string.state_hold), getString(R.string.state_ing), getString(R.string.state_post)};

        // 저장할 때 사용할 변수에 현재 state 저장
        ISSUE_SAVE_STATE = state;

        // 객체생성
        mUploadFilePath = new ArrayList<>();

        // 기존에 저장되어있던 이미지들(Bitmap)
//        mBitmaps_saved = new ArrayList<>();

        // 새롭게 추가된 이미지들(Bitmap)
        mBitmaps_new = new ArrayList<>();

        // 기존에 저장되어있던 이미지이름들(String)
        mUrls_saved = new ArrayList<>();

        // 새로 추가할 이미지이름들(String)
        mUrls_new = new ArrayList<>();

        // glide setting
        mGlide = Glide.with(this);

    }

    public Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return  size;
    }

    public void getStandardSize() {
        Point ScreenSize = getScreenSize(this);
        density  = getResources().getDisplayMetrics().density;

        standardSize_X = (int) (ScreenSize.x / density);
        standardSize_Y = (int) (ScreenSize.y / density);
    }


    /**
     * DB에 이미지 리스트 가져오기
     * 호출
     * 1) (onCreate -> initialSet에서 호출)
     * 2)
     */
    public void getImgName() {
        Call<ResponseBody> call = Common.sService_site.getImgName(issue_id, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (response.isSuccessful() && code == 200) {
                    try {
                        String result = response.body().string();
                        JSONArray jsonArray = new JSONArray(result);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        Log.v("getImgName: ", jsonObject+"");

                        if (jsonObject.getString("imgName").equals("null") || jsonObject.getString("imgName").equals("")) {
//                            new imageShow(IssueManagementDetailActivity.this).execute();
                            if(mUrls_TempSavedFlag){
                                mUrls_TempSavedFlag = false;
                            }
                            imageShow();
                            return;
                        } else {
                            // 데이터 베이스에 저장되어있는 이미지 이름들을 가져옴 Ex_1) 1_1,1_2,1_3  Ex_2) 2_3,2_12,2_22
                            mSavedImageList = jsonObject.getString("imgName");

                            if(mUrls_TempSavedFlag){
                                mUrls_TempSaved = jsonObject.getString("imgName");
                                mUrls_TempSavedFlag = false;
                            }

                            new imageCheck(IssueManagementDetailActivity.this).execute();
                        }
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getImgName");
                        if (!IssueManagementDetailActivity.this.isFinishing())
                            Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getImgName");
                        if (!IssueManagementDetailActivity.this.isFinishing())
                            Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                }
                else if (code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementDetailActivity.this , checkDialogClickListener);
                }
                else {
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    /**
     * 이미지 보기
     * 호출
     * 1) 데이터베이스에 저장된 이미지 이름들이 없을 때
     *
     */
    private void imageShow(){
        // 현재 저장되어있는 이미지 초기화
        mUrls_saved.clear();

        String[] list = mSavedImageList.split(",");
        int list_len = list.length;

        if(list[0].equals("")) return; // 로딩가능한 이미지가 없음

        for (int i = 0; i < list_len; i++) {
            mUrls_saved.add(list[i]);
        }

        // 키보드 내리기
        viewForcusRemove();

        // 현재 저장된 이미지들 adapter에 담고 RecyclerView와 adapter 연결
        layoutManager = new LinearLayoutManager(IssueManagementDetailActivity.this, LinearLayoutManager.VERTICAL, false){

            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                int childCount = getChildCount(); //지금 recyclerView 영역에 보이고 있는 아이템의 갯수
                int itemCount = getItemCount(); //전체 갯수
                Log.v(TAG,"true");

                // x 버튼 활성화 과연 될까?! -> 됩니다
                if(popupMenu_activity){
                    for (int childCnt = recyclerView.getItemDecorationCount() + recyclerView.getChildCount(), i = recyclerView.getItemDecorationCount(); i < childCnt; ++i) {
                        final RequestImageListAdapter.ViewHolder holder = (RequestImageListAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                        holder.linearLayout.setVisibility(View.VISIBLE);

                        // 그림자 추가
                        holder.imageView.setElevation(1f);
                        holder.mDelete_Img.setElevation(2f);

                        // 애니메이션 추가
                        holder.linearLayout.startAnimation(myAnim);
                    }
                } else {
                    for (int childCnt = recyclerView.getItemDecorationCount() + recyclerView.getChildCount(), i = recyclerView.getItemDecorationCount(); i < childCnt; ++i) {
                        final RequestImageListAdapter.ViewHolder holder = (RequestImageListAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));

                        // 그림자 없애기
                        holder.imageView.setElevation(0);
                        holder.mDelete_Img.setElevation(0);

                        holder.linearLayout.setVisibility(View.GONE);
                    }
                }

                if(childCount == itemCount){
                    //한 화면에 보여줄 수 있는 item갯수보다 적은 갯수를 가지고 있음
                }else{
                    //한 화면에 보여줄 수 있는 item갯수보다 많은 갯수를 가지고 있음
                }
            }
        };


        recyclerView.setLayoutManager(layoutManager);
        adapter = new RequestImageListAdapter(mUrls_saved, mServerPath);
//        adapter.setOnItemClickListener(new RequestImageListAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(int pos) {
//                // 현재는 눌러도 의미없음
//            }
//        });

        adapter.setOnLongItemClickListener(new RequestImageListAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {

                // 수정 모드일때만 활성화
                if(popupMenu_activity){
                    // 이미지 삭제 다이얼로그
                    imageDeleteAlertShow(pos);
                }
            }
        });

        adapter.setOnDeleteItemClickListener(new RequestImageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                // 수정 모드일때만 활성화
                if(popupMenu_activity){
                    // 이미지 삭제 다이얼로그
                    imageDeleteAlertShow(pos);
                }
            }
        });

        recyclerView.setAdapter(adapter);

    }

    /**
     * 키보드 내리기
     * 호출
     * 1) imageShow task가 onPostExecute가 되면 호출
     */
    private void viewForcusRemove(){
        InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mBinding.txtIsueContent.getWindowToken(), 0);
        manager.hideSoftInputFromWindow(mBinding.editMemoImage.getWindowToken(), 0);
        mBinding.txtIsueContent.clearFocus();
        mBinding.editMemoImage.clearFocus();
        mBinding.layout02.requestFocus();
        mBinding.linearLayoutHashview.requestFocus();
    }

    /**
     * 이슈 글 삭제 확인 다이얼 로그
     */
    private void showIssueDeleteDialog(){
        DeleteBottomSheetDialog deleteCheckBottomSheetDialog = new DeleteBottomSheetDialog(issue_deleteClickListener, getString(R.string.delete), getString(R.string.notice_delete));

        deleteCheckBottomSheetDialog.show(getSupportFragmentManager(), "deleteCheckBottomSheetDialog");
    }


    /**
     * Img or Ar 선택 다이얼 로그
     */
    private void showImgArDialog() {
        imgArSelectBottomSheetDialog imgArSelectBottomSheetDialog = new imgArSelectBottomSheetDialog(Img_ArDialogClickListener);

        imgArSelectBottomSheetDialog.show(getSupportFragmentManager(), "imgArSelectBottomSheetDialog");
    }


    /**
     * 이미지 삭제 다이얼로그 창
     */
    public void imageDeleteAlertShow(int pos) {
        // 이미지 삭제 clicklistener
        DeleteBottomSheetClickListener issue_ImageDeleteClickListener = new DeleteBottomSheetClickListener() {
            @Override
            public void onDeleteClick() {
                String[] list = mSavedImageList.split(",");
                String str_image_list = "";
                int list_len = list.length;
                for (int i = 0; i < list_len; i++) {
                    if (i == pos) continue;
                    else {
                        str_image_list += (str_image_list.equals("")) ? list[i] : "," + list[i];
                    }
                }
                try {
                    adapter.removeAt(pos);
                    deleteImage(list[pos], str_image_list); // list[pos]는 사용자가 선택한 삭제할 이미지임
                } catch (MalformedURLException e) {
                    Log.d(Common.TAG_ERR, "error MalformedURLException imageDeleteAlertShow()");
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                }
            }

            @Override
            public void onCancelClick() {
                // 자동으로 dismiss
            }
        };

        DeleteBottomSheetDialog deleteCheckBottomSheetDialog = new DeleteBottomSheetDialog(issue_ImageDeleteClickListener, getString(R.string.delete), getString(R.string.image_delete_comment));

        deleteCheckBottomSheetDialog.show(getSupportFragmentManager(), "deleteCheckBottomSheetDialog");

    }



    /**
     * 서버 이미지 삭제
     */
    private void deleteImage(String delete_imageName, String delete_after_image_list) throws MalformedURLException {
        Call<ResponseBody> call = Common.sService_site.deleteImage(issue_id, delete_imageName, delete_after_image_list, Common.sToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (response.isSuccessful() && code == 200) {
                    try {
                        String result = response.body().string();
                        if (result.equals("delete error")) {
                            Toast.makeText(IssueManagementDetailActivity.this, result, Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(IssueManagementDetailActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            mSavedImageList = delete_after_image_list;
                            getImgName();
                        }
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "error IOException deleteImage()");
                        if (!IssueManagementDetailActivity.this.isFinishing())
                            Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if(code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementDetailActivity.this, checkDialogClickListener);
                }
                else {
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    /**
     * 갤러리 인텐트 전환
     */
    private void dispatchTakeGalleryIntent(){
        int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED)
        { // 권한 없어서 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            intent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    /**
     * 카메라 촬영 인텐트 전환
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent(인텐트를 처리할 카메라가 있는지 확인)
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go(사진이 들어갈 파일 만들기)
            try {
                createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File(파일 생성중 오류 발생)
                Log.e(Common.TAG_ERR, "error IOException dispatchTakePictureIntent()");
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
            }
            // Continue only if the File was successfully created(파일이 성공적으로 생성된 경우에만 계속)
            if (mImage != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.gbsoft.ellosseum.fileprovider",
                        mImage);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * 촬영 이미지 파일 생성
     */
    private void createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        mImage = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents(파일 저장 : ACTION_VIEW 인텐트와 함께 사용할 경로)
        mCurrentPhotoPath = mImage.getAbsolutePath();
        Log.i(TAG_ERR, "CurrentPhotoPath : " + mCurrentPhotoPath);
    }

    /**
     * 웹 서버에 이미지 파일 존재 유무
     */
    class imageCheck extends AsyncTask<String, Void, Boolean> {
        private int responseHttp = 0;
        private boolean flag = false;
        private Context mContext;

        public imageCheck(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected Boolean doInBackground(String... urlPath) {
            try {
                String[] list = mSavedImageList.split(",");
                int len = list.length;

                int list_len = list.length;
                for (int i = 0; i < list_len; i++) {
                    URL url = null;
                    url = new URL(mServerPath + "/uploads_android/" + list[i] + ".jpg");

                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(2000);
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    responseHttp = httpConnection.getResponseCode();
                    if (responseHttp == HttpURLConnection.HTTP_OK) {
                        flag = true;
                    } else {
                        flag = false;
                        break;
                    }

                }

            } catch (Exception e) {
                Log.e(Common.TAG_ERR, "error Exception imageCheck class doInBackground()");
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
//                new imageShow(IssueManagementDetailActivity.this).execute();
                imageShow();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.err_pic_load), Toast.LENGTH_LONG).show();
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        }

    }

    /**
     * 선택한 사진 보이기 (INTENT 처리)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBitmaps_new.clear();
        mUrls_new.clear();
        mUploadFilePath.clear();
        boolean fg = true;
//        int savedListCount = mBitmaps_saved.size();
        int savedListCount = mUrls_saved.size();

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){ // 갤러리
            try {
                // 선택한 데이터가 없을 때
                if (data.getClipData() == null && data.getData() == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_picture_selected), Toast.LENGTH_SHORT).show();
                } else {
                    if (data.getData() != null) {   // 사진 1장 선택
                        if (savedListCount > MAX + 2) { // 사진 최댓값 넘었을 때
                            Toast.makeText(getApplicationContext(), getString(R.string.issue_max_pic, MAX), Toast.LENGTH_SHORT).show();
                            fg = false;
                        } else {    // 정상 동작
                            Uri uri = data.getData();
                            //tempPathInit(uri);
                            galleryBitmap(uri);
                        }
                    } else {    // 사진 여러장 선택
                        ClipData clipData = data.getClipData();
                        int count = clipData.getItemCount();
                        if (count > MAX_GALLERY) {
                            Toast.makeText(getApplicationContext(), getString(R.string.max_gallery, MAX_GALLERY), Toast.LENGTH_SHORT).show();
                            fg = false;
                        } else {
                            if (count - 2 > MAX - savedListCount) { // 사진 최댓값 넘었을 때
                                Toast.makeText(getApplicationContext(), getString(R.string.issue_max_pic, MAX), Toast.LENGTH_SHORT).show();
                                fg = false;
                            } else {    // 정상 동작
                                for (int i = 0; i < count; i++) {
                                    Uri uri = clipData.getItemAt(i).getUri();
//                                    tempPathInit(uri);
                                    galleryBitmap(uri, count);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(Common.TAG_ERR, "error onActivityResult Exception");
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){ // 카메라

            mGlide
                    .asBitmap()
                    .load(mCurrentPhotoPath)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true))
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            Log.v(TAG, "Glide Error");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                            new_img = resource;
                            mBitmaps_new.add(resource);
                            mUploadFilePath.add(mCurrentPhotoPath);
                            try {
                                uploadImage();
                            } catch (MalformedURLException e) {
                                Log.e(Common.TAG_ERR, "error onActivityResult MalformedURLException");
                                if (!IssueManagementDetailActivity.this.isFinishing())
                                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                            }
                            return false;
                        }
                    }).submit();

//            File file = new File(mCurrentPhotoPath);
//            Bitmap bitmap = null;
//            try {
//                if (Build.VERSION.SDK_INT >= 29) {
//                    ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
//                    bitmap = ImageDecoder.decodeBitmap(source);
//                } else {
//                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
//                }
//
//                new_img = imageController.getInstance().getRotateBitmap(bitmap, mCurrentPhotoPath);
//
//                bitmap.recycle();
////                mBitmaps_new.add(result);
////                mUrls_new.add(result);
//
//
//            } catch (IOException e) { e.printStackTrace(); }
        } else if (resultCode == RESULT_CANCELED) fg = false; // 뒤로가기 눌렀을 때

//        if (fg) {
//            try {
//                uploadImage();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 갤러리에서 한장 가져왔을때
     */
    private void galleryBitmap(Uri uri){
        mGlide
                .asBitmap()
                .load(mCurrentPhotoPath=imageController.getInstance().getRealPathFromURI(getContentResolver(), uri))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Log.v(TAG, "gallery bitmap load fail");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        Log.v(TAG, "gallery bitmap load success");
                        mBitmaps_new.add(resource);
                        mUploadFilePath.add(mCurrentPhotoPath);
                        try {
                            uploadImage();
                        } catch (MalformedURLException e) {
                            Log.e(Common.TAG_ERR, "error galleryBitmap MalformedURLException");
                            if (!IssueManagementDetailActivity.this.isFinishing())
                                Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                        }
                        return false;
                    }
                }).submit();
    }

    /**
     * 갤러리에서 여러장 가져왔을때
     */
    private void galleryBitmap(Uri uri, int count){
        mGlide
                .asBitmap()
                .load(mCurrentPhotoPath=imageController.getInstance().getRealPathFromURI(getContentResolver(), uri))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Log.v(TAG, "gallery bitmap load fail");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        Log.v(TAG, "gallery bitmap load success");
                        mBitmaps_new.add(resource);
                        mUploadFilePath.add(mCurrentPhotoPath);
                        if(count == mUploadFilePath.size()){
                            try {
                                uploadImage();
                            } catch (MalformedURLException e) {
                                Log.e(Common.TAG_ERR, "error galleryBitmap MalformedURLException");
                                if (!IssueManagementDetailActivity.this.isFinishing())
                                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                            }
                        }
                        return false;
                    }
                }).submit();
    }


    /**
     * 임시 이미지 파일 생성
     */
    private void tempPathInit(Uri uri){
        try{

            InputStream in = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in);

            String path = imageController.getInstance().getPath(this, uri);
            mBitmaps_new.add(imageController.getInstance().getRotateBitmap(bitmap, path));
            mUploadFilePath.add(path);

            in.close();
        }catch (Exception e){
            Log.e(Common.TAG_ERR, "error tempPathInit Exception");
            if (!IssueManagementDetailActivity.this.isFinishing())
                Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
        }
    }

    /**
     * 서버에 이미지 업로드
     */
    private void uploadImage() throws MalformedURLException {

        ArrayList<MultipartBody.Part> files = new ArrayList<>();

            int newItemSize = mBitmaps_new.size();
//        int newItemSize = mUrls_new.size();
        int lastItemNum;
        if (mSavedImageList.equals("")) {
            lastItemNum = 1;
        } else {
            String[] a = mSavedImageList.split(",", -1);
            String[] b = a[a.length - 1].split("_");
            String c = b[b.length - 1];
            lastItemNum = (mSavedImageList.equals("")) ? 1 : Integer.parseInt(c) + 1;
        }
        // TODO: 해시태그 부분 개선 필요

        Log.v(TAG, lastItemNum + ", " + mSavedImageList);
        mNewImageList = "";
        for (int i = 0; i < newItemSize; i++) {
            RequestBody fileBody = RequestBody.create(imageController.getInstance().bitmapToFile(mBitmaps_new.get(i), mUploadFilePath.get(i)), MediaType.parse("multipart/form-data"));
//            RequestBody fileBody = RequestBody.create(imageController.getInstance().bitmapToFile(new_img, mCurrentPhotoPath), MediaType.parse("multipart/form-data"));
            UUID uuid = UUID.randomUUID();
            // Glide가 알아서 bitmap recycle도 해줍니다..
            String fileName = uuid.toString() + "_" + issue_id + "_" + (lastItemNum + i);
            mNewImageList += (mNewImageList.equals("")) ? fileName : "," + fileName;
            Log.v(TAG, mNewImageList);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo", fileName + ".jpg", fileBody);
            files.add(filePart);
        }

//        if(!new_img.equals("") && !mCurrentPhotoPath.equals("")){
//            RequestBody fileBody = RequestBody.create(imageController.getInstance().bitmapToFile(new_img, mCurrentPhotoPath), MediaType.parse("multipart/form-data"));
//            UUID uuid = UUID.randomUUID();
//            String fileName = uuid.toString() + "_" + issue_id + "_" + (lastItemNum + 1);
//            mNewImageList += (mNewImageList.equals("")) ? fileName : "," + fileName;
//            Log.v(TAG, mNewImageList);
//            MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo", fileName + ".jpg", fileBody);
//            files.add(filePart);
//        } else {
//            Log.v(TAG, "추가할 이미지 이름이나 저장되어있는 경로가 비어있습니다.");
//            return;
//        }

        RequestBody requestBody_issue_id = RequestBody.create(issue_id+"", MultipartBody.FORM);
        RequestBody token = RequestBody.create(Common.sToken, MultipartBody.FORM);

        RequestBody imgName_list = RequestBody.create((mSavedImageList.equals("")) ? mNewImageList : mSavedImageList + "," + mNewImageList, MultipartBody.FORM);
        Log.v(TAG, (mSavedImageList.equals("")) ? mNewImageList : mSavedImageList + "," + mNewImageList);

        Call<ResponseBody> call = Common.sService_site.uploadImages(requestBody_issue_id, files, imgName_list, token);

        call.enqueue(new Callback<ResponseBody>() {     // 이미지 파일 서버에 전송
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {  // 응답 성공
                int code = response.code();

                // 성공했거나 실패했거나 상관없이 현재 가지고 있는 이미지는 재활용 해야한다.

                if (response.isSuccessful() && code == 200) {
                    try {
                        String result = response.body().string();
                        if (result.equals("multer error") || result.equals("unknown error")) {
                            mNewImageList = "";
                            Toast.makeText(IssueManagementDetailActivity.this, "error\n: " + result, Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(IssueManagementDetailActivity.this, "등록되었습니다.", Toast.LENGTH_SHORT).show();
                            mSavedImageList += (mSavedImageList.equals("")) ? mNewImageList : "," + mNewImageList;
                            getImgName();
                        }
                    } catch (IOException e) {
                        Log.e(TAG_ERR, "ERROR: IOException");
                        if (!IssueManagementDetailActivity.this.isFinishing())
                            Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if(code == 401){
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementDetailActivity.this, checkDialogClickListener);
                }
                else {
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }


    /**
     * 권한 요청
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.v(LOG_TAG2, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    /**
     * 관리자 유무 확인
     */
    private void CheckAdmin(){
        if(Common.sAuthority == Common.EMP){ // 근로자일 경우 이슈 글 수정/삭제 불가
            mBinding.btnMenu.setVisibility(View.GONE);
            mBinding.fabState.setVisibility(View.GONE);
            mBinding.fabEdit.setVisibility(View.GONE);
            mBinding.fabDelete.setVisibility(View.GONE);
            mBinding.fabSave.setVisibility(View.GONE);
//            mBinding.spinner.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 키보드 내리기
        viewForcusRemove();

        // 유저 권한 체크
        CheckAdmin();

        // 저장되어있는 state view에 반영
        mBinding.txtState.setText(stateArr[ISSUE_SAVE_STATE-1]);

        mBinding.btnBack.setOnClickListener(backClick);

        // Floating Action Button

        mBinding.btnMenu.setOnClickListener(fabMenuClick);
        mBinding.fabState.setOnClickListener(fabStateClick);
        mBinding.fabSave.setOnClickListener(fabSaveClick);
        mBinding.fabEdit.setOnClickListener(fabEditClick);
        mBinding.fabDelete.setOnClickListener(fabDeleteClick);

        mBinding.btnSave2.setOnClickListener(fabSaveClick);

        mBinding.imgviewAddbtn.setOnClickListener(imgAddClick);
    }

    // Floating Action button 메뉴 클릭
    View.OnClickListener fabMenuClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleFab(); // Floating action button animation 원래 좌표로
        }
    };

    // Floating Action button 상태 클릭
    View.OnClickListener fabStateClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleFab();
            // bottomSheetDialog 활성화
            IssueStateBottomSheetDialog bottomSheetDialog = new IssueStateBottomSheetDialog(bottomSheetClickListener, state);

            bottomSheetDialog.show(getSupportFragmentManager(), "bottomSheetDialog");
        }
    };

    // Floating Action button 저장 클릭
    View.OnClickListener fabSaveClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IssueDataCheck();
        }
    };

    // Floating Action button 수정 클릭
    View.OnClickListener fabEditClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showEdit();
        }
    };

    // Floating Action Button 삭제 클릭
    View.OnClickListener fabDeleteClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Toast.makeText(IssueManagementDetailActivity.this, "삭제 클릭", Toast.LENGTH_SHORT).show();

            toggleFab(0); //토글 닫힘
            // 이슈 글 삭제 다이얼 로그 활성화
            if (!IssueManagementDetailActivity.this.isFinishing())
                showIssueDeleteDialog();
        }
    };

    // 수정 상태일 때 카메라 이미지 클릭 (사진 추가)
    View.OnClickListener imgAddClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showImgArDialog();
        }
    };

    // 뒤로가기 버튼
    View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    // 토큰 에러 발생시 모두 종료하고 LoadingActivity 재실행
    CheckDialogClickListener checkDialogClickListener = new CheckDialogClickListener() {
        @Override
        public void onPositiveClick() {
            finishAffinity();

            Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
            startActivity(intent);

            System.runFinalization();
            System.exit(0);
        }
    };

    // bottomSheet 안에 버튼들 Listener
    IssueStateBottomSheetClickListener bottomSheetClickListener = new IssueStateBottomSheetClickListener() {
        @Override
        public void onPreClick() {
            if (ISSUE_CURRENT_STATE == ISSUE_STATE_PRE)
                return;
            ISSUE_CURRENT_STATE = ISSUE_STATE_PRE;
            // 실제 데이터베이스에 변경 상태 반영
            issue_state_update();
        } // 상태: 진행전(1)

        @Override
        public void onHoldClick() {
            if (ISSUE_CURRENT_STATE == ISSUE_STATE_HOLD)
                return;
            ISSUE_CURRENT_STATE = ISSUE_STATE_HOLD;
            // 실제 데이터베이스에 변경 상태 반영
            issue_state_update();
        } // 상태: 보류(2)

        @Override
        public void onIngClick() {
            ISSUE_CURRENT_STATE = ISSUE_STATE_ING;
            if (ISSUE_CURRENT_STATE == ISSUE_STATE_ING)
                return;
            // 실제 데이터베이스에 변경 상태 반영
            issue_state_update();
        } // 상태: 진행중(3)

        @Override
        public void onPostClick() {
            if (ISSUE_CURRENT_STATE == ISSUE_STATE_POST)
                return;
            ISSUE_CURRENT_STATE = ISSUE_STATE_POST;
            // 실제 데이터베이스에 변경 상태 반영
            issue_state_update();
        } // 상태: 진행완료(4)
    };


    // 로그아웃 다이얼로그 버튼클릭 리스너 설정
    Img_ArDialogClickListener Img_ArDialogClickListener = new Img_ArDialogClickListener() {

        // 카메라 인텐트 실행
        @Override
        public void onImgClick() {
//            if (mBitmaps_saved.size() == MAX) { // 저장되어 있는 이미지의 갯수가 설정한 MAX 값과 같으면 이미지 업로드 불가
            if (mUrls_saved.size() == MAX) { // 저장되어 있는 이미지의 갯수가 설정한 MAX 값과 같으면 이미지 업로드 불가
                Toast.makeText(getApplicationContext(), getString(R.string.issue_max_pic, MAX), Toast.LENGTH_SHORT).show();
            } else {
                dispatchTakePictureIntent();
            }
        }

        @Override
        public void onGalleryClick() {
            if (mUrls_saved.size() == MAX) { // 저장되어 있는 이미지의 갯수가 설정한 MAX 값과 같으면 이미지 업로드 불가
                Toast.makeText(getApplicationContext(), getString(R.string.issue_max_pic, MAX), Toast.LENGTH_SHORT).show();
            } else {
                dispatchTakeGalleryIntent();
            }
        }

        @Override
        public void onARClick() {

        }
    };

    // 이슈글 삭제 clicklistener
    DeleteBottomSheetClickListener issue_deleteClickListener = new DeleteBottomSheetClickListener() {
        @Override
        public void onDeleteClick() {
            issueDelete();
        }

        @Override
        public void onCancelClick() {
            // 자동으로 dismiss
        }
    };

    /**
     * 이슈 상태 변경
     */
    private void issue_state_update(){
        Call<ResponseBody> call;
        call = Common.sService_site.updateIssueState(issue_id, ISSUE_CURRENT_STATE, Common.sToken);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v("IssueUpdate: ", "employeeJoin result = " + result);

                        // 현재 선택된 STATE를 저장하는 STATE에 값을 덮어쓴다
                        ISSUE_SAVE_STATE = ISSUE_CURRENT_STATE;

                        // textview에 변경 상태 반영
                        mBinding.txtState.setText(stateArr[ISSUE_SAVE_STATE-1]);

                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - IssueUpdate");
                        if (!IssueManagementDetailActivity.this.isFinishing())
                            Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if(code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementDetailActivity.this, checkDialogClickListener);
                }
                else {
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    /**
     * 변경한 글 저장 후 editview 수정 안되게 view 상태 변경
     */
    private void viewissueSave(){
//        Toast.makeText(IssueManagementDetailActivity.this, "완료 클릭", Toast.LENGTH_SHORT).show();
        mBinding.txtIsueContent.setEnabled(false);
        mBinding.editMemoImage.setEnabled(false);
        // 저장하러 갑시다 update

        // editview 안보이게 하기
        mBinding.linearLayoutHashview.setVisibility(View.GONE);

        // 이미지추가 버튼 안보이게 하기
        mBinding.imgviewAddbtn.setVisibility(View.GONE);
        mBinding.imgviewAddbtn.setClickable(false);

        mBinding.btnSave2.setVisibility(View.GONE);

        // textview 보이게 하기
        mBinding.linearLayoutHashTextview.setVisibility(View.VISIBLE);

        // textview 모두 비우기
        mBinding.linearLayoutHashTextview.removeAllViews();

//        // 다시 가져오기..
//        mGlide.load(R.drawable.icon_hashtag)
//                .into(mBinding.imgHashtag);

        // 해시태그 텍스트 가져오기
        String[] text = mBinding.editMemoImage.getText().toString().split("#");
        Log.v(TAG, text+", " + text.length);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float scale = outMetrics.density;


        DynamicNewLine dynamicNewLine = new DynamicNewLine();
        dynamicNewLine.getNewLineLayout(text, 20, IssueManagementDetailActivity.this, mBinding.linearLayoutHashTextview, textViewOnclickListener, scale);

//
//        for(int i = 1; i < text.length; i++){
//            // 해시태그를 text view로 만드는 과정
//            TextView textview = new TextView(getApplicationContext());
//            textview.setText("#"+text[i]);
//            textview.setBackground(getDrawable(R.drawable.txt_hashtag_sizefree));
////            textview.setTextColor(Color.parseColor("#55A8FD"));
//            textview.setTextColor(Color.parseColor("#9EA4AE"));
//            textview.setTypeface(null, Typeface.BOLD);
//
//            textview.setLayoutParams(mLayoutParams);
//            Log.v(TAG, textview.getText().toString());
//
//            // 해시태그를 터치했을 때 선택한 해시태그로 글을 필터링함
//            textview.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent();
//                    String search_hashtag = textview.getText().toString().substring(1, textview.getText().toString().length());
//                    intent.putExtra("hashtag", search_hashtag);
//
//                    setResult(Activity.RESULT_OK, intent);
//                    finish();
//                }
//            });
//            mBinding.linearLayoutHashTextview.addView(textview);
//        }

        // 수정 상태일때만.. 사진을 삭제할 수 있음..
        popupMenu_activity = false;
        if(adapter != null)
            adapter.notifyDataSetChanged();
        issueUpdate();

        // floating Action Button 활성화/비활성화
        fabSave_Edit_status = !fabSave_Edit_status;
        toggleFab(0); //토글 닫힘
    }

    View.OnClickListener textViewOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            String search_hashtag = ((TextView)v).getText().toString().substring(1, ((TextView)v).getText().toString().length());
            intent.putExtra("hashtag", search_hashtag);

            setResult(Activity.RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    };

    /**
     * 현재 글 editview 수정 가능하게 view 상태 변경
     */
    private void showEdit(){
//        Toast.makeText(IssueManagementDetailActivity.this, "수정 클릭", Toast.LENGTH_SHORT).show();

        mBinding.txtIsueContent.setEnabled(true);

        //의미없음
//                            mBinding.editMemoImage.setEnabled(true);
        // textview 안보이게 하기
        mBinding.linearLayoutHashTextview.setVisibility(View.GONE);

        // editview 보이게 하기
        mBinding.linearLayoutHashview.setVisibility(View.VISIBLE);

        // editview 활성화
        mBinding.editMemoImage.setEnabled(true);

        // 처음에 editview가 아니라 textview가 나와야함.
        // 우선 editview를 비활성화 시키고 안에 있는 String을 가져와서 그 수만큼 동적으로 textview 생성
        // 그리고 완료 버튼을 누르면 textview를 비활성화 하며 editview를 활성화 하는 식으로 적용

        // 이미지 추가 버튼 보이게 하기
        mBinding.imgviewAddbtn.setVisibility(View.VISIBLE);
        mBinding.imgviewAddbtn.setClickable(true);

        mBinding.btnSave2.setVisibility(View.VISIBLE);

        // 수정 상태일때만.. 사진을 삭제할 수 있음..
        popupMenu_activity = true;




        // x 버튼 활성화 과연 될까?!
//        for (int childCount = recyclerView.getChildCount(), i = recyclerView.getItemDecorationCount(); i < childCount; ++i) {
//            final RequestImageListAdapter.ViewHolder holder = (RequestImageListAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
//            holder.linearLayout.setVisibility(View.VISIBLE);
//        }

        if(adapter != null)
            adapter.notifyDataSetChanged();


//                        menuItem.setVisible(false);

        // floating Action Button 활성화/비활성화
        fabSave_Edit_status = !fabSave_Edit_status;
        toggleFab(0); //토글 닫힘
    }

    // 수정이나 완료, 삭제, 상태 변경 버튼 눌러도 토글 닫힐 수 있게 추가
    public void toggleFab(int i){
        mBinding.fabState.animate()
                .translationY(0f);

        mBinding.fabSave.animate()
                .translationY(0f);

        mBinding.fabEdit.animate()
                .translationY(0f);

        mBinding.fabDelete.animate()
                .translationY(0f);

        mBinding.txtFabState.animate()
                .translationY(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mBinding.txtFabState.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        mBinding.txtFabState.setVisibility(View.GONE);
                    }
                });

        mBinding.txtFabDelete.animate()
                .translationY(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mBinding.txtFabDelete.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        mBinding.txtFabDelete.setVisibility(View.GONE);
                    }
                });

        mBinding.txtFabEdit.animate()
                .translationY(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mBinding.txtFabEdit.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        mBinding.txtFabEdit.setVisibility(View.GONE);
                    }
                });

        mBinding.txtFabSave.animate()
                .translationY(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mBinding.txtFabSave.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        mBinding.txtFabSave.setVisibility(View.GONE);
                    }
                });

//        int colorFrom = Color.parseColor("#CC000000");
//        int colorTo = Color.TRANSPARENT;
//        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
//        colorAnimation.setDuration(250); // milliseconds
//        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//            @Override
//            public void onAnimationUpdate(ValueAnimator animator) {
//                mBinding.framelayout.setBackgroundColor((int) animator.getAnimatedValue());
//            }
//
//        });
//        colorAnimation.start();

        mBinding.framelayout.setBackgroundColor(Color.TRANSPARENT);
        mBinding.framelayout.setClickable(false); // 뒷 화면 터치 허용
        mBinding.framelayout.setFocusable(false);


//        ObjectAnimator fs_animation = ObjectAnimator.ofFloat(mBinding.fabState, "translationY", 0f);
//        fs_animation.start();
//        ObjectAnimator fS_animation = ObjectAnimator.ofFloat(mBinding.fabSave, "translationY", 0f);
//        fS_animation.start();
//        ObjectAnimator fe_animation = ObjectAnimator.ofFloat(mBinding.fabEdit, "translationY", 0f);
//        fe_animation.start();
//        ObjectAnimator fd_animation = ObjectAnimator.ofFloat(mBinding.fabDelete, "translationY", 0f);
//        fd_animation.start();

        btnMenu_status = !btnMenu_status;
    }

    // 플로팅 액션 버튼 클릭시 애니메이션 효과
    public void toggleFab() {

        if(btnMenu_status) {
            toggleFab(0);
        }else {
            // 플로팅 액션 버튼 열기

//            IssueManagementDetailActivity.this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float density = getResources().getDisplayMetrics().density;
            float dpHeight = outMetrics.heightPixels / density;
            float dpWidth = outMetrics.widthPixels / density;


//            int colorFrom = Color.TRANSPARENT;
//            int colorTo = Color.parseColor("#CC000000");
//
//            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
//            colorAnimation.setDuration(250); // milliseconds
//            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//                @Override
//                public void onAnimationUpdate(ValueAnimator animator) {
//                    mBinding.framelayout.setBackgroundColor((int) animator.getAnimatedValue());
//                }
//
//            });
//            colorAnimation.start();
            mBinding.framelayout.setBackgroundColor(Color.parseColor("#CC000000"));
            mBinding.framelayout.setClickable(true); // 뒷 화면 터치 방지
            mBinding.framelayout.setFocusable(true);

            // 뒷 화면 터치시 토글 종료
            mBinding.framelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFab(0);
                }
            });


            mBinding.txtFabDelete.setVisibility(View.VISIBLE);

            mBinding.txtFabState.animate()
                    .translationY(standardSize_Y /3)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mBinding.txtFabState.setVisibility(View.VISIBLE);
                        }
                    });

            mBinding.fabState.animate()
                    .translationY(standardSize_Y /3);

            if(fabSave_Edit_status){
                mBinding.txtFabSave.setVisibility(View.VISIBLE);

                mBinding.txtFabSave.animate()
                        .translationY((standardSize_Y /3)*2)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                mBinding.txtFabSave.setVisibility(View.VISIBLE);
                            }
                        });
                mBinding.fabSave.animate()
                        .translationY((standardSize_Y /3)*2);
            } else {
                mBinding.txtFabEdit.setVisibility(View.VISIBLE);

                mBinding.txtFabEdit.animate()
                        .translationY((standardSize_Y /3)*2)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                mBinding.txtFabEdit.setVisibility(View.VISIBLE);
                            }
                        });

                mBinding.fabEdit.animate()
                        .translationY((standardSize_Y /3)*2);
            }

            mBinding.txtFabDelete.animate()
                    .translationY((standardSize_Y /3)*3)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mBinding.txtFabDelete.setVisibility(View.VISIBLE);
                        }
                    });

            mBinding.fabDelete.animate()
                    .translationY((standardSize_Y /3)*3);


//            ObjectAnimator fs_animation = ObjectAnimator.ofFloat(mBinding.fabState, "translationY", 150f);
//            fs_animation.start();
//
//            if(fabSave_Edit_status){
//                ObjectAnimator fS_animation = ObjectAnimator.ofFloat(mBinding.fabSave, "translationY", 300f);
//                fS_animation.start();
//            } else {
//                ObjectAnimator fe_animation = ObjectAnimator.ofFloat(mBinding.fabEdit, "translationY", 300f);
//                fe_animation.start();
//            }
//            ObjectAnimator fd_animation = ObjectAnimator.ofFloat(mBinding.fabDelete, "translationY", 450f);
//            fd_animation.start();
            // 메인 플로팅 이미지 변경
//            fabMain.setImageResource(R.drawable.ic_baseline_clear_24);

            // 플로팅 버튼 상태 변경
            btnMenu_status = !btnMenu_status;
        }
    }

    /**
     * 수정한 내용 문제없는지 체크
     */
    private void IssueDataCheck(){
        if(mBinding.txtIsueContent.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.enter_issue), Toast.LENGTH_SHORT).show();
            return;
        } else if(mBinding.editMemoImage.getText().toString().equals("") || mBinding.editMemoImage.getText().toString().equals(" ") || mBinding.editMemoImage.getText().toString().equals("#")){
            Toast.makeText(getApplicationContext(), getString(R.string.enter_hashtag), Toast.LENGTH_SHORT).show();
            return;
//        } else if(mBitmaps_saved.size() == 0){
        } else if(mUrls_saved.size() == 11){
            Toast.makeText(getApplicationContext(), getString(R.string.add_picture, MAX), Toast.LENGTH_SHORT).show();
            return;
        } else{
            viewissueSave();
        }
    }

    /**
     * 이슈 이미지 복원
     */
    public void issueImageRevert() {
        Call<ResponseBody> call;
        call = Common.sService_site.issueImageRevert(issue_id, mUrls_TempSaved, mSavedImageList, Common.sToken);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        String result = response.body().string();
                        Log.v("issueImageRevert: ", "issueImageRevert result = " + result);
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - issueImageRevert");
                        if (!IssueManagementDetailActivity.this.isFinishing())
                            Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if(code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementDetailActivity.this, checkDialogClickListener);
                }
                else {
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    /**
     * 이슈 게시글 업데이트
     */
    public void issueUpdate() {
        // 필요한 정보들 (게시글 id, 수정자 id, 내용, 해시태그), 사진은 따로 저장됩니다. 2021.12.30 기준
        hashtag_ = ""; // 다시 초기화 안하면 꼬입니다~
        String title = mBinding.txtTitle.getText().toString();
        content = mBinding.txtIsueContent.getText().toString();
        if(!mBinding.editMemoImage.getText().toString().equals("")){
            Log.v("HashTagLog_mBinding: ", mBinding.editMemoImage.getText().toString());
            String[] hashtag = mBinding.editMemoImage.getText().toString().split("#"); // # 제거
            Log.v("HashTagLog_hashtag[]: ", hashtag+"");
            for(int i = 1; i < hashtag.length; i++){
                if(i == hashtag.length - 1){
                    hashtag_ += hashtag[i];
                    continue;
                }
                hashtag_ += hashtag[i].substring(0, hashtag[i].length()-1) + ","; // 마지막 문자 제거 공백임 쉼표는 필요없는듯
                Log.v("HashTagLog_hashtag_: ", hashtag_+"");
            }
//            hashtag_ = hashtag_.substring(0, hashtag_.length()-1); // 마지막 , 는 필요없습니다.
            Log.v("HashTagLog_hashtag_List: ", hashtag_+"");
        }

//        int issue_state = mBinding.spinner.getSelectedItemPosition() + 1;

        Call<ResponseBody> call;
        call = Common.sService_site.updateissue(issue_id, title, content, hashtag_, ISSUE_SAVE_STATE, "", "", Common.sToken);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    try {
                        mUrls_TempSaved = mSavedImageList;
                        String result = response.body().string();
                        Log.v("IssueUpdate: ", "employeeJoin result = " + result);
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - IssueUpdate");
                        if (!IssueManagementDetailActivity.this.isFinishing())
                            Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if(code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementDetailActivity.this, checkDialogClickListener);
                }
                else {
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    /**
     * 이슈 게시글 삭제
     */
    private void issueDelete() {
        // 필요한 정보들 (게시글 id, 수정자 id, 내용, 해시태그), 사진은 따로 저장됩니다. 2021.12.30 기준
        Call<ResponseBody> call;
        call = Common.sService_site.deleteissue(issue_id, mSavedImageList, Common.sToken);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                } else if(code == 401){
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementDetailActivity.this, checkDialogClickListener);

                } else {
                    if (!IssueManagementDetailActivity.this.isFinishing())
                        Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementDetailActivity.this.isFinishing())
                    Common.showDialog(IssueManagementDetailActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }

    /**
     * 수정중일때 화면 벗어남 경고 다이얼 로그
     */
    private void NoteEditDataResetDialog(){
        DeleteBottomSheetDialog deleteCheckBottomSheetDialog = new DeleteBottomSheetDialog(noteEditDataResetClickListener, getString(R.string.note), getString(R.string.EditDataReset_Note));

        deleteCheckBottomSheetDialog.show(getSupportFragmentManager(), "deleteCheckBottomSheetDialog");
    }

    // 이슈글 삭제 clicklistener
    DeleteBottomSheetClickListener noteEditDataResetClickListener = new DeleteBottomSheetClickListener() {
        @Override
        public void onDeleteClick() {
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }

        @Override
        public void onCancelClick() {
            // 자동으로 dismiss
        }
    };

    @Override
    public void onBackPressed() {
        if(fabSave_Edit_status){
            NoteEditDataResetDialog();
        } else {
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    }

    @Override protected void onDestroy(){
        if(fabSave_Edit_status)
            issueImageRevert();
        //예외처리
        // adapter가 null이 아니라면 bitmap recycle 하기
//        if(adapter != null){
//            adapter.recycleBitmap();
//            mBitmaps_saved.clear();
//        }

        super.onDestroy();

        mBinding = null;
    }

}
