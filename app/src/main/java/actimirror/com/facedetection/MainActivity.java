package actimirror.com.facedetection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InflateParams")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivity extends Activity implements SurfaceHolder.Callback{

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    boolean preview = false;
    private LayoutInflater layoutInflater = null;
    private Button btnTakePicture;
//    private TextView txtFaceCount;
//    private TextView txtImagePath;

    final int RESULT_SAVEIMAGE = 0;
    private FaceOverlayView mFaceView;
    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.camPreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

//        txtFaceCount = (TextView)findViewById(R.id.tvFaceCount);
//        txtImagePath = (TextView)findViewById(R.id.tvImagePath);

        layoutInflater = LayoutInflater.from(getBaseContext());
        View viewControl = layoutInflater.inflate(R.layout.picture_control, null);
        LayoutParams layoutParamsControl
                = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);

        btnTakePicture = (Button)findViewById(R.id.takepicture);
        btnTakePicture.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                camera.takePicture(mShutterCallback,
                        mRawPictureCallback, mJPGPictureCallback);
            }
        });

        LinearLayout layoutBackground = (LinearLayout)findViewById(R.id.linearLayout);
        layoutBackground.setOnClickListener(new LinearLayout.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                btnTakePicture.setEnabled(false);
                camera.autoFocus(mAutoFocusCallback);
            }});

        mFaceView = new FaceOverlayView(this);
        addContentView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    }

    @SuppressLint("NewApi")
    FaceDetectionListener faceDetectionListener
            = new FaceDetectionListener(){

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {

            if (faces.length == 0){
                Toast.makeText(getApplicationContext(), "No face detected..!", Toast.LENGTH_SHORT).show();


            }else{

                //txtFaceCount.setText("Number of Faces Detected:" + " " +String.valueOf(faces.length));
            }


            mFaceView.setFaces(faces);
        }};

    AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            btnTakePicture.setEnabled(true);
        }};

    ShutterCallback mShutterCallback = new ShutterCallback(){
        @Override
        public void onShutter() {
            // TODO Auto-generated method stub

        }};

    PictureCallback mRawPictureCallback = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub

        }};

    PictureCallback mJPGPictureCallback = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub

            try {

                int imageNum = 0;

                Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "FaceDetection");
                if (!imagesFolder.exists()) {
                    imagesFolder.mkdirs();
                }

                String fileName = "image_" + String.valueOf(imageNum) + ".jpg";
                File output = new File(imagesFolder, fileName);

                while (output.exists()){
                    imageNum++;
                    fileName = "image_" + String.valueOf(imageNum) + ".jpg";
                    output = new File(imagesFolder, fileName);
                }

                Uri uriSavedImage = Uri.fromFile(output);
                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

                OutputStream imageFileOS;

                imageFileOS = getContentResolver().openOutputStream(uriSavedImage);
                imageFileOS.write(arg0);
                imageFileOS.flush();
                imageFileOS.close();

                //txtImagePath.setText("Image saved to:" + " " +uriSavedImage.toString());

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            camera.startPreview();
            camera.startFaceDetection();
        }};

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        // TODO Auto-generated method stub
        if(preview){
            camera.stopFaceDetection();
            camera.stopPreview();
            preview = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                camera.startFaceDetection();
                preview = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open();
        camera.setFaceDetectionListener(faceDetectionListener);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopFaceDetection();
        camera.stopPreview();
        camera.release();
        camera = null;
        preview = false;
    }
}