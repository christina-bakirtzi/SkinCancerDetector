package com.cancer_detector.models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Prediction {
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("probability")
    @Expose
    private Double probability;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

}