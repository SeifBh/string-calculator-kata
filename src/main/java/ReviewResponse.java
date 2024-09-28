package com.openai.models;

public class ReviewResponse {
    private int line;
    private String comment;

    public ReviewResponse(int line, String comment) {
        this.line = line;
        this.comment = comment;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
