package org.jconomy.expansions;

import java.net.URLClassLoader;

import org.jconomy.JConomyExpansion;

public record LoadedExpansion(JConomyExpansion expansion, URLClassLoader classLoader) { }
