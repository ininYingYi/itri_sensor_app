package edu.nthu.nmsl.itri_app.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import edu.nthu.nmsl.itri_app.DatabaseHandler;
import edu.nthu.nmsl.itri_app.MeasData;
import edu.nthu.nmsl.itri_app.MeasDataAdapter;
import edu.nthu.nmsl.itri_app.PartData;
import edu.nthu.nmsl.itri_app.PartDataAdapter;
import edu.nthu.nmsl.itri_app.R;

/**
 * Created by YingYi on 2016/9/22.
 */
public class ViewFragment extends Fragment {
    private static final String TAG = "ViewFragment";
    private Spinner selectPartSpinner, selectPartSerialSpinner, selectWorkSpinner;
    private Button confirm;
    private DatabaseHandler dbHandler;
    private ListView listView;

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
        View view = inflater.inflate(R.layout.view_page, null);
        selectPartSpinner = (Spinner) view.findViewById(R.id.view_select_part);
        selectPartSerialSpinner = (Spinner) view.findViewById(R.id.view_select_work);
        selectWorkSpinner = (Spinner) view.findViewById(R.id.view_select_process);
        listView = (ListView) view.findViewById(R.id.view_data);

        dbHandler = new DatabaseHandler(UIHandler);
        adapterListener = new AdapterListener();

        return view;
    }

    public Handler UIHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getActivity() == null ) return;
            switch (msg.what){
                case DatabaseHandler.statePartId:
                    //Log.d(TAG,"Receive:"+msg.obj.toString());
                    mPartDatas = (ArrayList<PartData>)msg.obj;
                    PartDataAdapter adapter = new PartDataAdapter(getActivity(), mPartDatas);
                    selectPartSpinner.setAdapter(adapter);
                    selectPartSpinner.setOnItemSelectedListener(adapterListener);
                    break;
                case DatabaseHandler.statePartSerialId:
                    mPartSerialIds = (ArrayList<String>)msg.obj;
                    ArrayAdapter<String> partSerialAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mPartSerialIds);
                    selectPartSerialSpinner.setAdapter(partSerialAdapter);
                    selectPartSerialSpinner.setOnItemSelectedListener(adapterListener);
                    break;
                case DatabaseHandler.stateWorkId:
                    Log.d(TAG,"Receive:"+msg.obj.toString());
                    mWorkIds = (ArrayList<String>)msg.obj;
                    ArrayAdapter<String> workAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mWorkIds);
                    selectWorkSpinner.setAdapter(workAdapter);
                    selectWorkSpinner.setOnItemSelectedListener(adapterListener);
                    break;
                case DatabaseHandler.stateGetAllMeasData:
                    firstSetPart = false;
                    firstSetPartSerial = false;
                    firstSetWork = false;
                    Log.d(TAG,"Receive:"+msg.obj.toString());
                    ArrayList<MeasData> list = (ArrayList<MeasData>)msg.obj;
                    listView.setAdapter(null);
                    MeasDataAdapter listAdapter = new MeasDataAdapter(getActivity(), list);
                    listView.setAdapter(listAdapter);
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
        firstSetPart = true;
        firstSetPartSerial = true;
        firstSetWork = true;
    }

    private String partID, partSerialID, workID;
    private boolean firstSetPart = true;
    private boolean firstSetPartSerial = true;
    private boolean firstSetWork = true;
    public class AdapterListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
            if (parent.equals(selectPartSpinner)) {
                if (firstSetPart) {
                    //firstSetPart = false;
                    return;
                }
                Log.d(TAG, "selectPartSpinner");
                selectedPartDate = position;
                PartData part = (PartData) parent.getItemAtPosition(position);
                partID = part.getPartId();
                selectPartSerialSpinner.setAdapter(null);
                selectWorkSpinner.setAdapter(null);
                dbHandler.requestPartSerialId(partID);

            } else if (parent.equals(selectPartSerialSpinner)) {
                if (firstSetPartSerial) {
                    //firstSetPartSerial = false;
                    return;
                }
                Log.d(TAG, "selectPartSerialSpinner");
                selectedPartSerialId = position;
                partSerialID = (String) parent.getItemAtPosition(position);
                selectWorkSpinner.setAdapter(null);
                dbHandler.requestWorkId(partID);
            } else if (parent.equals(selectWorkSpinner)) {
                if (firstSetWork) {
                    //firstSetWork = false;
                    return;
                }
                Log.d(TAG, "selectWorkSpinner");
                selectedWorkId = position;
                workID = (String) parent.getItemAtPosition(position);
                dbHandler.requestMeasData(partID, partSerialID, workID);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        if(savedInstanceState!=null) {
            firstSetPart = true;
            firstSetPartSerial = true;
            firstSetWork = true;
            Log.d(TAG,"savedInstanceState");
            this.mPartDatas = savedInstanceState.getParcelableArrayList(this.saveParts);
            if(this.mPartDatas.size() > 0) {
                PartDataAdapter adapter = new PartDataAdapter(getActivity(), this.mPartDatas);
                selectPartSpinner.setAdapter(adapter);
                selectPartSpinner.setOnItemSelectedListener(adapterListener);
                this.selectedPartDate = savedInstanceState.getInt(this.saveSelectedPart, 0);
                if (this.selectedPartDate != 0) selectPartSpinner.setSelection(this.selectedPartDate);
                this.partID = this.mPartDatas.get(this.selectedPartDate).getPartId();
            }


            this.mPartSerialIds = savedInstanceState.getStringArrayList(this.savePartSerialIds);
            if(this.mPartSerialIds.size() > 0) {
                ArrayAdapter<String> partSerialAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, this.mPartSerialIds);
                selectPartSerialSpinner.setAdapter(partSerialAdapter);
                selectPartSerialSpinner.setOnItemSelectedListener(adapterListener);
                this.selectedPartSerialId = savedInstanceState.getInt(this.saveSelectedPartSerial, 0);
                if (this.selectedPartSerialId != 0) selectPartSerialSpinner.setSelection(this.selectedPartSerialId);
                this.partSerialID = this.mPartSerialIds.get(this.selectedPartSerialId);
            }


            this.mWorkIds = savedInstanceState.getStringArrayList(this.saveWorkIds);
            if(this.mWorkIds.size() > 0) {
                ArrayAdapter<String> workAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, this.mWorkIds);
                selectWorkSpinner.setAdapter(workAdapter);
                selectWorkSpinner.setOnItemSelectedListener(adapterListener);
                this.selectedWorkId = savedInstanceState.getInt(this.saveSelevtedWorkId, 0);
                if (this.selectedWorkId != 0) selectWorkSpinner.setSelection(this.selectedWorkId);
                this.workID = this.mWorkIds.get(this.selectedWorkId);
                dbHandler.requestMeasData(this.partID, this.partSerialID, this.workID);
            }
        }else {
            Log.d(TAG,"requestPartId");
            dbHandler.requestPartId();
            firstSetPart = false;
            firstSetPartSerial = false;
            firstSetWork = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}