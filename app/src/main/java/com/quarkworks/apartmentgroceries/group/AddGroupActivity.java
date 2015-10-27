package com.quarkworks.apartmentgroceries.group;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.SyncGroup;
import com.quarkworks.apartmentgroceries.service.models.RGroup;

import bolts.Continuation;
import bolts.Task;

public class AddGroupActivity extends AppCompatActivity {
    private static final String TAG = AddGroupActivity.class.getSimpleName();

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private EditText groupNameEditText;
    private Button addGroupButton;

    public static void newIntent(Context context) {
        Intent intent = new Intent(context, AddGroupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_group_activity);

        /**
         * Get view references
         */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar_id);
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_id);
        groupNameEditText = (EditText) findViewById(R.id.add_group_name_id);
        addGroupButton = (Button) findViewById(R.id.add_group_add_button_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_add_group));

        /**
         * Set view OnClickListener
         */
        addGroupButton.setOnClickListener(addGroupButtonOnClick);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public View.OnClickListener addGroupButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String groupItemName = groupNameEditText.getText().toString();

            if (!groupItemName.isEmpty()) {

                RGroup groupItem = new RGroup();
                groupItem.setName(groupItemName);

                SyncGroup.add(groupItem).continueWith(checkAddingGroup, Task.UI_THREAD_EXECUTOR);
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.grocery_item_name_empty), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Continuation<Void, Void> checkAddingGroup = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Adding group failure", task.getError());
                Toast.makeText(getApplicationContext(),
                        getString(R.string.add_group_failure), Toast.LENGTH_SHORT).show();
            } else {
                HomeActivity.newIntent(AddGroupActivity.this);
                Toast.makeText(getApplicationContext(), getString(R.string.add_group_success),
                        Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    };
}
