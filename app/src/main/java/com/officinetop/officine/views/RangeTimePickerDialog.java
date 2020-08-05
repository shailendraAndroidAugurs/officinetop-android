package com.officinetop.officine.views;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RangeTimePickerDialog extends TimePickerDialog {

    private int mMinHour = -1;
    private int mMinMinute = -1;
    private int mMaxHour = 100;
    private int mMaxMinute = 100;
    private int mCurrentHour;
    private int mCurrentMinute;
    private boolean is24HourDay;

    public RangeTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay,
                                 int minute, boolean is24HourView) {
        super(context, callBack, hourOfDay, minute, is24HourView);
        mCurrentHour = hourOfDay;
        mCurrentMinute = minute;
        is24HourDay = is24HourView;
        // Somehow the onTimeChangedListener is not set by TimePickerDialog
        // in some Android Versions, so, Adding the listener using
        // reflections
        try {
            Class<?> superclass = getClass().getSuperclass();
            Field mTimePickerField = superclass.getDeclaredField("mTimePicker");
            mTimePickerField.setAccessible(true);
            TimePicker mTimePicker = (TimePicker) mTimePickerField.get(this);
            mTimePicker.setOnTimeChangedListener(this);
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }

    public void setMin(int hour, int minute) {
        mMinHour = hour;
        mMinMinute = minute;
    }

    public void setMax(int hour, int minute) {
        mMaxHour = hour;
        mMaxMinute = minute;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        super.onTimeChanged(view, hourOfDay, minute);
        boolean validTime;
        validTime = ((hourOfDay >= mMinHour) && (hourOfDay != mMinHour || minute >= mMinMinute))
                && ((hourOfDay <= mMaxHour) && (hourOfDay != mMaxHour || minute <= mMaxMinute));
        if (validTime) {
            mCurrentHour = hourOfDay;
            mCurrentMinute = minute;

        } else {
            updateTime(mCurrentHour, mCurrentMinute);

        }
    }
}