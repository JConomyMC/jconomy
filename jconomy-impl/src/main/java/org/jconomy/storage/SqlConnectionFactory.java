package org.jconomy.storage;

import java.sql.Connection;
import java.sql.SQLException;

public interface  SqlConnectionFactory {
    Connection createConnection() throws SQLException;
}
