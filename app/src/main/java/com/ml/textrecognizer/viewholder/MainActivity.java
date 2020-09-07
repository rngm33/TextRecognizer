package com.ml.textrecognizer.viewholder;

import android.os.Build;
import android.os.Bundle;

import com.ml.textrecognizer.R;
import com.ml.textrecognizer.fraginterface.FragmentActionListener;
import com.ml.textrecognizer.fragments.DeviceInfoFragment;
import com.ml.textrecognizer.fragments.MainViewFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements FragmentActionListener {
    private String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_PHONE_STATE",
            "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.CAMERA"};
    public static FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(permissions, requestCode);
        }

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fmlayout, new MainViewFragment(), "maintag").commit();

    }

    public void addDeviceInfoFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        DeviceInfoFragment deviceInfoFragment = new DeviceInfoFragment();
        fragmentTransaction.replace(R.id.fmlayout, deviceInfoFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onActionPerformed() {
//        addDeviceInfoFragment();
    }
}
