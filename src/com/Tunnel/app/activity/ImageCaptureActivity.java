package com.Tunnel.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;
import com.Tunnel.app.R;
import com.Tunnel.app.util.GlobalSwitch;
import com.Tunnel.app.util.Orientation;
import com.Tunnel.app.view.CameraSurfaceView;

/**
 * Created by Yang Yupeng on 2014/7/24.
 */
public class ImageCaptureActivity extends Activity implements SensorEventListener {

    private CameraSurfaceView surfaceView;
    private ImageButton takePic;

    private SensorManager sensorManager;
    private Sensor orientationSensor;
    private boolean getOrientation = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!android.os.Build.MANUFACTURER.toUpperCase().contains("MEIZU")) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.image_capture);

        takePic = (ImageButton) this.findViewById(R.id.mid);
        takePic.setVisibility(View.VISIBLE);
        takePic.setImageResource(R.drawable.capture);
        takePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                surfaceView.autoFocus();
            }
        });

        surfaceView = (CameraSurfaceView)this.findViewById(R.id.surface_camera);
        surfaceView.setShutterCallback(new ShutterCallback() {
            @Override
            public void onShutter() {
                getOrientation = true;
            }
        });

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (GlobalSwitch.bCorrectionMode) {
            Toast.makeText(this, R.string.correction_indicate, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (getOrientation && event.sensor.getType() == Sensor.TYPE_ORIENTATION)
        {
            Orientation.X = event.values[SensorManager.DATA_Y];
            Orientation.Y = event.values[SensorManager.DATA_Z];
            Toast.makeText(this, "" + Orientation.Y, Toast.LENGTH_LONG).show();
            getOrientation = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onBackPressed() {
        if (GlobalSwitch.bCorrectionMode)
        {
            GlobalSwitch.bCorrectionMode = false;
        }
        super.onBackPressed();
    }
}
