package com.example.voiceassistant.api.number;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface NumberApi {
    @GET("/json/convert/num2str")
    Call<Number> getCurrentNumber(@Query("num") String number);
}
