package ru.mmt.bluetoothand.ui.heartrate;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ru.mmt.bluetoothand.R;
import ru.mmt.bluetoothand.bluetooth.BluetoothController;

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class HeartRateActivity extends AppCompatActivity implements BluetoothController.BluetoothPairingCallback {

    private static final int REQUEST_ENABLE_BT = 1;
    TextView textView;
    ProgressBar progressBar;
    Button pairButton;

    void pairDevice() {
        BluetoothController.getInstance(HeartRateActivity.this).connectDevice();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_heart_rate);
        textView = findViewById(R.id.heart_rate_text_view);
        progressBar = findViewById(R.id.heart_rate_progress_bar);
        pairButton = findViewById(R.id.heart_rate_pair);
        pairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairDevice();
            }
        });
        BluetoothController.getInstance(this).setPairingCallback(this);
        setIsLoading(BluetoothController.getInstance(this).isLoading());
    }

    private void setIsLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            pairButton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            pairButton.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public void onBluetoothDisabled() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    @Override
    public void onBluetoothNotSupported() {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HeartRateActivity.this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onBounded() {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HeartRateActivity.this, R.string.bluetooth_device_paired, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onBusy() {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HeartRateActivity.this, R.string.bluetooth_device_busy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPairingDeviceFailed() {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HeartRateActivity.this, R.string.bluetooth_connection_failed, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onChangeStatus(final boolean isLoading) {
        invokeOnUIThread(new Runnable() {
            @Override
            public void run() {
                setIsLoading(isLoading);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothController.getInstance(this).setPairingCallback(null);
    }

    private void invokeOnUIThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

}
