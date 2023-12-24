package com.neer.facedetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Bitmap photo;
    FirebaseVisionCloudDetectorOptions options =
            new FirebaseVisionCloudDetectorOptions.Builder()
                    .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                    .setMaxResults(15)
                    .build();
    Button face_detect,camera;
    FirebaseVisionFaceDetectorOptions options1;
    FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        face_detect=findViewById(R.id.face_detect);
        camera=findViewById(R.id.camera);
        imageView=findViewById(R.id.imageView);

        options1 = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options1);

        if(checkCameraHardware(getApplicationContext())){
            if (
                    (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1888);
            }
            else{
                camera.setVisibility(View.VISIBLE);
            }
        }else{
            Toast.makeText(getApplicationContext(),"not avilable",Toast.LENGTH_SHORT).show();
        }
        face_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(photo);
                detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        for (FirebaseVisionFace face : firebaseVisionFaces) {
                            Rect bounds = face.getBoundingBox();

                            Bitmap mutableBitmap = photo.copy(Bitmap.Config.ARGB_8888, true);
                            Canvas canvas = new Canvas(mutableBitmap);
                            Paint p = new Paint();
                            p.setStyle(Paint.Style.FILL_AND_STROKE);
                            p.setAntiAlias(true);
                            p.setFilterBitmap(true);
                            p.setDither(true);
                            p.setColor(Color.RED);

                            canvas.drawLine(bounds.left, bounds.top, bounds.left, bounds.bottom, p);
                            canvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, p);
                            canvas.drawLine(bounds.left, bounds.bottom, bounds.right, bounds.bottom, p);
                            canvas.drawLine(bounds.right, bounds.bottom, bounds.right, bounds.top, p);
                            imageView.setImageBitmap(mutableBitmap);

                            /*
                            Log.d("datafadg",bounds.toString());
                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
                            Log.d("datafadg",rotY+"");
                            Log.d("datafadg",rotZ+"");


                            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                            if (leftEar != null) {
                                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                            }

                            // If contour detection was enabled:
                            List<FirebaseVisionPoint> leftEyeContour =
                                    face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                            List<FirebaseVisionPoint> upperLipBottomContour =
                                    face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();

                            // If classification was enabled:
                            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                float smileProb = face.getSmilingProbability();
                            }
                            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                            }

                            // If face tracking was enabled:
                            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                int id = face.getTrackingId();
                            }

                             */
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"face not detected",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, 1001);
            }
        });


    }
    private boolean checkCameraHardware(Context context) {
        return (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) ? true : false ;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1888 && resultCode == RESULT_OK) {
            camera.setVisibility(View.VISIBLE);
        }
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            face_detect.setVisibility(View.VISIBLE);

        }
    }
}