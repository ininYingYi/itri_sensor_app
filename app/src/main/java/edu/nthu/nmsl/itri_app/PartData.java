package edu.nthu.nmsl.itri_app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mao on 2016/10/4.
 */

public class PartData implements Parcelable {
    private String partId;
    private String partName;

    private PartData(Parcel in) {
        partId = in.readString();
        partName = in.readString();
    }


    public PartData(String partId, String partName) {
        this.partId = partId;
        this.partName = partName;
    }

    public String getPartId() {
        return partId;
    }

    public String getPartName() {
        return partName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(partId);
        parcel.writeString(partName);
    }

    public static final Parcelable.Creator<PartData> CREATOR = new Parcelable.Creator<PartData>() {
        @Override
        public PartData createFromParcel(Parcel in) {
            return new PartData(in);
        }

        @Override
        public PartData[] newArray(int size) {
            return new PartData[size];
        }
    };
}
