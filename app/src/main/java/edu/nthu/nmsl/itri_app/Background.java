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
    private final int dataOutOfDate = 200; //ms
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

    public void recieveData(String data) {
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
            keepRecieve = true;
            ms = 0;
        }
        else {
            keepRecieve = false;
        }
    }

    public String getValue() {
        if (keepRecieve) {
            return String.valueOf(readingValue);
        }
        else {
            return null;
        }
    }
}
