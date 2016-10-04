package edu.nthu.nmsl.itri_app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by NMSL-YingYi on 2016/10/4.
 */

public class PartDataAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private List<PartData> datas;

    public PartDataAdapter(Context context, List<PartData> datas) {
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
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.part_layout, null);
            holder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.partDataID),
                    (TextView) convertView.findViewById(R.id.partDataName)
            );
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PartData partData = (PartData) getItem(position);
        holder.id.setText(partData.getPartId());
        holder.name.setText(" (" + partData.getPartName() + ")");

        return convertView;
    }

    private class ViewHolder {
        public TextView id, name;

        public ViewHolder(TextView workId, TextView measID) {
            this.id = workId;
            this.name = measID;
        }
    }
}