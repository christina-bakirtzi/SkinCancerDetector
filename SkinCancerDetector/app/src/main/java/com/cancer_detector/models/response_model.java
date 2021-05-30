package com.cancer_detector.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class response_model {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("predictions")
    @Expose
    public List<Prediction> predictions = null;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }

}

