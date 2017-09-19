package edu.wilson.timeclock;

import android.annotation.SuppressLint;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

// A single Punch object contains time, location, and picture information for both
// the clock-in and the corresponding clock-out events 
public class Punch implements
java.io.Serializable
{
	private static final long serialVersionUID = -1315819703077160283L;
	double latitudeIn;
	double longitudeIn;
	double latitudeOut;
	double longitudeOut;
	long timeIn;
	long timeOut;
	byte pictureIn[];
	byte pictureOut[];
	boolean clockedIn;
	
	// constructor sets clock-in time to current time
	public Punch(double lat, double lon, byte pic[]) {		
		timeIn = Calendar.getInstance().getTimeInMillis();
		latitudeIn = lat;
		longitudeIn = lon;
		pictureIn = pic;
		clockedIn = true;
	}
	
	// method to clock-out the punch
	public void punchOut(double lat, double lon, byte pic[]) {
		timeOut = Calendar.getInstance().getTimeInMillis();
		latitudeOut = lat;
		longitudeOut = lon;
		pictureOut = pic;
		clockedIn = false;
	}
	
	// returns the time elapsed between clock-in and clock-out as a formatted string
	public String duration() {
		double minutes;
		DecimalFormat df = new DecimalFormat("#,###,##0.00");
		minutes = durationInMillis() / 60000D;
		if (minutes < 60) {
			return df.format(minutes) + " minutes";
		} else {
			return df.format(minutes / 60D) + " hours";
		}
	}
	
	// returns duration of punch in millis
	public long durationInMillis() {
		long out;
		if (clockedIn) {
			out = Calendar.getInstance().getTimeInMillis();
		} else {
			out = timeOut;
		}
		return out - timeIn;
	}
	
	@SuppressLint("SimpleDateFormat")
	@Override
	// A nice representation of the punch object for use in list displays
    public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("E, dd-MMM");
        return sdf.format(timeIn) + " : " + this.duration();
	}
}
