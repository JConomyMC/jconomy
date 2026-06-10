package com.jellyrekt.jconomy.storage;

public interface FlushRegistry {
    void register(Flushable flushable);
    void flushAll();
}
