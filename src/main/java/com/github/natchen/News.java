package com.github.natchen;

import java.time.Instant;

public class News {
    private Integer id;
    private String url;
    private String content;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;

    public News() {
    }

    public News(String url, String content, String title, Instant createdAt, Instant updatedAt) {
        this.url = url;
        this.content = content;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public News(News old) {
        this.id = old.id;
        this.url = old.url;
        this.content = old.content;
        this.title = old.title;
        this.createdAt = old.createdAt;
        this.updatedAt = old.updatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
