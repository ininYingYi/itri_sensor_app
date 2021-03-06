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

import android.view.Menu;
import android.view.MenuItem;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.nthu.nmsl.itri_app.settings.Devices;

import static android.content.Context.BIND_AUTO_CREATE;

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
    private Intent gattServiceIntent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.

        setContentView(R.layout.activity_fragment);

        radioGroup = (RadioGroup) findViewById(R.id.rg_tab);
        fragmentManager = getFragmentManager();



        if(savedInstanceState != null){
            //do nothing
            Log.e(TAG,"savedInstanceState");
        }else {
            Log.e(TAG,"click 0");
            radioGroup.check(radioGroup.getChildAt(0).getId());
            Fragment fragment = fragmentManager.findFragmentByTag(String.valueOf(R.id.radioButton1));
            if (fragment == null) {
                Log.i(TAG, "fragment is null, create one ");
                fragment = FragmentFactory.getInstanceByIndex(R.id.radioButton1);
            }
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, fragment,String.valueOf(R.id.radioButton1));
            transaction.commit();
        }


        Log.d(TAG,"getCheckedRadioButtonId:"+radioGroup.getCheckedRadioButtonId());

        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(Devices.getInstance().getDeviceAddress(Background.getInstance().getUsingSensorID()));
            Log.d(TAG, "Connect request result=" + result);
        }
        radioGroup.setOnCheckedChangeListener(radioGroupListener);
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
    SubMenu available_devics;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        available_devics.clear();
        for (int id = 0; id < Devices.getInstance().getDeviceNumber(); id++) {
            if (Background.getInstance().getUsingSensorID() == id)
                available_devics.add(menu_device_group_id,id,Menu.NONE,"* " + Devices.getInstance().getDeviceName(id));
            else
                available_devics.add(menu_device_group_id,id,Menu.NONE,Devices.getInstance().getDeviceName(id));
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_action_menu,menu);
        available_devics = menu.addSubMenu("更換連結裝置");
        for (int id = 0; id < Devices.getInstance().getDeviceNumber(); id++) {
            if (Background.getInstance().getUsingSensorID() == id)
                available_devics.add(menu_device_group_id,id,Menu.NONE,"A" + Devices.getInstance().getDeviceName(id));
            else
                available_devics.add(menu_device_group_id,id,Menu.NONE,Devices.getInstance().getDeviceName(id));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ble_scan:
                //scan BLE
                radioGroup.check(radioGroup.getChildAt(3).getId());
                break;
            default:
                //check if device
                if(item.getGroupId() == menu_device_group_id){
                    Log.d(TAG,"device id " + item.getItemId() + " selected.");
                    Background.getInstance().selectSensor(item.getItemId());
                    mBluetoothLeService.disconnect();
                    mConnected = false;
                    mBluetoothLeService.connect(Devices.getInstance().getDeviceAddress(Background.getInstance().getUsingSensorID()));
                    unbindService(mServiceConnection);
                    gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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
            mBluetoothLeService.connect(Devices.getInstance().getDeviceAddress(Background.getInstance().getUsingSensorID()));
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
                Toast.makeText(context, "裝置連線成功", Toast.LENGTH_SHORT).show();
                Background.getInstance().setConnectionState(true);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("BluetoothLeService","ACTION_GATT_CONNECTED");
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                Toast.makeText(context, "裝置離線",Toast.LENGTH_SHORT).show();
                Background.getInstance().setConnectionState(false);
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d("BluetoothLeService","ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d("BluetoothLeService","ACTION_DATA_AVAILABLE");
                Background.getInstance().recieveData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                //open notification
                if (gattCharacteristic.getUuid().toString().equals(Devices.deviceUUID)) {
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

            /*if (FragmentFactory.inMeasure2 == true && checkedId == R.id.radioButton2) {
                checkedId = R.id.button;
            }*/
            Fragment fragment = fragmentManager.findFragmentByTag(String.valueOf(checkedId));
            if (fragment == null) {
                Log.i(TAG, "fragment is null, create one ");
                fragment = FragmentFactory.getInstanceByIndex(checkedId);
            }


            Log.e(TAG,"getCheckedRadioButtonId:"+radioGroup.getCheckedRadioButtonId());
            Log.e(TAG, "CheckId = " + checkedId);

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (FragmentFactory.inMeasure2 == true && checkedId == R.id.radioButton2) {
                Fragment fragment2 = fragmentManager.findFragmentByTag(String.valueOf(R.id.button));
                if (fragment2 == null) {
                    Log.i(TAG, "fragment is null, create one ");
                    fragment2 = FragmentFactory.getInstanceByIndex(R.id.button);
                }
                transaction.replace(R.id.content, fragment2, String.valueOf(R.id.button));
            }else {
                transaction.replace(R.id.content, fragment,String.valueOf(checkedId));
            }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
