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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_page, null);
        selectPartSpinner = (Spinner) view.findViewById(R.id.view_select_part);
        selectPartSerialSpinner = (Spinner) view.findViewById(R.id.view_select_work);
        selectWorkSpinner = (Spinner) view.findViewById(R.id.view_select_process);
        confirm = (Button) view.findViewById(R.id.view_button);
        confirm.setOnClickListener(clickListener);
        listView = (ListView) view.findViewById(R.id.view_data);

        dbHandler = new DatabaseHandler(UIHandler);
        dbHandler.requestPartId();
        return view;
    }

    public Handler UIHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
                case DatabaseHandler.stateGetAllMeasData:
                    Log.d(TAG,"Receive:"+msg.obj.toString());
                    ArrayList<MeasData> list = (ArrayList<MeasData>)msg.obj;
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
                dbHandler.requestMeasData(partID, partSerialID, workID);
            }
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
                FragmentManager fragmentManager = getFragmentManager();;
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Bundle data = new Bundle();
                data.putString("partID", partID);
                data.putString("partSerialID", partSerialID);
                data.putString("workID", workID);
                Fragment fragment = new View2Fragment();
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