package edu.wilson.timeclock;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

// take a picture with the front facing camera and add it to the current time punch object
public class TakePictureActivity extends Activity {
	private SurfaceView surfaceView=null;
	private SurfaceHolder surfaceHolder=null;
	private Camera camera=null;
	private boolean inPreview=false;
	private boolean cameraConfigured=false;
	private static final String TAG = "TakePictureActivity";

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);

		surfaceView=(SurfaceView)findViewById(R.id.srfCameraPreview);
		surfaceHolder=surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void onResume() {
		super.onResume();

		camera=getCameraInstance();
		camera.setDisplayOrientation(90);
		startPreview();
	}

	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}
		camera.release();
		camera=null;
		inPreview=false;
		super.onPause();
	}

	// get the largest preview size that will fit the display
	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
		Camera.Size result=null;
		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width<=width && size.height<=height) {
				if (result==null) {
					result=size;
				}
				else {
					int resultArea=result.width*result.height;
					int newArea=size.width*size.height;
					if (newArea>resultArea) {
						result=size;
					}
				}
			}
		}
		return(result);
	}

	// get the front facing camera
	private static Camera getCameraInstance() {
		Camera cam = null;
		Camera.CameraInfo camInfo = new Camera.CameraInfo();
		int camNo;
		for (camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
			Camera.getCameraInfo(camNo, camInfo);
			if (camInfo.facing == (Camera.CameraInfo.CAMERA_FACING_FRONT)) {
				break;
			}
		}
		try {
			cam = Camera.open(camNo);
		}
		catch (Exception e) {
			Log.e(TAG, "Error opening camera: " + e.getMessage());
		}
		return cam;				
	}

	private void startPreview() {
		if (cameraConfigured && camera!=null) {
			try {           
				camera.setPreviewDisplay(surfaceHolder);          
				camera.startPreview();
				inPreview = true;
			} catch(Exception e) {
				Log.d(TAG, "Cannot start preview", e);    
			}
		}
	}

	SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (inPreview) {
				camera.stopPreview();
				inPreview = false;
			}
			Parameters parameters = camera.getParameters();
			Camera.Size size = getBestPreviewSize(width, height, parameters);
			// show the preview right side up even if the display is in portrait orientation
			Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
			switch (display.getRotation()) {
				case Surface.ROTATION_0 :
					parameters.setPreviewSize(size.width, size.height);                           
					camera.setDisplayOrientation(90);
					break;
				case Surface.ROTATION_180 : 
					parameters.setPreviewSize(size.width, size.height);
					camera.setDisplayOrientation(90);
					break;
				case Surface.ROTATION_90 :
					parameters.setPreviewSize(size.width, size.height);
					camera.setDisplayOrientation(0);
					break;
				case Surface.ROTATION_270 :
					parameters.setPreviewSize(size.width, size.height);
					camera.setDisplayOrientation(180);
					break;
			}
			camera.setParameters(parameters);
			cameraConfigured = true;
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};

	public void clickCapture(View v) {
		try {
			camera.takePicture(null, null, mPicture);
		} catch (Exception e) {
			Log.d(TAG, "Error takePicture: " + e.getMessage());
		}
	}

	private PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Intent returnIntent = new Intent();
			returnIntent.putExtra("pictureByteArray",data);
			setResult(RESULT_OK,returnIntent);     
			TakePictureActivity.this.finish();
		}
	};
}