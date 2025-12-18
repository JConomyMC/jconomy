package com.jellyrekt.jconomy.expansions;

import java.net.URLClassLoader;

import com.jellyrekt.jconomy.JConomyExpansion;

public record LoadedExpansion(JConomyExpansion expansion, URLClassLoader classLoader) { }
