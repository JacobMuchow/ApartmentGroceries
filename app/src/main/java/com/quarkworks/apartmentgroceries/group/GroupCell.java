package com.quarkworks.apartmentgroceries.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.main.HomeActivity;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RGroup;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by zz on 10/16/15.
 */
public class GroupCell extends RelativeLayout{

    private static final String TAG = GroupCell.class.getSimpleName();

    private TextView nameTextView;
    private Button joinGroupButton;

    public GroupCell(Context context) {
        super(context);
        initialize();
    }

    public GroupCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public GroupCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.group_cell, this);
        nameTextView = (TextView) findViewById(R.id.group_cell_name_id);
        joinGroupButton = (Button) findViewById(R.id.group_cell_join_group_button_id);
    }

    public void setViewData(final RGroup group){
        nameTextView.setText(group.getName());
        SharedPreferences sharedPreferences =
                MyApplication.getContext().getSharedPreferences(
                        MyApplication.getContext()
                                .getString(R.string.login_or_sign_up_session), 0);
        final String groupId = sharedPreferences.getString(SyncUser.JsonKeys.GROUP_ID, null);
        if (!TextUtils.isEmpty(groupId) && groupId.equals(group.getGroupId())) {
            joinGroupButton.setVisibility(GONE);
        } else {
            joinGroupButton.setVisibility(VISIBLE);
        }

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences =
                        MyApplication.getContext().getSharedPreferences(
                                MyApplication.getContext()
                                        .getString(R.string.login_or_sign_up_session), 0);
                String userId = sharedPreferences.getString(SyncUser.JsonKeys.USER_ID, null);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SyncUser.JsonKeys.GROUP_ID, group.getGroupId());
                editor.commit();
                SyncUser.joinGroup(userId, group.getGroupId()).onSuccess(new Continuation<Boolean, Void>() {
                    @Override
                    public Void then(Task<Boolean> task) throws Exception {
                        if (task.getResult()) {
                            Intent intent = new Intent(getContext(), HomeActivity.class);
                            getContext().startActivity(intent);
                            Toast.makeText(MyApplication.getContext(),
                                    MyApplication.getContext().getString(R.string.join_group_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyApplication.getContext(),
                                    MyApplication.getContext().getString(R.string.join_group_failure),
                                    Toast.LENGTH_LONG).show();
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            }
        });
    }
}
