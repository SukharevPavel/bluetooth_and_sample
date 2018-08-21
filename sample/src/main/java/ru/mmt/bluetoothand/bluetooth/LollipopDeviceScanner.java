package ru.mmt.bluetoothand.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by pasha on 11.07.17.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class LollipopDeviceScanner implements IDeviceScanner {

    private IDeviceScanner.Callback scanCallback;
    private boolean deviceFound = false;
    private BluetoothAdapter bluetoothAdapter;
    private Context context;
    private BluetoothLeScanner scanner;
    private boolean isScanning = false;
    private ScanCallback bluetoothScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i(BluetoothController.class.getSimpleName(), "onScanResult lollipop " + result.toString());
            boolean isHeartRateDevice = result.getScanRecord() != null &&
                    result.getScanRecord().getServiceUuids() != null &&
                    result.getScanRecord().getServiceUuids().contains(new ParcelUuid(BluetoothConstants.Service.BLOOD_PRESSURE_SERVICE_UUID));
            if (scanCallback != null && !deviceFound && isHeartRateDevice) {
                Log.i(BluetoothController.class.getSimpleName(), "found device " + result.toString());
                scanCallback.onDeviceFound(result.getDevice());
                deviceFound = true;
                stopScan();
            }
        }
    };

    LollipopDeviceScanner(Context context) {
        this.context = context;
    }


    public void startScan() {
        Log.i(BluetoothController.class.getSimpleName(), "startScan lollipop");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
        isScanning = true;
        deviceFound = false;
    /*    ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(BluetoothConstants.Service.BLOOD_PRESSURE_SERVICE_UUID))
                .build();
        List<ScanFilter> list = new ArrayList<>();
        list.add(filter);*/
        scanner.startScan(bluetoothScanCallback);
    }

    public void stopScan() {
        Log.i(BluetoothController.class.getSimpleName(), "stopScan lollipop");
        isScanning = false;
        try {
            scanner.stopScan(bluetoothScanCallback);
        } catch (IllegalStateException ex) {
            //bluetooth may be turned off
        }
        if (!deviceFound) {
            scanCallback.onNoDeviceFound();
        }

    }

    @Override
    public void getHeartRateDevice(Callback callback) {
        Log.i(BluetoothController.class.getSimpleName(), "getHeartRateDevice");

        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            callback.onBluetoothNotSupported();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            callback.onBluetoothDisabled();
            return;
        }
        {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (!isScanning) {
                scanCallback = callback;
                startScan();
            } else {
                callback.onBusy();
            }
        }
    }
}