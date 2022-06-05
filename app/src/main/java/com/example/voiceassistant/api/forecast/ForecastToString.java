package com.example.voiceassistant.api.forecast;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.voiceassistant.api.number.NumberToString;

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

                    NumberToString.getNumberForForecast(result.current.temperature, new Consumer<String>() {
                        @Override
                        public void accept(String numberString) {
                            if (!numberString.equals("Не могу получить из числа строку")) {
                                String answer = "Сейчас где-то "
                                        + numberString
                                        + declinationDegrees(result.current.temperature)
                                        + " и "
                                        + result.current.weather_descriptions.get(0);
                                callback.accept(answer);
                            } else {
                                callback.accept("Не получилось получить температуру");

                            }
                        }
                    });

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

    private static String declinationDegrees(int numb) {
        numb = Math.abs(numb);
        if (numb % 100 >= 5 && numb % 100 <= 20) {
            return " градусов";
        }

        if (numb % 10 == 1) {
            return "градус";
        }

        if (numb % 10 == 2 || numb % 10 == 3 || numb % 10 == 4) {
            return " градуса";
        }

        return "градусов";
    }
}
