package edu.wilson.timeclock;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

// Show the details of a selected punch including time, location, and photos
public class PunchDetailsActivity extends Activity {
	private Punch punch;
	private float angle = 270;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.punch_details);
		// intent contains which punch to show details about
		Intent intent = getIntent();
		if (intent != null) {
			int listIndex = intent.getIntExtra("listIndex", -1);
			if (listIndex >= 0) {
				punch = TimeClock.lstHistory.get(listIndex);
				displayPunchDetails();
			}
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private void displayPunchDetails() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MMM-yy");
		DecimalFormat dfLocation = new DecimalFormat("##0.0000");

		showClockIn(sdf, dfLocation);
		if (!punch.clockedIn) {
			showClockOut(sdf, dfLocation);
		} else {
			((TextView) findViewById(R.id.txtTimeOut)).setText("Still clocked in...");
		}
		((TextView) findViewById(R.id.txtTotalTime)).setText("Total Time: " + punch.duration());
	}
	
	private void showClockIn(SimpleDateFormat sdf, DecimalFormat dfLocation) {
		((TextView) findViewById(R.id.txtTimeIn)).setText("Time In: " + sdf.format(punch.timeIn));	        
		String locIn = "Lat: " + dfLocation.format(punch.latitudeIn) + 
				", Lon: " + dfLocation.format(punch.longitudeIn);
		((TextView) findViewById(R.id.txtLocationIn)).setText(locIn);
		Bitmap jpegIn = BitmapFactory.decodeByteArray(punch.pictureIn,0,punch.pictureIn.length);
		((ImageView) findViewById(R.id.imgPicIn)).setImageBitmap(jpegIn);
	}
	
	private void showClockOut(SimpleDateFormat sdf, DecimalFormat dfLocation) {
		Bitmap jpegOut = BitmapFactory.decodeByteArray(punch.pictureOut,0,punch.pictureOut.length);
		((ImageView) findViewById(R.id.imgPicOut)).setImageBitmap(jpegOut);
		String locOut = "Lat: " + dfLocation.format(punch.latitudeOut) + 
				", Lon: " + dfLocation.format(punch.longitudeOut);
		((TextView) findViewById(R.id.txtLocationOut)).setText(locOut);
		((TextView) findViewById(R.id.txtTimeOut)).setText("Time Out: " + sdf.format(punch.timeOut));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.details, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_rotate:
	        rotatePictures();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	// rotate the clock-in picture and the clock-out picture if it exists
	private void rotatePictures() {
		Bitmap jpegIn = BitmapFactory.decodeByteArray(punch.pictureIn,0,punch.pictureIn.length);
		Bitmap jpegRot = RotateBitmap(jpegIn, angle);
		((ImageView) findViewById(R.id.imgPicIn)).setImageBitmap(jpegRot);
		if (!punch.clockedIn) {
			Bitmap jpegOut = BitmapFactory.decodeByteArray(punch.pictureOut,0,punch.pictureOut.length);
			Bitmap jpegRotOut = RotateBitmap(jpegOut, angle);
			((ImageView) findViewById(R.id.imgPicOut)).setImageBitmap(jpegRotOut);
		}
		angle = (angle - 90) % 360;
	}

	// do the actual matrix rotation of the bitmap
	private static Bitmap RotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}	
}
