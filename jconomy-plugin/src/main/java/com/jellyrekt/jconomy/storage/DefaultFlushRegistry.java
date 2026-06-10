package com.jellyrekt.jconomy.storage;

import java.util.ArrayList;
import java.util.List;

public class DefaultFlushRegistry implements FlushRegistry {
    private final List<Flushable> flushables = new ArrayList<>();

    @Override
    public void register(Flushable flushable) {
        flushables.add(flushable);
    }

    @Override
    public void flushAll() {
        flushables.forEach(Flushable::flush);
    }
}
