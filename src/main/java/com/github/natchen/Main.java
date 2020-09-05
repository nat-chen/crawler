package com.github.natchen;

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
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        String TARGET_URL = "https://sina.cn";
        List<String> toBeProcessedLinkPool = new ArrayList<>(Arrays.asList(TARGET_URL));
        Set<String> processedLinkPool = new HashSet<>();

        while (true) {
            if (toBeProcessedLinkPool.isEmpty()) {
                return;
            }

            String currentLink = toBeProcessedLinkPool.remove(toBeProcessedLinkPool.size() - 1);

            if (processedLinkPool.contains(currentLink)) {
                continue;
            }

            if (isTargetLink(currentLink)) {
                Document currentHtml = fetchRequestAndParseHtml(currentLink);
                currentHtml.select("a").stream().map(aElement -> aElement.attr("href")).forEach(toBeProcessedLinkPool::add);
                storeIntoDatabaseIfNewsPage(currentHtml);
                processedLinkPool.add(currentLink);
            }
        }

    }

    private static void storeIntoDatabaseIfNewsPage(Document doc) {
        ArrayList<Element> articleElementList = doc.select("article");
        if (!articleElementList.isEmpty()) {
            for (Element articleElement : articleElementList) {
                String title = articleElementList.get(0).child(0).text();
            }
        }
    }

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
