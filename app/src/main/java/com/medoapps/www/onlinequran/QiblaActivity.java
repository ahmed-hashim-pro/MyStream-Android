package com.medoapps.www.onlinequran;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.graphics.Color;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import com.medoapps.www.onlinequran.view.CompassView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class QiblaActivity extends AppCompatActivity implements SensorEventListener {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final double KAABA_LAT = 21.4225;
    private static final double KAABA_LNG = 39.8262;

    private ImageView compassImage;
    private CompassView compassView;
    private TextView tvDegree;
    private TextView tvQiblaBearing;
    private TextView tvCalibration;
    private TextView tvStatus;
    private SensorManager sensorManager;
    private boolean isOnTarget = false;
    private float qiblaBearing = 0f;
    private boolean hasQiblaBearing = false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qibla);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.qibla_finder);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        compassImage = findViewById(R.id.compass_image);
        compassView = findViewById(R.id.compass_view);
        tvDegree = findViewById(R.id.tv_degree);
        tvQiblaBearing = findViewById(R.id.tv_qibla_bearing);
        tvCalibration = findViewById(R.id.tv_calibration);
        tvStatus = findViewById(R.id.tv_status);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocationAndCalculate();
    }

    private void requestLocationAndCalculate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }
        fetchLocationForQibla();
    }

    private void fetchLocationForQibla() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvStatus.setText("يرجى السماح بإذن الموقع");
            return;
        }

        tvStatus.setText("جاري تحديد الموقع...");

        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    setQiblaFromLocation(location.getLatitude(), location.getLongitude());
                } else {
                    requestFreshLocation();
                }
            })
            .addOnFailureListener(this, e -> {
                tvStatus.setText("تعذر تحديد الموقع - تأكد من تفعيل GPS");
            });
    }

    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        tvStatus.setText("جاري البحث عن GPS...");

        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1)
                .setWaitForAccurateLocation(false)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                android.location.Location loc = locationResult.getLastLocation();
                if (loc != null) {
                    setQiblaFromLocation(loc.getLatitude(), loc.getLongitude());
                } else {
                    tvStatus.setText("تعذر تحديد الموقع");
                }
                fusedLocationClient.removeLocationUpdates(this);
            }
        };

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    private void setQiblaFromLocation(double lat, double lng) {
        qiblaBearing = calculateQiblaDirection(lat, lng);
        hasQiblaBearing = true;
        String dir = getDirectionName(qiblaBearing);
        tvQiblaBearing.setText(String.format("اتجاه القبلة من الشمال: %.0f° (%s)", qiblaBearing, dir));
        if (noCompassSensor) {
            showStaticQiblaInfo();
        } else {
            tvStatus.setText("وجه هاتفك نحو اتجاه السهم");
            tvDegree.setText("--°");
        }
    }

    private void showStaticQiblaInfo() {
        // No compass sensor - show bearing as the big number
        compassImage.setRotation(0);
        tvDegree.setText(String.format("%.0f°", qiblaBearing));
        tvStatus.setText("⚠ هاتفك لا يحتوي على بوصلة\nاستخدم بوصلة خارجية لتحديد الاتجاه");
        tvCalibration.setVisibility(View.VISIBLE);
        tvCalibration.setText("الهاتف لا يدعم مستشعر البوصلة");
        tvCalibration.setTextColor(Color.parseColor("#E65100"));
    }

    private String getDirectionName(float bearing) {
        if (bearing >= 337.5 || bearing < 22.5) return "شمال";
        if (bearing < 67.5) return "شمال شرق";
        if (bearing < 112.5) return "شرق";
        if (bearing < 157.5) return "جنوب شرق";
        if (bearing < 202.5) return "جنوب";
        if (bearing < 247.5) return "جنوب غرب";
        if (bearing < 292.5) return "غرب";
        return "شمال غرب";
    }

    private float calculateQiblaDirection(double lat, double lng) {
        double latRad = Math.toRadians(lat);
        double lngRad = Math.toRadians(lng);
        double kaabaLatRad = Math.toRadians(KAABA_LAT);
        double kaabaLngRad = Math.toRadians(KAABA_LNG);

        double dLng = kaabaLngRad - lngRad;
        double y = Math.sin(dLng) * Math.cos(kaabaLatRad);
        double x = Math.cos(latRad) * Math.sin(kaabaLatRad)
                - Math.sin(latRad) * Math.cos(kaabaLatRad) * Math.cos(dLng);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (float) ((bearing + 360) % 360);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean hasSensor = false;

        // Try rotation vector first (fused sensor, most reliable)
        Sensor rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVector != null) {
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
            hasSensor = true;
        }

        // Also register accelerometer + magnetometer as fallback
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (magnetometer != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
            hasSensor = true;
        }

        if (!hasSensor) {
            noCompassSensor = true;
            showStaticQiblaInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private float[] gravity;
    private float[] geomagnetic;
    private boolean useRotationVector = false;
    private boolean noCompassSensor = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuth = 0;
        boolean gotAzimuth = false;

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);
            azimuth = (float) Math.toDegrees(orientation[0]);
            gotAzimuth = true;
            useRotationVector = true;
        }

        if (!useRotationVector) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values.clone();
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values.clone();
            }
            if (gravity != null && geomagnetic != null) {
                float[] R = new float[9];
                float[] I = new float[9];
                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimuth = (float) Math.toDegrees(orientation[0]);
                    gotAzimuth = true;
                }
            }
        }

        if (!gotAzimuth) return;

        azimuth = (azimuth + 360) % 360;

        float rotation;
        if (hasQiblaBearing) {
            rotation = (qiblaBearing - azimuth + 360) % 360;
        } else {
            rotation = (360 - azimuth) % 360;
        }

        compassView.setCompassRotation(-azimuth);
        compassImage.setRotation(rotation);
        tvDegree.setText(String.format("%.0f°", azimuth));

        // Check if pointing toward Qibla (within 5 degrees)
        if (hasQiblaBearing) {
            boolean onTarget = rotation < 5 || rotation > 355;
            if (onTarget != isOnTarget) {
                isOnTarget = onTarget;
                updateCircleColor(onTarget);
                if (onTarget) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (vibrator != null) vibrator.vibrate(100);
                }
            }
        }
    }

    private void updateCircleColor(boolean onTarget) {
        int green = Color.parseColor("#4CAF50");
        int gold = ContextCompat.getColor(this, R.color.gold_accent);
        if (onTarget) {
            compassView.setCircleColor(green);
            tvDegree.setTextColor(green);
            compassImage.setColorFilter(green);
            tvStatus.setText("✓ أنت تواجه القبلة");
        } else {
            compassView.setCircleColor(gold);
            tvDegree.setTextColor(gold);
            compassImage.setColorFilter(gold);
            tvStatus.setText("وجه هاتفك نحو اتجاه السهم");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            tvCalibration.setVisibility(View.VISIBLE);
            tvCalibration.setText("دقة البوصلة منخفضة - قم بتحريك الهاتف بشكل رقم 8 لتحسين الدقة");
            tvCalibration.setTextColor(Color.parseColor("#E65100"));
        } else if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            tvCalibration.setVisibility(View.VISIBLE);
            tvCalibration.setText("دقة البوصلة متوسطة");
            tvCalibration.setTextColor(ContextCompat.getColor(this, R.color.gold_accent));
        } else if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            tvCalibration.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationForQibla();
            } else {
                tvStatus.setText("يرجى السماح بإذن الموقع لتحديد اتجاه القبلة");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
