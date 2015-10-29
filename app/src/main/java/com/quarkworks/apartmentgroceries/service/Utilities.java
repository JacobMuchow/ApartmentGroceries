package com.quarkworks.apartmentgroceries.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static String dateToString (Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] inputData, int offset,
                                                           int width, int height) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(inputData, 0, inputData.length, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(inputData, 0, inputData.length, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int getCenterCropDimensionForBitmap(Bitmap bitmap) {
        int dimension;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width >= height) {
            dimension = height;
        } else {
            dimension = width;
        }

        return dimension;
    }

    public static Bitmap padBitmap(Bitmap input, int paddingX, int paddingY){
        Bitmap outputBitmap = Bitmap.createBitmap(input.getWidth() + 2 * paddingX, input.getHeight() + paddingY * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawBitmap(input, paddingX, paddingY, null);

        return outputBitmap;
    }
}
