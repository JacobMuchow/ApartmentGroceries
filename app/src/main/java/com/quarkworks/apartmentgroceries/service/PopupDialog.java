package com.quarkworks.apartmentgroceries.service;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by zhao on 10/31/15.
 */
public class PopupDialog extends DialogFragment {
    private static final String TAG = PopupDialog.class.getSimpleName();

    private static final String TITLE = "title";
    private static final String OLD_VALUE = "oldValue";
    private static final String FIELD_NAME = "fieldName";
    private String title;
    private String fieldName;
    private String oldValue;

    private EditText editText;

    public static PopupDialog newInstance(String title, String fieldName, String oldValue) {
        PopupDialog fragment = new PopupDialog();

        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(FIELD_NAME, fieldName);
        args.putString(OLD_VALUE, oldValue);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString(TITLE);
        fieldName = getArguments().getString(FIELD_NAME);
        oldValue = getArguments().getString(OLD_VALUE);

        int style = DialogFragment.STYLE_NO_TITLE;
        setStyle(style, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.popup_dialog, container, false);

        /*
            Reference
         */
        TextView titleTextView = (TextView) rootView.findViewById(R.id.popup_dialog_title_text_view_id);
        editText = (EditText) rootView.findViewById(R.id.popup_dialog_edit_text_id);
        TextView cancelTextView = (TextView) rootView.findViewById(R.id.popup_dialog_cancel_id);
        TextView saveTextView = (TextView) rootView.findViewById(R.id.popup_dialog_save_id);

        /*
            Set view data
         */
        titleTextView.setText(title);
        editText.setText(oldValue);

        /*
            Set view OnClickListener
         */
        cancelTextView.setOnClickListener(cancelTextViewOnClick);
        saveTextView.setOnClickListener(saveTextViewOnClick);

        editText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return rootView;
    }

    private View.OnClickListener cancelTextViewOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    private View.OnClickListener saveTextViewOnClick = new View.OnClickListener() {
        @Override
        public  void onClick(View v) {

            String newValue = editText.getText().toString();
            if (!TextUtils.isEmpty(newValue) && !newValue.equals(oldValue)) {
                Continuation<Void, Void> onUpdateProfileFinished = new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception{
                        if (task.isFaulted()) {
                            Exception exception = task.getError();
                            Log.e(TAG, "Error in updateProfile", exception);
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            throw exception;
                        }
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.update_failure), Toast.LENGTH_SHORT).show();
                        dismiss();

                        return null;
                    }
                };
                SyncUser.updateProfile(fieldName, newValue).continueWith(onUpdateProfileFinished, Task.UI_THREAD_EXECUTOR);

            } else if (!newValue.equals(oldValue) && TextUtils.isEmpty(newValue)){
                Toast.makeText(getActivity().getApplicationContext(), "Please input " + fieldName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.update_success), Toast.LENGTH_SHORT).show();
            }
        }
    };
}