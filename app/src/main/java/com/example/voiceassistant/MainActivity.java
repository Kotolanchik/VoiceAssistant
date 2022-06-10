package com.example.voiceassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voiceassistant.AI.AI;
import com.example.voiceassistant.DB.DBHelper;
import com.example.voiceassistant.api.message.Message;
import com.example.voiceassistant.api.message.MessageEntity;
import com.example.voiceassistant.api.message.MessageListAdapter;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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

    private static final String TAG = "MainActivity";
    int LOCATION_REQUEST_CODE = 10001;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                Log.d(TAG, "Longitude: " + location.getLongitude());
                Log.d(TAG, "Latitude: " + location.getLatitude());

                ai.setLongitude(String.valueOf(location.getLongitude()));
                ai.setLatitude(String.valueOf(location.getLatitude()));
            }
        }
    };

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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        chatMessageList = findViewById(R.id.chatMessageList);
        sendButton = findViewById(R.id.sendButton);
        questionText = findViewById(R.id.questionField);

        chatMessageList.setLayoutManager(new LinearLayoutManager(this));
        chatMessageList.setAdapter(messageListAdapter);
        chatMessageList.scrollToPosition(messageListAdapter.getItemCount() - 1);

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
        if (question.equals("")) {
            return;
        }

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
     * Сохраняем значение темы, сообщения. Останавливаем обновление местоположения.
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
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        cursor.close();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            getLastLocation();
            checkSettingsAndStartLocationUpdates();
        } else {
            askLocationPermission();
        }
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });

        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(MainActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog...");
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }
}