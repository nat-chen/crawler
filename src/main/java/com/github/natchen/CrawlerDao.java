package com.github.natchen;

import java.sql.SQLException;

public interface CrawlerDao {
    String readLinkFromDatabase(String sql) throws SQLException;

    String readLinkThenRemoveFromDatabase() throws SQLException;

    void updateDatabaseBySqlStatement(String link, String sql) throws SQLException;

    void writeIntoNewsDatabase(String url, String title, String content) throws SQLException;

    boolean isLinkPrecessed(String link) throws SQLException;
}

