package ru.financecompas.app.activities.quiz.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ru.financecompas.app.activities.quiz.model.QuizResult;

@Database(entities = {QuizResult.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuizResultDAO QuizResultDAO();
}

