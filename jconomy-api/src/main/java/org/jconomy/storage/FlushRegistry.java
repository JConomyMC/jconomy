package org.jconomy.storage;

public interface FlushRegistry {
    void register(Flushable flushable);
    void flushAll();
}
