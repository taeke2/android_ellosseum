package com.gbsoft.ellosseum;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // ====================================================================================================================================================================================  auth.js
    // 현장 리스트 조회
    @GET("/app-auth/getSiteList_")
    Call<ResponseBody> getSiteList_();

    // 토큰 재발급
    @FormUrlEncoded
    @POST("/app-auth/getNewToken_")
    Call<ResponseBody> getNewToken_(@Field("token") String token);

    // 대여폰 확인
    @GET("/app-auth/serialNumberCheck")
    Call<ResponseBody> serialNumberCheck(@Query("serialNumber") String serialNumber);

    // ====================================================================================================================================================================================  join.js
    // 승인코드 확인
    @GET("/app-join/authCheck")
    Call<ResponseBody> authCheck(@Query("inputCode") String inputCode);

    // 대여폰
    @FormUrlEncoded
    @POST("/app-join/rentAccept")
    Call<ResponseBody> rentAccept(@Field("employee_id") int employeeId, @Field("rent_id") int rentId);

    // 소속 파트너사 리스트
    @GET("/app-join/getSubcontractorList")
    Call<ResponseBody> getSubcontractorList();

    // 공통 코드 리스트
    @GET("/app-join/getCommonData")
    Call<ResponseBody> getCommonData(@Query("id") int id);

    // 근로자 회원가입 승인
    @FormUrlEncoded
    @POST("/app-join/employeeJoin")
    Call<ResponseBody> employeeJoin_(@Field("type") int type, @Field("name") String name, @Field("birth") String birth,
                                     @Field("bloodType") int blood, @Field("tel") String tel, @Field("address") String address,
                                     @Field("subcontractor_id") int subcontractor_id, @Field("occupation") int occupation, @Field("state") int state,
                                     @Field("remark") String remark, @Field("createAt") String createAt, @Field("vaccineYn") String vaccineYn,
                                     @Field("equipment_name") String equipment_name, @Field("equipment_type") int equipment_type,
                                     @Field("equipment_use") String equipment_use, @Field("site_id") int site_id);

    // 근로자 회원 정보 수정
    @FormUrlEncoded
    @PUT("/app-join/employeeUpdate/{id}")
    Call<ResponseBody> employeeUpdate_(@Path("id") int id, @Field("type") int type, @Field("name") String name, @Field("birth") String birth,
                                       @Field("bloodType") int blood, @Field("tel") String tel, @Field("address") String address,
                                       @Field("subcontractor_id") int subcontractor_id, @Field("occupation") int occupation, @Field("state") int state,
                                       @Field("remark") String remark, @Field("createAt") String createAt, @Field("vaccineYn") String vaccineYn,
                                       @Field("equipment_name") String equipment_name, @Field("equipment_type") int equipment_type,
                                       @Field("equipment_use") String equipment_use);

    // 승인 유무 확인
    @GET("/app-join/getApproveYn")
    Call<ResponseBody> getApproveYn_(@Query("id") int id);

    // 근로자 회원가입시 토큰 생성
    @GET("/app-join/getEmployeeToken")
    Call<ResponseBody> getEmployeeToken(@Query("id") int id, @Query("auth") int auth);

    // 사원 정보 조회
    @FormUrlEncoded
    @POST("/app-join/getEmployeeInfo")
    Call<ResponseBody> getEmployeeInfo(@Field("id") int id, @Field("token") String token);

    // 근로자 회원가입 승인
    @FormUrlEncoded
    @PUT("/app-join/setEmployeeAccess/{id}")
    Call<ResponseBody> setEmployeeAccess(@Path("id") int id, @Field("token") String token);

    // 대여자 이름 확인
    @GET("/app-join/checkRentEmployeeName")
    Call<ResponseBody> checkRentEmployeeName(@Query("id") int id, @Query("name") String name);

    // ====================================================================================================================================================================================  login.js
    // 근로자 로그인
    @FormUrlEncoded
    @POST("/app-login/employeeSignIn")
    Call<ResponseBody> employeeLogin(@Field("id") String id, @Field("pw") String pw, @Field("auth") int auth);

    // 근로자 로그인 시간 업데이트
    @FormUrlEncoded
    @PUT("/app-login/setLastLoginTime/{id}")
    Call<ResponseBody> setLastLoginTime_(@Path("id") String id, @Field("dateTime") String dateTime, @Field("token") String token);

    // 대여자 로그인
    @FormUrlEncoded
    @POST("/app-login/rentEmployeeLogin")
    Call<ResponseBody> rentEmployeeLogin(@Field("id") String id, @Field("pw") String pw, @Field("rentId") int rentId);

    // ====================================================================================================================================================================================  login.js (with WEB)
    // 관리자 로그인(현장id return)
    // 추후 더블디한테 수정사항 전달 후 signIn2 -> signIn으로 변경 (adminSignIn과 통합 후)
    @FormUrlEncoded
    @POST("/login/adminSignIn")
    Call<ResponseBody> adminSignIn(@Field("id") String id, @Field("pw") String pw);

//    // 관리자 로그인
//    @FormUrlEncoded
//    @POST("/login/signIn_")
//    Call<ResponseBody> adminLogin(@Field("id") String id, @Field("pw") String pw);

    // 아이디 찾기
    @GET("/login/find_id")
    Call<ResponseBody> findId(@Query("name") String name, @Query("email") String email);

    // 비밀번호 찾기
    @GET("/login/find_pw")
    Call<ResponseBody> findPw(@Query("name") String name, @Query("email") String email, @Query("id") String id);

    // ====================================================================================================================================================================================  main.js
    // FCM 토큰 업데이트
    @FormUrlEncoded
    @POST("/app-main/uniqueIdUpdate_")
    Call<ResponseBody> uniqueIdUpdate_(@Field("unique_id") String unique_id, @Field("date") String date, @Field("token") String token);

    // 위치 정보 업데이트
    @FormUrlEncoded
    @POST("/app-main/employeeUpdateLocation_")
    Call<ResponseBody> employeeUpdateLocation(@Field("employee_id") int employee_id, @Field("latitude") String latitude,
                                              @Field("longitude") String longitude, @Field("date") String date,
                                              @Field("site_id") int site_id);

    // 근로자 이름 조회
    @FormUrlEncoded
    @POST("/app-main/getEmployeeName_")
    Call<ResponseBody> getEmployeeName_(@Field("token") String token);

    // 관리자 이름 조회
    @FormUrlEncoded
    @POST("/app-main/getAdminName_")
    Call<ResponseBody> getAdminName_(@Field("token") String token);

    // 대여폰 logout 시 usingPhoneYn -> 0 으로 update
    @FormUrlEncoded
    @PUT("/app-main/rentPhoneLogout/{rentId}")
    Call<ResponseBody> rentPhoneLogout(@Path("rentId") int rentId, @Field("token") String token);

    // ====================================================================================================================================================================================  map.js
    // 근로자 마지막 위치 정보 조회
    @GET("/app-map/getEmployeeLocation")
    Call<ResponseBody> getEmployeeLocation(@Query("employee_id") String employee_id);

    // ====================================================================================================================================================================================  fcm.js
    // 알림 요청 로그 저장
    @FormUrlEncoded
    @POST("/fcm/emergencyAlarm")
    Call<ResponseBody> emergencyAlarm(@Field("employee_id") String employee_id, @Field("date") String date,
                                      @Field("state") int state, @Field("remark") String remark, @Field("area_id") int areaId);

    // 알림메세지 보내기
    @GET("/fcm/sosMessageAll")
    Call<ResponseBody> sosMessage(@Query("id") String id);

    // ====================================================================================================================================================================================  attendance.js
    // 근태현황 리스트
    @FormUrlEncoded
    @POST("/app-attendance/attendanceList")
    Call<ResponseBody> attendanceList(@Field("startDate") String startDate, @Field("endDate") String endDate, @Field("token") String token);

    // ====================================================================================================================================================================================  notice.js
    // 공지사항 리스트 조회
    @FormUrlEncoded
    @POST("/app-notice/noticeList_")
    Call<ResponseBody> noticeList(@Field("searchText") String searchText, @Field("token") String token);

    // ====================================================================================================================================================================================  remedy.js
    // 현장 신문고 등록
    @FormUrlEncoded
    @POST("/app-remedy/inputRemedy_")
    Call<ResponseBody> inputRemedy(@Field("title") String title, @Field("content") String content, @Field("createDate") String createDate,
                                   @Field("site_id") int siteCode, @Field("token") String token);

    // ====================================================================================================================================================================================  issueManagement.js
    // 이슈 리스트 조회

    /**
     * issue 조회
     */
    @FormUrlEncoded
    @POST("/app-issue/getissue")
    Call<ResponseBody> getissue(@Field("searchText") String searchText, @Field("token") String token);

    /**
     * issue hashtag로 조회
     */
    @FormUrlEncoded
    @POST("/app-issue/hashTagList")
    Call<ResponseBody> gethashtag_issue(@Field("hashTag") String hashTag, @Field("token") String token);

    /**
     * issue 추가 후 수정 (api 추후 개선 예정)
     */
    @FormUrlEncoded
    @POST("/app-issue/setissue")
    Call<ResponseBody> setissue(@Field("token") String token);

    /**
     * issue 수정
     */
    @FormUrlEncoded
    @POST("/app-issue/updateissue")
    Call<ResponseBody> updateissue(@Field("issue_id") int issue_id, @Field("title") String title, @Field("content") String content, @Field("hashtag") String hashtag, @Field("issue_state") int issue_state, @Field("lat") String lat, @Field("lon") String lon, @Field("token") String token);

    /**
     * issue 삭제
     */
    @FormUrlEncoded
    @POST("/app-issue/deleteissue")
    Call<ResponseBody> deleteissue(@Field("issue_id") int issue_id, @Field("mSavedImageList") String mSavedImageList, @Field("token") String token);

    /**
     * issue state 변경
     */
    @FormUrlEncoded
    @POST("/app-issue/updateIssueState")
    Call<ResponseBody> updateIssueState(@Field("issue_id") int issue_id, @Field("ISSUE_CURRENT_STATE") int ISSUE_CURRENT_STATE, @Field("token") String token);


    // 이미지명 조회
    /**
     * 이미지명 조회
     */
    @FormUrlEncoded
    @POST("/app-issue/getImgName")
    Call<ResponseBody> getImgName(@Field("issue_id") int issue_id, @Field("token") String token);

    /**
     * 이미지 조회
     */
//    @FormUrlEncoded
//    @POST("/app-issue/getimages")
//    @Streaming
//    Call<ResponseBody> getimages(@Field("images") String[] images, @Field("token") String token);

    /**
     * 이미지명 조회
     */
    @FormUrlEncoded
    @POST("/app-issue/imgRevert")
    Call<ResponseBody> issueImageRevert(@Field("issue_id") int issue_id, @Field("revertImageList") String imageRevertList, @Field("newImageList") String newImageList, @Field("token") String token);

    /**
     * 이미지 여러장 업로드
     */
    @Multipart
    @POST("/app-issue/uploadImages")
    Call<ResponseBody> uploadImages(@Part("issue_id") RequestBody issue_id, @Part List<MultipartBody.Part> photo, @Part("imgName_list") RequestBody imgName_list, @Part("token") RequestBody token);

    /**
     * 이미지 삭제
     */
    @FormUrlEncoded
    @POST("/app-issue/deleteImage")
    Call<ResponseBody> deleteImage(@Field("issue_id") int issue_id, @Field("delete_imageName") String delete_imageName,  @Field("delete_after_image_list") String delete_after_image_list, @Field("token") String token);


    // ======================================
    // 수정전 API (더블디 적용 API)
    // ======================================

    // =============================================================================================================================================== 승인코드 ~ 회원가입, 로그인 (임시 토큰)
    // 승인코드 확인
    @FormUrlEncoded
    @POST("/APP_auth_temp/authCheck")
    Call<ResponseBody> authCheck(@Field("inputCode") String inputCode, @Field("temp_token") String tempToken);

    // 소속 파트너사 리스트
    @FormUrlEncoded
    @POST("/APP_auth_temp/getSubcontractorList")
    Call<ResponseBody> getSubcontractorList(@Field("temp_token") String tempToken);

    // 현장 리스트
    @FormUrlEncoded
    @POST("/APP_auth/getSiteList")
    Call<ResponseBody> getSiteList(@Field("temp_token") String tempToken);

    // 공통 코드 리스트
    @FormUrlEncoded
    @POST("/APP_auth_temp/getCommonData")
    Call<ResponseBody> getCommonData(@Field("id") int id, @Field("temp_token") String tempToken);

    // 근로자 회원가입 승인
    @FormUrlEncoded
    @POST("/APP_auth_temp/employeeJoin")
    Call<ResponseBody> employeeJoin(@Field("type") int type, @Field("name") String name, @Field("birth") String birth,
                                    @Field("bloodType") int blood, @Field("tel") String tel, @Field("address") String address,
                                    @Field("subcontractor_id") int subcontractor_id, @Field("occupation") int occupation, @Field("state") int state,
                                    @Field("remark") String remark, @Field("createAt") String createAt, @Field("vaccineYn") String vaccineYn,
                                    @Field("equipment_name") String equipment_name, @Field("equipment_type") int equipment_type, @Field("equipment_use") String equipment_use,
                                    @Field("site_id") int site_id, @Field("temp_token") String tempToken);

    // 근로자 회원 정보 수정
    @FormUrlEncoded
    @POST("/APP_auth_temp/employeeUpdate")
    Call<ResponseBody> employeeUpdate(@Field("id") int id, @Field("type") int type, @Field("name") String name, @Field("birth") String birth,
                                      @Field("bloodType") int blood, @Field("tel") String tel, @Field("address") String address,
                                      @Field("subcontractor_id") int subcontractor_id, @Field("occupation") int occupation, @Field("state") int state,
                                      @Field("remark") String remark, @Field("createAt") String createAt, @Field("vaccineYn") String vaccineYn,
                                      @Field("equipment_name") String equipment_name, @Field("equipment_type") int equipment_type, @Field("equipment_use") String equipment_use,
                                      @Field("temp_token") String tempToken);

    // 근로자 로그인
    @FormUrlEncoded
    @POST("/APP_auth_temp/employeeSignIn")
    Call<ResponseBody> employeeLogin(@Field("id") String id, @Field("pw") String pw, @Field("temp_token") String temp_token, @Field("auth") int auth);

    // 관리자 로그인
    @FormUrlEncoded
    @POST("/login/signin2")
    Call<ResponseBody> adminLogin(@Field("id") String id, @Field("pw") String pw, @Field("temp_token") String tempToken);

    // 근로자 장비 저장
    @FormUrlEncoded
    @POST("/APP_auth_temp/setEquipment")
    Call<ResponseBody> setEquipment(@Field("id") int id, @Field("employee_id") int employee_id, @Field("equipment_name") String equipment_name, @Field("equipment_use") String equipment_use, @Field("equipment_type") int equipment_type, @Field("state") int state, @Field("temp_token") String token);

    // 근로자 회원가입시 토큰 생성
    @FormUrlEncoded
    @POST("/APP_auth_temp/employeeToken")
    Call<ResponseBody> employeeToken(@Field("id") int id, @Field("auth") int auth, @Field("temp_token") String tempToken);

    // =============================================================================================================================================== 승인코드 ~ 회원가입, 로그인

    // 토큰 재발급
    @FormUrlEncoded
    @POST("/APP_auth/getNewToken")
    Call<ResponseBody> getNewToken(@Field("id") String id, @Field("token") String token, @Field("auth") int auth);

    // 승인 유무 확인
    @FormUrlEncoded
    @POST("/APP_auth/getApproveYn")
    Call<ResponseBody> getApproveYn(@Field("id") int id);

    // 사원 정보 조회
    @FormUrlEncoded
    @POST("/APP_auth/getEmployeeInfo")
    Call<ResponseBody> getEmployeeInfo(@Field("id") int id, @Field("manager_id") String managerId, @Field("token") String token, @Field("auth") int auth);

    // 근로자 회원가입 승인
    @FormUrlEncoded
    @POST("/APP_auth/setEmployeeAccess")
    Call<ResponseBody> setEmployeeAccess(@Field("id") int id, @Field("manager_id") String managerId, @Field("token") String token, @Field("auth") int auth);

    // 근로자 로그인 시간 업데이트
    @FormUrlEncoded
    @POST("/APP_auth/setLastLoginTime")
    Call<ResponseBody> setLastLoginTime(@Field("id") String id, @Field("dateTime") String dateTime, @Field("token") String token, @Field("auth") int auth);

    // =============================================================================================================================================== 메인화면
    // FCM 토큰 업데이트
    @FormUrlEncoded
    @POST("/APP_main/uniqueIdUpdate")
    Call<ResponseBody> uniqueIdUpdate(@Field("employee_id") String employee_id, @Field("manager_id") String manager_id,
                                      @Field("unique_id") String unique_id, @Field("auth") int auth, @Field("date") String date, @Field("token") String token);

    // 근로자 이름 조회
    @FormUrlEncoded
    @POST("/APP_main/getEmployeeName")
    Call<ResponseBody> getEmployeeName(@Field("id") String id, @Field("token") String token, @Field("auth") int auth);

    // 관리자 이름 조회
    @FormUrlEncoded
    @POST("/APP_main/getAdminName")
    Call<ResponseBody> getAdminName(@Field("id") String id, @Field("token") String token, @Field("auth") int auth);

    // =============================================================================================================================================== 근태현황
    // 근태현황 리스트
    @FormUrlEncoded
    @POST("/APP_attendance/employeeAttendanceList")
    Call<ResponseBody> employeeAttendanceList(@Field("employee_id") String employee_id, @Field("startDate") String startDate,
                                              @Field("endDate") String endDate, @Field("token") String token, @Field("auth") int auth);

    // =============================================================================================================================================== 공지사항
    // 공지사항 리스트 조회
    @FormUrlEncoded
    @POST("/APP_notice/noticeList")
    Call<ResponseBody> noticeList(@Field("searchText") String searchText, @Field("token") String token, @Field("id") String id, @Field("auth") int auth);

    // =============================================================================================================================================== 지도
    // 근로자 마지막 위치 정보 조회
    @FormUrlEncoded
    @POST("/APP_map/getEmployeeLocation")
    Call<ResponseBody> getEmployeeLocation(@Field("employee_id") String employee_id, @Field("token") String token, @Field("auth") int auth);

    // =============================================================================================================================================== 현장 신문고
    // 현장 신문고 등록
    @FormUrlEncoded
    @POST("/APP_remedy/inputRemedy")
    Call<ResponseBody> inputRemedy(@Field("title") String title, @Field("content") String content, @Field("createDate") String createDate,
                                   @Field("site_id") int siteCode, @Field("token") String token, @Field("id") String id, @Field("auth") int auth);
}
