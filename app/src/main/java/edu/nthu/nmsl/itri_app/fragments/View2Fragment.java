package edu.nthu.nmsl.itri_app.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import edu.nthu.nmsl.itri_app.DatabaseHandler;
import edu.nthu.nmsl.itri_app.R;

/**
 * Created by YingYi on 2016/9/23.
 */

public class View2Fragment  extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_page, null);

        return view;
    }
}
