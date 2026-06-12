package com.jellyrekt.jconomy.storage;

public interface ImportRunRecord {
    boolean isCompleted(String importerId);
    void markCompleted(String importerId);
}
