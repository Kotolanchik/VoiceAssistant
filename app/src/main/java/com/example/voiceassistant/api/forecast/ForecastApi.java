package com.example.voiceassistant.api.forecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface ForecastApi {
    @GET("/current?access_key=7e3d0e5a6a4e1033ebaa8d8ab266d92c")
    Call<Forecast> getCurrentWeather(@Query("query") String city);
}
