package com.example.cookclock;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

public class TimerActivity extends AppCompatActivity {

    TextView timerLabel, timerCountdown;
    Button startBtn, pauseBtn, resumeBtn, cancelBtn;
    CountDownTimer countDownTimer;
    long remainingTime;
    boolean isPaused = false;
    MediaPlayer alertSound;

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
            if (!isPaused && countDownTimer != null) {
                countDownTimer.cancel();
                isPaused = true;
                pauseBtn.setVisibility(View.GONE);
                resumeBtn.setVisibility(View.VISIBLE);
            }
        });

        resumeBtn.setOnClickListener(v -> {
            if (isPaused) {
                startTimer(remainingTime);
                isPaused = false;
                resumeBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
            }
        });

        cancelBtn.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            if (alertSound != null) alertSound.release();
            finish();
        });

        // OPTIONAL: Set media volume to max for testing
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        }
    }

    private void startTimer(long timeInMillis) {
        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                timerCountdown.setText(String.format("%02d:%02d",
                        (millisUntilFinished / 60000),
                        (millisUntilFinished % 60000) / 1000));
            }

            @RequiresPermission(Manifest.permission.VIBRATE)
            public void onFinish() {
                playAlertSound();
                vibratePhone();

                Toast.makeText(TimerActivity.this, "Timer Finished!", Toast.LENGTH_SHORT).show();
                saveToHistory(timerLabel.getText().toString());
                finish();
            }
        }.start();
    }

    private void playAlertSound() {
        alertSound = MediaPlayer.create(this, R.raw.alert);
        if (alertSound != null) {
            alertSound.setOnCompletionListener(mp -> {
                mp.release(); // release MediaPlayer when done
            });
            alertSound.start();
        } else {
            Toast.makeText(this, "Failed to play alert sound", Toast.LENGTH_SHORT).show();
        }
    }

    private void vibratePhone() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
    }

    private void saveToHistory(String label) {
        SharedPreferences prefs = getSharedPreferences("TIMER_HISTORY", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String existing = prefs.getString("HISTORY", "");
        editor.putString("HISTORY", existing + label + " - Done\n");
        editor.apply();
    }
}
