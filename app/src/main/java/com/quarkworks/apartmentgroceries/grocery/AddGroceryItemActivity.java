package com.quarkworks.apartmentgroceries.grocery;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.Utilities;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class AddGroceryItemActivity extends AppCompatActivity {
    private static final String TAG = AddGroceryItemActivity.class.getSimpleName();

    private static final int SELECT_PICTURE_REQUEST_CODE = 1;
    private Uri outputFileUri;
    private ArrayList<byte[]> photoList;
    private ImageAdapter imageAdapter;

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private TextView titleRightTextView;
    private EditText groceryItemNameEditText;
    private GridView photoGridView;

    public static void newIntent(Context context) {
        Intent intent = new Intent(context, AddGroceryItemActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_grocery_activity);

        /**
         * Get view references
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        titleRightTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_right_id);
        groceryItemNameEditText = (EditText) findViewById(R.id.add_grocery_item_name_id);
        photoGridView = (GridView) findViewById(R.id.add_grocery_grid_view_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.cancel));
        titleRightTextView.setText(getString(R.string.post));

        photoList = new ArrayList<>();
        Bitmap cameraIconBitmap = getCameraIconBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        cameraIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] sampledInputData = stream.toByteArray();
        photoList.add(sampledInputData);
        imageAdapter = new ImageAdapter(this, photoList);
        photoGridView.setAdapter(imageAdapter);

        /**
         * Set view OnClickListener
         */
        titleTextView.setOnClickListener(cancelOnClick);
        titleRightTextView.setOnClickListener(postOnClick);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private View.OnClickListener cancelOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HomeActivity.newIntent(AddGroceryItemActivity.this);
        }
    };

    private View.OnClickListener postOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String groceryItemName = groceryItemNameEditText.getText().toString();

            if (!groceryItemName.isEmpty()) {
                SharedPreferences sharedPreferences = getApplication()
                        .getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
                String groupId = sharedPreferences.getString(RUser.JsonKeys.GROUP_ID, null);
                String userId = sharedPreferences.getString(RUser.JsonKeys.USER_ID, null);

                RGroceryItem rGroceryItem = new RGroceryItem();
                rGroceryItem.setName(groceryItemName);
                rGroceryItem.setGroupId(groupId);
                rGroceryItem.setCreatedBy(userId);

                photoList.remove(photoList.size() - 1);
                SyncGroceryItem.add(rGroceryItem, photoList).onSuccess(addGroceryItemOnSuccess, Task.UI_THREAD_EXECUTOR);
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.grocery_item_name_empty), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Continuation<Void, Void> addGroceryItemOnSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Failed to add grocery", task.getError());
                Toast.makeText(getApplicationContext(),
                        getString(R.string.add_grocery_item_failure), Toast.LENGTH_SHORT).show();
                return null;
            }

            HomeActivity.newIntent(AddGroceryItemActivity.this);
            Toast.makeText(getApplicationContext(), getString(R.string.add_grocery_item_success),
                    Toast.LENGTH_SHORT).show();

            return null;
        }
    };

    private class ImageAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<byte[]> photoList;

        public ImageAdapter(Context context, ArrayList<byte[]> photoList) {
            if (photoList == null) photoList = new ArrayList<>();
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
    }

    private void openImageIntent() {
        // root to save image
        String directoryName = Utilities.dateToString(new Date(), getString(R.string.photo_date_format_string));
        Log.d(TAG, directoryName);
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + directoryName + File.separator);
        root.mkdirs();
        final File sdImageMainDirectory = new File(root, directoryName);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // camera
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCamera = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCamera) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // file
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.photo_select_source));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, SELECT_PICTURE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data.getData();
                }

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    try {
                        byte[] inputData = Utilities.getBytesFromInputStream(inputStream);
                        Bitmap bitmap = Utilities.decodeSampledBitmapFromByteArray(inputData, 0, 400, 400);
                        int dimension = Utilities.getCenterCropDimensionForBitmap(bitmap);
                        bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] sampledInputData = stream.toByteArray();
                        if (photoList == null) photoList = new ArrayList<>();
                        byte[] cameraBytes = photoList.get(photoList.size() - 1);
                        photoList.remove(photoList.size() - 1);
                        photoList.add(sampledInputData);
                        photoList.add(cameraBytes);
                        imageAdapter.notifyDataSetChanged();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading image byte data from uri");
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Error file with uri " + selectedImageUri + " not found", e);
                }
            }
        }
    }

    private Bitmap getCameraIconBitmap() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_local_see_white_48dp);
        Bitmap originalBitmap = ((BitmapDrawable)drawable).getBitmap();
        Paint paint = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(filter);
        Bitmap cameraIconBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(cameraIconBitmap);
        canvas.drawBitmap(originalBitmap, 0, 0, paint);

        return Utilities.padBitmap(cameraIconBitmap, 20, 20);
    }
}
