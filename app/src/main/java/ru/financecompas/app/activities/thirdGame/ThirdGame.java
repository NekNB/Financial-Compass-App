package ru.financecompas.app.activities.thirdGame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ru.financecompas.app.R;
import ru.financecompas.app.activities.thirdGame.data.Question;

public class ThirdGame extends AppCompatActivity {

    private final List<Question> questions = new ArrayList<>();

    // Элементы интерфейса
    private TextView tvQuestion;
    private TextView tvComment;
    private Button btnRight;
    private Button btnLie;
    private Button btnNext;

    // Переменные состояния игры
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean hasAnswered = false; // Флаг, чтобы пользователь не мог нажать ответ дважды

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_game);

        // Инициализация UI элементов
        tvQuestion = findViewById(R.id.thirdGameQuestion);
        tvComment = findViewById(R.id.thirdGameComment);
        btnRight = findViewById(R.id.rightButton);
        btnLie = findViewById(R.id.lieButton);
        btnNext = findViewById(R.id.nextQuestionButton);

        loadQuestionsFromAssets();

        // Если вопросы успешно загрузились, показываем первый
        if (!questions.isEmpty()) {
            showQuestion();
        }

        // Обработчики нажатий
        btnRight.setOnClickListener(v -> checkAnswer(true));
        btnLie.setOnClickListener(v -> checkAnswer(false));

        btnNext.setOnClickListener(v -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                showQuestion();
            } else {
                showResultsDialog();
            }
        });
    }

    private void loadQuestionsFromAssets() {
        try {
            InputStream is = getAssets().open("quiz.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Question question = new Question();
                question.statement = obj.getString("statement");
                question.isCorrect = obj.getBoolean("isCorrect");
                question.correctComment = obj.getString("correctComment");
                question.unCorrectComment = obj.getString("unCorrectComment");

                questions.add(question);
            }
        } catch (Exception e) {
            Log.e("ThirdGame", "Error loading quiz.json", e);
        }
    }

    private void showQuestion() {
        hasAnswered = false;
        Question currentQuestion = questions.get(currentQuestionIndex);

        tvQuestion.setText(currentQuestion.statement);
        tvComment.setText("");
        tvComment.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);

        // Возвращаем кнопкам активный вид
        btnRight.setEnabled(true);
        btnLie.setEnabled(true);
        btnRight.setAlpha(1f);
        btnLie.setAlpha(1f);
    }

    private void checkAnswer(boolean userAnswer) {
        if (hasAnswered) return; // Защита от двойного нажатия
        hasAnswered = true;

        Question currentQuestion = questions.get(currentQuestionIndex);

        if (userAnswer == currentQuestion.isCorrect) {
            score++;
            tvComment.setText(currentQuestion.correctComment);
        } else {
            tvComment.setText(currentQuestion.unCorrectComment);
        }

        // Показываем комментарий и кнопку "Далее"
        tvComment.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);

        // Блокируем кнопки ответа, чтобы нельзя было изменить выбор
        btnRight.setEnabled(false);
        btnLie.setEnabled(false);
        // Делаем их немного полупрозрачными для визуальной обратной связи
        btnRight.setAlpha(0.5f);
        btnLie.setAlpha(0.5f);
    }

    private void showResultsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Игра окончена!");
        builder.setMessage("Вы набрали " + score + " из " + questions.size() + " очков.");
        builder.setCancelable(false); // Нельзя закрыть кликом вне окна

        builder.setPositiveButton("Еще раз", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Сбрасываем игру
                currentQuestionIndex = 0;
                score = 0;
                showQuestion();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Выйти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Закрываем активность
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}