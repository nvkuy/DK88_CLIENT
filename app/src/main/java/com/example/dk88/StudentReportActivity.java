package com.example.dk88;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dk88.Model.ApiUserRequester;
import com.example.dk88.Model.Picture;
import com.example.dk88.Model.PictureAdapter;
import com.example.dk88.Model.RealPathUtil;
import com.example.dk88.Model.ResponseObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentReportActivity extends AppCompatActivity {

    private String token = "";
    private String studentID;
    private String userName;
    private Button btnSave, btnUpload;
    private EditText edtTarget, edtProblem;
    private ListView listPicture;
    private PictureAdapter adapter;
    private int check = 0;
    private Uri uriFinal = null;
    private String url = "";

    private static final int MY_REQUEST_CODE = 1000;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;
    private ArrayList<Picture> arrayPicture;
    private ArrayList<Uri> uriPicture;
    private ArrayList<String> strPicture;

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            Toast.makeText(StudentReportActivity.this, "Upload file failed", Toast.LENGTH_LONG);
                            return;
                        }
                        Uri uri = data.getData();
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                            arrayPicture.add(new Picture(bitmap));
                            uriPicture.add(uri);
                            getData();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_report_layout);

        initView();
        getDataFromIntent();

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRequestPermission();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Uri uri : uriPicture) {
                    minimizeUri(uri);
                    if (uriFinal != null) {
                        uploadPicture(uriFinal);
                    }
                    uriFinal = null;
                }
            }
        });
    }

    private void getData() {
        adapter = new PictureAdapter(this, R.layout.picture_layout, arrayPicture);
        listPicture.setAdapter(adapter);
    }

    private void minimizeUri(Uri uri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int quality = 100;
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

            while (outputStream.toByteArray().length > 1024 * 1024 && quality > 0) {
                outputStream.reset();
                quality -= 5;
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            }

            File outputDir = getApplicationContext().getCacheDir();
            File outputFile = File.createTempFile("compressed_image", ".jpg", outputDir);

            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            fileOutputStream.write(outputStream.toByteArray());
            fileOutputStream.close();

            uriFinal = Uri.fromFile(outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadPicture(Uri uri) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("token", token);

        String strRealPath = RealPathUtil.getRealPath(this, uri);
        File file = new File(strRealPath);

        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part picture = MultipartBody.Part.createFormData("file", file.getName(), fileBody);

        Call<ResponseObject> call = ApiUserRequester.getJsonPlaceHolderApi().uploadPicture(headers, picture);
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(StudentReportActivity.this, "Error uploading picture", Toast.LENGTH_LONG).show();
                    return;
                }
                ResponseObject tmp = response.body();

                if (tmp.getRespCode() != ResponseObject.RESPONSE_OK) {
                    Toast.makeText(StudentReportActivity.this, tmp.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                url = tmp.getData().toString();
                check++;
                if (url.length() > 0) {
                    strPicture.add(url);
                }
                if (check == uriPicture.size()) {
                    sendBan();
                }
                Toast.makeText(StudentReportActivity.this, tmp.getData().toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Toast.makeText(StudentReportActivity.this, "Error uploading picture", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendBan() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("token", token);

        Map<String, Object> banInfo = new HashMap<>();
        banInfo.put("requestID", 2);
        banInfo.put("targetID", edtTarget.getText().toString());
        banInfo.put("requestCode", 1);
        banInfo.put("moreDetail", edtProblem.getText().toString());
        banInfo.put("imageProof", strPicture);

        Call<ResponseObject> call = ApiUserRequester.getJsonPlaceHolderApi().sendBanRequest(headers, banInfo);
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(StudentReportActivity.this, "Error uploading", Toast.LENGTH_LONG).show();
                    return;
                }
                ResponseObject tmp = response.body();
                token = response.headers().get("token");

                if (tmp.getRespCode() != ResponseObject.RESPONSE_OK) {
                    Toast.makeText(StudentReportActivity.this, tmp.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(StudentReportActivity.this, "Your request to ban the account is successful. Please wait for an admin to ban it.", Toast.LENGTH_LONG).show();
                Toast.makeText(StudentReportActivity.this, "You can swipe back.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Toast.makeText(StudentReportActivity.this, "Error uploading", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(StudentReportActivity.this, StudentTradeFinishActivity.class);
        intent.putExtra("studentID", studentID);
        intent.putExtra("token", token);
        intent.putExtra("userName", userName);
        finish();
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void onClickRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                String[] permission = {android.Manifest.permission.READ_MEDIA_IMAGES};
                requestPermissions(permission, MY_REQUEST_CODE);
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    String[] permission = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permission, MY_REQUEST_CODE);
                }
            } else {
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permission, MY_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void initView(){
        btnSave = findViewById(R.id.save);
        btnUpload = findViewById(R.id.upload);
        edtTarget = findViewById(R.id.targetID);
        edtProblem = findViewById(R.id.reportProblem);
        listPicture = findViewById(R.id.listView);

        arrayPicture = new ArrayList<>();
        uriPicture = new ArrayList<>();
        strPicture = new ArrayList<>();
    }

    private void getDataFromIntent(){
        token = getIntent().getStringExtra("token");
        studentID = getIntent().getStringExtra("studentID");
        userName = getIntent().getStringExtra("userName");
    }


}
