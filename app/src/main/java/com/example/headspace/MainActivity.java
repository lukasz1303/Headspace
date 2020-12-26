package com.example.headspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        boolean wasFirstLaunch = pref.getBoolean("First launch", false);
        if(!wasFirstLaunch){
            Intent i = new Intent(getApplicationContext(), SingleRunActivity.class);
            startActivity(i);
        }

        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        List<String> categories = new ArrayList<>();
        categories.add("Basics");
        categories.add("Basics 2");
        categories.add("Basics 3");
        categories.add("Productivity");
        RecyclerView.Adapter mAdapter = new CategoryAdapter(this, categories);
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reminder_settings) {
            Intent i = new Intent(getApplicationContext(), ReminderSettingsActivity.class);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
