/*
 * Created by Jonathan Nelson
 * 
 * Copyright 2013 Jonathan Nelson
 *
 * Released under the BSD license.  For details see the COPYING file.
*/
package org.ciasaboark.tacere.activity;

import org.ciasaboark.tacere.R;
import org.ciasaboark.tacere.R.id;
import org.ciasaboark.tacere.R.layout;
import org.ciasaboark.tacere.prefs.Prefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DonationActivity extends Activity {
	private Context context = this;
    private static final String SHOW_DONATION_THANKS = "SHOW_DONATION_THANKS";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donation);
        this.setTitle(R.string.donation_title);
	}
	 
	@Override
    public void onStart() {
		super.onStart();
    	
		Button closeButton = (Button)findViewById(R.id.donation_button_close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableDonationDialog();
				finish();
			}
		});
    }

    private void disableDonationDialog() {
        Prefs prefs = new Prefs(this);
        try {
            prefs.storePreference(SHOW_DONATION_THANKS, true);
        } catch (IllegalArgumentException e) {
            //boolean values are accepted, should not reach here
        }
    }

    public static void showDonationDialogIfNeeded(Context ctx) {
        //the donation dialog should only be shown if the donation key is also installed,
        //and only if it has not already been shown
        boolean isKeyInstalled = isDonationKeyInstalled(ctx);
        boolean hasDialogAlreadyBeenShown = hasDonationDialogAlreadyBeenShown(ctx);

        if (isKeyInstalled && !hasDialogAlreadyBeenShown) {
            Intent donationIntent = new Intent(ctx.getApplicationContext(), DonationActivity.class);
            ctx.startActivity(donationIntent);
        }
    }

    private static boolean isDonationKeyInstalled(Context ctx) {
        PackageManager manager = ctx.getPackageManager();
        boolean isKeyInstalled = manager.checkSignatures("org.ciasaboark.tacere", "org.ciasaboark.tacere.key") == PackageManager.SIGNATURE_MATCH;
        return isKeyInstalled;
    }

    private static boolean hasDonationDialogAlreadyBeenShown(Context ctx) {
        Prefs prefs = new Prefs(ctx);
        boolean hasBeenShown = false;
        try {
            boolean value = prefs.getBoolean(SHOW_DONATION_THANKS);
            if (value) {
                hasBeenShown = true;
            }
        } catch (IllegalArgumentException e) {}
        return hasBeenShown;
    }
}
