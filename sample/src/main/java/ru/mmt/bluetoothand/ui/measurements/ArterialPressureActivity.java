package ru.mmt.bluetoothand.ui.measurements;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import ru.mmt.bluetoothand.R;
import ru.mmt.bluetoothand.bluetooth.BluetoothController;
import ru.mmt.bluetoothand.ui.heartrate.HeartRateActivity;
import ru.mmt.bluetoothand.utils.ArterialPressure;
import ru.mmt.bluetoothand.utils.Constants;
import ru.mmt.bluetoothand.utils.KeyboardUtil;
import ru.mmt.bluetoothand.utils.MeasurementsResultContainer;



public class ArterialPressureActivity extends AppCompatActivity implements
        ArterialPressureFragment.OnArterialPressureListener, BluetoothController.BluetoothMeasurementCallback {

    private Toolbar toolbar;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arterial_pressure);
        setUpToolbar();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.arterial_pressure_fragment_container, new ArterialPressureFragment(), ArterialPressureFragment.TAG)
                    .commit();
        }
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.arterial_pressure_title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
                KeyboardUtil.hideIfShowed(ArterialPressureActivity.this);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothController.getInstance(this).setMeasurementCallback(this);
            BluetoothController.getInstance(this).getMeasurements();
        } else {
            Toast.makeText(this, getString(R.string.bluetooth_not_supported), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothController.getInstance(this).setMeasurementCallback(null);
        }
    }


    @Override
    public void onArterialPressureFabClicked() {
        Intent intent = new Intent(this, HeartRateActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    BluetoothController.getInstance(this).getMeasurements();
                }
            }
        }
        if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    @Override
    public void onSuccess(final BluetoothController.HeartRateResults results) {
        //todo submit result to server
        //in this app we simply modify local variable instead
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                MeasurementsResultContainer.getInstance().addPressure(new ArterialPressure(
                        results.getSystolicPressure(),
                        results.getDiasystolicPressure(),
                        results.getPulse(),
                        Constants.DATE_FORMAT_FULL.format(System.currentTimeMillis()
                        )));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothController.getInstance(ArterialPressureActivity.this).getMeasurements();
        }
    }


    @Override
    public void onNoResults() {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ArterialPressureActivity.this, R.string.bluetooth_no_result, Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    BluetoothController.getInstance(ArterialPressureActivity.this).getMeasurements();
                }
            }
        });
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onConnectionFailed() {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    BluetoothController.getInstance(ArterialPressureActivity.this).getMeasurements();
                }
            }
        });
    }

    @Override
    public void onBluetoothNotSupported() {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ArterialPressureActivity.this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
               // finish();
            }
        });
    }

    @Override
    public void onBusy() {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ArterialPressureActivity.this, R.string.bluetooth_device_busy, Toast.LENGTH_SHORT).show();
                // finish();
            }
        });
    }

    private void invokeOnUIThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }



}
