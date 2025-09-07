package io.ionic.starter;

public class TodoItem {
    private String text;
    private boolean completed;

    public TodoItem(String text, boolean completed) {
        this.text = text;
        this.completed = completed;
    }

    public String getText() {
        return text;
    }

    public boolean isCompleted() {
        return completed;
    }
}