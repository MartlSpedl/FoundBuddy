package com.example.foundbuddybackend.dto;

import com.example.foundbuddybackend.model.FoundItem;

public class AiSearchResult {

    private FoundItem item;
    private double score;

    private double clipScore;
    private double textScore;
    private double recencyScore;

    public AiSearchResult() {}

    public AiSearchResult(FoundItem item, double score) {
        this.item = item;
        this.score = score;
    }

    public AiSearchResult(FoundItem item, double score, double clipScore, double textScore, double recencyScore) {
        this.item = item;
        this.score = score;
        this.clipScore = clipScore;
        this.textScore = textScore;
        this.recencyScore = recencyScore;
    }

    public FoundItem getItem() { return item; }
    public void setItem(FoundItem item) { this.item = item; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getClipScore() { return clipScore; }
    public void setClipScore(double clipScore) { this.clipScore = clipScore; }

    public double getTextScore() { return textScore; }
    public void setTextScore(double textScore) { this.textScore = textScore; }

    public double getRecencyScore() { return recencyScore; }
    public void setRecencyScore(double recencyScore) { this.recencyScore = recencyScore; }
}
