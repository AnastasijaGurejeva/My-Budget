package com.example.mybudget.Chores;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mybudget.Home.MainActivity;
import com.example.mybudget.Models.User;
import com.example.mybudget.Profile.RegisterActivity;
import com.example.mybudget.R;

import static android.content.Context.MODE_PRIVATE;
import static android.support.constraint.Constraints.TAG;
import static com.example.mybudget.AvatarChangeActivity.PREFS_NAME;
import static com.example.mybudget.Profile.RegisterActivity.USER_ID;
import static com.example.mybudget.Profile.RegisterActivity.USER_PREFS_NAME;


/**
 * Send SMS
 * @author Alaa Al Sakka
 */


public class SendSms extends Fragment {
    EditText e1, e2;
    Button b1;
    private final static int SEND_SMS_PERMISSION_REQ = 1;
    SharedPreferences sharedPreferences;
    int userGlobalId;
    private String userPhone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_send_sms, container, false);
    // retreating phone number from the registered user
        sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(USER_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        userGlobalId = sharedPreferences.getInt(USER_ID, 0);
        if (userGlobalId != 0) {
            userPhone = ((ChoresActivity) getActivity()).db.retrievePhone(userGlobalId);
        }


        e1 = view.findViewById(R.id.editText);
        if (userPhone == null) {
            e1.setHint("Parent's number");
        } else if (userPhone != null) {
            e1.setText(userPhone);
        }
        e2 = view.findViewById(R.id.editText2);

        String userName = ((ChoresActivity) getActivity()).db.getUser(getActivity()
                .getSharedPreferences(RegisterActivity.USER_PREFS_NAME,
                        0).getInt(USER_ID, 0)).getUserFirstName();
        // set the text message that will be sent
        e2.setText(userName + " would like to be payed " +
                getArguments().getString("amount") +
                "SEK for successfully completing the chore: " +
                getArguments().getString("desc") + ".");

        b1 = view.findViewById(R.id.sendButton);

        if (e2.getText().toString().isEmpty())
            b1.setEnabled(false);


        if (checkPermission(Manifest.permission.SEND_SMS)) {
            b1.setEnabled(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQ);
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = ((ChoresActivity) getActivity()).db.getUser(userGlobalId);

                String s1 = e1.getText().toString();
                String s2 = e2.getText().toString();

                /**
                 * checking for permission and
                 * constrain the phone number
                 */
                if (!TextUtils.isEmpty(s1) && !TextUtils.isEmpty(s2)) {
                    if (s1.trim().length() == 10 && s1.startsWith("0")) {
                        if (checkPermission(Manifest.permission.SEND_SMS)) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(s1, null, s2, null, null);
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                        user.setUserPhone(s1);
                        ((ChoresActivity) getActivity()).db.updateUser(user, userGlobalId);

                    }
                    else {
                        Toast.makeText(getActivity(), "Phone number should be 10 digits and starts with 0", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Button cancelSendButton = view.findViewById(R.id.cancelSend);
        cancelSendButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Method sends the user back to the main menu
             * when the cancel button is initialised
             */
            @Override
            public void onClick(View v) {
                Log.v(TAG, "cancel button initialised");
                Intent intent = new Intent(getActivity(), ChoresActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }


    /**
     * method to check permission for sending sms
     */
    private boolean checkPermission(String sendSms) {
        int checkpermission = ContextCompat.checkSelfPermission(getActivity(), sendSms);
        return checkpermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS_PERMISSION_REQ:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    b1.setEnabled(true);
                }
                break;
        }
    }
}
