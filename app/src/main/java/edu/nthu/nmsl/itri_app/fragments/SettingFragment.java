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
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.nthu.nmsl.itri_app.R;
import edu.nthu.nmsl.itri_app.settings.Devices;

/**
 * Created by InIn on 2016/9/19.
 */
public class SettingFragment extends Fragment {


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
    private Button scan_new_BLE_device;
    private ProgressBar scan_progress;
    private ListView ble_devices_listview;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_page, null);
        connected_device_name = (TextView) view.findViewById(R.id.setting_connected_device);
        scan_new_BLE_device = (Button) view.findViewById(R.id.setting_scan_new_ble);
        scan_progress = (ProgressBar) view.findViewById(R.id.scan_BLE_ProgressBar);
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

        scan_progress.clearAnimation();

        scan_new_BLE_device.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mScanning){
                    scan_progress.animate();
                    scanLeDevice(true);
                }

            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //Scanning BLE Device
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scanx period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scan_progress.clearAnimation();
                    mBluetoothLeScanner.stopScan(mLeScanCallback);
                    //invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            scan_progress.clearAnimation();
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
        //invalidateOptionsMenu();
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
                    scan_progress.clearAnimation();
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

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("新增常用裝置");
            builder.setMessage("裝置名稱: " + device_name + "\n" + "裝置MAC編號" + device_mac);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Devices.getInstance().addDevice(device_name, device_mac);
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