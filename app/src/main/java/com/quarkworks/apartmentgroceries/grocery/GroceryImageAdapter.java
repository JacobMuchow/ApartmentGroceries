package com.quarkworks.apartmentgroceries.grocery;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhao on 10/29/15.
 */
public class GroceryImageAdapter extends BaseAdapter {
    private static final String TAG = GroceryImageAdapter.class.getSimpleName();

    private static final int SELECT_PICTURE_REQUEST_CODE = 1;
    private Context context;
    public ArrayList<byte[]> photoList;
    public Uri outputFileUri;

    public GroceryImageAdapter(Context context, ArrayList<byte[]> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    public int getCount() {
        return photoList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        View rootView = View.inflate(context, R.layout.add_grocery_grid_view_item, null);
        imageView = (ImageView) rootView.findViewById(R.id.add_grocery_grid_view_image_view_id);

        byte[] photoBytes = photoList.get(position);
        Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        imageView.setImageBitmap(bitmap);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == photoList.size() - 1) {
                    openImageIntent();
                }
            }
        });
        return rootView;
    }

    private void openImageIntent() {
        /*
            root to save image
         */
        String directoryName = Utilities.dateToString(new Date(), context.getString(R.string.photo_date_format_string));
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + directoryName + File.separator);
        root.mkdirs();
        final File sdImageMainDirectory = new File(root, directoryName);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        /*
            camera
         */
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCamera = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCamera) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        /*
            file
         */
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, context.getString(R.string.photo_select_source));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        ((Activity)context).startActivityForResult(chooserIntent, SELECT_PICTURE_REQUEST_CODE);
    }
}
