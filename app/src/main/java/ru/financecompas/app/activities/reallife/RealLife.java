package ru.financecompas.app.activities.reallife;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.financecompas.app.R;
import ru.financecompas.app.activities.reallife.data.Action;
import ru.financecompas.app.activities.reallife.data.EventData;

public class RealLife extends AppCompatActivity {

    // Игровые переменные
    private int balance = 35000;        // начальный баланс
    private int currentDay = 0;         // текущий день (1-30)
    private int lastProcessedSunday = 0; // последнее обработанное воскресенье для еды

    // Данные событий
    private final List<EventData> events = new ArrayList<>();
    private EventData currentEvent;

    // UI элементы
    private TextView tvDate;
    private TextView tvBalance;
    private TextView tvEvent;
    private LinearLayout buttonsContainer;
    private TextView tvComment;
    private Button btnNext;

    private boolean isActionChosen = false; // выбран ли action в текущем дне

    // Воскресенья для списания еды
    private static final int[] SUNDAYS = {7, 14, 21, 28};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_life);

        initViews();
        loadEventsFromAssets();
        updateUI();
        showInfoDialog();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvBalance = findViewById(R.id.tvBalance);
        tvEvent = findViewById(R.id.tvEvent);
        buttonsContainer = findViewById(R.id.buttonsContainer);
        tvComment = findViewById(R.id.tvComment);
        btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(v -> goToNextDay());

        // Кнопка информации в левом верхнем углу
        Button btnInfo = findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(v -> showInfoDialog());
    }

    private void loadEventsFromAssets() {
        try {
            InputStream is = getAssets().open("events.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                EventData event = new EventData();
                event.data = obj.getInt("data");
                event.event = obj.getString("event");
                event.comment = obj.getString("comment");

                JSONArray actionsArray = obj.getJSONArray("actions");
                event.actions = new ArrayList<>();
                for (int j = 0; j < actionsArray.length(); j++) {
                    JSONObject actionObj = actionsArray.getJSONObject(j);
                    Action action = new Action();
                    action.choice_option = actionObj.getString("choice_option");
                    action.operation = actionObj.getInt("operation");
                    action.is_right = actionObj.getBoolean("is_right");
                    event.actions.add(action);
                }

                events.add(event);
            }
        } catch (Exception e) {
            Log.e("RealLife", "Error loading events.json", e);
        }
    }

    private void showInfoDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_info, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Понятно", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            // Используем цвета из resources
            positiveButton.setTextColor(getResources().getColor(R.color.dialog_button_text_color));
            positiveButton.setBackgroundColor(getResources().getColor(R.color.dialog_button_color));

            positiveButton.setAllCaps(false);
        });

        dialog.show();
    }

    private void updateUI() {
        // Обновляем баланс (красный если минус)
        String balanceText = String.format(Locale.getDefault(), "Баланс: %,d ₽", balance);
        tvBalance.setText(balanceText);
        if (balance < 0) {
            tvBalance.setTextColor(Color.RED);
        } else {
            tvBalance.setTextColor(Color.BLACK);
        }





        // Показываем событие для текущего дня
        displayCurrentEvent();

    }

    private void displayCurrentEvent() {
        EventData event = getEventForCurrentDay();
        // Обновляем дату
        tvDate.setText(String.format("День: %d", currentDay));
        if (event == null) {
            showResultsScreen();
            return;
        }
        // Очищаем предыдущие кнопки
        buttonsContainer.removeAllViews();
        tvComment.setVisibility(View.GONE);
        tvComment.setText("");
        isActionChosen = false;
        btnNext.setEnabled(false);



        currentEvent = event;
        tvEvent.setText(event.event);

        if (event.actions != null && !event.actions.isEmpty()) {
            for (int i = 0; i < event.actions.size(); i++) {
                Action action = event.actions.get(i);
                Button btn = createActionButton(action, i);
                buttonsContainer.addView(btn);
            }
        }
    }

    private EventData getEventForCurrentDay() {
        Log.d("RealLife", "Текущий день:" + currentDay);
        Log.d("RealLife", "Длина событий:" + events.size());
        for (EventData event : events) {

            if (event.data > currentDay) { // Переход к следующему дню
//                event = events.get(events.indexOf(event) -1);
                currentDay = event.data;
                Log.d("RealLife", "Возвращая событие: " + event.data);
                return event;
            } else {
                Log.d("RealLife", "Дата события и дата дня: " + event.data + currentDay);
            }
        }
        return null;
    }

    private Button createActionButton(Action action, int index) {
        Button button = new Button(this);
        button.setText(action.choice_option);
        button.setPadding(32, 16, 32, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> onActionSelected(action, button));

        return button;
    }

    private void onActionSelected(Action action, Button button) {
        if (isActionChosen) return; // уже выбран action в этом дне

        isActionChosen = true;

        // Применяем действие (изменяем баланс)
        balance += action.operation;

        // Обновляем отображение баланса
        updateBalanceDisplay();

        // Показываем комментарий
        if (currentEvent != null && currentEvent.comment != null && !currentEvent.comment.isEmpty()) {
            tvComment.setText(currentEvent.comment);
            tvComment.setVisibility(View.VISIBLE);
        }

        // Подсвечиваем кнопку
        GradientDrawable drawable = new GradientDrawable();
        drawable.setStroke(4, action.is_right ? Color.GREEN : Color.RED);
        drawable.setCornerRadius(16);
        drawable.setColor(Color.TRANSPARENT);
        button.setBackground(drawable);

        // Отключаем все остальные кнопки
        disableAllActionButtons();

        // Включаем кнопку Next
        btnNext.setEnabled(true);
    }

    private void updateBalanceDisplay() {
        String balanceText = String.format(Locale.getDefault(), "Баланс: %,d ₽", balance);
        tvBalance.setText(balanceText);
        if (balance < 0) {
            tvBalance.setTextColor(Color.RED);
        } else {
            tvBalance.setTextColor(Color.BLACK);
        }
    }

    private void disableAllActionButtons() {
        for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
            buttonsContainer.getChildAt(i).setEnabled(false);
        }
    }

    private void goToNextDay() {
        if (!btnNext.isEnabled()) return;



        // Проверяем списание на еду (каждое воскресенье)
        checkFoodExpense();

        updateUI();
    }

    private void checkFoodExpense() {
        for (int sunday : SUNDAYS) {
            // если текущий день >= воскресенья, и мы еще не списали за это воскресенье
            if (currentDay >= sunday && lastProcessedSunday < sunday) {
                balance -= 2000;
                lastProcessedSunday = sunday;
                Log.d("RealLife", "Food expense: -2000 on day " + currentDay + " (Sunday " + sunday + ")");
                break;
            }
        }
    }

    private void showResultsScreen() {
        String message;
        if (balance >= 3000) {
            message = String.format(Locale.getDefault(),
                    "Месяц закончен!\n\nИтоговый баланс: %,d ₽\n\n✓ Поздравляем! Вы достигли цели!\nВы отложили более 3 000 ₽.",
                    balance);
        } else if (balance >= 0) {
            message = String.format(Locale.getDefault(),
                    "Месяц закончен!\n\nИтоговый баланс: %,d ₽\n\n✗ Вы не ушли в минус, но не смогли отложить 3 000 ₽.\nВ следующий раз получится!",
                    balance);
        } else {
            message = String.format(Locale.getDefault(),
                    "Месяц закончен!\n\nИтоговый баланс: %,d ₽\n\n✗ К сожалению, вы ушли в минус.\nВ следующий раз будьте внимательнее с тратами!",
                    balance);
        }

        new AlertDialog.Builder(this)
                .setTitle("Игра завершена")
                .setMessage(message)
                .setNeutralButton("Закрыть", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Выйти", (dialog, which) -> finish())
                .setNegativeButton("Заново", (dialog, which) -> {
                    resetGame();
                    updateUI();
                })
                .setCancelable(false)
                .show();
    }

    private void resetGame() {
        balance = 35000;
        currentDay = 1;
        lastProcessedSunday = 0;
        isActionChosen = false;
        currentEvent = null;
    }





}