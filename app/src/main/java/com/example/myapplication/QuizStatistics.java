package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.DatabaseClient;
import com.example.myapplication.model.QuizResult;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizStatistics extends AppCompatActivity {

    private LineChart chart;
    private TextView tvAverage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_statistics);

        chart = findViewById(R.id.chart);
        tvAverage = findViewById(R.id.tvAverage);

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<QuizResult> results = DatabaseClient.getInstance(this)
                    .QuizResultDAO()
                    .getLast10();

            // Разворачиваем список, чтобы старые результаты были слева
            Collections.reverse(results);

            runOnUiThread(() -> {
                if (results.isEmpty()) {
                    tvAverage.setText("Нет данных для отображения");
                    return;
                }
                updateChart(results);
            });
        }).start();
    }

    private void updateChart(List<QuizResult> results) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int sum = 0;

        for (int i = 0; i < results.size(); i++) {
            QuizResult result = results.get(i);

            int percent = result.percent;
            if (percent < 0) percent = 0;
            if (percent > 100) percent = 100;

            entries.add(new Entry(i, percent));
            labels.add(String.valueOf(i + 1)); // Номер попытки
            sum += result.percent;
        }

        // Вычисляем средний результат
        int average = sum / results.size();
        tvAverage.setText("Средний результат: " + average + "%");

        // Настройка набора данных
        LineDataSet dataSet = new LineDataSet(entries, "Результаты");
        dataSet.setColor(Color.rgb(33, 150, 243)); // Синий цвет линии
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.rgb(33, 150, 243));
        dataSet.setCircleRadius(6f);
        dataSet.setCircleHoleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setFillColor(Color.rgb(33, 150, 243));
        dataSet.setFillAlpha(50);
        dataSet.setDrawFilled(true);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER); // Плавная линия

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Настройка оси X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(results.size(), 10));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(Math.max(results.size() - 1, 1f));
        xAxis.setDrawGridLines(false); // ← убрать сетку X
        xAxis.setDrawAxisLine(true);   // ← оставить ось X
        xAxis.setAxisLineWidth(3f);
        // Настройка левой оси Y (проценты)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setLabelCount(6, true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setAxisLineWidth(3f);   // ← жирная ось Y
        leftAxis.setAxisLineColor(Color.BLACK);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.BLACK);

        // Отключаем правую ось Y
        chart.getAxisRight().setEnabled(false);

        // Настройка внешнего вида
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.animateX(500); // Анимация при загрузке
        chart.getLegend().setEnabled(false);

        chart.invalidate(); // Перерисовка
    }
}