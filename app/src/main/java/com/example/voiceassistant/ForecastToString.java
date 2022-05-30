package com.example.voiceassistant;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastToString {
    public static void getForecast(String city, final Consumer<String> callback) {
        ForecastApi api = ForecastService.getApi();
        Call<Forecast> call = api.getCurrentWeather(city);

        call.enqueue(new Callback<Forecast>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                Forecast result = response.body();
                if (result != null && result.current != null && result.current.temperature != null) {
                    String answer = "Сейчас где-то "
                            + result.current.temperature
                            + " градуса "
                            + " и "
                            + result.current.weather_descriptions.get(0);
                    callback.accept(answer);
                } else {
                    callback.accept("Не могу узнать погоду");
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFailure(Call<Forecast> call, Throwable t) {
                Log.v("WEATHER", t.getMessage());
            }
        });
    }
}
