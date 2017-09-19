package edu.wilson.timeclock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TimeClock extends Activity {
	public static final String PREFS_NAME = "TimeClockPrefsFile";
	public static final String TIME_CLOCK_FILE = "timeClockHistory";
	private static final int TAKE_PICTURE_ACTIVITY_REQUEST_CODE = 200;
	private static final int HISTORY_VIEW_ACTIVITY_REQUEST_CODE = 300;
	private static final int SELECT_PERIOD_ACTIVITY_REQUEST_CODE = 400;
	private static final String TAG = "TimeClock";
	public static ArrayList<Punch> lstHistory;
	public static ArrayList<Punch> lstPunchesToDelete;
	private LocationManager locmgr = null;
	private static volatile double latitude = 0;
	private static volatile double longitude = 0;

	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lstPunchesToDelete = new ArrayList<Punch>();
		lstHistory = new ArrayList<Punch>();
		loadHistory();
		setContentView(R.layout.main);

		TextView txtLocation = (TextView) findViewById(R.id.txtLocation);
		//grab the location manager service
		locmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		txtLocation.setText("Getting location...");
	}

	// adjusts status text and button text based on if currently clocked in
	private void setTimeClockDisplay() {
		TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
		Button btnPunch = (Button) findViewById(R.id.btnPunch);
		if (isClockedIn()) {
			btnPunch.setText("Clock Out");
			Punch lastPunch = lstHistory.get(lstHistory.size()-1);
			txtStatus.setText("You clocked in " + lastPunch.duration() + " ago");
		} else {
			btnPunch.setText("Clock In");
			txtStatus.setText("You are clocked out.");
		}
	}

	// click handler for btnPunch, starts TakePicture activity
	public void clickPunch(View view) {
		Intent i = new Intent(this, TakePictureActivity.class);
		startActivityForResult(i, TAKE_PICTURE_ACTIVITY_REQUEST_CODE);
	}

	// save list of time punches to file when exiting
	public void onDestroy() {
		super.onDestroy();
		saveHistory();
	}

	// convert list of punch objects to serialized byte stream and write to sdCard
	private void saveHistory() {
		File sdCard = Environment.getExternalStorageDirectory();
		File directory = new File (sdCard.getAbsolutePath() + "/MyFiles");
		directory.mkdirs();
		File file = new File(directory, TIME_CLOCK_FILE);
		try {
			FileOutputStream fOut = new FileOutputStream(file);
			ObjectOutputStream so = new ObjectOutputStream(fOut);
			// serialize the object
			so.writeObject(lstHistory);
			so.flush();
			so.close();
		} catch (NotSerializableException e) {
			Log.d(TAG, "Error not serializable: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error writing object: " + e.getMessage());
		}
	}

	// read the time punch history from sdCard and populate lstHistory list of time punches
	@SuppressWarnings("unchecked")
	private void loadHistory() {
		File sdCard = Environment.getExternalStorageDirectory();
		File directory = new File (sdCard.getAbsolutePath() + "/MyFiles");
		directory.mkdirs();
		File file = new File(directory, TIME_CLOCK_FILE);
		try {
			FileInputStream fIn = new FileInputStream(file);
			ObjectInputStream si = new ObjectInputStream(fIn);
			lstHistory = (ArrayList<Punch>) si.readObject();
			si.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error writing object: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.d(TAG, "Error finding Punch List Class: " + e.getMessage());
		}
	}

	// display a list of all punches in history, list items can be selected to see details
	public void clickHistory(View view) {
		if (lstHistory.size() == 0) {
			Toast.makeText(this, "There are no time punches in the history file.",
					Toast.LENGTH_LONG).show();
		} else {
			Intent i = new Intent(this, HistoryView.class);
			startActivityForResult(i, HISTORY_VIEW_ACTIVITY_REQUEST_CODE);
		}
	}

	// show dialog with multi-select list of punches in history and a Delete button
	// list of selected items is passed to eraseHistoryConfirm()
	public void clickDeleteHistory(View view) {
		final Dialog dia = new Dialog(this);
		dia.setContentView(R.layout.delete_list);
		dia.setTitle("Select Records to Delete");
		dia.setCancelable(true);
		
		final ListView list_alert = (ListView) dia.findViewById(R.id.alert_list);
		list_alert.setAdapter(new ArrayAdapter<Punch>(getApplicationContext(),
				android.R.layout.simple_list_item_multiple_choice, lstHistory));
		list_alert.setItemsCanFocus(false);
		list_alert.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		list_alert.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
			}
		});
		Button btn = (Button) dia.findViewById(R.id.btnDelete);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SparseBooleanArray positionsToDelete = list_alert.getCheckedItemPositions();
				lstPunchesToDelete.clear();
				for (int i = 0; i < positionsToDelete.size(); i++) {
					int pos = positionsToDelete.keyAt(i);
					if (positionsToDelete.get(pos)) {
						lstPunchesToDelete.add(lstHistory.get(pos));
						Log.d(TAG,"selected " + pos);
					}
				}
				dia.dismiss();
				if (lstPunchesToDelete.size() > 0) {
					eraseHistoryConfirm();
				}
			}
		});
		dia.show();
	}
	
	// Display "are you sure" dialog before deleting punches from history list
	private void eraseHistoryConfirm() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Delete Time Clock History");
		builder.setMessage("Are you sure you want to permanently delete these " +
				lstPunchesToDelete.size() + " time punches?");
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				for (int i = 0; i < lstPunchesToDelete.size(); i++) {
					Punch punch = lstPunchesToDelete.get(i);
					lstHistory.remove(punch);
				}
				setTimeClockDisplay();
			}

		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	// choose date range of period and see total hours in range
	public void clickSelectPeriod(View view) {
		Intent i = new Intent(this, SelectPeriodActivity.class);
		startActivityForResult(i, SELECT_PERIOD_ACTIVITY_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// If user took photo, save jpeg to Punch object
		if (requestCode == TAKE_PICTURE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {      
				byte jpeg[] = data.getByteArrayExtra("pictureByteArray");
				if (isClockedIn()) {
					Toast.makeText(this, "Clock out successful",
							Toast.LENGTH_LONG).show();
					Punch punch = lstHistory.get(lstHistory.size()-1);
					punch.punchOut(latitude, longitude, jpeg);
				} else {
					Toast.makeText(this, "Clock in successful",
							Toast.LENGTH_LONG).show();
					Punch punch = new Punch(latitude, longitude, jpeg);
					lstHistory.add(punch);
				}
				setTimeClockDisplay();
			}
			else if (resultCode == RESULT_CANCELED) {    
				Toast.makeText(this, "Time punch cancelled",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private boolean isClockedIn() {
		if (lstHistory.size() > 0) {
			Punch lastPunch = lstHistory.get(lstHistory.size()-1);
			return lastPunch.clockedIn;
		} else {
			return false;
		}
	}
	
	//Start a location listener
	LocationListener onLocationChange=new LocationListener() {
		public void onLocationChanged(Location loc) {
			//sets and displays the lat/long when a location is provided
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
			DecimalFormat df = new DecimalFormat("##0.0000");
			String latlong = "Lat: " + df.format(latitude) + "\nLong: " + df.format(longitude);   
			((TextView) findViewById(R.id.txtLocation)).setText(latlong);
		}

		public void onProviderDisabled(String provider) {
			// required for interface, not used
		}

		public void onProviderEnabled(String provider) {
			// required for interface, not used
		}

		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			// required for interface, not used
		}
	};

	//pauses listener while app is inactive
	@Override
	public void onPause() {
		super.onPause();
		locmgr.removeUpdates(onLocationChange);
	}

	//reactivates listener when app is resumed
	@Override
	public void onResume() {
		super.onResume();
		setTimeClockDisplay();
		locmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10000.0f,onLocationChange);
	}
}
