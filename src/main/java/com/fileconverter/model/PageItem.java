package com.fileconverter.model;

import javafx.scene.image.Image;

public class PageItem {
    private final int originalIndex; // 0-based index into the source PDF
    private final Image thumbnail;

    public PageItem(int originalIndex, Image thumbnail) {
        this.originalIndex = originalIndex;
        this.thumbnail = thumbnail;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public int getDisplayPageNumber() {
        return originalIndex + 1;
    }
}