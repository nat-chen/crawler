package com.github.natchen;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String USERNAME = "root";

    @Override
    public void writeNews(String url, String title, String content) throws SQLException {

    }

    private static final String PASSWORD = "root";

    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
           this.connection = DriverManager.getConnection("jdbc:h2:file:///Users/natchen/Documents/java/crawler/news", USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String readLinkFromDatabase(String sql) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }

    @Override
    public String readLinkThenRemoveFromDatabase() throws SQLException {
        String SQL_SELECT_LINK_TO_BE_PROCESSED = "SELECT LINK FROM LINK_TO_BE_PROCESSED LIMIT 1";
        String SQL_REMOVE_LINK_TO_BE_PROCESSED = "DELETE FROM LINK_TO_BE_PROCESSED WHERE LINK = ?";
        String link = readLinkFromDatabase(SQL_SELECT_LINK_TO_BE_PROCESSED);
        if (link != null) {
            updateDatabaseBySqlStatement(link, SQL_REMOVE_LINK_TO_BE_PROCESSED);
        }
        return link;
    }

    public void updateDatabaseBySqlStatement(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void writeIntoNewsDatabase(String url, String title, String content) throws SQLException {
        String SQL_INSERT_NEWS = "INSERT INTO NEWS (URL, TITLE, CONTENT, CREATED_AT, UPDATED_AT) VALUES (?,?,?,NOW(),NOW())";
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_NEWS)) {
            preparedStatement.setString(1, url);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, content);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public boolean isLinkPrecessed(String link) throws SQLException {
        String SQL_SELECT_LINK_PROCESSED = "SELECT LINK FROM LINK_PROCESSED WHERE LINK = ?";
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_LINK_PROCESSED)) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    @Override
    public void writeLinkProcessed(String link) {

    }

    @Override
    public void writeLinkToBeProcessed(String link) {

    }
}
