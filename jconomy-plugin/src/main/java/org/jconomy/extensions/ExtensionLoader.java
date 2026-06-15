package org.jconomy.extensions;

import java.util.Set;

public interface ExtensionLoader {

    Set<LoadedExtension> load();

}
