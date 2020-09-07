package com.ml.textrecognizer.viewholder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ml.textrecognizer.R;
import com.ml.textrecognizer.camera.CameraSourcePreview;
import com.ml.textrecognizer.camera.CameraSources;
import com.ml.textrecognizer.camera.GraphicOverlay;
import com.ml.textrecognizer.camera.OcrDetectorProcessor;
import com.ml.textrecognizer.camera.OcrGraphic;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

//import com.google.android.gms.vision.CameraSource;

public class NextActivity extends AppCompatActivity implements FloatingActionButton.OnClickListener {
    private CameraSources mCamerasrc;
    //    private CameraSources cameraSource;
    private CameraSourcePreview preview;
    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private EditText tv;
    private FloatingActionButton fButton;
    private boolean hasFlash;

    private GraphicOverlay<OcrGraphic> graphicOverlay;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    TextToSpeech.OnInitListener listener;
    private Menu mMenu;
    private TextToSpeech tts;
    private SharedPreferences sp;
    private float speed, pitch;
    private String voiceCh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
//        sfview = findViewById(R.id.sfview);
        preview = findViewById(R.id.preview);
        tv = findViewById(R.id.tv);
        fButton = findViewById(R.id.floating_action_button);
        fButton.setOnClickListener(this);
        graphicOverlay = findViewById(R.id.graphicOverlay);
        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        getSavedSettingsValue();

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            startCamSrc();
        } else {
            requestCameraPermission();
        }

        /*
         * First check if device is supporting flashlight or not
         */
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        // Set up the Text To Speech engine.
        listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
//                            Log.d("OnInitListener", "Text to speech engine started successfully.");
//                            tts.setLanguage(Locale.US);
//                        } else {
//                            Log.d("OnInitListener", "Error starting the text to speech engine.");
//                        }
                            Set<String> a = new HashSet<>();
                            a.add(voiceCh);//here you can give male if you want to select male voice.
                            Voice v = new Voice("en-us-x-sfg#" + voiceCh + "_2-local", new Locale("en", "US"), 400, 200, true, a);

                            tts.setVoice(v);
                            tts.setSpeechRate(speed);
                            tts.setPitch(pitch);

//                             int result = tts.setLanguage(Locale.US);
                            int result = tts.setVoice(v);

                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "This Language is not supported");
                            } else {
                            }

                        } else {
                            Log.e("TTS", "Initilization Failed!");
                        }

                    }
                };

//        tts = new TextToSpeech(this, listener);

        tts = new TextToSpeech(NextActivity.this, listener, "com.google.android.tts");

    }

    private void getSavedSettingsValue() {
        if (sp.contains("keyspeed")) {
            speed = sp.getInt("keyspeed", 0);
        }
        if (sp.contains("keypitch")) {
            pitch = sp.getInt("keypitch", 0);
        }
        if (sp.contains("voice")) {
            voiceCh = sp.getString("voice", "female");
        }
    }

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCamerasrc != null) {
            try {
                preview.start(mCamerasrc, graphicOverlay);
            } catch (IOException e) {

                mCamerasrc.release();
                mCamerasrc = null;
            }
        }
    }


    private void startCamSrc() {
        Context context = getApplicationContext();
        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(graphicOverlay));
        // Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            Log.w("lol", "Detector dependencies are not yet available.");
            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();

            }
        } else {
            mCamerasrc = new CameraSources.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSources.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                    .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
                    .setRequestedFps(15.0f)
                    .build();

//            sfview.getHolder().addCallback(new SurfaceHolder.Callback() {
//                @Override
//                public void surfaceCreated(SurfaceHolder holder) {
//
//                    try {
//                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
//                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//
//                            ActivityCompat.requestPermissions(NextActivity.this,
//                                    new String[]{Manifest.permission.CAMERA},
//                                    200);
//                            return;
//                        }
//                        mCamerasrc.start(sfview.getHolder());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//                }
//
//                @Override
//                public void surfaceDestroyed(SurfaceHolder holder) {
//                    mCamerasrc.stop();
//                }
//            });


//            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
//                @Override
//                public void release() {
//
//                }
//
//                @Override
//                public void receiveDetections(Detector.Detections<TextBlock> detections) {
//                    final SparseArray<TextBlock> items = detections.getDetectedItems();
//                    if (items.size() != 0 ){
//
//                        tv.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                StringBuilder stringBuilder = new StringBuilder();
//                                for (int i = 0; i < items.size(); i++) {
//                                    TextBlock item = items.valueAt(i);
//                                    stringBuilder.append(item.getValue());
//                                    stringBuilder.append("\n");
//                                }
//                                tv.setText(stringBuilder.toString());
//                                }
//                            });
//                    }
//                }
//            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.itmRefresh) {
            startCameraSource();
            fButton.setVisibility(View.GONE);
            if (tv.length() > 0) {
                tv.setText("");
                tv.setVisibility(View.GONE);
            }
            return true;
        }

        if (id == R.id.itmflashoff) {
            item.setVisible(false);
            mMenu.findItem(R.id.itmflashon).setVisible(true);

            if (!hasFlash) {
                // device doesn't support flash
                // Show alert message and close the application

                AlertDialog alert = new AlertDialog.Builder(this).create();
                alert.setTitle("Error");
                alert.setMessage("Sorry, your device doesn't support flash light!");
                alert.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // closing the application
                        finish();
                    }
                });
                alert.show();
            } else {
                mCamerasrc.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            return true;
        }
        if (id == R.id.itmflashon) {
            mCamerasrc.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            item.setVisible(false);
            mMenu.findItem(R.id.itmflashoff).setVisible(true);
            return true;
        }

        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void ReadFromText() {
        if (tv.length() == 0) {
            Toast.makeText(this, "no text found to Read", Toast.LENGTH_SHORT).show();
        } else {
            if (!tts.isSpeaking()) {
                getSavedSettingsValue();

                Set<String> a = new HashSet<>();
                a.add(voiceCh);//here you can give male if you want to select male voice.
                Voice v = new Voice("en-us-x-sfg#" + voiceCh + "_2-local", new Locale("en", "US"), 400, 200, true, a);
                tts.setSpeechRate(speed);
                tts.setVoice(v);
                tts.setPitch(pitch);

                tts.speak(tv.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "Default");
            } else {
                Snackbar.make(graphicOverlay, "I am Currently reading.. please have a patience :)", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.startCameraSource();
    }

    /**
     * Stops the camera.
     */

    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
        tts.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
        if (tts.isSpeaking()) {
            tts.shutdown();
            tts.stop();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
//        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(graphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_HANDLE_CAMERA_PERM) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamSrc();
                Toast.makeText(this, "permission granted :)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied :(", Toast.LENGTH_SHORT).show();
                this.finish();
            }

        } else if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
    }

    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = graphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
                Log.d("text", "text data is being spoken! " + text.getValue());
                // Speak the string.
//                tts.speak(text.getValue(), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                tv.setVisibility(View.VISIBLE);
                tv.setText(text.getValue());
                fButton.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
                fButton.setVisibility(View.GONE);
            }
        } else {
            tv.setVisibility(View.GONE);
            fButton.setVisibility(View.GONE);
        }
        return text != null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.floating_action_button) {
            this.ReadFromText();

        }

    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mCamerasrc != null) {
                mCamerasrc.doZoom(detector.getScaleFactor());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }
}


