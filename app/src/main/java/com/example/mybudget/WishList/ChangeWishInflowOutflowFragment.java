package com.example.mybudget.WishList;
/**
 * Fragment allows the user to transfer
 * money from balance to a wish
 *
 * @author Anastasija Gurejeva
 * @author Daniel Beadleson
 */

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybudget.Home.SettingsActivity;
import com.example.mybudget.Models.Entry;
import com.example.mybudget.Models.WishList;
import com.example.mybudget.Profile.RegisterActivity;
import com.example.mybudget.R;
import com.example.mybudget.SendMailTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.mybudget.Profile.RegisterActivity.USER_ID;


public class ChangeWishInflowOutflowFragment extends Fragment {

    private static final String TAG = "InflowOutflowFragment";
    private EditText mAmount;
    private TextView mfragmentTitle, mbalance;
    private ImageView mimageViewHero;
    private Boolean addingMoney2Wish;
    private WishList wish2Update;
    private Button btn_cancelTransaction, btn_saveTransfer;
    private View view;
    private int dbid;
    private int index;
    private int balance;
    private GoalReachedDialog goalReached;
    private GoalHalfReachedDialog goalHalfReached;
    protected FloatingActionButton completed_wishes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_change_wish_inflow_outflow, container, false);

        completed_wishes = getActivity().findViewById(R.id.completed_wishes);
        completed_wishes.hide();

        mfragmentTitle = view.findViewById(R.id.title_change_money_fragment);
        mimageViewHero = view.findViewById(R.id.imageViewHero_wishlist);
        mAmount = view.findViewById(R.id.amount);
        btn_cancelTransaction = view.findViewById(R.id.btn_cancelTransaction);
        btn_saveTransfer = view.findViewById(R.id.btn_saveTransfer);
        mbalance = view.findViewById(R.id.balance_wish_fragment);

        dbid = ((WishlistActivity) getActivity()).id;
        wish2Update = ((WishlistActivity) getActivity()).db.returnWish(dbid);

        addingMoney2Wish = getArguments().getBoolean("inflow");
        index = getArguments().getInt("index");

        if (addingMoney2Wish) {
            mfragmentTitle.setText("Save for your wish");
        } else {
            mfragmentTitle.setText("Deduct from from your wish");
        }

        setBalance();
        setAvatar();
        return view;
    }

    /**
     * Method sets the balance
     */
    public void setBalance() {
        balance = ((WishlistActivity) getActivity()).db.balance();
        if (balance > 999999) {
            mbalance.setText(balance / 1000000 + "M SEK");
        } else if (balance > 999) {
            mbalance.setText(balance / 1000 + "k SEK");
        } else {
            mbalance.setText(balance + " SEK");
        }
    }

    /**
     * Method sets the avatar image from system
     * preferences
     */
    public void setAvatar() {
        SharedPreferences settings = getActivity().getSharedPreferences("themePreferenceFile", 0);
        int imageResId = settings.getInt("imageResId", -1);
        if (imageResId != -1) {
            Drawable d = getActivity().getDrawable(imageResId);
            mimageViewHero.setImageDrawable(d);
        }
    }

    /**
     * Method retrieves the input data or exits
     * the fragment
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.v(TAG, "onViewCreated inititialsed");
        Log.v(TAG, "inflow" + addingMoney2Wish);

        btn_saveTransfer.setOnClickListener(new View.OnClickListener() {
            /*
             * Method either adds or takes away money from the wish
             * if the user has enough money from their balance
             * and has not entered 0 SEK
             */
            @Override
            public void onClick(View v) {

                try {
                    String sAmount = mAmount.getText().toString().trim();
                    if (sAmount.isEmpty()) {
                        mAmount.setError("You have transferred 0 sek");
                    } else if (Integer.parseInt(sAmount) > balance) {
                        mAmount.setError("You don't have enough money in your account");
                    } else if (Integer.parseInt(sAmount) <= 0) {
                        mAmount.setError("Must be larger than 0");
                    } else if (Integer.parseInt(sAmount) > 6000) {
                        mAmount.setError("Single transaction can't be more than 6000");
                    } else {

                        int amount = Integer.parseInt(sAmount);
                        String wishName;
                        Entry entry = new Entry();
                        entry.setDate(LocalDate.now());
                        entry.setAmount(Integer.parseInt(sAmount));

                        if (addingMoney2Wish) {
                            addMoney2Wish(entry, amount, wish2Update, dbid);

                        } else if (!addingMoney2Wish) {
                            takeMoneyFromWish(entry, amount, wish2Update, dbid);
                        }
                    }
                } catch (Exception e) {
                    mAmount.setError("Try Again");
                }
            }
        });

        btn_cancelTransaction.setOnClickListener(new View.OnClickListener() {
            /*
             * Method sends the user back to the previous fragment
             * when the cancel button is initialised
             */
            @Override
            public void onClick(View v) {

                exitFragment();
            }
        });
    }


    /**
     * Method adds money to the wish
     * if the amount doesnt exceed the total price
     * of the wish
     */
    public void addMoney2Wish(Entry entry, Integer amount, WishList wish2Update, final int dbid) {
        entry.setTypeOfEntry(2);
        String entryDescription = ((WishlistActivity) getActivity()).mWishNames.get(((WishlistActivity) getActivity()).index)
                + " transfer";
        if (amount > (wish2Update.getCost() - wish2Update.getSaved())) {
            mAmount.setError("Your goal doesn't need that much money, try " +
                    (wish2Update.getCost() - wish2Update.getSaved()) + " SEK");


        } else if (wish2Update.getCost() / 2 > wish2Update.getSaved() && (wish2Update.getCost() / 2 <= wish2Update.getSaved() + amount) &&
                (wish2Update.getCost() != wish2Update.getSaved() + amount)) {

            Log.d(TAG, "addMoney2Wish: cost/2=saved");
            goalHalfReached = new GoalHalfReachedDialog();
            goalHalfReached.show(getFragmentManager(), "GoalHalfReachedDialog");
            ((WishlistActivity) getActivity()).db.updateWish(dbid, wish2Update.getTitle()
                    , wish2Update.getCost(), amount + wish2Update.getSaved(),
                    wish2Update.getImage());
            entry.setDesc(entryDescription);
            ((WishlistActivity) getActivity()).db.addEntry(entry);

            exitFragment();


        } else if ((wish2Update.getCost() == wish2Update.getSaved() + amount)) {
            Log.d(TAG, "addMoney2Wish: cost==saved");
            goalReached = new GoalReachedDialog();
            goalReached.show(getFragmentManager(), "GoalReachedDialog");
            ((WishlistActivity) getActivity()).db.updateWish(dbid, wish2Update.getTitle()
                    , wish2Update.getCost(), amount + wish2Update.getSaved(),
                    wish2Update.getImage());
            entry.setDesc(entryDescription);
            ((WishlistActivity) getActivity()).db.addEntry(entry);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivity(), WishlistActivity.class);
                    startActivity(intent);
                }
            }, 2000);


            Boolean emailEnabled = getActivity().getSharedPreferences(SettingsActivity.SETTINGSPREFS_NAME,
                    0).getBoolean(SettingsActivity.EMAILPREFS, false);
            if (emailEnabled) {
                sendEmail();
            }


        } else {
            ((WishlistActivity) getActivity()).db.updateWish(dbid, wish2Update.getTitle()
                    , wish2Update.getCost(), amount + wish2Update.getSaved(),
                    wish2Update.getImage());
            entry.setDesc(entryDescription);
            ((WishlistActivity) getActivity()).db.addEntry(entry);

            exitFragment();
        }
    }


    /**
     * Method takes away money from the list
     * if the amount doesnt exceed the total
     * amount of money allocated to the wish
     */
    public void takeMoneyFromWish(Entry entry, Integer amount, WishList wish2Update, int dbid) {
        entry.setTypeOfEntry(1);
        String entryDescription = ((WishlistActivity) getActivity()).mWishNames.get(((WishlistActivity) getActivity()).index)
                + " return";

        if (amount <= wish2Update.getSaved()) {

            ((WishlistActivity) getActivity()).db.updateWish(dbid, wish2Update.getTitle()
                    , wish2Update.getCost(), wish2Update.getSaved() - amount,
                    wish2Update.getImage());
            entry.setDesc(entryDescription);
            ((WishlistActivity) getActivity()).db.addEntry(entry);

            exitFragment();
        } else {
            mAmount.setError("Can't be more than " +
                    (wish2Update.getSaved()) +
                    " SEK");
        }
    }

    /**
     * Method sends an email of the completed wish
     * to the parent email (if enabled via Settings)
     *
     * @author Anastasija Gurejeva
     * @author Daniel Beadleson
     */
    public void sendEmail() {
        int userGlobalId = getActivity().getSharedPreferences(RegisterActivity.USER_PREFS_NAME,
                0).getInt(USER_ID, 0);
        String userParentEmail = ((WishlistActivity) getActivity()).db.getUser(userGlobalId).getUserMail();
        String userName = ((WishlistActivity) getActivity()).db.getUser(userGlobalId).getUserFirstName();

        writeTheFileForEmail();
        String emailBody = userName + " has completed saving for a  " + wish2Update.getTitle()
                + ". \n " + userName + " Has successfully saved: " + wish2Update.getCost()
                + " SEK, to pay for his/her dream.";
        writeTheFileForEmail();

        new SendMailTask().execute(userParentEmail, emailBody);

        Toast.makeText(getActivity(), "Email sent to " + userName + "'s parent", Toast.LENGTH_LONG).show();


    }

    /**
     * Method attaches the logo to the email
     */

    public void writeTheFileForEmail() {
        ActivityCompat.requestPermissions((WishlistActivity) getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0);

        try {
            String appDirectoryName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getResources().getString(R.string.app_name);
            String fileName = "logo_pm.jpg";
            File directory = new File(appDirectoryName);

            if (!directory.exists()) {
                directory.mkdirs();
                Log.d(TAG, "writeTheFileForEmail:" + directory);

            }
            File fullPath = new File(appDirectoryName, fileName);
            if (fullPath.isFile()) {
                fullPath.delete();
                Log.d(TAG, "writeTheFileForEmail:" + fullPath);


            }
            InputStream inputStream = getActivity().getAssets().open("logo_pm.jpg");

            try (FileOutputStream outputStream = new FileOutputStream(fullPath)) {

                int read;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }


            } catch (Exception e) {
                e.getMessage();
            }

        } catch (Exception e) {

            e.getMessage();
        }

    }

    /**
     * Method exits the fragment
     */
    public void exitFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_wish_fragment, new WishFragment())
                .commit();
    }
}
