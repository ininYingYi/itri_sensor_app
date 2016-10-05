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
import android.view.MotionEvent;
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
import edu.nthu.nmsl.itri_app.PartData;
import edu.nthu.nmsl.itri_app.PartDataAdapter;
import edu.nthu.nmsl.itri_app.R;

/**
 * Created by YingYi on 2016/9/22.
 */
public class MeasureFragment extends Fragment {
    private static final String TAG = "MeasureFragment";
    private Spinner selectPartSpinner, selectPartSerialSpinner, selectWorkSpinner;
    private Button confirm;
    private DatabaseHandler dbHandler;

    //define save
    private final String saveParts = "saveParts";
    private final String savePartSerialIds = "savePartSerialIds";
    private final String saveWorkIds = "saveWorkIds";
    private final String saveSelectedPart = "saveSelectedPart";
    private final String saveSelectedPartSerial = "saveSelectedPartSerial";
    private final String saveSelevtedWorkId = "saveSelevtedWorkId";

    //Store value
    private ArrayList<PartData> mPartDatas;
    private ArrayList<String> mWorkIds;
    private ArrayList<String> mPartSerialIds;
    private int selectedPartDate = 0;
    private int selectedWorkId = 0;
    private int selectedPartSerialId = 0;
    private AdapterListener adapterListener;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("MeasureFragment", "onCreateView");
        View view = inflater.inflate(R.layout.measure_page, null);
        selectPartSpinner = (Spinner) view.findViewById(R.id.select_part);
        selectPartSerialSpinner = (Spinner) view.findViewById(R.id.select_work);
        selectWorkSpinner = (Spinner) view.findViewById(R.id.select_process);
        confirm = (Button) view.findViewById(R.id.button);
        confirm.setOnClickListener(clickListener);
        dbHandler = new DatabaseHandler(UIHandler);
        adapterListener = new AdapterListener();
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
                    mPartDatas = (ArrayList<PartData>)msg.obj;
                    PartDataAdapter adapter = new PartDataAdapter(getActivity(), mPartDatas);
                    selectPartSpinner.setAdapter(adapter);
                    selectPartSpinner.setOnItemSelectedListener(adapterListener);
                    selectPartSpinner.setOnTouchListener(adapterListener);
                    break;
                case DatabaseHandler.statePartSerialId:
                    mPartSerialIds = (ArrayList<String>)msg.obj;
                    ArrayAdapter<String> partSerialAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mPartSerialIds);
                    selectPartSerialSpinner.setAdapter(partSerialAdapter);
                    selectPartSerialSpinner.setOnItemSelectedListener(adapterListener);
                    selectPartSerialSpinner.setOnTouchListener(adapterListener);
                    break;
                case DatabaseHandler.stateWorkId:
                    Log.d(TAG,"Receive:"+msg.obj.toString());
                    mWorkIds = (ArrayList<String>)msg.obj;
                    ArrayAdapter<String> workAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mWorkIds);
                    selectWorkSpinner.setAdapter(workAdapter);
                    selectWorkSpinner.setOnItemSelectedListener(adapterListener);
                    selectWorkSpinner.setOnTouchListener(adapterListener);
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

    public class AdapterListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

        boolean userSelect = false;
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "onItemSelected");
            if (userSelect) {
                if (parent.equals(selectPartSpinner)) {
                    selectedPartDate = position;
                    PartData part = (PartData) parent.getItemAtPosition(position);
                    partID = part.getPartId();
                    selectPartSerialSpinner.setAdapter(null);
                    selectWorkSpinner.setAdapter(null);
                    dbHandler.requestPartSerialId(partID);
                } else if (parent.equals(selectPartSerialSpinner)) {
                    selectedPartSerialId = position;
                    partSerialID = (String) parent.getItemAtPosition(position);
                    selectWorkSpinner.setAdapter(null);
                    dbHandler.requestWorkId(partID);
                } else if (parent.equals(selectWorkSpinner)) {
                    selectedWorkId = position;
                    workID = (String) parent.getItemAtPosition(position);
                }
                userSelect = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onNothingSelected");
            userSelect = false;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.d(TAG, "onTouch");
            userSelect = true;
            return false;
        }
    }


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
                transaction.add(R.id.content, fragment);
                transaction.commit();
            }
        }
    };
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");
        outState.putParcelableArrayList(this.saveParts,this.mPartDatas);
        outState.putStringArrayList(this.savePartSerialIds,this.mPartSerialIds);
        outState.putStringArrayList(this.saveWorkIds,this.mWorkIds);
        outState.putInt(this.saveSelectedPart,this.selectedPartDate);
        outState.putInt(this.saveSelectedPartSerial,this.selectedPartSerialId);
        outState.putInt(this.saveSelevtedWorkId,this.selectedWorkId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"onActivityCreated");
        if(savedInstanceState!=null) {
            Log.d(TAG,"savedInstanceState");
            this.mPartDatas = savedInstanceState.getParcelableArrayList(this.saveParts);
            if(this.mPartDatas.size() > 0) {
                PartDataAdapter adapter = new PartDataAdapter(getActivity(), this.mPartDatas);
                selectPartSpinner.setAdapter(adapter);
                selectPartSpinner.setOnItemSelectedListener(adapterListener);
                selectPartSpinner.setOnTouchListener(adapterListener);
                this.selectedPartDate = savedInstanceState.getInt(this.saveSelectedPart, 0);
                if (this.selectedPartDate != 0) selectPartSpinner.setSelection(this.selectedPartDate);
            }


            this.mPartSerialIds = savedInstanceState.getStringArrayList(this.savePartSerialIds);
            if(this.mPartSerialIds.size() > 0) {
                ArrayAdapter<String> partSerialAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, this.mPartSerialIds);
                selectPartSerialSpinner.setAdapter(partSerialAdapter);
                selectPartSerialSpinner.setOnItemSelectedListener(adapterListener);
                selectPartSerialSpinner.setOnTouchListener(adapterListener);
                this.selectedPartSerialId = savedInstanceState.getInt(this.saveSelectedPartSerial, 0);
                if (this.selectedPartSerialId != 0) selectPartSerialSpinner.setSelection(this.selectedPartSerialId);
            }


            this.mWorkIds = savedInstanceState.getStringArrayList(this.saveWorkIds);

            if(this.mWorkIds.size() > 0) {
                ArrayAdapter<String> workAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, this.mWorkIds);
                selectWorkSpinner.setAdapter(workAdapter);
                selectWorkSpinner.setOnItemSelectedListener(adapterListener);
                selectWorkSpinner.setOnTouchListener(adapterListener);
                this.selectedWorkId = savedInstanceState.getInt(this.saveSelevtedWorkId, 0);
                if (this.selectedWorkId != 0) selectWorkSpinner.setSelection(this.selectedWorkId);
            }


        }else {
            Log.d(TAG,"requestPartId");
            dbHandler.requestPartId();
        }
    }


}