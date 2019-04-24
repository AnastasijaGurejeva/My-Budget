package com.example.mybudget.Account;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.mybudget.Chores.ChoresActivity;
import com.example.mybudget.Home.MainActivity;
import com.example.mybudget.Models.Entry;
import com.example.mybudget.R;
import com.example.mybudget.SettingsActivity;
import com.example.mybudget.WishList.WishlistActivity;
import com.example.mybudget.myDbHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends SettingsActivity {
    private static final String TAG = "AccountActivityLog";
    RecyclerView mRecyclerView;
    private List<AccountsRow> data;
    private ArrayList<Entry> entries;
    private AccountsRecyclerViewAdapter adapter;
    private TextView tvBalance;
    private int currentlog = 0;
    private Spinner mySpinner;
    private Spinner mySpinnerMonths;
    private BottomNavigationView navigation;

    myDbHelper db = new myDbHelper(this, "myDb.db", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        tvBalance = findViewById(R.id.tvBalance);
        currentlog = db.balance();
        tvBalance.setText("Available: " + String.valueOf(currentlog) + " SEK");


        //create recycler view
        mRecyclerView = findViewById(R.id.recyclerview);

        mySpinner = (findViewById(R.id.spinner1));

        String [] labels={"Everything","Expenses","Income","On wish","Chore Money"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mySpinner.setAdapter(spinnerAdapter);
        mySpinnerOnClick();

        mySpinnerMonths = (findViewById(R.id.spinner_months));

        String [] labelsMonths = {"Everything","January","February","March","April", "May", "April", "June", "July",
        "August", "September", "October", "November", "December"};

        ArrayAdapter<String> spinnerAdapterMonths = new ArrayAdapter<String>(this,
                R.layout.spinner_item, labelsMonths);
        spinnerAdapterMonths.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mySpinnerMonths.setAdapter(spinnerAdapterMonths);
        mySpinnerMonthsOnClick();

        /*
         * Method creates a pathway to the other
         * activities via a navigation bar
         */

        navigation = findViewById(R.id.bottom_navigation);
        Menu menu = navigation.getMenu();
        MenuItem menuItem =menu.getItem(2);
        menuItem.setChecked(true);
        bottomNavigationOnClick();

        settingAdapter();
    }

    public void mySpinnerOnClick() {

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(AccountActivity.this, "position: "+position, Toast.LENGTH_SHORT).show();
                if(position == 0){
                    currentlog = db.balance();
                    tvBalance.setText("Available: " + String.valueOf(currentlog) + " SEK");
                }
                else if(position == 1)
                {
                    currentlog = db.calcExpenses();
                    tvBalance.setText("Spent: " + String.valueOf(currentlog) + " SEK");
                }
                else if (position == 2){
                    currentlog = db.calcIncome();
                    tvBalance.setText("Income: " + String.valueOf(currentlog) + " SEK");
                }
                else if(position == 3){
                    currentlog = db.calcWish();
                    tvBalance.setText("On wish: " + String.valueOf(currentlog) + " SEK");
                }
                else if(position == 4){
                    currentlog = db.calcEarning();
                    tvBalance.setText("Chore Money: " + String.valueOf(currentlog) + " SEK");
                }
                //TODO Dawnie
                settingAdapter();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void mySpinnerMonthsOnClick() {
        mySpinnerMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(AccountActivity.this, "position: "+position, Toast.LENGTH_SHORT).show();
                // TODO: 2019-04-24 Dawnie
                settingAdapter();
           }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void bottomNavigationOnClick () {
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.nav_home:
                        Intent intent1= new Intent(AccountActivity.this, MainActivity.class);
                        startActivity(intent1);
                        break;

                    case R.id.nav_wishlist:
                        Intent intent2 = new Intent(AccountActivity.this, WishlistActivity.class);
                        startActivity(intent2);
                        break;

                    case R.id.nav_account:
                        break;
                    case R.id.nav_chores:
                        Intent intent3 = new Intent(AccountActivity.this, ChoresActivity.class);
                        startActivity(intent3);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * @param typeOfEntry a filter taken from spinner position to specify the type of entry
     * @return an arrayList contains filtered data
     */
    public ArrayList<Entry> fill_with_data(int typeOfEntry) {
        Log.v(TAG, "typeOfEntry: "+typeOfEntry);
//        List<AccountsRow> row = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();

        if (typeOfEntry == 1) {
            entries = db.expensesEntries();
//            for (Entry e : entries) {
//                row.add(new AccountsRow(e.getDate(), e.getDesc(), e.getAmount(), e.getTypeOfEntry()));
//            }
        } else if (typeOfEntry == 2) {
            entries = db.incomeEntries();
//            for (Entry e : entries) {
//                row.add(new AccountsRow(e.getDate(), e.getDesc(), e.getAmount(), e.getTypeOfEntry()));
//            }
        } else if (typeOfEntry == 3) {
            entries = db.wishEntries();
//            for (Entry e : entries) {
//                row.add(new AccountsRow(e.getDate(), e.getDesc(), e.getAmount(), e.getTypeOfEntry()));
//            }
        } else if (typeOfEntry == 4) {
            entries = db.earningsEntries();
//            for (Entry e : entries) {
//                row.add(new AccountsRow(e.getDate(), e.getDesc(), e.getAmount(), e.getTypeOfEntry()));
//            }
        } else if (typeOfEntry == 0){
            entries = db.allEntries();
//            for (Entry e : entries) {
//                row.add(new AccountsRow(e.getDate(), e.getDesc(), e.getAmount(), e.getTypeOfEntry()));
//            }
        }
        return entries;
    }

    /** Sets the number of months to the spinner
     * @return number of monthes since the user logged an Entry
     * @auth DAWNIE SAFAR
     */
    public int getMonthesOfEntries() {
        int numberOfMonthes = 1;
        LocalDate date = LocalDate.now();
        ArrayList<Entry> allEntries = db.allEntries();

        for (Entry e : allEntries) {
            if (e.getDate().isBefore(date)) {
                date = e.getDate();
                numberOfMonthes++;
            }
        }
        return numberOfMonthes;
    }

    /**
     * @param dataFilteredByTypeOfEntry specifies entries filter by type of entry (income, expenses...)
     * @param spinnerPosition a date filter as per the spinner position
     * @return data filtered by data and type of entry
     * @auth Dawnie Safar
     */
    public List<AccountsRow> filterEntriesByDate(ArrayList<Entry> dataFilteredByTypeOfEntry, int spinnerPosition){
        ArrayList<Entry> allEntries = new ArrayList<>();
        List<AccountsRow> row = new ArrayList<>();

        if(spinnerPosition == 0) {
            allEntries = dataFilteredByTypeOfEntry;
        }
        else if(spinnerPosition == 1) {
            for (Entry entry : dataFilteredByTypeOfEntry) {
                if (entry.getDate().getMonth().toString() == "JANUARY") {
                    allEntries.add(entry);
                }
            }
        }
        else if (spinnerPosition == 2){
            for (Entry entry : dataFilteredByTypeOfEntry) {
                if (entry.getDate().getMonth().toString() == "FEBRUARY") {
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 3){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "MARCH"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 4){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "APRIL"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 5){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "MAY"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 6){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "JUNE"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 7){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "JULY"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 8){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "AUGUST"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 9){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "SEPTEMBER"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 10){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "OCTOBER"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 11){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "NOVEMBER"){
                    allEntries.add(entry);
                }
            }
        }
        else if(spinnerPosition == 12){
            for(Entry entry : dataFilteredByTypeOfEntry){
                if(entry.getDate().getMonth().toString() == "DECEMBER"){
                    allEntries.add(entry);
                }
            }
        }
        for(Entry e : allEntries){
            row.add(new AccountsRow(e.getDate(), e.getDesc(), e.getAmount(), e.getTypeOfEntry()));
        }
        return row;
    }

    /**
     * setting the spinners adapter with the selected filters
     * @auth Dawnie Safar
     */
    public void settingAdapter(){
        entries = fill_with_data(mySpinner.getSelectedItemPosition());
        data = filterEntriesByDate(entries, mySpinnerMonths.getSelectedItemPosition());
        adapter = new AccountsRecyclerViewAdapter(data, getApplication());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }
}



