package edu.nthu.nmsl.itri_app.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.nthu.nmsl.itri_app.R;

/**
 * Created by InIn on 2016/9/19.
 */
public class SettingFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_page, null);
        TextView textView = (TextView) view.findViewById(R.id.setting_content);
        textView.setText("setup here");
        return view;
    }
}