package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.GroceryItemBuilder;
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.Utilities;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import bolts.Continuation;
import bolts.Task;

public class AddGroceryItemActivity extends AppCompatActivity {
    private static final String TAG = AddGroceryItemActivity.class.getSimpleName();

    private static final int SELECT_PICTURE_REQUEST_CODE = 1;
    private ArrayList<byte[]> photoList;
    private GroceryImageAdapter imageAdapter;

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
        imageAdapter = new GroceryImageAdapter(this, photoList);
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
                RGroceryItem rGroceryItem = new RGroceryItem();
                rGroceryItem.setName(groceryItemName);

                photoList.remove(photoList.size() - 1);

                GroceryItemBuilder builder = new GroceryItemBuilder();
                builder.setGroceryName(groceryItemName);
                builder.setPhotoList(photoList);

                SyncGroceryItem.add(builder).onSuccess(addGroceryItemOnSuccess, Task.UI_THREAD_EXECUTOR);
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
                    selectedImageUri = imageAdapter.outputFileUri;
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
