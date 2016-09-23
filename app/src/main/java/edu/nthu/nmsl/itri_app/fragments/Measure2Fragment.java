package edu.nthu.nmsl.itri_app.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import edu.nthu.nmsl.itri_app.Background;
import edu.nthu.nmsl.itri_app.DatabaseHandler;
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
    private TextView measIDText, valueText;
    private Button left, right, upload;
    private ImageView image;
    private int measIndex = 0, measNumber = 0;
    private Timer timer = new Timer();
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
        valueText = (TextView) view.findViewById(R.id.textView3);
        dbHandler = new DatabaseHandler(UIHandler);
        timer.schedule(updateValue, 0, 10);

        return view;
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
                case DatabaseHandler.imageTask:
                    if (msg.obj != null) {
                        image.setImageBitmap((Bitmap) msg.obj);
                    }
                    break;
                default:
                    Log.d(TAG,"Error");
                    break;
            }
        }
    };

    public void updateUI() {
        MeasData meas = measID.get(measIndex);
        measIDText.setText(String.valueOf(meas.getMeasID()));
        dbHandler.imageTask(meas.getImageURL());
    }
    private Handler mHandler = new Handler();
    private TimerTask updateValue = new TimerTask() {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                public void run()
                {
                    if (Background.getInstance().getValue() != null)
                        valueText.setText(Background.getInstance().getValue());
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
            }
            else if (v.equals(left)) {
                if (measIndex < measNumber) measIndex += 1;
            }
            else if (v.equals(upload)) {
                Log.d(TAG, "UPLOAD DATA");
                dbHandler.insertMeasData(partSerialID, String.valueOf(measID.get(measIndex).getMeasID()), 0000);
            }

        }
    };
}
