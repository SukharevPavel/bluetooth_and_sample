package ru.mmt.bluetoothand.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.UUID;

/**
 * Class for getting heart rate bluetooth device for android devices with API > 18 & API < 21
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class LegacyDeviceScanner implements IDeviceScanner {

    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isScanning = false;
    private boolean deviceFound = false;
    private IDeviceScanner.Callback scanCallback;
    private BluetoothAdapter.LeScanCallback bluetoothCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(BluetoothController.class.getSimpleName(), "onLeScan");
            if (scanCallback != null) {
                Log.i(BluetoothController.class.getSimpleName(), "found device " + device.getAddress() + " " + device.getName());
                if (device.getName() != null) {
                    Log.i(BluetoothController.class.getSimpleName(), device.getName());
                    //todo better verifying
                    if (device.getName().startsWith("A&D") && !deviceFound) {
                        deviceFound = true;
                        scanCallback.onDeviceFound(device);
                        stopScan();
                    }
                }

            }
        }
    };

    LegacyDeviceScanner(Context context) {
        this.context = context;
    }

    private void stopScan() {
        if (!deviceFound && scanCallback != null) {
            scanCallback.onNoDeviceFound();
        }
        isScanning = false;
        bluetoothAdapter.stopLeScan(bluetoothCallback);
    }

    @Override
    public void getHeartRateDevice(IDeviceScanner.Callback callback) {
        Log.i(BluetoothController.class.getSimpleName(), "getHeartRateDevice");

        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            callback.onBluetoothNotSupported();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            callback.onBluetoothDisabled();
            return;
        }
        if (!isScanning) {
            this.scanCallback = callback;
            startScan();
        } else {
            callback.onBusy();
        }

    }

    private void startScan() {
        Log.i(BluetoothController.class.getSimpleName(), "startScan");
        Handler handler = new Handler();
        // Stops scanning after a pre-defined scan period.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
        deviceFound = false;
        isScanning = true;
        bluetoothAdapter.startLeScan(new UUID[]{BluetoothConstants.Service.BLOOD_PRESSURE_SERVICE_UUID}, bluetoothCallback);

    }
}
