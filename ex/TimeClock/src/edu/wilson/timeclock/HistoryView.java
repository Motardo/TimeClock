package edu.wilson.timeclock;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// show the time punches in the history list as clickable list items
public class HistoryView extends ListActivity {
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void onListItemClick(ListView parent, View v, int position, long id) {
        Intent intent = new Intent(this, PunchDetailsActivity.class);
        intent.putExtra("listIndex", position);
        startActivity(intent);
    }
    
	@Override
	public void onResume() {
		super.onResume();
        setListAdapter(new ArrayAdapter<Punch>(this, android.R.layout.simple_list_item_1,
        		TimeClock.lstHistory));
	}
}