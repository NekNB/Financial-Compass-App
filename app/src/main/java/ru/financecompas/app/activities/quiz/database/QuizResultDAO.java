package ru.financecompas.app.activities.quiz.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ru.financecompas.app.activities.quiz.model.QuizResult;

@Dao
public interface QuizResultDAO {
    @Insert
    void insert(QuizResult result);

    // последние 10 результатов
    @Query("SELECT * FROM QuizResult ORDER BY timestamp DESC LIMIT 10")
    List<QuizResult> getLast10();

    // удаление старых (если хочешь строго 10)
    @Query("DELETE FROM QuizResult WHERE id NOT IN " +
            "(SELECT id FROM QuizResult ORDER BY timestamp DESC LIMIT 10)")
    void keepOnlyLast10();
}