package com.quarkworks.apartmentgroceries.service;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by zz on 10/15/15.
 */
public class Utilities {
    private static final String TAG = Utilities.class.getSimpleName();

    public static String encodeURIComponent(String s) {
        String result = null;

        try
        {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    private Utilities() {
        super();
    }

    public static String getReadableDate(String dateString, String inputFormat, String outputFormat) {

        if (TextUtils.isEmpty(inputFormat)) {
            inputFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss.SSS'Z'";
        }

        if (TextUtils.isEmpty(outputFormat)) {
            outputFormat = "MM/dd/yyyy";
        }

        SimpleDateFormat inputDateFormt = new SimpleDateFormat(inputFormat);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);

        try {
            Date inputDate = inputDateFormt.parse(dateString);
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            calendar.setTime(inputDate);

            return outputDateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date string:" + dateString + ", with format string:" + inputFormat, e);
        }

        return null;
    }
}
