package edu.nthu.nmsl.itri_app;

/**
 * Created by mao on 2016/9/22.
 */

public class MeasData {

    private String workId;
    private int measId;
    private String value;
    private String normalSize;
    private String status;
    private String toleranceU;
    private String toleranceL;
    private String finalMeas;
    private String isKeyMeas;
    private String imageURL;

    public MeasData(String workId, int measId, String value, String normalSize, String status, String toleranceU, String toleranceL, String finalMeas, String isKeyMeas) {
        this.workId = workId;
        this.measId = measId;
        this.value = value;
        this.normalSize = normalSize;
        this.status = status;
        this.toleranceU = toleranceU;
        this.toleranceL = toleranceL;
        this.finalMeas = finalMeas;
        this.isKeyMeas = isKeyMeas;
    }

    public MeasData(int measId, String imageURL) {
        this.measId = measId;
        this.imageURL = imageURL;
    }

    public int getMeasID() {
        return measId;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getWorkId() { return workId;}
    public String getNormalSize() { return normalSize;}
    public String getStatus() { return status;}
    public String getToleranceU() { return toleranceU;}
    public String getToleranceL() { return toleranceL;}
    public String getFinalMeas() { return finalMeas;}
    public String getIsKeyMeas() { return isKeyMeas;}
    public String getValue() { return value;}
    }
