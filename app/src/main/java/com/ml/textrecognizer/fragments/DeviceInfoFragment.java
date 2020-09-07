package com.ml.textrecognizer.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.ml.textrecognizer.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class DeviceInfoFragment extends Fragment {
    private static String TAG = "Buildinfo";
    private TextView tvModel, tvBrand, tvHardware, tvBoard, tvOs, tvDisplay;
    private MaterialCardView cardView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_device_info, container, false);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Device Info");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        setData();

    }

    private void setData() {
        String model = Build.MODEL;
        String hardware = Build.HARDWARE;
        String board = Build.BOARD;
        String brand = Build.BRAND;
        String display = Build.DISPLAY;
        String OS = Build.VERSION.RELEASE;
        tvBoard.setText(board);
        tvModel.setText(model);
        tvHardware.setText(hardware);
        tvBrand.setText(brand);
        tvOs.setText(OS);
        tvDisplay.setText(display);
    }

    private void initView(View view) {
        tvBoard = view.findViewById(R.id.tvboard);
        tvHardware = view.findViewById(R.id.tvhardware);
        tvBrand = view.findViewById(R.id.tvbrand);
        tvModel = view.findViewById(R.id.tvmodel);
        tvOs = view.findViewById(R.id.tvos);
        tvDisplay = view.findViewById(R.id.tvdisplay);
        cardView = view.findViewById(R.id.cardview);
    }
}
