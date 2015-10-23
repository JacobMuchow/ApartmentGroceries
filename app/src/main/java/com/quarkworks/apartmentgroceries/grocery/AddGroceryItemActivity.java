package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import bolts.Continuation;
import bolts.Task;

public class AddGroceryItemActivity extends AppCompatActivity {
    private static final String TAG = AddGroceryItemActivity.class.getSimpleName();

    /*
        References
     */
    private Toolbar toolbar;
    private TextView titleTextView;
    private EditText groceryItemNameEditText;
    private Button addButton;

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
        groceryItemNameEditText = (EditText) findViewById(R.id.add_grocery_item_name_id);
        addButton = (Button) findViewById(R.id.add_grocery_item_add_button_id);

        /**
         * Set view data
         */
        titleTextView.setText(getString(R.string.title_activity_add_grocery_item));

        /**
         * Set view OnClickListener
         */
        addButton.setOnClickListener(addGroceryButtonOnClick());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public View.OnClickListener addGroceryButtonOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groceryItemName = groceryItemNameEditText.getText().toString();

                if (!groceryItemName.isEmpty()) {
                    SharedPreferences sharedPreferences = getApplication()
                            .getSharedPreferences(getApplication().getString(R.string.login_or_sign_up_session), 0);
                    String groupId = sharedPreferences.getString("groupId", null);
                    String userId = sharedPreferences.getString("userId", null);

                    RGroceryItem rGroceryItem = new RGroceryItem();
                    rGroceryItem.setCreatedBy(sharedPreferences.getString(RUser.JsonKeys.USER_ID, null));
                    rGroceryItem.setGroupId(sharedPreferences.getString(RUser.JsonKeys.GROUP_ID, null));
                    rGroceryItem.setName(groceryItemName);
                    rGroceryItem.setGroupId(groupId);
                    rGroceryItem.setCreatedBy(userId);

                    Continuation<Boolean, Void> addGroceryItemOnSuccess = new Continuation<Boolean, Void>() {
                        @Override
                        public Void then(Task<Boolean> task) throws Exception {
                            if (task.getResult()) {
                                Intent intent = new Intent(MyApplication.getContext(), HomeActivity.class);
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(), getString(R.string.add_grocery_item_success),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.add_grocery_item_failure), Toast.LENGTH_SHORT).show();
                            }
                            return null;
                        }
                    };

                    SyncGroceryItem.add(rGroceryItem).onSuccess(addGroceryItemOnSuccess, Task.UI_THREAD_EXECUTOR);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.grocery_item_name_empty), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
