package com.github.natchen;

import java.sql.SQLException;

public class MyBatisCrawlerDao implements CrawlerDao {
    @Override
    public String readLinkFromDatabase(String sql) throws SQLException {
        return null;
    }

    @Override
    public String readLinkThenRemoveFromDatabase() throws SQLException {
        return null;
    }

    @Override
    public void updateDatabaseBySqlStatement(String link, String sql) throws SQLException {

    }

    @Override
    public void writeIntoNewsDatabase(String url, String title, String content) throws SQLException {

    }

    @Override
    public boolean isLinkPrecessed(String link) throws SQLException {
        return false;
    }
}
