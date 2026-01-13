package com.example.foundbuddybackend.dto;

import com.example.foundbuddybackend.model.FoundItem;

public class AiSearchResult {

    private FoundItem item;
    private double score;

    private double clipScore;
    private double textScore;
    private double recencyScore;


    public AiSearchResult(FoundItem item, double score, double clip, double text, double recency) {
        this.item = item;
        this.score = score;
        this.clipScore = clip;
        this.textScore = text;
        this.recencyScore = recency;

    }

    public FoundItem getItem() {
        return item;
    }

    public double getScore() {
        return score;
    }

    public double getClipScore() {
        return clipScore;
    }

    public double getTextScore() {
        return textScore;
    }

    public double getRecencyScore() {
        return recencyScore;
    }
}
