package com.example.voiceassistant;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.sendButton);
        questionText = findViewById(R.id.questionField);

        messageListAdapter = new MessageListAdapter();
        chatMessageList = findViewById(R.id.chatMessageList);
        chatMessageList.setLayoutManager(new LinearLayoutManager(this));
        chatMessageList.setAdapter(messageListAdapter);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("ru"));
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
    }
}