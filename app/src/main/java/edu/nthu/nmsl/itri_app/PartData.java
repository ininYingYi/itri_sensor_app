package edu.nthu.nmsl.itri_app;

/**
 * Created by mao on 2016/10/4.
 */

public class PartData {
    private String partId;
    private String partName;

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
}
