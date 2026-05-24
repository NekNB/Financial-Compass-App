package ru.financecompas.app.activities.quiz.database;

import android.content.Context;

import androidx.room.Room;

// Новый класс DatabaseClient.java
public class DatabaseClient {
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "quiz_database"
            ).build();
        }
        return instance;
    }
}