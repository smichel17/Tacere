/*
 * Copyright (c) 2014 Jonathan Nelson
 * Released under the BSD license.  For details see the COPYING file.
 */

package org.ciasaboark.tacere.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import org.ciasaboark.tacere.R;
import org.ciasaboark.tacere.activity.fragment.SelectCalendarsFragment;

public class SelectCalendarsActivity extends FragmentActivity {
    @SuppressWarnings("unused")
    private final String TAG = "SelectCalendarsActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_calendars);

        //this activity might be displayed as a dialog, so there might not be an actionbar
        if (getActionBar() != null) {
            Drawable upIcon = getResources().getDrawable(R.drawable.calendar_icon);
            int c = getResources().getColor(R.color.header_text_color);
            upIcon.mutate().setColorFilter(c, PorterDuff.Mode.MULTIPLY);
            getActionBar().setIcon(upIcon);
        }

        if (findViewById(R.id.select_calendars_fragment) != null) {
            if (savedInstanceState == null) {
                SelectCalendarsFragment selectCalendarsFragment = new SelectCalendarsFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.select_calendars_fragment, selectCalendarsFragment).commit();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_calendars, menu);
        return true;
    }
}
