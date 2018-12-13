package com.example.emily.qrcontact;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class Help extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ArrayList<Make.Profile> passer = new ArrayList<>();


//        if (getIntent().getExtras() != null) {
//            passer = (ArrayList<Make.Profile>) getIntent().getExtras().get("Profiles");
//        }
//
//
//        final ArrayList<Make.Profile> profiles = passer;


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_scan:
                        Intent intent1 = new Intent(Help.this, MainActivity.class);
//                        if (profiles != null) {
//                            intent1.putExtra("Profiles", profiles);
//                        }
                        startActivity(intent1);
                        break;
                    case R.id.navigation_make:
                        Intent intent2 = new Intent(Help.this, Make.class);
//                        if (profiles != null) {
//                            intent2.putExtra("Profiles", profiles);
//                        }
                        startActivity(intent2);
                        break;
                    case R.id.navigation_help:
                        break;
                }

                return false;
            }
        });
    }
}
