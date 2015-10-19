package com.quarkworks.apartmentgroceries.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.Promise;
import com.quarkworks.apartmentgroceries.service.SyncGroceryItem;
import com.quarkworks.apartmentgroceries.service.models.RGroceryItem;

public class AddGroceryItemActivity extends AppCompatActivity {
    private static final String TAG = AddGroceryItemActivity.class.getSimpleName();

    private EditText groceryItemNameEditText;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_grocery_item_activity);

        groceryItemNameEditText = (EditText) findViewById(R.id.add_grocery_item_name_id);
        addButton = (Button) findViewById(R.id.add_grocery_item_add_button_id);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groceryItemName = groceryItemNameEditText.getText().toString();

                if (!groceryItemName.isEmpty()) {

                    RGroceryItem rGroceryItem = new RGroceryItem();
                    rGroceryItem.setName(groceryItemName);
                    SyncGroceryItem.add(rGroceryItem)
                            .setCallbacks(addSuccesCallback, addFailureCallback);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.grocery_item_name_empty), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private Promise.Success addSuccesCallback = new Promise.Success() {
        @Override
        public void onSuccess() {
            Intent intent = new Intent(MyApplication.getContext(), HomeActivity.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), getString(R.string.add_grocery_item_success),
                    Toast.LENGTH_LONG).show();
        }
    };

    private Promise.Failure addFailureCallback = new Promise.Failure() {
        @Override
        public void onFailure() {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.add_grocery_item_failure), Toast.LENGTH_LONG).show();
        }
    };
}
