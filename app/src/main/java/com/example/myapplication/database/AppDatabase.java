package com.example.myapplication.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.myapplication.model.QuizResult;

@Database(entities = {QuizResult.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuizResultDAO QuizResultDAO();
}

