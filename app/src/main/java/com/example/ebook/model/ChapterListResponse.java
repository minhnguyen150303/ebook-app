package com.example.ebook.model;

import java.util.List;

public class ChapterListResponse {
    private boolean success;
    private List<Chapter> chapters;

    public boolean isSuccess() {
        return success;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }
}
