package edu.nthu.nmsl.itri_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.nthu.nmsl.itri_app.settings.Settings;

/**
 * Created by mao on 2016/9/22.
 */

public class DatabaseHandler {
    private static final String TAG = "DBHandler";
    public static final int statePartId = 0;
    public static final int statePartSerialId = 1;
    public static final int stateWorkId = 2;
    public static final int stateMeasId = 3;
    public static final int stateGetAllMeasData = 4;
    public static final int sendData = 5;
    public static final int imageTask = 6;
    public static final int no_image_available = 7;
    private Handler ActivityUIHandler;

    public DatabaseHandler (Handler mHandler){
        this.ActivityUIHandler = mHandler;
    }


    public void requestPartId(){
        String mURL = Settings.serverURL + "appGetPartId.php";
        URL url;
        try {
            url = new URL(mURL);
            Thread getThread;
            getThread = new Thread(new httpGet(url,statePartId));
            getThread.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"URL error");
        }
    }

    public void requestPartSerialId(String selectedPartId){
        String mURL = Settings.serverURL + "appGetPartSerialId.php?partId="+selectedPartId;
        URL url;
        try {
            url = new URL(mURL);
            Thread getThread;
            getThread = new Thread(new httpGet(url,statePartSerialId));
            getThread.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"URL error");
        }
    }

    public void requestWorkId(String selectedPartId){
        String mURL = Settings.serverURL + "appGetWorkId.php?partId=" + selectedPartId;
        URL url;
        try {
            url = new URL(mURL);
            Thread getThread;
            getThread = new Thread(new httpGet(url,stateWorkId));
            getThread.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"URL error");
        }
    }

    public void requestMeasId(String selectedPartId, String selectedWorkId){
        String mURL = Settings.serverURL + "appGetMeasId.php?partId=" + selectedPartId + "&workId=" + selectedWorkId;

        URL url;
        try {
            url = new URL(mURL);
            Thread getThread;
            getThread = new Thread(new httpGet(url,stateMeasId));
            getThread.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"URL error");
        }
    }

    public void requestMeasData(String selectedPartId, String selectedPartSerial,String selectedWorkId){
        String mURL = Settings.serverURL
                + "appGetMeasData.php?partId=" + selectedPartId + "&workId="
                + selectedWorkId + "&partSerialId=" + selectedPartSerial;

        URL url;
        try {
            url = new URL(mURL);
            Thread getThread;
            getThread = new Thread(new httpGet(url, stateGetAllMeasData));
            getThread.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"URL error");
        }
    }

    public void insertMeasData(String selectedPartSerial, String measId, double value){
        String mURL = Settings.serverURL
                + "insertMeasData.php?partSerialId=" + selectedPartSerial + "&measId="
                + measId + "&value=" + value;
        URL url;
        try {
            url = new URL(mURL);
            Thread getThread;
            getThread = new Thread(new httpGet(url, sendData));
            getThread.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"URL error");
        }
    }

    public void imageTask(String imageURL){
        String mURL = Settings.imageURL + imageURL;
        URL url;
        try {
            url = new URL(mURL);
            Thread getThread;
            getThread = new Thread(new imageDownloader(url, imageTask));
            getThread.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"URL error");
        }
    }

    public class imageDownloader implements Runnable {
        private URL url;
        private int state;

        public imageDownloader(URL url, int s){
            this.state = s;
            this.url = url;
        }

        @Override
        public void run() {
            HttpURLConnection urlConnection;
            try {
                urlConnection = (HttpURLConnection) this.url.openConnection();
                urlConnection.setRequestMethod("GET");

                if( urlConnection.getResponseCode() == 200){

                    Bitmap image = BitmapFactory.decodeStream(urlConnection.getInputStream());

                    urlConnection.getInputStream().close();

                    Message message = ResponseHandler.obtainMessage(this.state,image);
                    message.sendToTarget();

                } else {

                    Log.d(TAG, " Http connection error with code:" + urlConnection.getResponseCode());
                }
                urlConnection.disconnect();

            } catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    public class httpGet implements Runnable {
        private URL url;
        private int state;

        public httpGet(URL url, int s){
            this.state = s;
            this.url = url;
        }

        @Override
        public void run() {
            HttpURLConnection urlConnection;
            try {
                urlConnection = (HttpURLConnection) this.url.openConnection();
                urlConnection.setRequestMethod("GET");

                if( urlConnection.getResponseCode() == 200){

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    StringBuilder builder = new StringBuilder();
                    String seg = null;

                    while( (seg = reader.readLine()) != null ){
                        builder.append(seg);
                    }

                    urlConnection.getInputStream().close();
                    reader.close();

                    String res = builder.toString();
                    Message message = ResponseHandler.obtainMessage(this.state,res);
                    message.sendToTarget();

                } else {
                    Log.d(TAG, " Http connection error with code:" + urlConnection.getResponseCode());
                }
                urlConnection.disconnect();

            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private Handler ResponseHandler = new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Message message;
            switch (msg.what){
                case statePartId:
                    ArrayList<PartData> mPartIdArray = new ArrayList<PartData>();
                    try {
                        JSONArray rec_part_json = new JSONArray(msg.obj.toString());
                        for(int i=0;i < rec_part_json.length();i++){
                            JSONObject data = rec_part_json.getJSONObject(i);
                            PartData mdata = new PartData(data.getString("PartID"),data.getString("PartName"));
                            mPartIdArray.add(mdata);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    message = ActivityUIHandler.obtainMessage(msg.what,mPartIdArray);
                    message.sendToTarget();


                    Log.d(TAG,"Receive:"+mPartIdArray.toString());
                    break;
                case statePartSerialId:

                    ArrayList<String> mPartSerialIdArray = new ArrayList<String>();
                    try {

                        JSONArray rec_part_json = new JSONArray(msg.obj.toString());
                        for(int i=0;i < rec_part_json.length();i++){
                            mPartSerialIdArray.add(rec_part_json.get(i).toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    message = ActivityUIHandler.obtainMessage(msg.what,mPartSerialIdArray);
                    message.sendToTarget();

                    Log.d(TAG,"Receive:"+mPartSerialIdArray.toString());
                    break;
                case stateWorkId:

                    ArrayList<String> mWorkIdArray = new ArrayList<String>();
                    try {

                        JSONArray rec_part_json = new JSONArray(msg.obj.toString());
                        for(int i=0;i < rec_part_json.length();i++){
                            mWorkIdArray.add(rec_part_json.get(i).toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    message = ActivityUIHandler.obtainMessage(msg.what,mWorkIdArray);
                    message.sendToTarget();

                    Log.d(TAG,"mWorkIdArray Receive:"+mWorkIdArray.toString());
                    break;
                case stateMeasId:

                    ArrayList<MeasData> mMeasIdArray = new ArrayList<MeasData>();
                    try {

                        JSONArray rec_part_json = new JSONArray(msg.obj.toString());
                        for(int i=0;i < rec_part_json.length();i++){
                            JSONObject data = rec_part_json.getJSONObject(i);
                            MeasData mdata = new MeasData(Integer.parseInt(data.getString("MeasID")),data.getString("ImagePath"));
                            mMeasIdArray.add(mdata);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    message = ActivityUIHandler.obtainMessage(msg.what,mMeasIdArray);
                    message.sendToTarget();
                    Log.d(TAG,"mMeasIdArray Receive:"+mMeasIdArray.toString());
                    break;
                case stateGetAllMeasData:
                    ArrayList<MeasData> mMeasDataArray = new ArrayList<MeasData>();
                    try {
                        JSONArray rec_part_json = new JSONArray(msg.obj.toString());
                        for(int i=0;i < rec_part_json.length();i++){
                            JSONObject data = rec_part_json.getJSONObject(i);
                            MeasData mdata = new MeasData(data.getString("WorkID"),Integer.parseInt(data.getString("MeasID")),
                                    data.getString("Value"),data.getString("NormalSize"),data.getString("Status"),
                                    data.getString("ToleranceU"),data.getString("ToleranceL"),
                                    data.getString("FinalMeas"),data.getString("IsKeyMeas"));
                            mMeasDataArray.add(mdata);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    message = ActivityUIHandler.obtainMessage(msg.what,mMeasDataArray);
                    message.sendToTarget();

                    Log.d(TAG,"Receive:"+mMeasDataArray.toString());
                    break;
                case sendData:
                    message = ActivityUIHandler.obtainMessage(msg.what,msg.obj);
                    message.sendToTarget();

                    break;
                case imageTask:
                    message = ActivityUIHandler.obtainMessage(msg.what,msg.obj);
                    message.sendToTarget();
                    Log.d(TAG,"Downloaded image.");
                    break;
                default:
                    Log.d(TAG,"Http Error");
                    break;
            }
        }
    };
}
