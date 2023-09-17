package com.gbsoft.ellosseum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.gbsoft.ellosseum.databinding.ActivityIsuemanagementAddBinding;
import com.github.chrisbanes.photoview.PhotoView;

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
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IssueManagementAddActivity extends AppCompatActivity {

//    private ActivityIsuemanagementDetailBinding mBinding;
    private ActivityIsuemanagementAddBinding mBinding;

    private static final String TAG = "IssueManagementAddActivity";


    private static final String TAG_ERR = "ErrorMessage";    // LOGTAG : 로그 태그
    private static final String LOG_TAG2 = "SuccessMessage";    // LOGTAG : 로그 태그
//    private static final int MAX = 10;   // 사진 최대 갯수
    private static final int MAX = 10;   // 사진 최대 갯수, 단말기 메모리 한계 문제로 조정
    private static final int MAX_GALLERY = 10;   // 갤러리 최대 선택 갯수


    private static final int REQUEST_CODE = 100;  // 갤러리 Intent 반환 코드
    private static final int REQUEST_TAKE_PHOTO = 200;    // 카메라 촬영 Intent 반환 코드

    // UI 객체
    PhotoView photo_detail;
    RecyclerView recyclerView;

    // Adapter
    private RequestImageListAdapter adapter;    // recyclerView 어뎁터

    private String mNewImageList = "";  // 새로운 이미지 리스트
    private String mCurrentPhotoPath;   // 촬영한 이미지 경로
    private String mSavedImageList = "";  // 저장된 이미지 리스트
    private File mImage; // 카메라 촬영 이미지 저장

    // 미리 저장합시다 인적정보
    private int issue_id; // 해당 게시물의 index
    private String title = ""; // 게시물의 제목
    private String content = ""; // 게시물의 내용
    private String hashtag_ = ""; // 게시물의 해시태그

    private Boolean add_flag = false; // 강제 종료 되었을 때는 글을 삭제


    private ArrayList<String> mUploadFilePath;   // 업로드할 파일 실제 경로, 파일명 리스트
    private ArrayList<Bitmap> mBitmaps_saved;   // 저장된 이미지 리스트
    private ArrayList<String> mUrls_saved; // 저장된 이미지 이름 리스트
    private ArrayList<String> mUrls_new; // 등록할 이미지 이름 리스트
    private ArrayList<Bitmap> mBitmaps_new; // 등록할 이미지 리스트

    private String mServerPath = "";

    private RecyclerView.LayoutManager layoutManager;

    private Animation myAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mBinding = ActivityIsuemanagementDetailBinding.inflate(getLayoutInflater());
        mBinding = ActivityIsuemanagementAddBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialSet() {
        Intent intent = getIntent();
        issue_id = intent.getIntExtra("id", -1);
        SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
        mServerPath = sharedPreferences.getString("siteLink", "");

        if(issue_id == -1) finish();


        // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG2, "권한 설정 완료");
            } else {
                Log.v(LOG_TAG2, "권한 설정 요청");
                ActivityCompat.requestPermissions(IssueManagementAddActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        mBinding.btnBack.setOnClickListener(backClick);
        mBinding.btnSave.setOnClickListener(saveclick);
        mBinding.imgviewAddbtn.setOnClickListener(imgAddClick);
        mBinding.imgviewAddbtn.setClickable(true);

        mBinding.btnSave.setEnabled(false);

        mBinding.editTitle.addTextChangedListener(textWatcher);
        mBinding.editMemoImage.addTextChangedListener(textWatcher);
        mBinding.editIsueContent.addTextChangedListener(textWatcher);

        mBinding.btnSave.setOnTouchListener(saveTouch);

//        photo_detail = (PhotoView) findViewById(R.id.pv_detail);
        viewSettingInit();
        valueSettingInit();

        getImgName();


//        WebSetting setting = new WebSetting(IsueManagementDetailActivity.this);
//        WebView webView = setting.setWebSettings(mBinding.webView);
//        webView.loadData(Common.sNoticeDTOs.get(i).getContent(), "text/html; charset=utf-8", "UTF-8");

    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener saveTouch = (v, event) -> {
        if (mBinding.btnSave.isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP :
                    mBinding.btnSave.setTextColor(getColor(R.color.default_red));
                    break;
                case MotionEvent.ACTION_DOWN :
                    mBinding.btnSave.setTextColor(getColor(R.color.dark_red));
                    break;
            }
        }
        return false;
    };


    /**
     * view 초기설정 (onCreate -> initialSet에서 호출)
     */
    private void viewSettingInit(){
        // recyclerview에 image view가 순차적으로 들어감
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        Glide.with(this)
                .load(R.drawable.ic_plus)
                .into(mBinding.imgviewAdd);

        // animation 추가
        myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
    }

    /**
     * value 초기 설정 (onCreate -> initialSet에서 호출)
     */
    private void valueSettingInit(){
        // 객체생성
        mUploadFilePath = new ArrayList<>();

        // 기존에 저장되어있던 이미지들(Bitmap)
        mBitmaps_saved = new ArrayList<>();

        // 새롭게 추가된 이미지들(Bitmap)
        mBitmaps_new = new ArrayList<>();

        // 기존에 저장되어있던 이미지이름들(String)
        mUrls_saved = new ArrayList<>();

        // 새로 추가할 이미지이름들(String)
        mUrls_new = new ArrayList<>();
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
//                            new imageShow(IssueManagementAddActivity.this).execute();
                            imageShow();
                            return;
                        } else {
                            // 데이터 베이스에 저장되어있는 이미지 이름들을 가져옴 Ex_1) 1_1,1_2,1_3  Ex_2) 2_3,2_12,2_22
                            mSavedImageList = jsonObject.getString("imgName");
                            new imageCheck(IssueManagementAddActivity.this).execute();
                        }
                    } catch (JSONException e) {
                        Log.e(Common.TAG_ERR, "ERROR: JSON Parsing Error - getImgName");
                        if (!IssueManagementAddActivity.this.isFinishing())
                            Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - getImgName");
                        if (!IssueManagementAddActivity.this.isFinishing())
                            Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                }
                else if (code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementAddActivity.this, checkDialogClickListener);

                } else {
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementAddActivity.this.isFinishing())
                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
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
//        viewForcusRemove();

        // 현재 저장된 이미지들 adapter에 담고 RecyclerView와 adapter 연결
        layoutManager = new LinearLayoutManager(IssueManagementAddActivity.this, LinearLayoutManager.VERTICAL, false){
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                int childCount = getChildCount(); //지금 recyclerView 영역에 보이고 있는 아이템의 갯수
                int itemCount = getItemCount(); //전체 갯수

                for (int childCnt = recyclerView.getItemDecorationCount() + recyclerView.getChildCount(), i = recyclerView.getItemDecorationCount(); i < childCnt; ++i) {
                    final RequestImageListAdapter.ViewHolder holder = (RequestImageListAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                    holder.linearLayout.setVisibility(View.VISIBLE);

                    // 그림자 추가
                    holder.imageView.setElevation(1f);
                    holder.mDelete_Img.setElevation(2f);

                    // 애니메이션 추가
                    holder.linearLayout.startAnimation(myAnim);
                }
            }
        };
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RequestImageListAdapter(mUrls_saved, mServerPath);
        adapter.setOnItemClickListener(new RequestImageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                // 현재는 눌러도 의미없음
            }
        });

        adapter.setOnLongItemClickListener(new RequestImageListAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(int pos) {

                // 수정 모드일때만 활성화
//                if(popupMenu_activity){
                    // 이미지 삭제 다이얼로그
                    imageDeleteAlertShow(pos);
//                }
            }
        });

        adapter.setOnDeleteItemClickListener(new RequestImageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                // 이미지 삭제 다이얼로그
                imageDeleteAlertShow(pos);
            }
        });

        recyclerView.setAdapter(adapter);
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
        DeleteBottomSheetClickListener issue_ImageDeleteClickListener = new DeleteBottomSheetClickListener() {
            @Override
            public void onDeleteClick() {
                String[] list = mSavedImageList.split(",");
                String str_image_list = "";
                int list_len = list.length;
                for (int i = 0; i < list_len; i++) {
                    if (i == pos) continue;
                    else
                        str_image_list += (str_image_list.equals("")) ? list[i] : "," + list[i];
                }
                try {
                    adapter.removeAt(pos);
                    deleteImage(list[pos], str_image_list);
                } catch (MalformedURLException e) {
                    Log.e(Common.TAG_ERR, "error MalformedURLException imageDeleteAlertShow()");
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
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
                            //Toast.makeText(IssueManagementAddActivity.this, result, Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(IssueManagementAddActivity.this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                            mSavedImageList = delete_after_image_list;
                            getImgName();
                        }
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "error IOException deleteImage()");
                        if (!IssueManagementAddActivity.this.isFinishing())
                            Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if (code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementAddActivity.this, checkDialogClickListener);

                } else {
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementAddActivity.this.isFinishing())
                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
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
                if (!IssueManagementAddActivity.this.isFinishing())
                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
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
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
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
                Log.e(Common.TAG_ERR, "error imageCheck class Exception doInBackground");
                if (!IssueManagementAddActivity.this.isFinishing())
                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
//                new imageShow(IssueManagementAddActivity.this).execute();
                imageShow();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.err_pic_load), Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    /**
     * 선택한 사진 보이기
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
//                            tempPathInit(uri)
                            ;galleryBitmap(uri);
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
                if (!IssueManagementAddActivity.this.isFinishing())
                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){ // 카메라

            Glide.with(this)
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
                                Log.e(Common.TAG_ERR, "error MatlformedURLException onActivityResult");
                                if (!IssueManagementAddActivity.this.isFinishing())
                                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
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
//                Bitmap result = imageController.getInstance().getRotateBitmap(bitmap, mCurrentPhotoPath);
//                mBitmaps_new.add(result);
//                mUploadFilePath.add(mCurrentPhotoPath);
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
        Glide.with(this)
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
                            Log.e(Common.TAG_ERR, "error MatlformedURLException galleryBitmap uri");
                            if (!IssueManagementAddActivity.this.isFinishing())
                                Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                        }
                        return false;
                    }
                }).submit();
    }

    /**
     * 갤러리에서 여러장 가져왔을때
     */
    private void galleryBitmap(Uri uri, int count){
        Glide.with(this)
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
                                Log.e(Common.TAG_ERR, "error MatlformedURLException galleryBitmap rui count");
                                if (!IssueManagementAddActivity.this.isFinishing())
                                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
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
            Log.e(Common.TAG_ERR, "error MatlformedURLException tempPathInit");
            if (!IssueManagementAddActivity.this.isFinishing())
                Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
        }
    }

    /**
     * 서버에 이미지 업로드
     */
    private void uploadImage() throws MalformedURLException {

        ArrayList<MultipartBody.Part> files = new ArrayList<>();

        int newItemSize = mBitmaps_new.size();
        int lastItemNum;
        if (mSavedImageList.equals("")) {
            lastItemNum = 1;
        } else {
            String[] a = mSavedImageList.split(",", -1);
            String[] b = a[a.length - 1].split("_");
            String c = b[b.length - 1];
            lastItemNum = (mSavedImageList.equals("")) ? 1 : Integer.parseInt(c) + 1;
        }


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

        RequestBody requestBody_issue_id = RequestBody.create(issue_id+"", MultipartBody.FORM);
        RequestBody token = RequestBody.create(Common.sToken, MultipartBody.FORM);

        RequestBody imgName_list = RequestBody.create((mSavedImageList.equals("")) ? mNewImageList : mSavedImageList + "," + mNewImageList, MultipartBody.FORM);
        Call<ResponseBody> call = Common.sService_site.uploadImages(requestBody_issue_id, files, imgName_list, token);

        call.enqueue(new Callback<ResponseBody>() {     // 이미지 파일 서버에 전송
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {  // 응답 성공
                int code = response.code();
                if (response.isSuccessful() && code == 200) {
                    try {
                        String result = response.body().string();
                        if (result.equals("multer error") || result.equals("unknown error")) {
                            mNewImageList = "";
                            // Toast.makeText(IssueManagementAddActivity.this, "error\n: " + result, Toast.LENGTH_SHORT).show();
                        } else {
                            // Toast.makeText(IssueManagementAddActivity.this, getString(R.string.registered), Toast.LENGTH_SHORT).show();
                            mSavedImageList += (mSavedImageList.equals("")) ? mNewImageList : "," + mNewImageList;
                            getImgName();
                        }
                    } catch (IOException e) {
                        Log.e(TAG_ERR, "ERROR: IOException");
                        if (!IssueManagementAddActivity.this.isFinishing())
                            Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if (code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementAddActivity.this, checkDialogClickListener);

                } else {
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementAddActivity.this.isFinishing())
                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
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

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            issueEditCheck();
        };

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            issueEditCheck();
        }

        @Override
        public void afterTextChanged(Editable s) {
            issueEditCheck();
        }
    };

    private void issueEditCheck(){
        if(mBinding.editTitle.getText().toString().equals("") || mBinding.editMemoImage.getText().toString().equals("") || mBinding.editIsueContent.getText().toString().equals("")){
            mBinding.btnSave.setTextColor(getColor(R.color.light_red));
            mBinding.btnSave.setEnabled(false);
        } else {
            mBinding.btnSave.setTextColor(getColor(R.color.default_red));
            mBinding.btnSave.setEnabled(true);
        }
    }

    // 로그아웃 다이얼로그 버튼클릭 리스너 설정
    Img_ArDialogClickListener Img_ArDialogClickListener = new Img_ArDialogClickListener() {
        @Override
        public void onImgClick() {
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

    // 카메라 이미지 클릭 (사진 추가)
    View.OnClickListener imgAddClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showImgArDialog();
        }
    };

    // 저장 버튼 클릭
    View.OnClickListener saveclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IssueDataCheck();
        }
    };

    // 뒤로가기 버튼 클릭
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

    /**
     * 입력한 내용 문제없는지 체크
     */
    private void IssueDataCheck(){
        if(mBinding.editTitle.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.enter_issue_title), Toast.LENGTH_SHORT).show();
            return;
        } else if(mBinding.editIsueContent.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.enter_issue), Toast.LENGTH_SHORT).show();
            return;
        } else if(mBinding.editMemoImage.getText().toString().equals("") || mBinding.editMemoImage.getText().toString().equals(" ") || mBinding.editMemoImage.getText().toString().equals("#")){
            Toast.makeText(getApplicationContext(), getString(R.string.enter_hashtag), Toast.LENGTH_SHORT).show();
            return;
        } else if(mUrls_saved.size() > 10){
            Toast.makeText(getApplicationContext(), getString(R.string.add_picture, MAX), Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            issueInsert();
        }
    }


    /**
     * 이슈 게시글 추가
     */
    private void issueInsert() {
        // 필요한 정보들 (게시글 id, 수정자 id, 내용, 해시태그), 사진은 따로 저장됩니다. 2021.12.30 기준

        hashtag_ = ""; // 다시 초기화 안하면 꼬입니다~
        title = mBinding.editTitle.getText().toString();
        content = mBinding.editIsueContent.getText().toString();
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
        }

        Common.startLocationService(getApplicationContext(), Common.isLocationServiceRunning(IssueManagementAddActivity.this)); // GPS 서비스 실행
        Common.sIssueGpsHandler = mIssueGpsHandler;
    }

    /**
     * 이슈 글 저장시 현재 경도, 위도 정보 저장
     */
    private final Handler mIssueGpsHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 2) {
                SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
                String lat = sharedPreferences.getString("lat", "");
                String lon = sharedPreferences.getString("lon", "");

                Call<ResponseBody> call;
                call = Common.sService_site.updateissue(issue_id, title, content, hashtag_, 1, lat, lon, Common.sToken);
                call.enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        int code = response.code();
                        if (code == 200) {
                            try {
                                String result = response.body().string();
                                add_flag = true;

                                // handler 삭제
                                Common.sIssueGpsHandler = null;
                                Common.stopLocationService(IssueManagementAddActivity.this, Common.isLocationServiceRunning(IssueManagementAddActivity.this));
                                finish(); // 추후 변경 예정, detail 이동
                            } catch (IOException e) {
                                Log.e(Common.TAG_ERR, "ERROR: IOException - IssueUpdate");
                                if (!IssueManagementAddActivity.this.isFinishing())
                                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                            }
                        } else if (code == 401){
                            Log.v(TAG, "INVALID_ACCESS_TOKEN");
                            if (!IssueManagementAddActivity.this.isFinishing())
                                Common.tokencheckDialog(IssueManagementAddActivity.this, checkDialogClickListener);
                        }
                        else {
                            if (!IssueManagementAddActivity.this.isFinishing())
                                Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if (!IssueManagementAddActivity.this.isFinishing())
                            Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
                    }
                });
            }
        }
    };

    @Override
    public void onBackPressed() {
        DeleteBottomSheetClickListener noteEditDataResetClickListener = new DeleteBottomSheetClickListener() {
            @Override
            public void onDeleteClick() {
                issueDelete();
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }

            @Override
            public void onCancelClick() {
                // 자동으로 dismiss
            }
        };

        DeleteBottomSheetDialog deleteCheckBottomSheetDialog = new DeleteBottomSheetDialog(noteEditDataResetClickListener, getString(R.string.note), getString(R.string.insertDataReset_Note));

        deleteCheckBottomSheetDialog.show(getSupportFragmentManager(), "deleteCheckBottomSheetDialog");
    }

    @Override protected void onDestroy(){

        // 어플이 저장되기전 강제종료되면 글 삭제
        if(!add_flag)
            issueDelete();

//        //예외처리
//        if(adapter != null){
//            adapter.recycleBitmap();
//            mBitmaps_saved.clear();
//        }
        super.onDestroy();

        mBinding = null;
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
                    try {
                        String result = response.body().string();

                        finish();
                    } catch (IOException e) {
                        Log.e(Common.TAG_ERR, "ERROR: IOException - IssueUpdate");
                        if (!IssueManagementAddActivity.this.isFinishing())
                            Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_catch_error_content), () -> { });
                    }
                } else if (code == 401) {
                    Log.v(TAG, "INVALID_ACCESS_TOKEN");
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.tokencheckDialog(IssueManagementAddActivity.this, checkDialogClickListener);

                } else {
                    if (!IssueManagementAddActivity.this.isFinishing())
                        Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_response_error_content), () -> { });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!IssueManagementAddActivity.this.isFinishing())
                    Common.showDialog(IssueManagementAddActivity.this, getString(R.string.dialog_error_title), getString(R.string.dialog_connect_error_content), () -> { });
            }
        });
    }
}
