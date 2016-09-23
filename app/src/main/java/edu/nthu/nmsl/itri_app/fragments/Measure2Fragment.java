package edu.nthu.nmsl.itri_app.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextSwitcher;

import edu.nthu.nmsl.itri_app.R;

/**
 * Created by YingYi on 2016/9/22.
 */

public class Measure2Fragment extends Fragment {
    private TextSwitcher textSwitcher;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("MeasureFragment", "onCreateView");
        View view = inflater.inflate(R.layout.measure2_page, null);

        return view;
    }
}
