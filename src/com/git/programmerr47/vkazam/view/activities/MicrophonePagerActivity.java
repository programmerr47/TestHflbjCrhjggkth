package com.git.programmerr47.vkazam.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.git.programmerr47.testhflbjcrhjggkth.R;
import com.git.programmerr47.vkazam.view.adapters.MicrophonePagerAdapter;
import com.git.programmerr47.vkazam.view.adapters.MyPagerAdapter;

public class MicrophonePagerActivity extends PagerActivity {
	private static final String TAG = "MicrophonePagerActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "Binding to service");
		super.onCreate(savedInstanceState);
        setupUi();
	}

	@Override
	protected MyPagerAdapter getAdapter() {
		return new MicrophonePagerAdapter(getSupportFragmentManager(),
				getApplicationContext());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			Log.v("Settings", "Creating settings activity");
			Intent intent = new Intent(this, SettingsActivity.class);
			Intent parentIntent = new Intent(this,
					MicrophonePagerActivity.class);
			parentIntent.putExtra(PAGE_NUMBER, pager.getCurrentItem());
			intent.putExtra(SettingsActivity.PARENT_INTENT, parentIntent);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
