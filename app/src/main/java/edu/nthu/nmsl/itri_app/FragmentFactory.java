package edu.nthu.nmsl.itri_app;

import android.app.Fragment;

import edu.nthu.nmsl.itri_app.fragments.IndexFragment;
import edu.nthu.nmsl.itri_app.fragments.Measure2Fragment;
import edu.nthu.nmsl.itri_app.fragments.MeasureFragment;
import edu.nthu.nmsl.itri_app.fragments.SettingFragment;
import edu.nthu.nmsl.itri_app.fragments.ViewFragment;

/**
 * Created by InIn on 2016/9/19.
 */
public class FragmentFactory {
    public static IndexFragment indexFragment = null;
    public static MeasureFragment measureFragment = null;
    public static Measure2Fragment measure2Fragment = null;
    public static ViewFragment viewFragment = null;
    public static SettingFragment settingFragment = null;
    public static boolean inMeasure2 = false;

    public static Fragment getInstanceByIndex(int index){
        Fragment fragment = null;
        switch (index){
            case R.id.radioButton1:
                if (indexFragment==null) {
                    indexFragment = new IndexFragment();
                }
                fragment = indexFragment;
                break;
            case R.id.radioButton2:
                if (inMeasure2) {
                    if (measure2Fragment==null) {
                        measure2Fragment = new Measure2Fragment();
                    }
                    fragment = measure2Fragment;
                }
                else {
                    if (measureFragment == null) {
                        measureFragment = new MeasureFragment();
                    }
                    fragment = measureFragment;
                }
                break;
            case R.id.radioButton3:
                if (viewFragment==null) {
                    viewFragment = new ViewFragment();
                }
                fragment = viewFragment;
                break;
            case R.id.radioButton4:
                if (settingFragment==null) {
                    settingFragment = new SettingFragment();
                }
                fragment = settingFragment;
                break;
            case R.id.button:
                if (measure2Fragment==null) {
                    measure2Fragment = new Measure2Fragment();
                }
                inMeasure2 = true;
                fragment = measure2Fragment;
                break;
        }
        return fragment;
    }
}
