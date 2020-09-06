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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Crawler {
    private final CrawlerDao dao = new JdbcCrawlerDao();

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }

    public void run() throws SQLException, IOException {
        String currentLink;
        while ((currentLink = dao.readLinkThenRemoveFromDatabase()) != null) {
            if (dao.isLinkPrecessed(currentLink)) {
                continue;
            }

            if (isTargetLink(currentLink)) {
                System.out.println(currentLink);
                Document currentHtml = fetchRequestAndParseHtml(currentLink);
                parseUrlFromPageAndWriteIntoDatabase(currentHtml);
                writeIntoDatabaseIfNewsPage(currentHtml, currentLink);
                String SQL_INSERT_LINK_PROCESSED = "INSERT INTO LINK_PROCESSED (link) values (?)";
                dao.updateDatabaseBySqlStatement(currentLink, SQL_INSERT_LINK_PROCESSED);
            }
        }
    }

    public void parseUrlFromPageAndWriteIntoDatabase(Document doc) throws SQLException {
        String SQL_INSERT_LINK_TO_BE_PROCESSED = "INSERT INTO LINK_TO_BE_PROCESSED (link) values (?)";
        List<Element> elementList = doc.select("a");
        for (Element element : elementList) {
            String link = element.attr("href");

            if (link.startsWith("//")) {
                link = "https:" + link;
            }

            if (!link.toLowerCase().startsWith("javascript")) {
                dao.updateDatabaseBySqlStatement(link, SQL_INSERT_LINK_TO_BE_PROCESSED);
            }
        }
    }

    public void writeIntoDatabaseIfNewsPage(Document doc, String link) throws SQLException {
        ArrayList<Element> articleElementList = doc.select("article");
        if (!articleElementList.isEmpty()) {
            for (Element articleElement : articleElementList) {
                String title = articleElementList.get(0).child(0).text();
                String content = articleElement.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.writeIntoNewsDatabase(link, title, content);
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
