package com.example.dk88;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileActivity extends AppCompatActivity {
    EditText edtOld,edtNew,edtName,edtPhone,edtEmail;
    Button btnOK;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile_layout);
        String token=getIntent().getStringExtra("token");
        Admin admin=(Admin) getIntent().getSerializableExtra("admin");

        Toast.makeText(AdminProfileActivity.this,token,Toast.LENGTH_LONG).show();
        edtOld=(EditText) findViewById(R.id.Password);
        edtNew=(EditText) findViewById(R.id.Password1);
        edtName=(EditText) findViewById(R.id.fullname);
        edtPhone=(EditText) findViewById(R.id.phone);
        edtEmail=(EditText) findViewById(R.id.email);
        btnOK=(Button) findViewById(R.id.ok);
        edtName.setText(admin.getName());
        edtPhone.setText(admin.getPhoneNumber());
        edtEmail.setText(admin.getEmail());
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> changeInfo=new HashMap<>();
                changeInfo.put("userName",admin.getUserName());
                changeInfo.put("name",edtName.getText().toString());
                changeInfo.put("phoneNumber",edtPhone.getText().toString());
                changeInfo.put("roleCode",Integer.toString(admin.getRoleCode()));

                Call<ResponseObject> call = ApiUserRequester.getJsonPlaceHolderApi().changeProfile(changeInfo);
                call.enqueue(new Callback<ResponseObject>() {
                    @Override
                    public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(AdminProfileActivity.this, "Error", Toast.LENGTH_LONG).show();
                            return;
                        }
                        ResponseObject tmp = response.body();
                        if (tmp.getRespCode() != ResponseObject.RESPONSE_OK) {
                            Toast.makeText(AdminProfileActivity.this, tmp.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        Map<String, Object> data = (Map<String, Object>) tmp.getData();
                        String userRole = response.headers().get("UserRole");
                        Toast.makeText(AdminProfileActivity.this, "Change Data successfully ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<ResponseObject> call, Throwable t) {
                        Toast.makeText(AdminProfileActivity.this, "Error", Toast.LENGTH_LONG).show();
                    }
                });
                if(edtOld.getText().toString()!=""){
                    if(edtOld.getText().toString().compareTo(edtNew.getText().toString())==0){
                        Toast.makeText(AdminProfileActivity.this,"The new password is duplicated than old password",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Map<String,Object> passInfo=new HashMap<>();
                        passInfo.put("oldHassPass",edtOld.getText().toString());
                        passInfo.put("newHassPass",edtNew.getText().toString());

                        Call<ResponseObject> call1 = ApiUserRequester.getJsonPlaceHolderApi().changePass(passInfo);
                        call.enqueue(new Callback<ResponseObject>() {
                            @Override
                            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                                if (!response.isSuccessful()) {
                                    Toast.makeText(AdminProfileActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                ResponseObject tmp = response.body();
                                if (tmp.getRespCode() != ResponseObject.RESPONSE_OK) {
                                    Toast.makeText(AdminProfileActivity.this, tmp.getMessage(), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Map<String, Object> data = (Map<String, Object>) tmp.getData();
                                String userRole = response.headers().get("UserRole");
                                Toast.makeText(AdminProfileActivity.this, "Change Password successfully ", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(Call<ResponseObject> call, Throwable t) {
                                Toast.makeText(AdminProfileActivity.this, "Error", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }
}
