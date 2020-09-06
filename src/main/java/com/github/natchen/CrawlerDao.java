package com.github.natchen;

import java.sql.SQLException;

public interface CrawlerDao {
    String readLinkThenRemoveFromDatabase() throws SQLException;

    void writeNews(String url, String title, String content) throws SQLException;

    boolean isLinkPrecessed(String link) throws SQLException;

    void writeLinkProcessed(String link);

    void writeLinkToBeProcessed(String link);
}

