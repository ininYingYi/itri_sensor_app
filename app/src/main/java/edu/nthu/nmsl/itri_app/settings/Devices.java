package edu.nthu.nmsl.itri_app.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by YingYi on 2016/9/22.
 */

public class Devices {
    private static Devices self = null;
    private ArrayList<String> deviceName = new ArrayList<String>();
    private ArrayList<String> deviceAddress = new ArrayList<String>();
    public static String deviceUUID = "a495ff11-c5b1-4b44-b512-1370f02d74de";
    public static Devices getInstance() {
        if (self == null) self = new Devices();
        return self;
    }

    public Devices() {

    }

    public String getDeviceName(int i) {
        return deviceName.get(i);
    }

    public String getDeviceAddress(int i) {
        return deviceAddress.get(i);
    }

    public int getDeviceNumber() {
        return deviceName.size();
    }

    public void addDevice(String name, String mac) {
        deviceName.add(name);
        deviceAddress.add(mac);
    }

    public void loadDevice(String name, String address) {
        String[] nameArray = name.split(";");
        for (String device : nameArray) {
            deviceName.add(device);
        }
        String[] addressArray = address.split(";");
        for (String mac : addressArray) {
            deviceAddress.add(mac);
        }
    }

    public String getAllDeviceName() {
        StringBuilder sb = new StringBuilder();
        for (String s : deviceName)
        {
            sb.append(s);
            sb.append(";");
        }
        return sb.toString();
    }

    public String getAllDeviceAddress() {
        StringBuilder sb = new StringBuilder();
        for (String s : deviceAddress)
        {
            sb.append(s);
            sb.append(";");
        }
        return sb.toString();
    }

    public String[] getArrayDeviceName() {
        String array[] = new String[deviceName.size()];
        int i =0;
        for (String s : deviceName)
        {
            array[i] = s;
            i++;
        }
        return array;
    }
}
