package com.example.mybudget;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Method creates a pathway to the other
         * activities via a navigation bar
         */

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        Menu menu = navigation.getMenu();
        MenuItem menuItem =menu.getItem(0);
        menuItem.setChecked(true);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.nav_home:
                        break;

                    case R.id.nav_wishlist:
                         Intent intent1 =new Intent(MainActivity.this, WishlistActivity.class);
                         startActivity(intent1);
                         break;

                    case R.id.nav_account:
                         Intent intent2 = new Intent(MainActivity.this, AccountActivity.class);
                         startActivity(intent2);
                         break;
                    case R.id.nav_chores:
                        Intent intent3 = new Intent(MainActivity.this, ChoresActivity.class);
                        startActivity(intent3);
                        break;

                }
                return false;
            }
        });




    }
}
