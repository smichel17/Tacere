/*
 * Created by Jonathan Nelson
 * 
 * Copyright 2013 Jonathan Nelson
 *
 * Released under the BSD license.  For details see the COPYING file.
*/

package org.ciasaboark.tacere;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private static final String TAG = "Settings";
	
	private boolean isActivated;
	private int ringerType;
	private boolean adjustMedia;
	private int mediaVolume;
	private boolean adjustAlarm;
	private int alarmVolume;
	private int quickSilenceMinutes;
	private int quickSilenceHours;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		setupActionBar();
		
		//read the saved preferences
		readSettings();
		
		//Log the results
		Log.d(TAG, "isActivated: " + String.valueOf(isActivated));
		Log.d(TAG, "ringerType: " + String.valueOf(ringerType));
		Log.d(TAG, "adjustMedia: " + String.valueOf(adjustMedia));
		Log.d(TAG, "mediaVolume: " + String.valueOf(mediaVolume));
		Log.d(TAG, "adjustAlarm: " + String.valueOf(adjustAlarm));
		Log.d(TAG, "alarmVolume: " + String.valueOf(alarmVolume));
		Log.d(TAG, "quickSilenceMinutes: " + String.valueOf(quickSilenceMinutes));
		Log.d(TAG, "quickSilenceHours: " + String.valueOf(quickSilenceHours));
		
		refreshDisplay();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings_restore:
            //restore settings to default values then navigate to the main activity
			restoreDefaults();
			//navigate back to the main screen
			Toast.makeText(getApplicationContext(),"Settings have been restored to defaults", Toast.LENGTH_SHORT).show();
            return true;
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			saveSettings();
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void refreshDisplay() {
		//the service state toggle
		CheckBox serviceCB = (CheckBox)findViewById(R.id.activateServiceCheckBox);
		TextView serviceTV = (TextView)findViewById(R.id.activateServiceDescription);
		if (isActivated) {
			serviceCB.setChecked(true);
			serviceTV.setText("Service will run periodically");
		} else {
			serviceCB.setChecked(false);
			serviceTV.setText("Service is disabled");
		}
		
		//the ringer type description
		TextView ringerTV = (TextView)findViewById(R.id.ringerTypeDescription);
		switch (ringerType) {
			case 1:
				ringerTV.setText(R.string.pref_ringer_type_normal);
				break;
			case 2:
				ringerTV.setText(R.string.pref_ringer_type_vibrate);
				break;
			case 3:
				ringerTV.setText(R.string.pref_ringer_type_silent);
				break;
			default :
				ringerTV.setText(R.string.pref_ringer_type_silent);
		}
		

		
		//the media volumes toggle
		CheckBox mediaCB = (CheckBox)findViewById(R.id.adjustMediaCheckBox);
		TextView mediaTV = (TextView)findViewById(R.id.adjustMediaDescription);
		if (adjustMedia) {
			mediaCB.setChecked(true);
			mediaTV.setText("Media volume will be adjusted");
		} else {
			mediaCB.setChecked(false);
			mediaTV.setText("Media volume will not be adjusted");
		}
		
		//the media volumes slider
		SeekBar mediaSB = (SeekBar)findViewById(R.id.mediaSeekBar);
		mediaSB.setMax(DefPrefs.mediaVolumeMax);
		mediaSB.setProgress(mediaVolume);
		if (!adjustMedia) {
			mediaSB.setEnabled(false);
		} else {
			mediaSB.setEnabled(true);
		}
		mediaSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mediaVolume = progress;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
                //required stub
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
                //required stub
			}
		});

		
		//the alarm volumes toggle
		CheckBox alarmCB = (CheckBox)findViewById(R.id.adjustAlarmCheckBox);
		TextView alarmTV = (TextView)findViewById(R.id.adjustAlarmDescription);
		if (adjustAlarm) {
			alarmCB.setChecked(true);
			alarmTV.setText("Alarm volume will be adjusted");
		} else {
			alarmCB.setChecked(false);
			alarmTV.setText("Alarm volume will not be adjusted");
		}
		
		//the alarm volumes slider
		SeekBar alarmSB = (SeekBar)findViewById(R.id.alarmSeekBar);
		alarmSB.setMax(DefPrefs.alarmVolumeMax);
		alarmSB.setProgress(alarmVolume);
		if (!adjustAlarm) {
			alarmSB.setEnabled(false);
		} else {
			alarmSB.setEnabled(true);
		}
		alarmSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				alarmVolume = progress;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
                //required stub
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
                //required stub
			}
		});
		
		//the quick silence button
		StringBuilder sb = new StringBuilder("Silence for ");
	   if (quickSilenceHours != 0) {
		   sb.append(quickSilenceHours + " hours, ");
		}
		sb.append(quickSilenceMinutes + " minutes");
		TextView quickTV = (TextView)findViewById(R.id.quickSilenceDescription);
		quickTV.setText(sb.toString());
	}
	
	private void readSettings() {
		SharedPreferences preferences = this.getSharedPreferences("org.ciasaboark.tacere.preferences", Context.MODE_PRIVATE);
		isActivated = preferences.getBoolean("isActivated", DefPrefs.isActivated);
		ringerType = preferences.getInt("ringerType", DefPrefs.ringerType);
		adjustMedia = preferences.getBoolean("adjustMedia", DefPrefs.adjustMedia);
		mediaVolume = preferences.getInt("mediaVolume", DefPrefs.mediaVolume);
		adjustAlarm = preferences.getBoolean("adjustAlarm", DefPrefs.adjustAlarm);
		alarmVolume = preferences.getInt("alarmVolume", DefPrefs.alarmVolume);
		quickSilenceMinutes = preferences.getInt("quickSilenceMinutes", DefPrefs.quickSilenceMinutes);
		quickSilenceHours = preferences.getInt("quickSilenceHours", DefPrefs.quickSilenceHours);
		
		Log.d(TAG, "readSettings() called");
	}
	
	private void restoreDefaults() {
		Log.d(TAG, "restoreDefaults() called");
		
		SharedPreferences preferences = this.getSharedPreferences("org.ciasaboark.tacere.preferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putBoolean("isActivated", DefPrefs.isActivated);
		editor.putBoolean("silenceFreeTime", DefPrefs.silenceFreeTime);
		editor.putBoolean("silenceAllDay", DefPrefs.silenceAllDay);
		editor.putInt("ringerType", DefPrefs.ringerType);
		editor.putBoolean("adjustMedia", DefPrefs.adjustMedia);
		editor.putBoolean("adjustAlarm", DefPrefs.adjustAlarm);
		editor.putInt("mediaVolume", DefPrefs.mediaVolume);
		editor.putInt("alarmVolume", DefPrefs.alarmVolume);
		editor.putInt("quickSilenceMinutes", DefPrefs.quickSilenceMinutes);
		editor.putInt("quickSilenceHours", DefPrefs.quickSilenceHours);
		editor.putInt("refreshInterval", DefPrefs.refreshInterval);
		editor.putInt("bufferMinutes", DefPrefs.bufferMinutes);
		editor.putBoolean("wakeDevice", DefPrefs.wakeDevice);
		editor.commit();
		readSettings();
		refreshDisplay();
	}

	
	private void saveSettings() {
		Log.d(TAG, "saveSettings() called");
		SharedPreferences preferences = this.getSharedPreferences("org.ciasaboark.tacere.preferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isActivated", isActivated);
		editor.putInt("ringerType", 3);
		editor.putBoolean("adjustMedia", adjustMedia);
		editor.putBoolean("adjustAlarm", adjustAlarm);
		editor.putInt("mediaVolume", mediaVolume);
		editor.putInt("alarmVolume", alarmVolume);
		editor.putInt("quickSilenceMinutes", quickSilenceMinutes);
		editor.putInt("quickSilenceHours", quickSilenceHours);
		editor.commit();
	}

	public void onPause() {
		Log.d(TAG, "onPause() called");
		
		//save all changes to the preferences
		saveSettings();
		super.onPause();
	}


	public void onClickActivateService(View v) {
		isActivated = !isActivated;
		//if the service has been reactivated then we should restart it
		if (isActivated) {
			Intent i = new Intent(this, PollService.class);
			i.putExtra("type", "activityRestart");
			startService(i);
		}
		refreshDisplay();
	}

	public void onClickRingerType(View v) {
		Log.d(TAG, "onClickRingerType() called");
		//TODO show alert with ringer types to select
	}
	
	public void onClickAdjustMedia(View v) {
		adjustMedia = !adjustMedia;
		refreshDisplay();
	}
	
	public void onClickAdjustAlarm(View v) {
		adjustAlarm = !adjustAlarm;
		refreshDisplay();
	}
	
	public void onClickQuickSilence(View v) {
		Log.d(TAG, "onClickQuickSilence() called");
		
		LayoutInflater inflator = LayoutInflater.from(this);
		View view = inflator.inflate(R.layout.dialog_quicksilent, null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Quick Silence");
		builder.setView(view);
	
		final NumberPicker hourP = (NumberPicker)view.findViewById(R.id.hourPicker);
		final NumberPicker minP = (NumberPicker)view.findViewById(R.id.minutePicker);
		
		String[] hours = new String[25];
		String[] minutes = new String[61];
		
		for(int i = 0; i < hours.length; i++) {
            hours[i] = Integer.toString(i);
		}
		
		for(int i = 0; i < minutes.length; i++) {
			 StringBuilder sb = new StringBuilder(Integer.toString(i));
			    if (i < 10) {
			    	sb.insert(0, "0");
			}
            minutes[i] = sb.toString();
		}
		
		hourP.setMinValue(1);
		hourP.setMaxValue(hours.length - 1);
		hourP.setWrapSelectorWheel(false);
		hourP.setDisplayedValues(hours);
		hourP.setValue(quickSilenceHours + 1);
		
		minP.setMinValue(1);
		minP.setMaxValue(minutes.length - 1);
		minP.setWrapSelectorWheel(false);
		minP.setDisplayedValues(minutes);
		minP.setValue(quickSilenceMinutes + 1);
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				Log.d(TAG, "Selected hours: " + hourP.getValue());
				Log.d(TAG, "Selected minutes: " + minP.getValue());
				quickSilenceHours = hourP.getValue() - 1;
				quickSilenceMinutes = minP.getValue() - 1;
				saveSettings();
				refreshDisplay();
			}
		});
	           
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		//do nothing
	    	}
	    });
	    
	    builder.show();
	}
	

	
	public void onClickAdvancedSettings(View v) {
		Log.d(TAG, "onClickAdvancedSettings() called");
		
		Intent i = new Intent(this, AdvancedSettingsActivity.class);
		startActivity(i);
		
	}
	
}
