package edu.nthu.nmsl.itri_app;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import edu.nthu.nmsl.itri_app.settings.Devices;

/**
 * Created by InIn on 2016/9/19.
 */
public class Background {
    private static Background self = null;
    public static Background getInstance() {
        if (self == null) self = new Background();
        return self;
    }

    private final int updatePeriod = 10; //ms
    private final int dataOutOfDate = 500; //ms
    private Timer timer = new Timer();
    public Background() {
        timer.schedule(timerTask, 0, updatePeriod);
    }
    private int ms = 0;
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            ms += updatePeriod;
            if (ms > dataOutOfDate) keepRecieve = false;
            if (ms > Integer.MAX_VALUE) ms = 0;
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
    private Boolean keepRecieve = false;
    private Boolean isConnect = false;
    // keep update the sensor value
    public void recieveData(String data) {
        if (data != null) {
            final String[] tmp = data.split(",");
            try {
                this.readingValue = Double.parseDouble(tmp[1]);
                this.angle1 = Integer.parseInt(tmp[4]);
                this.angle2 = Integer.parseInt(tmp[5]);
                this.angle3 = Integer.parseInt(tmp[6]);
                this.battery_voltage = Double.parseDouble(tmp[7].substring(0, 2));
                if(tmp[2].contains("0")){
                    this.unit = 0;
                }else if(tmp[2].contains("1")){
                    this.unit = 1;
                }else{
                    this.unit = 0;
                }

                unit_label = unitLabel[unit];
                //Log.d(TAG,"Unit:"+tmp[2]);
                unit_label = unitLabel[unit];
                keepRecieve = true;
                ms = 0;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            keepRecieve = false;
        }
    }

    public String getSensorValue() {
        if (keepRecieve) {
            return String.valueOf(readingValue);
        }
        else {
            return null;
        }
    }

    public String getSensorUnit() {
        if (keepRecieve) {
            return unit_label;
        }
        else {
            return null;
        }
    }

    private int selectSensor = 0;
    public void selectSensor(int i) {
        selectSensor = i;
        ms = 0;
    }

    public int getUsingSensorID() {
        return selectSensor;
    }

    public String getSensorName() {
        if (isConnect) {
            return Devices.getInstance().getDeviceName(selectSensor);
        }
        else {
            return "裝置離線";
        }
    }

    public void setConnectionState(boolean state) {
        isConnect = state;
    }

}
