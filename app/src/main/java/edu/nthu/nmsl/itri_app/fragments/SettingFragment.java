package edu.nthu.nmsl.itri_app.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.LinkAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import edu.nthu.nmsl.itri_app.Background;
import edu.nthu.nmsl.itri_app.DatabaseHandler;
import edu.nthu.nmsl.itri_app.MeasData;
import edu.nthu.nmsl.itri_app.MeasDataAdapter;
import edu.nthu.nmsl.itri_app.R;
import edu.nthu.nmsl.itri_app.settings.Devices;
import edu.nthu.nmsl.itri_app.settings.Settings;

/**
 * Created by InIn on 2016/9/19.
 */
public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 30 seconds.
    private static final long SCAN_PERIOD = 30000;

    private TextView connected_device_name;
    private TextView connected_server_name;

    private Button scan_new_BLE_device;
    private Button set_db_btn;
    private Button recover_db_btn;
    private Button set_default_sensor_btn;
    //private ProgressBar scan_progress;
    private ListView ble_devices_listview;
    private DatabaseHandler dbHandler;

    ViewGroup progressView;
    //LinearLayout progressView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_page, null);
        progressView = (ViewGroup) view.findViewById(R.id.progressBarView);
        inflater.inflate(R.layout.actionbar_indeterminate_progress, progressView);
        progressView.setVisibility(View.INVISIBLE);
        connected_device_name = (TextView) view.findViewById(R.id.setting_connected_device);
        connected_server_name = (TextView) view.findViewById(R.id.setting_connected_server);
        scan_new_BLE_device = (Button) view.findViewById(R.id.setting_scan_new_ble);
        recover_db_btn = (Button) view.findViewById(R.id.setting_set_db_default);
        set_default_sensor_btn = (Button) view.findViewById(R.id.setting_set_sensor_default);
        set_db_btn = (Button) view.findViewById(R.id.setting_set_db);
        //progressView = (LinearLayout) view.findViewById(R.id.progressBarView);
        ble_devices_listview = (ListView) view.findViewById(R.id.BLE_device_list);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null | mBluetoothLeScanner == null) {
            Toast.makeText(getActivity(), "Device does not support BLE.", Toast.LENGTH_SHORT).show();
            this.onStop();
        }

        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter(getActivity());
        ble_devices_listview.setAdapter(mLeDeviceListAdapter);
        ble_devices_listview.setOnItemClickListener(adapterListener);

        scan_new_BLE_device.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            if(!mScanning){
                scanLeDevice(true);
            }

            }
        });

        recover_db_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("恢復預設資料庫");
                builder.setMessage("是否要恢復成工研院預設資料庫?");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.serverURL = Settings.default_serverURL;
                        Settings.imageURL = Settings.default_imageURL;
                        Settings.serverName = Settings.default_serverName;
                        Toast.makeText(getActivity(),"恢復成功",Toast.LENGTH_SHORT).show();
                        dbHandler.isServerAlive();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        set_db_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final View item = LayoutInflater.from(getActivity()).inflate(R.layout.setting_set_db_view, null);
                final EditText db_url = (EditText)item.findViewById(R.id.EditDBsetting);
                final EditText db_name = (EditText)item.findViewById(R.id.EditDBNameSetting);
                final EditText db_image_url = (EditText)item.findViewById(R.id.EditImageSetting);

                db_url.setHint(Settings.serverURL);
                db_name.setHint(Settings.serverName);
                db_image_url.setHint(Settings.imageURL);
                db_image_url.setHint(Settings.imageURL);

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("進階設定資料庫位置");
                builder.setView(item);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences settings = getActivity().getSharedPreferences("devices", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        if(db_url.getText().toString().length() > 0){
                            editor.putString("db_url", db_url.getText().toString()+"/");
                            Settings.serverURL = db_url.getText().toString();
                        }
                        if(db_name.getText().toString().length() > 0){
                            editor.putString("db_name", db_name.getText().toString());
                            Settings.serverName = db_name.getText().toString();
                        }
                        if(db_image_url.getText().toString().length() > 0){
                            editor.putString("db_image_url", db_image_url.getText().toString()+"/");
                            Settings.imageURL = db_image_url.getText().toString();
                        }
                        Log.d(TAG,"db_url:"+ Settings.serverURL + " db_name:"+Settings.serverName + " db_image_url"+Settings.imageURL);
                        editor.commit();

                        dbHandler.isServerAlive();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();

            }
        });

        set_default_sensor_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final String test[] = Devices.getInstance().getArrayDeviceName();
                new AlertDialog.Builder(getActivity())
                        .setTitle("請選擇預設裝置")
                        .setItems(test, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = test[which];
                                Toast.makeText(getContext(), name + "於下次啟動時將會自動連接", Toast.LENGTH_SHORT).show();
                                Background.getInstance().selectSensor(which);
                                SharedPreferences settings = getActivity().getSharedPreferences("devices", 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt("selectSensor", which);
                                editor.commit();
                            }
                        })
                        .show();
            }
        });

        connected_device_name.setText(Background.getInstance().getSensorName());

        dbHandler = new DatabaseHandler(UIHandler);
        dbHandler.isServerAlive();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            //update info
            connected_device_name.setText(Background.getInstance().getSensorName());
            dbHandler.isServerAlive();
        }
    }

    public Handler UIHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getActivity() == null ) return;
            switch (msg.what){
                case DatabaseHandler.check_alive:
                    //Log.d(TAG,"Receive:"+msg.obj.toString());
                    boolean alive = (boolean)msg.obj;
                    if(alive){
                        connected_server_name.setText(Settings.serverName);
                    }else {
                        connected_server_name.setText(R.string.service_offline);
                    }
                    break;

                default:
                    Log.d(TAG,"Error");
                    break;
            }
        }
    };

    @Override
    public void onStop() {
        Log.d("TEST","onStop");
        super.onStop();
        mBluetoothLeScanner.stopScan(mLeScanCallback);
    }

    @Override
    public void onResume() {
        Log.d("TEST","onResume");
        super.onResume();
        connected_device_name.setText(Background.getInstance().getSensorName());
        progressView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause() {
        Log.d("TEST","onPause");
        super.onPause();
        mBluetoothLeScanner.stopScan(mLeScanCallback);
    }

    //Scanning BLE Device
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scanx period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    progressView.setVisibility(View.INVISIBLE);
                    mBluetoothLeScanner.stopScan(mLeScanCallback);
                    //invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            progressView.setVisibility(View.VISIBLE);
            mScanning = true;
            mBluetoothLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            progressView.setVisibility(View.INVISIBLE);
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
        getActivity().invalidateOptionsMenu();
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            super.onBatchScanResults(results);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ScanResult result : results) {
                        mLeDeviceListAdapter.addDevice(result.getDevice());
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    //scan_progress.clearAnimation();
                }
            });
        }

    };

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter(Context context) {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = LayoutInflater.from(context);
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    };

    AdapterView.OnItemClickListener adapterListener = new AdapterView.OnItemClickListener() {
        String device_name, device_mac;
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            BluetoothDevice ble_deice = (BluetoothDevice) adapterView.getItemAtPosition(i);

            device_name = ble_deice.getName();
            device_mac = ble_deice.getAddress();
            final View item = LayoutInflater.from(getActivity()).inflate(R.layout.setting_add_device, null);
            final TextView info = (TextView)item.findViewById(R.id.deviceInfo);
            info.setText("裝置名稱:" + device_name + "\n裝置MAC編號:" + device_mac );
            final EditText name = (EditText)item.findViewById(R.id.deviceEditText);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("新增常用裝置");
            builder.setView(item);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Devices.getInstance().addDevice(name.getText().toString(), device_mac);
                    SharedPreferences settings = getActivity().getSharedPreferences("devices", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("deviceName", Devices.getInstance().getAllDeviceName());
                    editor.putString("deviceAddress", Devices.getInstance().getAllDeviceAddress());
                    editor.commit();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }

    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }




}