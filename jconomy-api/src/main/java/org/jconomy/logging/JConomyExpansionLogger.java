package org.jconomy.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jconomy.JConomyExpansion;

public class JConomyExpansionLogger<TExpansion extends JConomyExpansion> {
    private final String prefix;
    private final Logger logger;

    public JConomyExpansionLogger(TExpansion expansion, Logger logger) {
        this.prefix = String.format("[%s] ", expansion.getName());
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
