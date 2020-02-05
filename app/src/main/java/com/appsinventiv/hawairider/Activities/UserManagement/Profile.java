package com.appsinventiv.hawairider.Activities.UserManagement;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.appsinventiv.hawairider.Models.RiderModel;
import com.appsinventiv.hawairider.R;
import com.appsinventiv.hawairider.Utils.CommonUtils;
import com.appsinventiv.hawairider.Utils.CompressImage;
import com.appsinventiv.hawairider.Utils.Glide4Engine;
import com.appsinventiv.hawairider.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    ImageView pick;
    CircleImageView image;
    EditText name, phone, password;
    Button update;
    private List<Uri> mSelected = new ArrayList<>();
    DatabaseReference mDatabase;
    private String imageUrl;
    RelativeLayout wholeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        this.setTitle("Edit profile");
        image = findViewById(R.id.image);
        pick = findViewById(R.id.pick);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        update = findViewById(R.id.update);
        wholeLayout = findViewById(R.id.wholeLayout);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        getPermissions();

        getDataFromServer();
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMatisse();
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().length() == 0) {
                    name.setError("Enter name");
                } else if (phone.getText().length() == 0) {
                    phone.setError("Enter phone");
                } else if (password.getText().length() == 0) {
                    password.setError("Enter password");
                } else {
                    signup();
                }
            }
        });


    }

    private void getDataFromServer() {
        wholeLayout.setVisibility(View.VISIBLE);
        mDatabase.child("Riders").child(SharedPrefs.getUserModel().getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    RiderModel model = dataSnapshot.getValue(RiderModel.class);
                    if (model != null) {
                        SharedPrefs.setUserModel(model);
                        wholeLayout.setVisibility(View.GONE);
                        if(model.getPicUrl()!=null) {
                            Glide.with(Profile.this).load(model.getPicUrl()).into(image);
                        }
                        name.setText(model.getName());
                        phone.setText(model.getPhone());
                        password.setText(model.getPassword());

                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void signup() {
        if (mSelected.size() == 0) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", name.getText().toString());
            map.put("phone", phone.getText().toString());
            map.put("password", password.getText().toString());

            mDatabase.child("Riders").child(SharedPrefs.getUserModel().getPhone()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    CommonUtils.showToast("Updated");
                }
            });
        } else {
            putPictures(imageUrl);
        }
    }

    private void initMatisse() {
        mSelected = new ArrayList<>();
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(23);
    }

    public void putPictures(String path) {
        wholeLayout.setVisibility(View.VISIBLE);
        CommonUtils.showToast("Uploading image");
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

        ;
        Uri file = Uri.fromFile(new File(path));


        StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
//                        Uri downloadUrl = taskSnapshot.getre;
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        CommonUtils.showToast("Registering");
                        Uri downloadUrl = urlTask.getResult();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("name", name.getText().toString());
                        map.put("password", password.getText().toString());
                        map.put("picUrl", "" + downloadUrl);

                        mDatabase.child("Riders").child(SharedPrefs.getUserModel().getPhone()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                CommonUtils.showToast("Updated");
                                wholeLayout.setVisibility(View.GONE);
                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        CommonUtils.showToast(exception.getMessage());
                        wholeLayout.setVisibility(View.GONE);
                        // ...
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 23 && data != null) {

            mSelected = Matisse.obtainResult(data);
            Glide.with(Profile.this).load(mSelected.get(0)).into(image);
            CompressImage compressImage = new CompressImage(this);
            imageUrl = compressImage.compressImage("" + mSelected.get(0));
        }
    }

    private void getPermissions() {


        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                } else {
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {


            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
