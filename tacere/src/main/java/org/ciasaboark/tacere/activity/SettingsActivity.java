/*
 * Created by Jonathan Nelson
 * 
 * Copyright 2013 Jonathan Nelson
 *
 * Released under the BSD license.  For details see the COPYING file.
*/

package org.ciasaboark.tacere.activity;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.graphics.drawable.Drawable;
import android.view.ViewAnimationUtils;
import android.widget.NumberPicker;
import android.widget.RemoteViews;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.ciasaboark.tacere.R;
import org.ciasaboark.tacere.R.id;
import org.ciasaboark.tacere.manager.VolumesManager;
import org.ciasaboark.tacere.prefs.Prefs;
import org.ciasaboark.tacere.provider.QuickSilenceProvider;
import org.ciasaboark.tacere.service.EventSilencerService;
import org.ciasaboark.tacere.service.RequestTypes;

import java.lang.annotation.Target;


public class SettingsActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "Settings";
	private final Context context = this;
	private final Prefs prefs = new Prefs(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		setupActionBar();
        Switch adjustAlarmSwitch = (Switch) findViewById(id.adjustAlarmCheckBox);
        adjustAlarmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.setAdjustAlarm(!prefs.getAdjustAlarm());
                drawAlarmWidgets();
            }
        });

        Switch adjustMediaSwitch = (Switch) findViewById(id.adjustMediaCheckBox);
        adjustMediaSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.setAdjustMedia(!prefs.getAdjustMedia());
                drawMediaWidgets();
            }
        });

        Switch serviceSwitch = (Switch) findViewById(id.activateServiceSwitch);
        serviceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.setIsServiceActivated(!prefs.getIsServiceActivated());
                //if the service has been reactivated then we should restart it
                if (prefs.getIsServiceActivated()) {
                    Intent i = new Intent(context, EventSilencerService.class);
                    i.putExtra("type", RequestTypes.ACTIVITY_RESTART);
                    startService(i);
                }
                drawServiceWidget();
                drawMediaWidgets();
                drawAlarmWidgets();
                drawRingerWidgets();
            }
        });


		drawAllWidgets();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setIcon(R.drawable.action_settings);
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
			Toast.makeText(getApplicationContext(),R.string.settings_restored, Toast.LENGTH_SHORT).show();
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

	private void drawAllWidgets() {
        drawServiceWidget();
        drawRingerWidgets();
        drawMediaWidgets();
        drawAlarmWidgets();
        drawQuickSilenceWidget();
    }

    private void drawQuickSilenceWidget() {
        //the quick silence button
        TextView quickTV = (TextView)findViewById(id.quickSilenceDescription);
        String quicksilenceText = getResources().getString(R.string.pref_quicksilent_duration);
        String hrs = "";
        if (prefs.getQuickSilenceHours() > 0) {
            hrs = String.valueOf(prefs.getQuickSilenceHours()) + " " + getString(R.string.hours_lower) + " ";
        }
        quickTV.setText(String.format(quicksilenceText, hrs, prefs.getQuicksilenceMinutes()));
    }

    private void drawAlarmWidgets() {
        TextView alarmTV = (TextView)findViewById(id.settings_alarmText);
        if (prefs.getIsServiceActivated()) {
            alarmTV.setTextColor(getResources().getColor(R.color.textcolor));
        } else {
            alarmTV.setTextColor(getResources().getColor(R.color.textColorDisabled));
        }

        //the alarm volumes toggle
        Switch alarmSwitch = (Switch)findViewById(id.adjustAlarmCheckBox);
        if (prefs.getAdjustAlarm()) {
            alarmSwitch.setChecked(true);
        } else {
            alarmSwitch.setChecked(false);
        }
        alarmSwitch.setEnabled(prefs.getIsServiceActivated());

        //the alarm volumes slider
        SeekBar alarmSB = (SeekBar)findViewById(id.alarmSeekBar);
        alarmSB.setMax(VolumesManager.getMaxAlarmVolume());
        alarmSB.setProgress(prefs.getCurAlarmVolume());
        if (prefs.getAdjustAlarm() && prefs.getIsServiceActivated()) {
            this.animateRevealView(alarmSB);
            alarmSB.setEnabled(true);
                    } else {
            alarmSB.setEnabled(false);
            this.animateHideView(alarmSB);
        }
        alarmSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.setCurAlarmVolume(progress);
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
    }

    private void drawServiceWidget() {
        //the service state toggle
        Switch serviceActivatedSwitch = (Switch)findViewById(id.activateServiceSwitch);
        TextView serviceTV = (TextView)findViewById(id.activateServiceDescription);
        if (prefs.getIsServiceActivated()) {
            serviceActivatedSwitch.setChecked(true);
            serviceTV.setText(R.string.pref_service_enabled);
        } else {
            serviceActivatedSwitch.setChecked(false);
            serviceTV.setText(R.string.pref_service_disabled);
        }
    }

    private void drawRingerWidgets() {
        //the ringer type description
        TextView ringerDescriptionTV = (TextView)findViewById(id.ringerTypeDescription);
        TextView ringerTV = (TextView)findViewById(id.settings_ringerTitle);

        Drawable icon;
        switch (prefs.getRingerType()) {
            case 1:
                ringerDescriptionTV.setText(R.string.pref_ringer_type_normal);
                icon = getResources().getDrawable(R.drawable.ringer_normal_cropped);
                break;
            case 2:
                ringerDescriptionTV.setText(R.string.pref_ringer_type_vibrate);
                icon = getResources().getDrawable(R.drawable.ringer_vibrate_cropped);
                break;
            default:
                ringerDescriptionTV.setText(R.string.pref_ringer_type_silent);
                icon = getResources().getDrawable(R.drawable.ringer_silent_cropped);
                break;
        }


        if (prefs.getIsServiceActivated()) {
            int iconColor = getResources().getColor(R.color.accent);
            icon.mutate().setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            ringerDescriptionTV.setTextColor(getResources().getColor(R.color.textcolor));
            ringerTV.setTextColor(getResources().getColor(R.color.textcolor));
        } else {
            int iconColor = getResources().getColor(android.R.color.darker_gray);
            icon.mutate().setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            ringerDescriptionTV.setTextColor(getResources().getColor(R.color.textColorDisabled));
            ringerTV.setTextColor(getResources().getColor(R.color.textColorDisabled));
        }

        ImageButton ringerIcon = (ImageButton)findViewById(id.settings_ringerIcon);
        setImageButtonIcon(ringerIcon, icon);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecated")
    private void setImageButtonIcon(ImageButton button, Drawable icon) {
        if (Build.VERSION.SDK_INT >= 16) {
            button.setBackground(icon);
        } else {
            button.setBackgroundDrawable(icon);
        }
    }

    private void drawMediaWidgets() {
        TextView mediaTV = (TextView)findViewById(id.settings_mediaText);
        if (prefs.getIsServiceActivated()) {
            mediaTV.setTextColor(getResources().getColor(R.color.textcolor));
        } else {
            mediaTV.setTextColor(getResources().getColor(R.color.textColorDisabled));
        }

        //the media volumes toggle
        Switch mediaSwitch = (Switch)findViewById(id.adjustMediaCheckBox);
        SeekBar mediaSB = (SeekBar)findViewById(id.mediaSeekBar);
        if (prefs.getAdjustMedia()) {
            mediaSwitch.setChecked(true);
        } else {
            mediaSwitch.setChecked(false);
        }
        mediaSwitch.setEnabled(prefs.getIsServiceActivated());


        //the media volumes slider
        mediaSB.setMax(VolumesManager.getMaxMediaVolume());
        mediaSB.setProgress(prefs.getCurMediaVolume());
        if (prefs.getAdjustMedia() && prefs.getIsServiceActivated()) {
            this.animateRevealView(mediaSB);
            mediaSB.setEnabled(true);
        } else {
            mediaSB.setEnabled(false);
            this.animateHideView(mediaSB);
        }
        mediaSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.setCurMediaVolume(progress);
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
    }

    @TargetApi(21)
    private void animateRevealView(View v) {
        int apiLevelAvailable = android.os.Build.VERSION.SDK_INT;
        if (apiLevelAvailable >= 20) {  //TODO this should really be API 21
            // previously invisible view
            v.setVisibility(View.VISIBLE);

            // get the center for the clipping circle
            int cx = (v.getLeft() + v.getRight()) / 2;
            int cy = (v.getTop() + v.getBottom()) / 2;

            // get the final radius for the clipping circle
            int finalRadius = v.getWidth();

            // create and start the animator for this view
            // (the start radius is zero)
            android.animation.ValueAnimator anim =
                    android.view.ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
            anim.start();
        }
    }

    @TargetApi(21)
    private void animateHideView(final View v) {
        int apiLevelAvailable = android.os.Build.VERSION.SDK_INT;
        if (apiLevelAvailable >= 20) {  //TODO this should really be API 21
            // get the center for the clipping circle
            int cx = (v.getLeft() + v.getRight()) / 2;
            int cy = (v.getTop() + v.getBottom()) / 2;

            // get the initial radius for the clipping circle
            int initialRadius = v.getWidth();

            // create the animation (the final radius is zero)
            ValueAnimator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, initialRadius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(View.GONE);
                }
            });

            // start the animation
            anim.start();
        }
    }
	
	private void restoreDefaults() {
		prefs.restoreDefaultPreferences();
        drawAllWidgets();
	}

	private void saveSettings() {
		//we also need to notify any active widgets of the settings change so
		//+ that they can redraw
		AppWidgetManager wManager = AppWidgetManager.getInstance(this.getApplicationContext());
		ComponentName qsWidget = new ComponentName(getApplicationContext(), QuickSilenceProvider.class);
		RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.quicksilence_widget_layout);
		int [] widgets = wManager.getAppWidgetIds(qsWidget);
		wManager.updateAppWidget(widgets, remoteViews);
	}

	public void onPause() {
		super.onPause();
	}

	public void onClickRingerType(View v) {
		AlertDialog alert = new AlertDialog.Builder(this)
				.setTitle(R.string.pref_ringer_type)
				.setSingleChoiceItems(R.array.ringer_types, prefs.getRingerType() - 1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						prefs.setRingerType(which + 1);
						saveSettings();
						drawRingerWidgets();
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//do nothing
						
					}
				})
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//do nothing
						
					}
				})
				.create();
				
		alert.show();
	}

	
	public void onClickQuickSilence(View v) {
		LayoutInflater inflator = LayoutInflater.from(this);
		View view = inflator.inflate(R.layout.dialog_quicksilent, null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.notification_quicksilence_title);
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
		hourP.setValue(prefs.getQuickSilenceHours() + 1);
		
		minP.setMinValue(1);
		minP.setMaxValue(minutes.length - 1);
		minP.setWrapSelectorWheel(false);
		minP.setDisplayedValues(minutes);
		minP.setValue(prefs.getQuicksilenceMinutes() + 1);
		
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				prefs.setQuickSilenceHours(hourP.getValue() - 1);
				prefs.setQuicksilenceMinutes(minP.getValue() - 1);
				saveSettings();
				drawQuickSilenceWidget();
			}
		});
	           
	    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		//do nothing
	    	}
	    });
	    
	    builder.show();
	}
	
	public void onClickAdvancedSettings(View v) {
		Intent i = new Intent(this, AdvancedSettingsActivity.class);
		startActivity(i);
	}
}