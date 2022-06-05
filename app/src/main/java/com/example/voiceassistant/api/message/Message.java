package com.example.voiceassistant.api.message;

import java.text.ParseException;
import java.util.Date;

public class Message {
    public String text;
    public Date date;
    public Boolean isSend;

    public Message(String text, Boolean isSend) {
        this.text = text;
        this.isSend = isSend;
        this.date = new Date();
    }

    public Message(MessageEntity entity) throws ParseException {
        this.text = entity.text;
        this.date = new Date(entity.date);
        this.isSend = entity.isSend == 1;
    }
}
