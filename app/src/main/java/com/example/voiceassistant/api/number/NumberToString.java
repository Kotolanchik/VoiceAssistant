package com.example.voiceassistant.api.number;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NumberToString {
    public static void getNumberForForecast(int number, final Consumer<String> callback) {
        NumberApi api = NumberService.getApi();
        Call<Number> call = api.getCurrentNumber(String.valueOf(Math.abs(number)));

        call.enqueue(new Callback<Number>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<Number> call, Response<Number> response) {
                Number result = response.body();
                if (result != null && result.str != null) {
                    if (number > 0) {
                        callback.accept(result.str.substring(0, result.str.indexOf("руб")));
                    } else {
                        callback.accept("минус " + result.str.substring(0, result.str.indexOf("руб")));
                    }
                } else {
                    callback.accept("Не могу получить из числа строку");
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFailure(Call<Number> call, Throwable t) {
                Log.v("ERROR_NUMBER", t.getMessage());
            }
        });
    }
}
