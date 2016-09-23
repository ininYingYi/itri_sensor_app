package edu.nthu.nmsl.itri_app;

import android.content.Context;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by YingYi on 2016/9/23.
 */

public class MeasDataAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private List<MeasData> datas;

    public MeasDataAdapter(Context context, List<MeasData> datas){
        myInflater = LayoutInflater.from(context);
        this.datas = datas;
    }
    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return datas.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.meas_layout, null);
            holder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.show_workId),
                    (TextView) convertView.findViewById(R.id.show_measID),
                    (TextView) convertView.findViewById(R.id.show_normalSize),
                    (TextView) convertView.findViewById(R.id.show_toleranceU),
                    (TextView) convertView.findViewById(R.id.show_toleranceL),
                    (TextView) convertView.findViewById(R.id.show_finalMeas),
                    (TextView) convertView.findViewById(R.id.show_isKeyMeas),
                    (TextView) convertView.findViewById(R.id.show_value)
            );
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        MeasData measData = (MeasData)getItem(position);
        holder.workId.setText(measData.getWorkId());
        holder.measID.setText(String.valueOf(measData.getMeasID()));
        holder.normalSize.setText(measData.getNormalSize());
        holder.toleranceU.setText(measData.getToleranceU());
        holder.toleranceL.setText(measData.getToleranceL());
        holder.finalMeas.setText(measData.getFinalMeas());
        holder.isKeyMeas.setText(measData.getIsKeyMeas());
        holder.value.setText(measData.getValue());
        return convertView;
    }

    private class ViewHolder {
        public TextView workId, measID, normalSize, toleranceU, toleranceL, finalMeas, isKeyMeas, value;
        public ViewHolder(TextView workId, TextView measID, TextView normalSize, TextView toleranceU, TextView toleranceL, TextView finalMeas, TextView isKeyMeas, TextView value){
            this.workId = workId;
            this.measID = measID;
            this.normalSize = normalSize;
            this.toleranceU = toleranceU;
            this.toleranceL = toleranceL;
            this.finalMeas = finalMeas;
            this.isKeyMeas = isKeyMeas;
            this.value = value;
        }
    }
}
