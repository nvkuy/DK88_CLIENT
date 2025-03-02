package com.example.dk88.Model;

import android.net.wifi.rtt.ResponderConfig;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface JsonPlaceHolderApi {

    @GET(".")
    Call<ResponseObject> test();

    @POST("User/Login")
    Call<ResponseObject> login(@Body Map<String, Object> body);

    @POST("User/CreateAccountStudent")
    Call<ResponseObject> signup(@Body Map<String, Object> body);

    // @Headers("Content-Type: application/json")
    @POST("User/ChangePassword")
    Call<ResponseObject> changePass(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

    @POST("User/ChangePublicInfo")
    Call<ResponseObject> changeProfile(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

    @POST("Request/Active")
    Call<ResponseObject> sendActiveRequest(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

    @POST("Request/Ban")
    Call<ResponseObject> sendBanRequest(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

    @GET("Request/Page/{page}")
    Call<ResponseObject> readRequestPage(@HeaderMap Map<String, Object> header, @Path("page") int pageID);

    @POST("Request/Detail")
    Call<ResponseObject> readDetailRequest(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

    @POST("Request/Handle")
    Call<ResponseObject> handleRequest(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

    @Multipart
    @POST("File/UploadImage")
    Call<ResponseObject> uploadPicture(@HeaderMap Map<String, Object> header, @Part MultipartBody.Part picture);

    @GET("File/GetImage/{url}")
    Call<ResponseObject> readImage(@HeaderMap Map<String, Object> header, @Path("url") String url);

    @POST("Class/ChangeClass")
    Call<ResponseObject> changeClass(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

    @GET("Class/QueryClass/{id}")
    Call<ResponseObject> getNewQueryClass(@HeaderMap Map<String, Object> header, @Path("id") int id);

    @GET("User/ReadPublicInfo/{id}")
    Call<ResponseObject> getStudentInfo(@HeaderMap Map<String, Object> header, @Path("id") String studentID);

    @GET("Group/Info/{group_id}")
    Call<ResponseObject> getGroupInfo(@HeaderMap Map<String, Object> header, @Path("group_id") String group_id);

    @POST("Group/ListInfo")
    Call<ResponseObject> getListGroupInfo(@HeaderMap Map<String, Object> header, @Body List<String> body);

    @POST("Group/VoteGroup")
    Call<ResponseObject> voteGroup(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

    @GET("Group/Joined/{student_id}")
    Call<ResponseObject> getGroupOfStudent(@HeaderMap Map<String, Object> header, @Path("student_id") String student_id);

    @GET("User/ReadAllStudentID")
    Call<ResponseObject> getAllStudentID(@HeaderMap Map<String, Object> header);

    @POST("User/ChangeStudentStatus")
    Call<ResponseObject> changeStudentStatus(@HeaderMap Map<String, Object> header, @Body Map<String, Object> body);

}