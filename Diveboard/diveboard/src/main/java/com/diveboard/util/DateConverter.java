package com.diveboard.util;

import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import androidx.databinding.InverseMethod;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;

public class DateConverter {
    private Context context;

    public DateConverter(Context context) {
        this.context = context;
    }

    @InverseMethod("convertStringToDate")
    public String convertDateToString(Calendar date) {
        DateFormat df = getDateFormat(context);
        return df.format(date.getTime());
    }

    public Calendar convertStringToDate(String str) {
        DateFormat df = getDateFormat(context);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(df.parse(str));
            return cal;
        } catch (ParseException e) {

        }
        return null;
    }

    public Calendar convertStringToTime(String str) {
        DateFormat df = getTimeFormat(context);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(df.parse(str));
            return cal;
        } catch (ParseException e) {

        }
        return null;
    }

    @InverseMethod("convertStringToTime")
    public String convertTimeToString(Calendar time) {
        DateFormat df = getTimeFormat(context);
        return df.format(time.getTime());
    }
}