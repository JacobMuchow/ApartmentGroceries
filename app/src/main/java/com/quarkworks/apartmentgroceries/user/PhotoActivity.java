package com.quarkworks.apartmentgroceries.user;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.DataStore;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = PhotoActivity.class.getSimpleName();

    private static final int SELECT_PICTURE_REQUEST_CODE = 1;
    private Uri outputFileUri;

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private Button addPhotoButton;
    private ImageView photoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);

        /**
         * Get view references
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        photoImageView = (ImageView) findViewById(R.id.photo_image_view_id);
        addPhotoButton = (Button) findViewById(R.id.photo_add_button_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_user_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
        String userId = sharedPreferences.getString(RUser.JsonKeys.USER_ID, null);

        RUser rUser = DataStore.getInstance().getRealm().where(RUser.class)
                .equalTo(RUser.RealmKeys.USER_ID, userId).findFirst();
        Glide.with(this)
                .load(rUser.getUrl())
                .placeholder(R.drawable.ic_launcher)
                .centerCrop()
                .crossFade()
                .into(photoImageView);
        SyncUser.getById(rUser.getUserId());

    }

    private void openImageIntent() {
        // root to save image
        String directoryName = dateToString(new Date(), getString(R.string.photo_date_format_string));
        Log.d(TAG, directoryName);
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + directoryName + File.separator);
        root.mkdirs();
        final File sdImageMainDirectory = new File(root, directoryName);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // camera
        final List<Intent> camaraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCamera = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCamera) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            camaraIntents.add(intent);
        }

        // file
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.photo_select_source));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, camaraIntents.toArray(new Parcelable[camaraIntents.size()]));
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
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data.getData();
                }

                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(selectedImageUri);

                    byte[] inputData;
                    try {
                        String photoName = dateToString(new Date(), getString(R.string.photo_date_format_string));
                        inputData = getBytes(inputStream);
                        SyncUser.updateProfilePhoto(photoName + ".jpg", inputData).onSuccess(new Continuation<JSONObject, Void>() {
                            @Override
                            public Void then(Task<JSONObject> task) {
                                try {
                                    String updatedAt = task.getResult().getString(RUser.JsonKeys.UPDATED_AT);
                                    if (!TextUtils.isEmpty(updatedAt)) {
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.photo_update_success), Toast.LENGTH_SHORT).show();
                                        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.login_or_sign_up_session), 0);
                                        String userId = sharedPreferences.getString(RUser.JsonKeys.USER_ID, null);

                                        SyncUser.getById(userId);

                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.photo_update_failure), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error updating photo", e);
                                }
                                return null;
                            }
                        }, Task.UI_THREAD_EXECUTOR);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading image byte data from uri");
                    }

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Error file with uri " + selectedImageUri + " not found", e);
                }

                photoImageView.setImageURI(selectedImageUri);
            }
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String dateToString (Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }
}
