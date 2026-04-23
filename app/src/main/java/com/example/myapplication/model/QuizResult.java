package com.example.myapplication.model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class QuizResult {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int percent;
    public long timestamp;
}