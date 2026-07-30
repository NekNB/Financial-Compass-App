package ru.financecompas.app.activities.fourthGame;

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
import java.util.Random;

import ru.financecompas.app.R;

public class FourthGame extends AppCompatActivity {

    // UI элементы
    private TextView tvYear, tvStats, tvQuestion, tvComment;
    private Button btnA, btnB, btnC, btnNext;

    // Данные из JSON
    private JSONObject step1Data, step2Data;
    private JSONArray eventsData;

    // Состояние игры
    private int currentYear = 1; // С 20 до 30 лет (10 лет)
    private int currentStep = 1; // 1 - выбор %, 2 - выбор инвестиций, 3 - событие
    private double totalSavings = 10000; // Стартовые накопления
    private final double GOAL = 600000;

    // Выбор пользователя в текущем году
    private int selectedStep1 = 0; // 0=A, 1=B, 2=C
    private int selectedStep2 = 0;
    private int currentEventIndex = 0;
    private int selectedEventOption = 0;

    private boolean hasAnswered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fourth_game_activity);

        tvYear = findViewById(R.id.fourthGameYear);
        tvStats = findViewById(R.id.fourthGameStats);
        tvQuestion = findViewById(R.id.fourthGameQuestion);
        tvComment = findViewById(R.id.fourthGameComment);

        btnA = findViewById(R.id.optionA);
        btnB = findViewById(R.id.optionB);
        btnC = findViewById(R.id.optionC);
        btnNext = findViewById(R.id.nextStepButton);

        loadGameData();

        btnA.setOnClickListener(v -> handleAnswer(0));
        btnB.setOnClickListener(v -> handleAnswer(1));
        btnC.setOnClickListener(v -> handleAnswer(2));

        btnNext.setOnClickListener(v -> {
            if (currentStep < 3) {
                currentStep++;
                showCurrentStep();
            } else {
                // Конец года, считаем результаты
                calculateYearEnd();
                currentYear++;
                if (currentYear <= 10) {
                    currentStep = 1;
                    showCurrentStep();
                } else {
                    showResultsDialog();
                }
            }
        });

        startGame();
    }

    private void loadGameData() {
        try {
            InputStream is = getAssets().open("game4.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject root = new JSONObject(json);
            step1Data = root.getJSONObject("step1");
            step2Data = root.getJSONObject("step2");
            eventsData = root.getJSONArray("events");

        } catch (Exception e) {
            Log.e("FourthGame", "Error loading game4.json", e);
        }
    }

    private void startGame() {
        currentYear = 1;
        currentStep = 1;
        totalSavings = 10000;
        showCurrentStep();
    }

    private void showCurrentStep() {
        hasAnswered = false;
        tvComment.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);

        // Визуальная разблокировка кнопок
        setButtonsEnabled(true, 1f);

        tvYear.setText("Возраст: " + (19 + currentYear) + " лет");
        tvStats.setText("Накоплено: " + (int) totalSavings + " ₽ / Цель: 600 000 ₽");

        try {
            if (currentStep == 1) {
                tvQuestion.setText(step1Data.getString("question"));
                btnA.setText(step1Data.getString("a"));
                btnB.setText(step1Data.getString("b"));
                btnC.setText(step1Data.getString("c"));
            } else if (currentStep == 2) {
                tvQuestion.setText(step2Data.getString("question"));
                btnA.setText(step2Data.getString("a"));
                btnB.setText(step2Data.getString("b"));
                btnC.setText(step2Data.getString("c"));
            } else if (currentStep == 3) {
                // Выбираем случайное событие
                currentEventIndex = new Random().nextInt(eventsData.length());
                JSONObject event = eventsData.getJSONObject(currentEventIndex);

                tvQuestion.setText(event.getString("title") + "\n\n" + event.getString("desc"));
                btnA.setText(event.getString("a"));
                btnB.setText(event.getString("b"));
                btnC.setText(event.getString("c"));

                if (currentYear == 10) {
                    btnNext.setText("Завершить игру");
                }
            }
        } catch (Exception e) {
            Log.e("FourthGame", "Error parsing step data", e);
        }
    }

    private void handleAnswer(int optionIndex) {
        if (hasAnswered) return;
        hasAnswered = true;

        setButtonsEnabled(false, 0.5f);

        try {
            if (currentStep == 1) {
                selectedStep1 = optionIndex;
                tvComment.setText("Выбор принят. Процент отчислений установлен.");
            } else if (currentStep == 2) {
                selectedStep2 = optionIndex;
                tvComment.setText("Выбор принят. Инвестиционный портфель сформирован.");
            } else if (currentStep == 3) {
                selectedEventOption = optionIndex;
                JSONObject event = eventsData.getJSONObject(currentEventIndex);
                String comment = "";
                switch (optionIndex) {
                    case 0: comment = event.getString("commentA"); break;
                    case 1: comment = event.getString("commentB"); break;
                    case 2: comment = event.getString("commentC"); break;
                }
                tvComment.setText(comment);
            }

            tvComment.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
            btnNext.setText(currentStep == 3 ? (currentYear == 10 ? "Завершить игру" : "Следующий год") : "Далее");

        } catch (Exception e) {
            Log.e("FourthGame", "Error handling answer", e);
        }
    }

    private void calculateYearEnd() {
        // 1. Считаем отчисления за год
        double yearlyContribution = 0;
        if (selectedStep1 == 0) yearlyContribution = 24000;      // 5%
        else if (selectedStep1 == 1) yearlyContribution = 48000; // 10%
        else if (selectedStep1 == 2) yearlyContribution = 72000; // 15%

        totalSavings += yearlyContribution;

        // 2. Считаем доходность инвестиций (применяем к итоговой сумме)
        double rate = 0;
        if (selectedStep2 == 0) rate = 0.06;      // Вклад
        else if (selectedStep2 == 1) rate = 0.12; // ИИС
        else if (selectedStep2 == 2) rate = 0.20; // Акции

        totalSavings += totalSavings * rate;

        // 3. Обрабатываем событие (упрощенная математика для симуляции)
        if (currentEventIndex == 0) { // Инфляция
            if (selectedEventOption == 1) totalSavings += totalSavings * 0.05; // Увеличил отчисления
            else if (selectedEventOption == 2) totalSavings -= totalSavings * 0.10; // Забрал наличными
        } else if (currentEventIndex == 1) { // Кризис
            if (selectedEventOption == 0) totalSavings -= totalSavings * 0.20; // Продал в панике
            else if (selectedEventOption == 2) rate += 0.15; // Докупил, увеличиваем доходность (уже применена, но добавим бонус визуально в лог)
            // Для простоты добавим фиксированный бонус за докупку
            if (selectedEventOption == 2) totalSavings += 10000;
        } else if (currentEventIndex == 2) { // Премия
            if (selectedEventOption == 1) totalSavings += 25000;
            else if (selectedEventOption == 2) totalSavings += 50000;
        }
        // Другие события можно добавить по аналогии

        if (totalSavings < 0) totalSavings = 0;
    }

    private void setButtonsEnabled(boolean enabled, float alpha) {
        btnA.setEnabled(enabled);
        btnB.setEnabled(enabled);
        btnC.setEnabled(enabled);
        btnA.setAlpha(alpha);
        btnB.setAlpha(alpha);
        btnC.setAlpha(alpha);
    }

    private void showResultsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Игра окончена! (Вам 30 лет)");

        boolean isGoalReached = totalSavings >= GOAL;
        String resultText = "Ваши накопления составили: " + (int) totalSavings + " ₽\n" +
                "Цель в 600 000 ₽: " + (isGoalReached ? "достигнута! 🎉" : "не достигнута.");

        // Формируем отчет по стратегии
        String strategy = "\n\nВаша стратегия:\n" +
                "Процент отчислений: " + (selectedStep1 == 0 ? "5%" : selectedStep1 == 1 ? "10%" : "15%") + "\n" +
                "Инструмент: " + (selectedStep2 == 0 ? "Вклад" : selectedStep2 == 1 ? "ИИС" : "Акции");

        builder.setMessage(resultText + strategy);
        builder.setCancelable(false);

        builder.setPositiveButton("Играть еще раз", (dialog, which) -> {
            startGame();
            dialog.dismiss();
        });

        builder.setNegativeButton("Выйти", (dialog, which) -> {
            finish();
        });

        builder.create().show();
    }
}