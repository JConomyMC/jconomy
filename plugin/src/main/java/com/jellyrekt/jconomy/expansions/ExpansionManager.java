package com.jellyrekt.jconomy.expansions;

import java.util.Set;

import com.jellyrekt.jconomy.JConomyExpansion;

public interface ExpansionManager {

    Set<JConomyExpansion> getExpansions();

    void close();

}