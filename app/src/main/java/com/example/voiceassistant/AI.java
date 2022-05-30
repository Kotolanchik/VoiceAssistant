package com.example.voiceassistant;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AI {
    private final Map<String, String> question_answer;
    private final Calendar calendar;
    private final String localisation;

    public AI() {
        localisation = Locale.getDefault().getLanguage();
        calendar = new GregorianCalendar();
        this.question_answer = initMap();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getAnswer(String question, final Consumer<String> callback) {
        Pattern cityPattern = Pattern.compile("погода в городе (\\p{L}+)", Pattern.CASE_INSENSITIVE);
        Matcher cityMatcher = cityPattern.matcher(question);

        if (question_answer.containsKey(question)) {
            callback.accept(question_answer.get(question));

        } else if (cityMatcher.find()) {
            String cityName = cityMatcher.group(1);
            ForecastToString.getForecast(cityName, weatherString -> {
                if (weatherString.equals("Не могу узнать погоду")) {
                    callback.accept("Не знаю я, какая там погода у вас в городе " + cityName);
                } else {
                    callback.accept(weatherString);
                }
            });

        } else if (question.contains("праздник")) {
            Observable.fromCallable(() -> ParsingHtmlService.getHoliday(question.split("праздник")[1].trim()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(callback::accept);
        } else {
            callback.accept("Я ещё не придумал, что ответить");
        }
    }

    private Map<String, String> initMap() {
        Map<String, String> question_answer = new HashMap<>();
        if (localisation.equals("ru")) {
            question_answer.put("привет", "Привет");
            question_answer.put("приветик", "Здравствуйте");
            question_answer.put("чем занимаешься", "Отвечаю на вопросы");
            question_answer.put("а чем занимаешься", "Отвечаю на вопросы");
            question_answer.put("что делаешь", "Отвечаю на глупые вопросы");
            question_answer.put("а что делаешь", "Отвечаю на глупые вопросы");
            question_answer.put("как дела", "Неплохо");
            question_answer.put("какой сегодня день", getNowDayOfTheMonth());
            question_answer.put("который час", getNowHour());
            question_answer.put("какой день недели", getNowDayOfTheWeek());
            question_answer.put("сколько дней осталось до лета", getNumberOfDaysUntilSummer());
        }

        if (localisation.equals("en")) {

        }

        return question_answer;
    }

    private String getNowDayOfTheMonth() {
        String nowMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

        return "Сегодня "
                + calendar.get(Calendar.DAY_OF_MONTH)
                + " "
                + nowMonth.substring(0, 1).toUpperCase()
                + nowMonth.substring(1);
    }

    private String getNowHour() {
        return "Время " + calendar.get(Calendar.HOUR_OF_DAY) + " часов";
    }

    private String getNowDayOfTheWeek() {
        return "Сейчас " + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }

    private String getNumberOfDaysUntilSummer() {
        Calendar summer = new GregorianCalendar(2022, 5, 1, 0, 0, 0);
        return "До лета осталось " + (summer.getTimeInMillis() - calendar.getTimeInMillis()) / (1000 * 60 * 60 * 24) + " дней";
    }
}
