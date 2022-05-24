package com.example.voiceassistant;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AI {
    private Map<String, String> question_answer;
    private Calendar calendar;
    private String localisation;

    public AI() {
        localisation = Locale.getDefault().getLanguage();
        calendar = new GregorianCalendar();
        this.question_answer = initMap();
    }

    public String getAnswer(String question) {
        if (question_answer.containsKey(question)) {
            return question_answer.get(question);
        }

        return "Я ещё не придумал, что ответить";
    }

    private Map<String, String> initMap() {
        Map<String, String> question_answer = new HashMap<String, String>();
        if (localisation == "ru") {
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

        if (localisation == "en") {

        }

        return question_answer;
    }

    private String getNowDayOfTheMonth() {
        String nowMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

        return "Сегодня "
                + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
                + " "
                + nowMonth.substring(0, 1).toUpperCase()
                + nowMonth.substring(1);
    }

    private String getNowHour() {
        return "Время " + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + " часов";
    }

    private String getNowDayOfTheWeek() {
        return "Сейчас " + String.valueOf(
                calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        );
    }

    private String getNumberOfDaysUntilSummer() {
        Calendar summer = new GregorianCalendar(2022, 5, 1, 0, 0, 0);
        return "До лета осталось " + String.valueOf((summer.getTimeInMillis() - calendar.getTimeInMillis()) / (1000 * 60 * 60 * 24)) + " дней";
    }
}
