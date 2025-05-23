package com.example.cookclock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddTimerActivity extends AppCompatActivity {

    private EditText dishNameEditText, minutesEditText;
    private Button startCustomTimerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timer);

        // Set the title shown in the ActionBar
        setTitle("CookClock");

        dishNameEditText = findViewById(R.id.dishNameEditText);
        minutesEditText = findViewById(R.id.minutesEditText);
        startCustomTimerBtn = findViewById(R.id.startCustomTimerBtn);

        startCustomTimerBtn.setOnClickListener(v -> {
            String label = dishNameEditText.getText().toString().trim();
            String timeStr = minutesEditText.getText().toString().trim();

            if (label.isEmpty() || timeStr.isEmpty()) {
                Toast.makeText(this, "Please enter dish and time", Toast.LENGTH_SHORT).show();
                return;
            }

            int timeInMin;
            try {
                timeInMin = Integer.parseInt(timeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid time in minutes", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(AddTimerActivity.this, TimerActivity.class);
            intent.putExtra("label", label);
            intent.putExtra("timeInMinutes", timeInMin);
            startActivity(intent);
        });
    }
}
