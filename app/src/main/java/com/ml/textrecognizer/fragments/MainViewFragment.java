package com.ml.textrecognizer.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.ml.textrecognizer.R;
import com.ml.textrecognizer.viewholder.MainActivity;
import com.ml.textrecognizer.viewholder.NextActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class MainViewFragment extends Fragment {
    private Button btnCapture, btnDetect, btnNext, btnInfo;
    private ImageView imgView;
    private Bitmap bmp;
    private EditText tview;
    private static int REQCODE = 1;
//    FragmentActionListener fragmentActionListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Text Recognizer");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectText();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NextActivity.class));
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                fragmentActionListener.onActionPerformed();
                MainActivity.fragmentManager.beginTransaction().replace(R.id.fmlayout, new DeviceInfoFragment(), "devicetag").addToBackStack(null).commit();
            }
        });

    }

    private void initView(View view) {
        btnCapture = view.findViewById(R.id.btncapture);
        btnDetect = view.findViewById(R.id.btndetect);
        btnNext = view.findViewById(R.id.btnNext);
        btnInfo = view.findViewById(R.id.btninfo);
        imgView = view.findViewById(R.id.imgview);
        tview = view.findViewById(R.id.tv);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQCODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            bmp = (Bitmap) bundle.get("data");
            imgView.setImageBitmap(bmp);

        }
    }

    private void detectText() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bmp);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        detector.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processText(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void processText(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();

        if (blocks.size() != 0) {
            for (FirebaseVisionText.TextBlock tblock : firebaseVisionText.getTextBlocks()) {
                String st = "";
                st = st + tblock.getText() + "\n";

                tview.setText(st);
                tview.setEnabled(true);
            }
        } else {
            Toast.makeText(getContext(), "No Text Detected", Toast.LENGTH_SHORT).show();
        }

    }

//    public void setFragmentActionListener(FragmentActionListener fragmentActionListener) {
//        this.fragmentActionListener = fragmentActionListener;
//    }

}
