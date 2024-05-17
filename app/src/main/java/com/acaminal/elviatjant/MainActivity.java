package com.acaminal.elviatjant;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private float currentX = 0;
    private float currentY = 0;
    private ImageView characterImageView;
    private ImageView backgroundImageView;
    private FrameLayout frameLayout;

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor accelerometer;

    private boolean isAnimationRunning = true;
    private SensorEventListener sensorEventListener;

    private AnimationDrawable[] backgroundAnimation = new AnimationDrawable[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        characterImageView = findViewById(R.id.pj_1);
        backgroundImageView = findViewById(R.id.imageView6);
        frameLayout = findViewById(R.id.mainLayout);

        characterImageView.setBackgroundResource(R.drawable.stickman);
        final AnimationDrawable[] characterAnimation = {(AnimationDrawable) characterImageView.getBackground()};
        characterAnimation[0].start();

        characterImageView.setX(currentX);
        characterImageView.setY(currentY);

        backgroundImageView.setBackgroundResource(R.drawable.fons);
        backgroundAnimation[0] = (AnimationDrawable) backgroundImageView.getBackground();

        // Establece el OnTouchListener para mover la ImageView
        characterImageView.setOnTouchListener(new View.OnTouchListener() {
            private float xDelta;
            private float yDelta;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getRawX();
                float y = event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        xDelta = x - view.getX();
                        yDelta = y - view.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float newX = x - xDelta;
                        float newY = y - yDelta;

                        // Restricciones para no salir del FrameLayout
                        newX = Math.max(0, Math.min(newX, frameLayout.getWidth() - view.getWidth()));
                        newY = Math.max(0, Math.min(newY, frameLayout.getHeight() - view.getHeight()));

                        view.setX(newX);
                        view.setY(newY);
                        break;
                }
                return true;
            }
        });

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] rotationMatrix = new float[9];
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

                float[] orientationAngles = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientationAngles);

                float azimuth = (float) Math.toDegrees(orientationAngles[0]);

                if (Math.abs(azimuth) == 180) {
                    if (isAnimationRunning) {
                        characterAnimation[0].stop();
                        characterImageView.setBackgroundResource(R.drawable.pj_1);
                        isAnimationRunning = false;
                    }
                } else {
                    if (!isAnimationRunning) {
                        characterImageView.setBackgroundResource(R.drawable.stickman);
                        characterAnimation[0] = (AnimationDrawable) characterImageView.getBackground();
                        characterAnimation[0].start();
                        isAnimationRunning = true;
                    }

                    updateCharacterPosition(event.values[1], event.values[0]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void updateCharacterPosition(float x, float y) {
        float movementFactor = 5.0f;
        float previousX = currentX;

        currentX += x * movementFactor;
        currentY += y * movementFactor;

        if (currentX < 0) currentX = 0;
        if (currentY < 0) currentY = 0;
        if (currentX > (frameLayout.getWidth() - characterImageView.getWidth()))
            currentX = frameLayout.getWidth() - characterImageView.getWidth();
        if (currentY > (frameLayout.getHeight() - characterImageView.getHeight()))
            currentY = frameLayout.getHeight() - characterImageView.getHeight();

        if (currentX > previousX) {
            characterImageView.setScaleX(1.0f);
        } else if (currentX < previousX) {
            characterImageView.setScaleX(-1.0f);
        }

        characterImageView.setX(currentX);
        characterImageView.setY(currentY);

        Log.d("currentX", String.valueOf((int) currentX));
        // Iniciar la animación de backgroundImageView si pj_1 llega al lateral derecho
        if ((int) currentX >= frameLayout.getWidth() - characterImageView.getWidth() || (int) currentX <= 1) {
            if (!backgroundAnimation[0].isRunning()) {
                backgroundAnimation[0].start();
            }
        } else {
            if (backgroundAnimation[0].isRunning()) {
                backgroundAnimation[0].stop();
            }
        }
    }
}
