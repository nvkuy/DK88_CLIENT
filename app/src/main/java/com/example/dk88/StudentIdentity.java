package com.example.dk88;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentIdentity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 1000;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;
    ImageButton imgFront,imgBack;
    ImageView imageFront,imageBack;
    private int imageCode=0;
    String token;
    private Uri mUri1,mUri2;
    Button btnOK;
    String strFront="",strBack="";
    Student student;
    int check=0;
    private static final String TAG=StudentIdentity.class.getName();
    private ActivityResultLauncher<Intent> mActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        if(data==null){
                            return;
                        }
                        Uri uri=data.getData();
                        try{
                            Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                            Glide.with(StudentIdentity.this)
                                    .asBitmap()
                                    .load(uri) // hoặc load(url)
                                    .apply(new RequestOptions()
                                            .override(1024, 1024)) // kích thước tối đa của ảnh sau khi nén
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                                            // bitmap đã được nén và load thành công, tiếp tục xử lý ở đây
                                            String path=MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"Title",null);

                                            if(imageCode==1) {
                                                mUri1=Uri.parse(path);
                                                Log.e(TAG, mUri1.toString());
                                                imageFront.setImageBitmap(bitmap);
                                            }
                                            else if(imageCode==2){
                                                mUri2=Uri.parse(path);
                                                Log.e(TAG, mUri2.toString());
                                                imageBack.setImageBitmap(bitmap);
                                            }
                                        }

                                        @Override
                                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                            Log.e(TAG, "Error transform");
                                        }
                                    });


                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_active_request_layout);

        token=getIntent().getStringExtra("token");
        student=(Student) getIntent().getSerializableExtra("student");

        imgFront=(ImageButton) findViewById(R.id.imgFront);
        imgBack=(ImageButton) findViewById(R.id.imgBack);
        imageFront=(ImageView) findViewById(R.id.picture);
        imageBack=(ImageView) findViewById(R.id.picture1);
        btnOK=(Button) findViewById(R.id.ok);

        imgFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCode=1;
                onClickRequestPermission();
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCode=2;
                onClickRequestPermission();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check=0;
                if(mUri1!=null){
                    uploadPicture(mUri1,"Front");
                }
                if(mUri2!=null){
                    uploadPicture(mUri2,"Back");
                }


            }
        });

    }
    private void uploadPicture(Uri uri,String text){
        Map<String,Object> headers=new HashMap<>();
        headers.put("token",token);
        //Map<String, Object> uploadInfo = new HashMap<>();
        Log.e(TAG,uri.toString());
        String strRealPath=RealPathUtil.getRealPath(this,uri);
        File file =new File(strRealPath);
        Log.e(TAG,file.toString());
        Log.e(TAG,token.toString());
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        Log.e(TAG,"FileBody: "+fileBody.toString());
        MultipartBody.Part picture =MultipartBody.Part.createFormData("file",file.getName(),fileBody);
       // uploadInfo.put("file",file);

        Call<ResponseObject> call=ApiUserRequester.getJsonPlaceHolderApi().uploadPicture(headers,picture);
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(StudentIdentity.this, "Error picture1", Toast.LENGTH_LONG).show();
                    return;
                }
                ResponseObject tmp = response.body();


                if (tmp.getRespCode() != ResponseObject.RESPONSE_OK) {
                    Toast.makeText(StudentIdentity.this, tmp.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }


                if(text.compareTo("Front")==0){
                    strFront=tmp.getData().toString();
                }
                else{
                    strBack=tmp.getData().toString();
                }
                check++;
                Toast.makeText(StudentIdentity.this, "Upload success in " + text +" picture", Toast.LENGTH_LONG).show();
                if(check==2){
                    sendActive();
                }
            }


            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Toast.makeText(StudentIdentity.this, "Error_picture", Toast.LENGTH_LONG).show();
            }
        });

    }
    private void sendActive(){
        Map<String,Object> headers=new HashMap<>();
        headers.put("token",token);
        Map<String, Object> activeInfo = new HashMap<>();
        activeInfo.put("requestID",1);
        activeInfo.put("targetID",student.getStudentID());
        activeInfo.put("requestCode",0);
        activeInfo.put("imageFront",strFront);
        activeInfo.put("imageBack",strBack);

        Call<ResponseObject> call=ApiUserRequester.getJsonPlaceHolderApi().sendActiveRequest(headers,activeInfo);
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(StudentIdentity.this, "Error_upload1", Toast.LENGTH_LONG).show();
                    return;
                }
                ResponseObject tmp = response.body();
                token = response.headers().get("token");

                if (tmp.getRespCode() != ResponseObject.RESPONSE_OK) {
                    Toast.makeText(StudentIdentity.this, tmp.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }



                Toast.makeText(StudentIdentity.this, "Your request to activation account is successfully, please wait for admin to active ", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(StudentIdentity.this,SignInActivity.class);
                startActivity(intent);

            }


            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Toast.makeText(StudentIdentity.this, "Error_UPLOAD", Toast.LENGTH_LONG).show();
            }
        });


    }
    private void onClickRequestPermission() {

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                openGallery();
            }
            else{
                String[] permission={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission,MY_REQUEST_CODE);
            }

        }
        else{
            String[] permission={Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permission,MY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==MY_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent,"Select Picture"));
    }
}