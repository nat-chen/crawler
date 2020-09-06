package com.github.natchen;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        String databaseUrl = "jdbc:h2:file:///Users/natchen/Documents/java/crawler/src/target/test";
        Connection connection = DriverManager.getConnection(databaseUrl, USERNAME, PASSWORD);

        String currentLink;

        while ((currentLink = readLinkThenRemoveFromDatabase(connection)) != null) {
            if (isLinkPrecessed(connection, currentLink)) {
                continue;
            }

            if (isTargetLink(currentLink)) {
                System.out.printf(currentLink);
                Document currentHtml = fetchRequestAndParseHtml(currentLink);
                parseUrlFromPageAndWriteIntoDatabase(connection, currentHtml);
                writeIntoDatabaseIfNewsPage(connection, currentHtml, currentLink);
                String SQL_INSERT_LINK_PROCESSED = "INSERT INTO LINKS_ALREADY_PROCESSED (link) values (?)";
                updateDatabaseBySqlStatement(connection, currentLink, SQL_INSERT_LINK_PROCESSED);
            }
        }

    }

    private static String readLinkFromDatabase(Connection connection, String sql) throws SQLException {
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

    private static String readLinkThenRemoveFromDatabase(Connection connection) throws SQLException {
        String SQL_SELECT_LINK_TO_BE_PROCESSED = "select link from LINKS_TO_BE_PROCESSED LIMIT 1";
        String SQL_REMOVE_LINK_TO_BE_PROCESSED = "DELETE FROM LINKS_TO_BE_PROCESSED where link = ?";
        String link = readLinkFromDatabase(connection, SQL_SELECT_LINK_TO_BE_PROCESSED);
        if (link != null) {
            updateDatabaseBySqlStatement(connection, link, SQL_REMOVE_LINK_TO_BE_PROCESSED);
        }
        return link;
    }

    private static void updateDatabaseBySqlStatement(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }


    private static void parseUrlFromPageAndWriteIntoDatabase(Connection connection, Document doc) throws SQLException {
        String SQL_INSERT_LINK_TO_BE_PROCESSED = "INSERT INTO LINK_TO_BE_PROCESSED (link) values (?)";
        List<Element> elementList = doc.select("a");
        for (Element element : elementList) {
            String link = element.attr("href");

            if (link.startsWith("//")) {
                link = "https:" + link;
            }

            if (!link.toLowerCase().startsWith("javascript")) {
                updateDatabaseBySqlStatement(connection, link, SQL_INSERT_LINK_TO_BE_PROCESSED);
            }
        }
    }

    private static void writeLinkIntoDatabase(Connection connection, String link) throws SQLException {
        String SQL_INSERT_LINK_PROCESSED = "INSERT INTO LINK_PROCESSED (link) VALUES (?)";
        System.out.println(link);
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_LINK_PROCESSED)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    private static boolean isLinkPrecessed(Connection connection, String link) throws SQLException {
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


    private static void writeIntoDatabaseIfNewsPage(Connection connection, Document doc, String link) throws SQLException {
        ArrayList<Element> articleElementList = doc.select("article");
        if (!articleElementList.isEmpty()) {
            for (Element articleElement : articleElementList) {
                String title = articleElementList.get(0).child(0).text();
                String content = articleElement.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                String SQL_INSERT_NEWS = "INSERT INTO NEWS (URL, TITLE, CONTENT, CREATED_AT, UPDATED_AT) VALUES (?,?,?.NOW(), NOW())";
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_NEWS)) {
                    preparedStatement.setString(1, link);
                    preparedStatement.setString(2, title);
                    preparedStatement.setString(3, content);
                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private static Document fetchRequestAndParseHtml(String link) throws IOException {
        System.out.println(link);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity));
        }
    }

    private static boolean isTargetLink(String link) {
        return (isNewsPage(link) || isHomePage(link)) && isNotLoginPage(link);
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isHomePage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }
}
