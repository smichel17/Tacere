package org.ciasaboark.tacere.prefs;

import android.test.AndroidTestCase;

import junit.framework.TestCase;

public class PrefsTest extends AndroidTestCase {

    @Test (expected = IllegalArgumentException.class)
    public void testStorePreferenceWithNullKey() {
        Prefs prefs = new Prefs(this.getContext());
        prefs.storePreference(null, "foo");
    }

    @Test (expected = IllegalArgumentException.class)
    public void testStorePreferenceWithNullValue() {
        Prefs prefs = new Prefs(this.getContext());
        prefs.storePreference("foo", null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testStoreUnknownValueObject() {
        Prefs prefs = new Prefs(this.getContext());
        prefs.storePreference("foo", new Object());
    }

    public void testStoreIntegerValue() {
        Prefs prefs = new Prefs(this.getContext());
        prefs.storePreference("foo", 1);
        assert(prefs.readInt("foo") == 1);
    }

    public void testStoreBooleanValue() {
        Prefs prefs = new Prefs(this.getContext());
        prefs.storePreference("foo", false);
        assert(prefs.getBoolean("foo") == false);
    }

    public void testStoreLongValue() {
        Prefs prefs = new Prefs(this.getContext());
        prefs.storePreference("foo", 999l);
        assert(prefs.readLong("foo") == 999l);
    }

    public void testRestoreDefaultValues() {
        Prefs prefs = new Prefs(this.getContext());
        prefs.restoreDefaultPreferences();
        assert(prefs.getAdjustAlarm() == DefaultPrefs.ADJUST_ALARM);
        assert(prefs.getAdjustMedia() == DefaultPrefs.ADJUST_MEDIA);
        assert(prefs.getIsServiceActivated() == DefaultPrefs.IS_ACTIVATED);
        assert(prefs.getSilenceAllDayEvents() == DefaultPrefs.SILENCE_ALL_DAY);
        assert(prefs.getSilenceFreeTimeEvents() == DefaultPrefs.SILENCE_FREE_TIME);
        assert(prefs.getBufferMinutes() == DefaultPrefs.BUFFER_MINUTES);
        assert(prefs.getCurAlarmVolume() == DefaultPrefs.ALARM_VOLUME);
        assert(prefs.getCurMediaVolume() == DefaultPrefs.MEDIA_VOLUME);
        assert(prefs.getLookaheadDays() == DefaultPrefs.LOOKAHEAD_DAYS);
        assert(prefs.getQuickSilenceHours() == DefaultPrefs.QUICK_SILENCE_HOURS);
        assert(prefs.getQuicksilenceMinutes() == DefaultPrefs.QUICK_SILENCE_MINUTES);
        assert(prefs.getRingerType() == DefaultPrefs.RINGER_TYPE);
    }

    public void test

    public void testRestoreDefaultPreferences() throws Exception {

    }

}