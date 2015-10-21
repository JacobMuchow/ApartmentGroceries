package com.quarkworks.apartmentgroceries.group;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.Promise;
import com.quarkworks.apartmentgroceries.service.SyncGroup;
import com.quarkworks.apartmentgroceries.service.models.RGroup;

public class AddGroupActivity extends AppCompatActivity {
    private static final String TAG = AddGroupActivity.class.getSimpleName();

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private EditText groupNameEditText;
    private Button addGroupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_group_activity);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        titleTextView.setText(getString(R.string.title_activity_add_group));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        groupNameEditText = (EditText) findViewById(R.id.add_group_name_id);
        addGroupButton = (Button) findViewById(R.id.add_group_add_button_id);

        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupItemName = groupNameEditText.getText().toString();

                if (!groupItemName.isEmpty()) {

                    RGroup groupItem = new RGroup();
                    groupItem.setName(groupItemName);
                    SyncGroup.add(groupItem)
                            .setCallbacks(addSuccessCallback, addFailureCallback);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.grocery_item_name_empty), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private Promise.Success addSuccessCallback = new Promise.Success() {
        @Override
        public void onSuccess() {
            Intent intent = new Intent(MyApplication.getContext(), GroupActivity.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), getString(R.string.add_group_success),
                    Toast.LENGTH_LONG).show();
        }
    };

    private Promise.Failure addFailureCallback = new Promise.Failure() {
        @Override
        public void onFailure() {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.add_group_failure), Toast.LENGTH_LONG).show();
        }
    };
}
