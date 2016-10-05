package edu.nthu.nmsl.itri_app.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import edu.nthu.nmsl.itri_app.Background;
import edu.nthu.nmsl.itri_app.DatabaseHandler;
import edu.nthu.nmsl.itri_app.FragmentFactory;
import edu.nthu.nmsl.itri_app.MeasData;
import edu.nthu.nmsl.itri_app.R;

import static edu.nthu.nmsl.itri_app.R.id.button;

/**
 * Created by YingYi on 2016/9/22.
 */

public class Measure2Fragment extends Fragment {
    private static final String TAG = "Measure2Fragment";
    private TextSwitcher textSwitcher;
    private String partID, partSerialID, workID;
    private DatabaseHandler dbHandler;
    private ArrayList<MeasData> measID;
    private TextView measIDText, valueText, deviceName;
    private Button left, right, upload, reset;
    private ImageView image;
    private int measIndex = 0, measNumber = 0;
    private Timer timer = new Timer();
    private Resources res;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("MeasureFragment", "onCreateView");
        Bundle data = getArguments();
        partID = data.getString("partID");
        partSerialID = data.getString("partSerialID");
        workID = data.getString("workID");

        View view = inflater.inflate(R.layout.measure2_page, null);
        measIDText = (TextView) view.findViewById(R.id.textView);
        left = (Button) view.findViewById(R.id.button2);
        left.setOnClickListener(clickListener);
        right = (Button) view.findViewById(R.id.button3);
        right.setOnClickListener(clickListener);
        image = (ImageView) view.findViewById(R.id.imageView);
        upload = (Button) view.findViewById(R.id.upload);
        upload.setOnClickListener(clickListener);
        reset = (Button) view.findViewById(R.id.reset);
        reset.setOnClickListener(clickListener);
        valueText = (TextView) view.findViewById(R.id.textView3);
        deviceName = (TextView) view.findViewById(R.id.device_model);
        dbHandler = new DatabaseHandler(UIHandler);
        dbHandler.requestMeasId(partID, workID);
        timer = new Timer();
        updateValue = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        sensorValue = Background.getInstance().getSensorValue();
                        sensorName = Background.getInstance().getSensorName();
                        deviceName.setText(sensorName);
                        if (sensorValue != null) {
                            valueText.setText(sensorValue);
                        }
                        else {
                            valueText.setText("NO DATA");
                        }
                    }
                });
            }
        };
        timer.schedule(updateValue, 0, 10);

        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        res = getResources();
    }

    @Override
    public void onStop() {
        Log.d(TAG,"onStop");
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        timer.cancel();
    }

    public Handler UIHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DatabaseHandler.stateMeasId:
                    measID = (ArrayList<MeasData>)msg.obj;
                    measNumber = measID.size();
                    updateUI();
                    break;
                case DatabaseHandler.imageTask:
                    if (msg.obj != null) {
                        image.setImageBitmap((Bitmap) msg.obj);
                        image.invalidate();
                    }
                    break;
                case DatabaseHandler.sendData:
                    String result = msg.obj.toString();
                    Log.d(TAG, result);
                    if (result.equals("Successfully"))
                        Toast.makeText(getActivity(), "Upload succesfully", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.d(TAG,"Error");
                    break;
            }
        }
    };

    public void updateUI() {
        Log.d(TAG, String.valueOf(measIndex));
        if (measIndex < measNumber) {
            MeasData meas = measID.get(measIndex);
            measIDText.setText("程序:" + workID + "編號:" + String.valueOf(meas.getMeasID()));
            dbHandler.imageTask(meas.getImageURL());

            Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.noimage);

            image.setImageBitmap(bmp);
            image.invalidate();
        }
    }
    private Handler mHandler = new Handler();
    private String sensorValue, sensorName;
    private TimerTask updateValue = new TimerTask() {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                public void run()
                {
                    sensorValue = Background.getInstance().getSensorValue();
                    sensorName = Background.getInstance().getSensorName();
                    if (sensorValue != null) {
                        valueText.setText(sensorValue);
                        deviceName.setText(sensorName);
                    }
                    else
                        valueText.setText("NO DATA");
                }
            });
        }
    };

    Button.OnClickListener clickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(left)) {
                if (measIndex > 0) measIndex -= 1;
                updateUI();
            }
            else if (v.equals(right)) {
                if (measIndex < measNumber - 1) measIndex += 1;
                updateUI();
            }
            else if (v.equals(upload)) {
                Log.d(TAG, "UPLOAD DATA");
                if (sensorValue != null)
                    dbHandler.insertMeasData(partSerialID, String.valueOf(measID.get(measIndex).getMeasID()), Double.valueOf(sensorValue));
                else
                    Log.e(TAG, "NO data");
            }
            else if (v.equals(reset)) {
                FragmentFactory.inMeasure2 = false;
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Fragment fragment = FragmentFactory.getInstanceByIndex(R.id.radioButton2);
                transaction.replace(R.id.content, fragment);
                transaction.commit();
            }
        }
    };
}
