package edu.nthu.nmsl.itri_app.settings;

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
        deviceName.add("BlueDial157");
        deviceAddress.add("C4:BE:84:49:C5:3E");
        deviceName.add("WiMER242");
        deviceAddress.add("5C:31:3E:5C:48:BC");
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
}
