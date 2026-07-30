package ru.financecompas.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import ru.financecompas.app.activities.fourthGame.FourthGame;
import ru.financecompas.app.activities.quiz.QuizActivity;

import ru.financecompas.app.activities.reallife.RealLife;
import ru.financecompas.app.activities.thirdGame.ThirdGame;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button quizButton = findViewById(R.id.quizButton);
        quizButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            startActivity(intent);
        });


        Button realLifeButton = findViewById(R.id.realLIfeButton);
        realLifeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RealLife.class);
            startActivity(intent);
        });


        Button thirdGameButton = findViewById(R.id.thirdGameButton);
        thirdGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThirdGame.class);
            startActivity(intent);
        });


        Button fourthGameButton = findViewById(R.id.stepToFutureButton);
        fourthGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FourthGame.class);
            startActivity(intent);
        });

    }

}



