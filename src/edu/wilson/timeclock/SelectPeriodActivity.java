package edu.wilson.timeclock;

import java.text.DecimalFormat;
import java.util.Calendar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

// Shows the total hours clocked in in a period. The start and end dates of the period can be
// selected by the user
public class SelectPeriodActivity extends Activity {
	private int mYearStart;
	private int mMonthStart;
	private int mDayStart;
	private int mYearEnd;
	private int mMonthEnd;
	private int mDayEnd;
	private static long mStart;
	private static long mEnd;
	static final int START_DATE_DIALOG_ID = 1;	
	static final int END_DATE_DIALOG_ID = 2;

	@Override
	// set up start and end dates in text boxes
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_period);
		// get date of oldest punch
		Calendar start = Calendar.getInstance();
		if (TimeClock.lstHistory.size() > 0) {
			start.setTimeInMillis(TimeClock.lstHistory.get(0).timeIn);
		}
		mYearStart = start.get(Calendar.YEAR);
		mMonthStart = start.get(Calendar.MONTH);
		mDayStart = start.get(Calendar.DAY_OF_MONTH);
		// get the current date
		final Calendar end = Calendar.getInstance();
		mYearEnd = end.get(Calendar.YEAR);
		mMonthEnd = end.get(Calendar.MONTH);
		mDayEnd = end.get(Calendar.DAY_OF_MONTH);
		// display the dates
		updateStartDate();
		updateEndDate();
		calculateTotal();
	}

	@SuppressWarnings("deprecation")
	public void clickStartDate(View view) {
		showDialog(START_DATE_DIALOG_ID);
	}
	@SuppressWarnings("deprecation")
	public void clickEndDate(View view) {
		showDialog(END_DATE_DIALOG_ID);
	}

	private void updateStartDate() {
		Calendar start = Calendar.getInstance();
		start.set(mYearStart, mMonthStart, mDayStart, 0, 0, 1);
		mStart = start.getTimeInMillis();
		((TextView) findViewById(R.id.txtStartDate)).setText(
				new StringBuilder()
				.append("Start Date: ")
				// Month is 0 based so add 1
				.append(mMonthStart + 1).append("-")
				.append(mDayStart).append("-")
				.append(mYearStart).append(" "));
	}

	private void updateEndDate() {
		Calendar end = Calendar.getInstance();
		end.set(mYearEnd, mMonthEnd, mDayEnd, 23, 59, 59); //end of the day
		mEnd = end.getTimeInMillis();
		((TextView) findViewById(R.id.txtEndDate)).setText(
				new StringBuilder()
				.append("End Date: ")
				// Month is 0 based so add 1
				.append(mMonthEnd + 1).append("-")
				.append(mDayEnd).append("-")
				.append(mYearEnd).append(" "));
	}

	private DatePickerDialog.OnDateSetListener mStartDateSetListener =
			new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, 
				int monthOfYear, int dayOfMonth) {
			mYearStart = year;
			mMonthStart = monthOfYear;
			mDayStart = dayOfMonth;
			updateStartDate();
			calculateTotal();
		}
	};
	private DatePickerDialog.OnDateSetListener mEndDateSetListener =
			new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, 
				int monthOfYear, int dayOfMonth) {
			mYearEnd = year;
			mMonthEnd = monthOfYear;
			mDayEnd = dayOfMonth;
			updateEndDate();
			calculateTotal();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case START_DATE_DIALOG_ID:
			return new DatePickerDialog(this,
					mStartDateSetListener,
					mYearStart, mMonthStart, mDayStart);

		case END_DATE_DIALOG_ID:
			return new DatePickerDialog(this,
					mEndDateSetListener,
					mYearEnd, mMonthEnd, mDayEnd);
		}
		return null;
	}

	// run through all time punches between the start and end date
	// total the time clocked in and write the formatted total to the view
	public void calculateTotal() {
		double total = 0;
		for (int i = 0; i < TimeClock.lstHistory.size(); i++) {
			Punch punch = TimeClock.lstHistory.get(i);
			if (punch.timeIn >= mStart && punch.timeIn <= mEnd) {
				total += punch.durationInMillis();
			}
		}
		// convert millis to hours
		total /= 3600000.0;
		DecimalFormat df = new DecimalFormat("#,###,##0.000");
		((TextView) findViewById(R.id.txtTotal)).setText(
				new StringBuilder()
				.append("Total: ")
				.append(df.format(total))
				.append(" hours"));
	}
}
