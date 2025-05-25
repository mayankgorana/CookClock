package com.example.cookclock;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TimerActivity extends AppCompatActivity implements SensorEventListener {

    TextView timerLabel, timerCountdown;
    Button startBtn, pauseBtn, resumeBtn, cancelBtn;
    CountDownTimer countDownTimer;
    long remainingTime;
    boolean isPaused = false;
    MediaPlayer alertSound;

    // Sensor related
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timerLabel = findViewById(R.id.timerLabel);
        timerCountdown = findViewById(R.id.timerCountdown);
        startBtn = findViewById(R.id.btnStart);
        pauseBtn = findViewById(R.id.pauseBtn);
        resumeBtn = findViewById(R.id.resumeBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        String label = getIntent().getStringExtra("TIMER_LABEL");
        int minutes = getIntent().getIntExtra("TIMER_TIME", 1);
        timerLabel.setText(label);

        remainingTime = minutes * 60 * 1000L;

        pauseBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);

        startBtn.setOnClickListener(v -> {
            startTimer(remainingTime);
            startBtn.setVisibility(View.GONE);
            pauseBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
        });

        pauseBtn.setOnClickListener(v -> {
            pauseTimer();
        });

        resumeBtn.setOnClickListener(v -> {
            resumeTimer();
        });

        cancelBtn.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            finish(); // close activity or reset UI as needed
        });

        // Setup sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    private void startTimer(long timeInMillis) {
        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                timerCountdown.setText(String.format("%02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                timerCountdown.setText("00:00");
                Toast.makeText(TimerActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                // play sound or vibrate here if needed
            }
        }.start();
        isPaused = false;
        pauseBtn.setVisibility(View.VISIBLE);
        resumeBtn.setVisibility(View.GONE);
    }

    private void pauseTimer() {
        if (!isPaused && countDownTimer != null) {
            countDownTimer.cancel();
            isPaused = true;
            pauseBtn.setVisibility(View.GONE);
            resumeBtn.setVisibility(View.VISIBLE);
            Toast.makeText(this, "▶\uFE0F Timer paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void resumeTimer() {
        if (isPaused) {
            startTimer(remainingTime);
            Toast.makeText(this, "⏸\uFE0F Timer resumed", Toast.LENGTH_SHORT).show();
        }
    }

    // Register sensor listener on resume
    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // Unregister sensor listener on pause
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // Shake detection logic
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate acceleration magnitude (removing gravity approx.)
            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > SHAKE_THRESHOLD) {
                long now = System.currentTimeMillis();
                // Prevent multiple shakes in quick succession (500ms)
                if (now - lastShakeTime > 500) {
                    lastShakeTime = now;
                    onShake();
                }
            }
        }
    }

    private void onShake() {
        // If timer running => pause
        // If paused => resume
        if (!isPaused && countDownTimer != null) {
            pauseTimer();
        } else if (isPaused) {
            resumeTimer();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
