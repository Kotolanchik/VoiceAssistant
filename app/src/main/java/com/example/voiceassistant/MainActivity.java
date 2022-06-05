package com.example.voiceassistant;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voiceassistant.AI.AI;
import com.example.voiceassistant.DB.DBHelper;
import com.example.voiceassistant.api.message.Message;
import com.example.voiceassistant.api.message.MessageEntity;
import com.example.voiceassistant.api.message.MessageListAdapter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    protected Button sendButton;
    protected EditText questionText;
    protected RecyclerView chatMessageList;

    protected MessageListAdapter messageListAdapter;
    protected AI ai = new AI();
    protected TextToSpeech textToSpeech;

    SharedPreferences sPref;
    public static final String APP_PREFERENCES = "mysettings";
    private boolean isLight = true;
    private String THEME = "THEME";

    DBHelper dBHelper;
    SQLiteDatabase database;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sPref = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        isLight = sPref.getBoolean(THEME, true);
        if (!isLight) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        dBHelper = new DBHelper(this);
        database = dBHelper.getWritableDatabase();
        messageListAdapter = new MessageListAdapter();
        cursor = database.query(dBHelper.TABLE_MESSAGES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int messageIndex = cursor.getColumnIndex(dBHelper.FIELD_MESSAGE);
            int dateIndex = cursor.getColumnIndex(dBHelper.FIELD_DATE);
            int sendIndex = cursor.getColumnIndex(dBHelper.FIELD_SEND);

            do {
                MessageEntity entity = new MessageEntity(cursor.getString(messageIndex),
                        cursor.getString(dateIndex), cursor.getInt(sendIndex));

                Message message = null;
                try {
                    message = new Message(entity);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                messageListAdapter.messageList.add(message);
            } while (cursor.moveToNext());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatMessageList = findViewById(R.id.chatMessageList);
        sendButton = findViewById(R.id.sendButton);
        questionText = findViewById(R.id.questionField);

        chatMessageList.setLayoutManager(new LinearLayoutManager(this));
        chatMessageList.setAdapter(messageListAdapter);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("ru"));
                    textToSpeech.speak("С возвращением, кожаный", TextToSpeech.QUEUE_ADD, null);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    onSend();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    protected void onSend() throws IOException {
        String question = questionText.getText().toString();
        ai.getAnswer(question.toLowerCase(), new Consumer<String>() {
            @Override
            public void accept(String answer) {
                messageListAdapter.messageList.add(new Message(question, true));
                messageListAdapter.messageList.add(new Message(answer, false));
                messageListAdapter.notifyDataSetChanged();

                chatMessageList.scrollToPosition(messageListAdapter.messageList.size() - 1);
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        questionText.setText("");
    }

    /**
     * Запускаем меню - "надуваем".
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Оператор выбора темы.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.day_settings:
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLight = true;
                onStop();
                break;

            case R.id.night_settings:
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLight = false;
                onStop();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Сохраняем значение темы.
     */
    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(THEME, isLight);
        editor.apply();

        database.delete(dBHelper.TABLE_MESSAGES, null, null);
        for (Message message : messageListAdapter.messageList) {
            MessageEntity messageEntity = new MessageEntity(message);

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.FIELD_MESSAGE, messageEntity.text);
            contentValues.put(DBHelper.FIELD_DATE, messageEntity.date);
            contentValues.put(DBHelper.FIELD_SEND, messageEntity.isSend);

            database.insert(dBHelper.TABLE_MESSAGES, null, contentValues);
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        cursor.close();
        super.onDestroy();
    }
}