package com.quarkworks.apartmentgroceries.service;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.quarkworks.apartmentgroceries.R;

import bolts.Task;

/**
 * Created by zhao on 10/31/15.
 */
public class PopupDialog extends DialogFragment {
    private static final String TAG = PopupDialog.class.getSimpleName();

    private static final String TITLE = "title";
    private static final String FIELD_NAME = "fieldName";
    private static final String FIELD_VALUE = "fieldValue";
    private String title;
    private String fieldName;
    private String fieldValue;

    public Task task;

    private EditText editText;

    public interface NoticeDialogListener {
        void onDialogPositiveClick(PopupDialog dialog);
        void onDialogNegativeClick(PopupDialog dialog);
    }

    private NoticeDialogListener noticeDialogListener;

    public static PopupDialog newInstance(String title, String fieldName, String fieldValue) {
        PopupDialog fragment = new PopupDialog();

        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(FIELD_NAME, fieldName);
        args.putString(FIELD_VALUE, fieldValue);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString(TITLE);
        fieldName = getArguments().getString(FIELD_NAME);
        fieldValue = getArguments().getString(FIELD_VALUE);

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
        editText.setText(fieldValue);

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
            noticeDialogListener.onDialogNegativeClick(PopupDialog.this);
            dismiss();
        }
    };

    private View.OnClickListener saveTextViewOnClick = new View.OnClickListener() {
        @Override
        public  void onClick(View v) {
            String newValue = editText.getText().toString();
            if (!TextUtils.isEmpty(newValue) && !newValue.equals(fieldValue)) {
                task = SyncUser.updateProfile(fieldName, newValue);
                noticeDialogListener.onDialogPositiveClick(PopupDialog.this);
            } else {
                dismiss();
            }
        }
    };

    public void setNoticeDialogListener(NoticeDialogListener noticeDialogListener) {
        this.noticeDialogListener = noticeDialogListener;
    }
}