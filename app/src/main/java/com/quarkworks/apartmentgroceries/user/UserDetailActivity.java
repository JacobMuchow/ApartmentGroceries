package com.quarkworks.apartmentgroceries.user;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.MyDialogFragment;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.Utilities;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import org.json.JSONObject;

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

public class UserDetailActivity extends AppCompatActivity {
    private static final String TAG = UserDetailActivity.class.getSimpleName();

    private static final int SELECT_PICTURE_REQUEST_CODE = 1;
    private static final String USER_ID = "userId";
    private String userId;
    private Uri outputFileUri;

    /**
     * References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private TextView editPhotoTextView;
    private TextView usernameTextView;
    private TextView phoneTextView;
    private ImageView profileImageView;

    public static void newIntent(Context context, String userId) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail_activity);

        userId = getIntent().getStringExtra(USER_ID);
        RUser rUser = DataStore.getInstance().getRealm().where(RUser.class)
                .equalTo(USER_ID, userId).findFirst();

        /**
         * Get view reference
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        editPhotoTextView = (TextView) findViewById(R.id.user_detail_edit_photo_text_view_id);
        usernameTextView = (TextView) findViewById(R.id.user_detail_edit_username_text_view_id);
        phoneTextView = (TextView) findViewById(R.id.user_detail_edit_phone_text_view_id);
        profileImageView = (ImageView) findViewById(R.id.user_detail_profile_image_view_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_user_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        usernameTextView.setText(rUser.getUsername());
        phoneTextView.setText(rUser.getPhone());

        Glide.with(this)
                .load(rUser.getUrl())
                .placeholder(R.drawable.ic_launcher)
                .centerCrop()
                .crossFade()
                .into(profileImageView);

        /**
         * Set view on click
         */
        editPhotoTextView.setOnClickListener(editPhotoTextViewOnClick);
        phoneTextView.setOnClickListener(phoneOnClick);
    }

    private View.OnClickListener editPhotoTextViewOnClick =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openImageIntent();
        }
    };

    private View.OnClickListener phoneOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            android.app.FragmentManager manager = getFragmentManager();
            MyDialogFragment editPhoneDialog = MyDialogFragment.newInstance("phone");
            editPhoneDialog.show(manager, "phone");
        }
    };

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
                        String photoName = Utilities.dateToString(new Date(), getString(R.string.photo_date_format_string));
                        byte[] inputData = Utilities.getBytesFromInputStream(inputStream);
                        Bitmap bitmap = Utilities.decodeSampledBitmapFromByteArray(inputData, 0, 400, 400);
                        int dimension = Utilities.getCenterCropDimensionForBitmap(bitmap);
                        bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] sampledInputData = stream.toByteArray();

                        Continuation<JSONObject, Void> checkUpdatingPhoto = new Continuation<JSONObject, Void>() {
                            @Override
                            public Void then(Task<JSONObject> task) {
                                if (task.isFaulted()) {
                                    Log.e(TAG, "Failed updating photo", task.getError());
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.photo_update_failure), Toast.LENGTH_SHORT).show();
                                    return null;
                                }

                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.photo_update_success), Toast.LENGTH_SHORT).show();

                                SyncUser.getById(userId).continueWith(new Continuation<RUser, Void>() {
                                    @Override
                                    public Void then(Task<RUser> task) throws Exception {
                                        if (task.isFaulted()) {
                                            Log.e(TAG, "Failed to add grocery", task.getError());
                                            return null;
                                        }

                                        Glide.with(getApplication())
                                                .load(task.getResult().getUrl())
                                                .placeholder(R.drawable.ic_launcher)
                                                .centerCrop()
                                                .crossFade()
                                                .into(profileImageView);

                                        return null;
                                    }
                                }, Task.UI_THREAD_EXECUTOR);

                                return null;
                            }
                        };

                        SyncUser.updateProfilePhoto(photoName + ".jpg", sampledInputData)
                                .continueWith(checkUpdatingPhoto, Task.UI_THREAD_EXECUTOR);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading image byte data from uri");
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Error file with uri " + selectedImageUri + " not found", e);
                }
            }
        }
    }
}
