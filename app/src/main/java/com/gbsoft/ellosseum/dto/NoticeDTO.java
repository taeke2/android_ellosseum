package com.gbsoft.ellosseum.dto;

public class NoticeDTO {
    private int id;
    private String writerId;
    private String writer;
    private int publicScope;
    private String title;
    private String content;
    private int importantYn;
    private String createDate;
    private String updateDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWriterId() {
        return writerId;
    }

    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public int getPublicScope() {
        return publicScope;
    }

    public void setPublicScope(int publicScope) {
        this.publicScope = publicScope;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getImportantYn() {
        return importantYn;
    }

    public void setImportantYn(int importantYn) {
        this.importantYn = importantYn;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
