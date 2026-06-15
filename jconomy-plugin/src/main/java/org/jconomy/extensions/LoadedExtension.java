package org.jconomy.extensions;

import java.net.URLClassLoader;

import org.jconomy.JConomyExtension;

public record LoadedExtension(JConomyExtension extension, URLClassLoader classLoader) { }
