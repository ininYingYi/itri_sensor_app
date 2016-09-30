package edu.nthu.nmsl.itri_app.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

import edu.nthu.nmsl.itri_app.DatabaseHandler;
import edu.nthu.nmsl.itri_app.FragmentActivity;
import edu.nthu.nmsl.itri_app.FragmentFactory;
import edu.nthu.nmsl.itri_app.MainActivity;
import edu.nthu.nmsl.itri_app.R;

/**
 * Created by YingYi on 2016/9/22.
 */
public class MeasureFragment extends Fragment {
    private static final String TAG = "MeasureFragment";
    private Spinner selectPartSpinner, selectPartSerialSpinner, selectWorkSpinner;
    private Button confirm;
    private DatabaseHandler dbHandler;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("MeasureFragment", "onCreateView");
        View view = inflater.inflate(R.layout.measure_page, null);
        selectPartSpinner = (Spinner) view.findViewById(R.id.select_part);
        selectPartSerialSpinner = (Spinner) view.findViewById(R.id.select_work);
        selectWorkSpinner = (Spinner) view.findViewById(R.id.select_process);
        confirm = (Button) view.findViewById(R.id.button);
        confirm.setOnClickListener(clickListener);
        dbHandler = new DatabaseHandler(UIHandler);
        dbHandler.requestPartId();
        return view;
    }

    public Handler UIHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getActivity() == null) return;
            switch (msg.what){
                case DatabaseHandler.statePartId:
                    //Log.d(TAG,"Receive:"+msg.obj.toString());
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, (ArrayList<String>)msg.obj);
                    selectPartSpinner.setAdapter(adapter);
                    selectPartSpinner.setOnItemSelectedListener(adapterListener);
                    break;
                case DatabaseHandler.statePartSerialId:
                    ArrayAdapter<String> partSerialAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, (ArrayList<String>)msg.obj);
                    selectPartSerialSpinner.setAdapter(partSerialAdapter);
                    selectPartSerialSpinner.setOnItemSelectedListener(adapterListener);
                    break;
                case DatabaseHandler.stateWorkId:
                    Log.d(TAG,"Receive:"+msg.obj.toString());
                    ArrayAdapter<String> workAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, (ArrayList<String>)msg.obj);
                    selectWorkSpinner.setAdapter(workAdapter);
                    selectWorkSpinner.setOnItemSelectedListener(adapterListener);
                    break;
                case DatabaseHandler.stateMeasId:
                    Log.d(TAG,"Receive:"+msg.obj.toString());
                    break;
                default:
                    Log.d(TAG,"Error");
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MeasureFragment", "onResume");

    }
    private String partID, partSerialID, workID;
    AdapterView.OnItemSelectedListener adapterListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.equals(selectPartSpinner)) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                partID = (String)parent.getItemAtPosition(position);
                dbHandler.requestPartSerialId(partID);
            }
            else if (parent.equals(selectPartSerialSpinner)) {
                partSerialID = (String)parent.getItemAtPosition(position);
                dbHandler.requestWorkId(partID);
            }
            else if (parent.equals(selectWorkSpinner)) {
                workID = (String)parent.getItemAtPosition(position);
            }

            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(100);
                        }
                        catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }
    };

    Button.OnClickListener clickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if ( v.equals(confirm)) {

                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Bundle data = new Bundle();
                data.putString("partID", partID);
                data.putString("partSerialID", partSerialID);
                data.putString("workID", workID);
                Fragment fragment = FragmentFactory.getInstanceByIndex(R.id.button);
                fragment.setArguments(data);
                transaction.replace(R.id.content, fragment);
                transaction.commit();

            }
        }
    };
    @Override
    public void onPause() {
        super.onPause();

    }
}