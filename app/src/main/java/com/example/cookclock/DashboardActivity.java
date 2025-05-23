package com.example.cookclock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    String userName;
    TextView welcomeText;
    ListView presetList;
    Button addTimer, viewHistory;

    String[] dishes = {"Boil Eggs - 10 min", "Chocolate Cake - 50 min", "Pizza - 15 min", "Noodles - 5 min", "Pasta - 9 min", "Popcorn - 3 min"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        userName = getIntent().getStringExtra("USERNAME");
        welcomeText = findViewById(R.id.welcomeText);
        presetList = findViewById(R.id.presetList);
        addTimer = findViewById(R.id.btnAddTimer);

        welcomeText.setText("Hi " + userName + "! What do you want to cook today?");
        presetList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dishes));

        presetList.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(DashboardActivity.this, TimerActivity.class);
            intent.putExtra("TIMER_LABEL", dishes[i]);
            intent.putExtra("TIMER_TIME", extractMinutes(dishes[i]));
            startActivity(intent);
        });

        addTimer.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddTimerActivity.class)));
    }

    private int extractMinutes(String dish) {
        String[] split = dish.split("-");
        return Integer.parseInt(split[1].replaceAll("[^0-9]", ""));
    }
}
