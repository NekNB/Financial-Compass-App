package ru.financecompas.app.model;
import java.util.List;

public class Question {
    public String text;
    public List<String> answers;
    public int correctIndex;
    public String correctComment;
    public String wrongComment;

    public Question(String text, List<String> answers, int correctIndex,
                    String correctComment, String wrongComment) {
        this.text = text;
        this.answers = answers;
        this.correctIndex = correctIndex;
        this.correctComment = correctComment;
        this.wrongComment = wrongComment;
    }
}