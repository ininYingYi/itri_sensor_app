package edu.nthu.nmsl.itri_app.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
    private ArrayList<String> workIDs;
    private DatabaseHandler dbHandler;
    private ArrayList<MeasData> measID;
    private TextView measIDText, valueText, deviceName;
    private Button left, right, upload, reset;
    private ImageView image;

    //save measIndex
    private final String save_measIndex = "measIndex";

    private int measIndex = 0, measNumber = 0;


    private Timer timer = new Timer();
    private Resources res;

    private boolean isCMM_status = false;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("MeasureFragment", "onCreateView");

        //get value from instance
        Bundle data = getArguments();
        partID = data.getString("partID");
        partSerialID = data.getString("partSerialID");
        workID = data.getString("workID");
        workIDs = data.getStringArrayList("workIDs");

        //handle UI components
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

        //connect to the server
        dbHandler = new DatabaseHandler(UIHandler);


        //start a timer to get sensor's data
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
    public void onStart() {
        super.onStart();
        //ask for measId -> results will be handled by Handler
        dbHandler.requestMeasId(partID, workID);
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
                    if (isLeft) {
                        measIndex = measNumber - 1;
                    }
                    else {
                        measIndex = 0;
                    }
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
                    if (result.equals("Successfully")) {
                        Toast.makeText(getActivity(), "成功上傳", Toast.LENGTH_SHORT).show();
                        if (measIndex < measNumber - 1){
                            measIndex += 1;
                            updateUI();
                        }
                    }else
                        Toast.makeText(getActivity(), "伺服器錯誤", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.d(TAG,"Error");
                    break;
            }
        }
    };
    private boolean isLeft = false;
    public void updateUI() {
        Log.d(TAG, String.valueOf(measIndex));
        if (measIndex < measNumber) {
            MeasData meas = measID.get(measIndex);
            measIDText.setText("程序:" + workID + " 編號:" + String.valueOf(meas.getMeasID()));
            dbHandler.imageTask(meas.getImageURL());

            Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.noimage);

            image.setImageBitmap(bmp);
            image.invalidate();

            //handle CMM
            if(meas.isCMM()){
                this.upload.setEnabled(false);
                if(isAdded()) {
                    this.upload.setBackground(getResources().getDrawable(R.drawable.btn_disable));
                    Toast.makeText(getActivity(), "此為CMM量測值，三次元資料無須上傳", Toast.LENGTH_LONG).show();
                }else{
                    Log.d(TAG,"Fragment not attached to Activity");
                }
            }else{
                this.upload.setEnabled(true);
                if(isAdded()) {
                    this.upload.setBackground(getResources().getDrawable(R.drawable.btn_success));
                }
                else{
                    Log.d(TAG,"Fragment not attached to Activity");
                }
            }
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
                    deviceName.setText(sensorName);
                    if (sensorValue != null) {
                        valueText.setText(sensorValue);
                    }
                    else
                        valueText.setText("NO DATA");
                }
            });
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(this.save_measIndex,measIndex);
        outState.putString("workID", workID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null){
            this.measIndex = savedInstanceState.getInt(this.save_measIndex);
            workID = savedInstanceState.getString("workID");

        }
    }

    Button.OnClickListener clickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (v.equals(left)) {
                if (measIndex > 0) {
                    measIndex -= 1;
                    updateUI();
                }
                else {
                    int index = workIDs.indexOf(workID);
                    if ( index > 0) {
                        isLeft = true;
                        workID = (String) workIDs.get(--index);
                        dbHandler.requestMeasId(partID, workID);
                    }
                }
            }
            else if (v.equals(right)) {
                if (measIndex < measNumber - 1){
                    measIndex += 1;
                    updateUI();
                }
                else {
                    int totalWorkIDNumber = workIDs.size();
                    int index = workIDs.indexOf(workID);
                    Log.e(TAG, workID + "index: " + index);
                    if ( index < totalWorkIDNumber - 1) {
                        isLeft = false;
                        workID = (String) workIDs.get(++index);
                        dbHandler.requestMeasId(partID, workID);
                        updateUI();
                    }
                }
            }
            else if (v.equals(upload)) {
                Log.d(TAG, "UPLOAD DATA");
                if (sensorValue != null)
                    dbHandler.insertMeasData(partSerialID, String.valueOf(measID.get(measIndex).getMeasID()), Double.valueOf(sensorValue));
                else
                    Toast.makeText(getActivity(),"裝置錯誤，請重新連線",Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "NO data");
            }
            else if (v.equals(reset)) {
                FragmentFactory.inMeasure2 = false;
                measIndex = 0;
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(Measure2Fragment.this);
                transaction.commit();
            }
        }
    };
}
