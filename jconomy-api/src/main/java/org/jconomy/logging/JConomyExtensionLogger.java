package org.jconomy.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jconomy.JConomyExtension;

public class JConomyExtensionLogger<TExtension extends JConomyExtension> {
    private final String prefix;
    private final Logger logger;

    public JConomyExtensionLogger(TExtension extension, Logger logger) {
        this.prefix = String.format("[%s] ", extension.getName());
        this.logger = logger;
    }

    public void info(String messageFormat, Object... args) {
        logger.log(Level.INFO, prefix + messageFormat, args);
    }

    public void warning(String messageFormat, Object... args) {
        logger.log(Level.WARNING, prefix + messageFormat, args);
    }

    public void severe(String messageFormat, Object... args) {
        logger.log(Level.SEVERE, prefix + messageFormat, args);
    }
}
