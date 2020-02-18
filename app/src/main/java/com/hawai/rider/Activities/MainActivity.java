package com.hawai.rider.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.hawai.rider.Activities.UserManagement.Profile;
import com.hawai.rider.R;
import com.hawai.rider.Services.GpsTrackerService;
import com.hawai.rider.Utils.ApplicationClass;
import com.hawai.rider.Utils.CommonUtils;
import com.hawai.rider.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    Switch serviceSwitch;
    private boolean isClipboardServiceRunning;

    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getPermissions();
        serviceSwitch = findViewById(R.id.serviceSwitch);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        isClipboardServiceRunning = isMyServiceRunning(GpsTrackerService.class);
        if (isClipboardServiceRunning) {
            serviceSwitch.setChecked(true);
        } else {
            serviceSwitch.setChecked(false);
        }


        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isPressed()) {
                    sendDataToDb(b);
                    if (b) {

                        startServiceNow();
//                        Intent intent = new Intent(MainActivity.this, GPSTrackerActivity.class);
//                        startActivityForResult(intent, 1);
                    } else {
                        stopServiceNow();
                    }
                }
            }
        });


        initDrawer();

    }

    private void sendDataToDb(boolean b) {
        mDatabase.child("Riders").child(SharedPrefs.getUserModel().getPhone()).child("active").setValue(b).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = data.getExtras();
        double lng = extras.getDouble("Longitude");
        double lat = extras.getDouble("Latitude");
    }

    private void getPermissions() {


        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION

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


    private void stopServiceNow() {
        sendMessage();
        isClipboardServiceRunning = isMyServiceRunning(GpsTrackerService.class);

        if (isClipboardServiceRunning) {
            stopService(new Intent(getApplicationContext(), GpsTrackerService.class));

        } else {
            CommonUtils.showToast("Service not running running");
        }


    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void startServiceNow() {
        isClipboardServiceRunning = isMyServiceRunning(GpsTrackerService.class);

        if (!isClipboardServiceRunning) {
            Intent svc = new Intent(MainActivity.this, GpsTrackerService.class);

            startService(svc);
        } else {
            CommonUtils.showToast("Service running");

        }
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ApplicationClass.getInstance().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void initDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);

        CircleImageView image = headerView.findViewById(R.id.imageView);
        TextView navUsername = (TextView) headerView.findViewById(R.id.name_drawer);
//        TextView navSubtitle = (TextView) headerView.findViewById(R.id.subtitle_drawer);
        if (SharedPrefs.getUserModel().getPicUrl() != null) {
            Glide.with(MainActivity.this).load(SharedPrefs.getUserModel().getPicUrl()).into(image);
        } else {
            Glide.with(MainActivity.this).load(R.drawable.logo).into(image);

        }

        navUsername.setText("Welcome, " + SharedPrefs.getUserModel().getName());
//        navSubtitle.setText(SharedPrefs.getUserModel().getCell1());
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            // Handle the camera action
        } else if (id == R.id.profile) {
            startActivity(new Intent(MainActivity.this, Profile.class));

        }
//        else if (id == R.id.nav_my_orders) {
//            startActivity(new Intent(MainActivity.this, MyOrders.class));
//
//        } else if (id == R.id.nav_logout) {
//            SharedPrefs.logout();
//            Intent i = new Intent(MainActivity.this, Splash.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(i);
//            finish();
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
