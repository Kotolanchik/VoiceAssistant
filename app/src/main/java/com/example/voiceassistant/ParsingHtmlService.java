package com.example.voiceassistant;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ParsingHtmlService {
    private static final String URL = "http://mirkosmosa.ru/holiday/2022";

    public static String getHoliday(String date)  throws IOException {
        Document document = Jsoup.connect(URL).get();
        Elements body = document.getElementsByClass("month_row");

        for (Element innerBody : body) {
            String dateHoliday = innerBody.getElementsByClass("month_cel_date").get(0).getAllElements().get(1).text();
            Elements allHolidaysToDate = innerBody.getElementsByClass("holiday_month_day_holiday").get(0).getElementsByTag("li");

            if (dateHoliday.equals(date)) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Element holiday : allHolidaysToDate) {
                    stringBuilder.append(holiday.text());
                    stringBuilder.append("\n");
                }
                return stringBuilder.toString();
            }
        }

        return "Не нашел";
    }

}
