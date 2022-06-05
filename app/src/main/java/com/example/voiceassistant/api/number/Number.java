package com.example.voiceassistant.api.number;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Number implements Serializable {
    @SerializedName("str")
    @Expose
    public String str;
}
