package com.example.myapplication;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseClient;
import com.example.myapplication.database.QuizResultDAO;
import com.example.myapplication.model.Question;
import com.example.myapplication.model.QuizResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizActivity extends AppCompatActivity {

    AppDatabase db;
    TextView questionText, explanationText, quizSuccessRate;
    Button nextButton, restartButton, inMainButton;
    LinearLayout answersContainer;

    List<Question> questions;
    int currentQuestionIndex;

    int totalQuestions;
    int currentScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // UI элементы
        questionText = findViewById(R.id.questionText);
        explanationText = findViewById(R.id.explanationText);
        nextButton = findViewById(R.id.nextButton);
        answersContainer = findViewById(R.id.answersContainer);
        restartButton = findViewById(R.id.restartButton);
        inMainButton = findViewById(R.id.inMain);
        quizSuccessRate = findViewById(R.id.quizSuccessRate);

        // загрузка вопросов из JSON
        questions = loadQuestionsFromJson();
        Log.d("DEBUG", "Questions loaded: " + questions.size());
        if (questions.isEmpty()) {
            questionText.setText("ERROR: no questions loaded");
            return;
        }
        this.totalQuestions = questions.size();

        // первый вопрос
        currentQuestionIndex = 0;
        loadQuestion();

        // кнопка "Далее"
        nextButton.setOnClickListener(v -> {
            currentQuestionIndex++;

            if (currentQuestionIndex < questions.size()) {
                loadQuestion();
            } else {
                showFinishScreen();
            }
        });

        // кнопка "Пройти заново"
        restartButton.setOnClickListener(v -> {
            currentQuestionIndex = 0;
            restartButton.setVisibility(View.GONE);
            loadQuestion();
            this.currentScore = 0;
            updateQuizSuccessRate();
        });


        // Кнопка в Меню
        inMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(QuizActivity.this, MainActivity.class);
            startActivity(intent);
        });


    }

    private void showFinishScreen() {

        answersContainer.removeAllViews();
        questionText.setText(R.string.quiz_finish_message);

        explanationText.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        restartButton.setVisibility(View.VISIBLE);
        inMainButton.setVisibility(View.VISIBLE);
        insertQuizResults();
    }

    private void insertQuizResults() {
        // Запускаем в фоновом потоке
        new Thread(() -> {
            db = DatabaseClient.getInstance(this);

            QuizResult result = new QuizResult();
            result.percent = this.currentScore * 100 / this.totalQuestions;

            result.timestamp = System.currentTimeMillis();
            db.QuizResultDAO().insert(result);
        }).start();
    }


    private void loadQuestion() {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        Question q = questions.get(currentQuestionIndex);

        // 1. текст вопроса
        questionText.setText(q.text);

        // 2. очистка старых кнопок
        answersContainer.removeAllViews();

        // 3. скрываем UI объяснения и "Далее"
        explanationText.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        inMainButton.setVisibility(View.GONE);

        // 4. создаём кнопки ответов
        for (int i = 0; i < q.answers.size(); i++) {
            int index = i;

            Button btn = new Button(this);
            btn.setBackgroundResource(R.drawable.rounded_button);
            btn.setText(q.answers.get(i));

            // Создаём LayoutParams для каждой кнопки отдельно
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = 24; // отступ между кнопками в пикселях
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                checkAnswer(index);
                // блокируем все кнопки после выбора
                setButtonsEnabled(false);
            });

            answersContainer.addView(btn);
        }

        // 5. обновляем успешность (один раз за вопрос)
        updateQuizSuccessRate();

        // 6. снова включаем кнопки (для нового вопроса)
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        for (int i = 0; i < answersContainer.getChildCount(); i++) {
            View v = answersContainer.getChildAt(i);
            v.setEnabled(enabled);
        }
    }
    private List<Question> loadQuestionsFromJson() {
        List<Question> list = new ArrayList<>();

        try {
            InputStream is = getAssets().open("questions.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String text = obj.getString("text");
                int correctIndex = obj.getInt("correctIndex");
                String correctComment = obj.getString("correctComment");
                String wrongComment = obj.getString("wrongComment");

                JSONArray answersJson = obj.getJSONArray("answers");
                List<String> answers = new ArrayList<>();

                for (int j = 0; j < answersJson.length(); j++) {
                    answers.add(answersJson.getString(j));
                }

                list.add(new Question(text, answers, correctIndex, correctComment, wrongComment));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private void checkAnswer(int index) {
        Question q = questions.get(currentQuestionIndex);

        if (index == q.correctIndex) {
            explanationText.setText(q.correctComment);
            this.currentScore++;
            updateQuizSuccessRate();
        } else {
            explanationText.setText(q.wrongComment);
        }

        explanationText.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void updateQuizSuccessRate() {
        String rate = getString( R.string.quiz_success_rate, this.currentScore * 100 / this.totalQuestions);
        quizSuccessRate.setText(rate);
    }

}

