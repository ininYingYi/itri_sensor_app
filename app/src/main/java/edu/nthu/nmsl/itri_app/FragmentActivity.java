package edu.nthu.nmsl.itri_app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.nthu.nmsl.itri_app.settings.Devices;

/**
 * Created by InIn on 2016/9/19.
 */
public class FragmentActivity extends AppCompatActivity {
    private static final String TAG = "FragmentActivity";
    private RadioGroup radioGroup;
    private FragmentManager fragmentManager;
    private BluetoothLeService mBluetoothLeService;
    private boolean mBound;
    private static final int menu_device_group_id = 2;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.

        setContentView(R.layout.activity_fragment);

        radioGroup = (RadioGroup) findViewById(R.id.rg_tab);
        fragmentManager = getFragmentManager();
        radioGroup.setOnCheckedChangeListener(radioGroupListener);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(Devices.deviceAddress[0]);
            Log.d(TAG, "Connect request result=" + result);
        }

    }



    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }


    //dynamical add item to menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_action_menu,menu);
        SubMenu available_devics = menu.addSubMenu("更換連結裝置");
        for (int id = 0; id < Devices.deviceName.length; id++) {
            available_devics.add(menu_device_group_id,id,Menu.NONE,Devices.deviceName[id]);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_ble_scan:
                //scan BLE
                break;
            default:
                //check if device
                if(item.getGroupId() == menu_device_group_id){
                    Log.d(TAG,"device id " + item.getItemId() + " selected.");
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBound = true;
            Log.e(TAG, "onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(Devices.deviceAddress[0]);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected");
            mBluetoothLeService = null;
            mBound = false;
        }
    };

    private boolean mConnected = false;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("BluetoothLeService","ACTION_GATT_CONNECTED");
                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("BluetoothLeService","ACTION_GATT_CONNECTED");
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d("BluetoothLeService","ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());


            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d("BluetoothLeService","ACTION_DATA_AVAILABLE");
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    private Double readingValue = 0.0;
    private int angle1 = 0;
    private int angle2 = 0;
    private int angle3 = 0;
    private int unit = 0;
    private Double battery_voltage = 0.0;
    private int time_interval = 0;
    private int trigger_flag = 0;
    private int version_flag = 0;

    private String[] unitLabel = {"mm","inch"};
    private String unit_label = "mm";

    private void displayData(String data) {
        if (data != null) {
            final String[] tmp = data.split(",");

            try {
                this.readingValue = Double.parseDouble(tmp[1]);
                this.angle1 = Integer.parseInt(tmp[4]);
                this.angle2 = Integer.parseInt(tmp[5]);
                this.angle3 = Integer.parseInt(tmp[6]);
                this.battery_voltage = Double.parseDouble(tmp[7].substring(0, 2));
                this.unit = Integer.parseInt(tmp[2]);
                unit_label = unitLabel[unit];
                //Log.d(TAG,"Unit:"+tmp[2]);
                unit_label = unitLabel[unit];
            } catch (Exception e){
                e.printStackTrace();
            }
            Log.d("SensorValue", tmp[1]);
            //mReadingField.setText( readingValue.toString() + " " + unit_label);
            //this.mAngleField.setText(String.format("%d,%d,%d",angle1,angle2,angle3));
            //this.mBatteryField.setText(String.format("%.1f volt",battery_voltage));


            //mDataField.setText(data);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                //open notification
                if (gattCharacteristic.getUuid().toString().equals(Devices.deviceUUID[0])) {
                    if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                    }
                }

            }
        }
    }

    private RadioGroup.OnCheckedChangeListener radioGroupListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            //change fragment when the radio group checked item changed
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = FragmentFactory.getInstanceByIndex(checkedId);

            Log.e(TAG, "CheckId = " + checkedId);
            if(fragment == null){
                Log.i(TAG, "fragment is null");
            }
            transaction.replace(R.id.content, fragment);
            transaction.commit();
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
