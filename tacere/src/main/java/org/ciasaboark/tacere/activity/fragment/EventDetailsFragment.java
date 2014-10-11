/*
 * Copyright (c) 2014 Jonathan Nelson
 * Released under the BSD license.  For details see the COPYING file.
 */

package org.ciasaboark.tacere.activity.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ciasaboark.tacere.R;
import org.ciasaboark.tacere.database.DataSetManager;
import org.ciasaboark.tacere.database.DatabaseInterface;
import org.ciasaboark.tacere.database.NoSuchEventInstanceException;
import org.ciasaboark.tacere.event.EventInstance;
import org.ciasaboark.tacere.event.EventManager;
import org.ciasaboark.tacere.event.ringer.RingerSource;
import org.ciasaboark.tacere.event.ringer.RingerType;
import org.ciasaboark.tacere.prefs.Prefs;

import java.util.HashMap;

public class EventDetailsFragment extends DialogFragment {
    public static final String TAG = "EventLongClickFragment";
    private DatabaseInterface databaseInterface;
    private Prefs prefs;
    private EventInstance event;
    private int instanceId;
    private Context context;
    private View view;

    public static EventDetailsFragment newInstance(EventInstance eventInstance) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putLong("instanceId", eventInstance.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        long instanceId = getArguments().getLong("instanceId");
        context = getActivity().getApplicationContext();
        databaseInterface = DatabaseInterface.getInstance(context);
        prefs = new Prefs(context);

        try {
            event = databaseInterface.getEvent(instanceId);
        } catch (NoSuchEventInstanceException e) {
            Log.e(TAG, "unable to find event with id " + instanceId);
            return null;
        }

        AlertDialog.Builder thisDialog = new AlertDialog.Builder(getActivity());
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_event_longclick, null);
        setupWidgetsForView();
        thisDialog.setView(view);

        //the clear button should only be visible if the event has a custom ringer or if the events
        //series has a custom ringer set
        EventManager eventManager = new EventManager(context, event);

        if (eventManager.getRingerSource() == RingerSource.EVENT_SERIES ||
                eventManager.getRingerSource() == RingerSource.INSTANCE) {
            thisDialog.setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    resetEvent();
                }
            });
        }


        thisDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //nothing to do here
            }
        });

        //the positive button should only be enabled if the selected ringer is not UNDEFINED
        AlertDialog dialog = thisDialog.create();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        thisDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveSettings();
            }
        });

        Dialog d = thisDialog.create();
        d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return d;
    }

    private void setupWidgetsForView() {
        TextView eventTitle = (TextView) view.findViewById(R.id.event_title);
        eventTitle.setText(event.getTitle());

        TextView calendarTitle = (TextView) view.findViewById(R.id.calendar_title);
        calendarTitle.setText(databaseInterface.getCalendarNameForId(event.getCalendarId()));

        LinearLayout eventRepetitonBox = (LinearLayout) view.findViewById(R.id.event_details_repetitions_box);
        if (databaseInterface.doesEventRepeat(event.getEventId())) {
            eventRepetitonBox.setVisibility(View.VISIBLE);
            TextView eventRepetitionText = (TextView) view.findViewById(R.id.event_details_repetition_text);
            long eventRepetitions = databaseInterface.getEventRepetitionCount(event.getEventId());
            eventRepetitionText.setText("Repeats " + eventRepetitions + " times");
        } else {
            eventRepetitonBox.setVisibility(View.GONE);
        }

        colorizeIcons();
        drawIndicators();

        ImageButton buttonNormal = (ImageButton) view.findViewById(R.id.imageButtonNormal);
        ImageButton buttonVibrate = (ImageButton) view.findViewById(R.id.imageButtonVibrate);
        ImageButton buttonSilent = (ImageButton) view.findViewById(R.id.imageButtonSilent);
        ImageButton buttonIgnore = (ImageButton) view.findViewById(R.id.imageButtonIgnore);
        buttonNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRingerType(RingerType.NORMAL);
            }
        });

        buttonVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRingerType(RingerType.VIBRATE);
            }
        });

        buttonSilent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRingerType(RingerType.SILENT);
            }
        });

        buttonIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRingerType(RingerType.IGNORE);
            }
        });

        //only display the checkbox if there are multiple instances of this event
        CheckBox cb = (CheckBox) view.findViewById(R.id.all_events_checkbox);
        boolean eventRepeats = databaseInterface.doesEventRepeat(event.getEventId());
        if (eventRepeats) {
            cb.setText(R.string.event_details_checkbox);
        } else {
            cb.setVisibility(View.GONE);
        }
    }

    private void resetEvent() {
        CheckBox cb = (CheckBox) view.findViewById(R.id.all_events_checkbox);
        if (cb.isChecked()) {
            resetAllEvents();
        } else {
            databaseInterface.setRingerForInstance(event.getId(), RingerType.UNDEFINED);
        }
        notifyDatasetChanged();
    }

    private void saveSettings() {
        CheckBox cb = (CheckBox) view.findViewById(R.id.all_events_checkbox);
        if (cb.isChecked()) {
            //if the checkbox is checked then we need to make sure to erase any previously chosen
            //ringers for all instances before saving the current settings
            resetAllEvents();
            saveSettingsForAllEvents();
        } else {
            databaseInterface.setRingerForInstance(event.getId(), event.getRingerType());
        }
        notifyDatasetChanged();
    }

    private void colorizeIcons() {
        HashMap<ImageButton, RingerType> buttons = new HashMap<ImageButton, RingerType>();
        buttons.put((ImageButton) view.findViewById(R.id.imageButtonNormal), RingerType.NORMAL);
        buttons.put((ImageButton) view.findViewById(R.id.imageButtonVibrate), RingerType.VIBRATE);
        buttons.put((ImageButton) view.findViewById(R.id.imageButtonSilent), RingerType.SILENT);
        buttons.put((ImageButton) view.findViewById(R.id.imageButtonIgnore), RingerType.IGNORE);

        for (ImageButton thisButton : buttons.keySet()) {
            thisButton.setImageDrawable(getColorizedIcon(buttons.get(thisButton)));
        }


    }

    private void drawIndicators() {
        LinearLayout indicatorNormal = (LinearLayout) view.findViewById(R.id.indicator_normal);
        LinearLayout indicatorVibrate = (LinearLayout) view.findViewById(R.id.indicator_vibrate);
        LinearLayout indicatorSilent = (LinearLayout) view.findViewById(R.id.indicator_silent);
        LinearLayout indicatorIgnore = (LinearLayout) view.findViewById(R.id.indicator_ignore);

        int colorActive = getResources().getColor(R.color.accent);
        int colorInactive = getResources().getColor(R.color.primary);
        RingerType ringerMode = event.getRingerType();

        if (ringerMode == RingerType.NORMAL) {
            indicatorNormal.setBackgroundColor(colorActive);
        } else {
            indicatorNormal.setBackgroundColor(colorInactive);
        }

        if (ringerMode == RingerType.VIBRATE) {
            indicatorVibrate.setBackgroundColor(colorActive);
        } else {
            indicatorVibrate.setBackgroundColor(colorInactive);
        }

        if (ringerMode == RingerType.SILENT) {
            indicatorSilent.setBackgroundColor(colorActive);
        } else {
            indicatorSilent.setBackgroundColor(colorInactive);
        }

        if (ringerMode == RingerType.IGNORE) {
            indicatorIgnore.setBackgroundColor(colorActive);
        } else {
            indicatorIgnore.setBackgroundColor(colorInactive);
        }
    }

    private void setRingerType(RingerType type) {
        event.setRingerType(type);
        drawIndicators();
        colorizeIcons();
    }

    private void resetAllEvents() {
        prefs.unsetRingerTypeForEventSeries(event.getEventId());
        databaseInterface.setRingerForAllInstancesOfEvent(event.getEventId(),
                RingerType.UNDEFINED);
    }

    private void notifyDatasetChanged() {
        DataSetManager dsm = new DataSetManager(this, context);
        dsm.broadcastDataSetChangedMessage();
    }

    private void saveSettingsForAllEvents() {
        prefs.setRingerForEventSeries(event.getEventId(), event.getRingerType());
    }

    private Drawable getColorizedIcon(RingerType ringerType) {
        Drawable colorizedIcon;
        switch (ringerType) {
            case NORMAL:
                colorizedIcon = getResources().getDrawable(R.drawable.ic_state_normal);
                break;
            case VIBRATE:
                colorizedIcon = getResources().getDrawable(R.drawable.ic_state_vibrate);
                break;
            case SILENT:
                colorizedIcon = getResources().getDrawable(R.drawable.ic_state_silent);
                break;
            default:
                colorizedIcon = getResources().getDrawable(R.drawable.ic_state_ignore);
        }

        int color = getResources().getColor(R.color.primary);
        if (event.getRingerType() == ringerType) {
            color = getResources().getColor(R.color.accent);
        }

        colorizedIcon.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        return colorizedIcon;
    }
}
