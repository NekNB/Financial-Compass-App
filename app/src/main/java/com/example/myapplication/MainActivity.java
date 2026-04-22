package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.io.InputStream;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import com.example.myapplication.model.Question;


public class MainActivity extends AppCompatActivity {
    TextView questionText, explanationText;
    Button nextButton, restartButton;
    LinearLayout answersContainer;

    List<Question> questions;
    int currentQuestionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI элементы
        questionText = findViewById(R.id.questionText);
        explanationText = findViewById(R.id.explanationText);
        nextButton = findViewById(R.id.nextButton);
        answersContainer = findViewById(R.id.answersContainer);
        restartButton = findViewById(R.id.restartButton);

        // загрузка вопросов из JSON
        questions = loadQuestionsFromJson();
        Log.d("DEBUG", "Questions loaded: " + questions.size());
        if (questions.isEmpty()) {
            questionText.setText("ERROR: no questions loaded");
            return;
        }

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
        });
    }

    private void showFinishScreen() {

        answersContainer.removeAllViews();
        questionText.setText("Тест завершён");

        explanationText.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        restartButton.setVisibility(View.VISIBLE);
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

        // 4. создаём кнопки ответов
        for (int i = 0; i < q.answers.size(); i++) {
            int index = i;

            Button btn = new Button(this);
            btn.setText(q.answers.get(i));

            btn.setOnClickListener(v -> {
                checkAnswer(index);

                // блокируем все кнопки после выбора
                setButtonsEnabled(false);
            });

            answersContainer.addView(btn);
        }

        // 5. снова включаем кнопки (для нового вопроса)
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

            String json = new String(buffer, "UTF-8");
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
        } else {
            explanationText.setText(q.wrongComment);
        }

        explanationText.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }
}



