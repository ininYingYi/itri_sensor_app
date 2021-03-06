package edu.nthu.nmsl.itri_app;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import edu.nthu.nmsl.itri_app.settings.Devices;
import edu.nthu.nmsl.itri_app.settings.Settings;

public class MainActivity extends AppCompatActivity {
    private ProgressBar myProgressBar;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private boolean getPermission = false;
    private final static String TAG = "BLEDeviceControl";

    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(Devices.getInstance().getDeviceAddress(0));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // load the devices settings and server settings. if there is no value will load the default settings.
        SharedPreferences settings = getSharedPreferences("devices", 0);
        String deviceName = settings.getString("deviceName","BlueDial157;WiMER242");
        String deviceAddress = settings.getString("deviceAddress","C4:BE:84:49:C5:3E;5C:31:3E:5C:48:BC");

        Settings.serverURL = settings.getString("db_url","http://140.96.173.245/cps/php/");
        Settings.serverName = settings.getString("db_name","140.96.173.245");
        Settings.imageURL = settings.getString("db_image_url","http://140.96.173.245/cps/Content/Picture/");

        int selectorSensor = settings.getInt("selectSensor", 0);
        Background.getInstance().selectSensor(selectorSensor);
        Log.d(TAG,"db_url:"+ Settings.serverURL + " db_name:"+Settings.serverName + " db_image_url"+Settings.imageURL);

        Devices.getInstance().loadDevice(deviceName, deviceAddress);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        if( ContextCompat.checkSelfPermission(this , android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
        }
        else {
            getPermission = true;
            startConnectBLE();
        }

        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {

                while (mProgressStatus < 100) {
                    mProgressStatus = loading();
                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            myProgressBar.setProgress(mProgressStatus);
                        }
                    });
                    try {
                        Thread.sleep(20);
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.post(new Runnable() {
                    public void run() {
                        mBluetoothLeScanner.stopScan(mLeScanCallback);
                        // jump to FragmentActivity (Index, Measure, View, Setting)
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, FragmentActivity.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                    }
                });
            }
            private int loading() {
                if (getPermission) {
                    mProgressStatus++;
                }
                return mProgressStatus;
            };
        }).start();
    }
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private void startConnectBLE() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null | mBluetoothLeScanner == null) {
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //mBluetoothLeScanner.startScan(mLeScanCallback);
    }
    private ScanCallback mLeScanCallback = new ScanCallback(){
        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            super.onBatchScanResults(results);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for ( ScanResult result: results ) {
                        Log.d("SCAN", result.getDevice().getName());
                        //mLeDeviceListAdapter.addDevice(result.getDevice());
                        //mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mLeDeviceListAdapter.addDevice(result.getDevice());
                    //mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }

    };

    //check permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "coarse location permission granted");
                    getPermission = true;
                    startConnectBLE();
                } else {
                    getPermission = false;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
