package ca.concordia.soen342.poc.model;

public class Tag {
    private int tagId;
    private String keyword;

    public Tag() {
    }

    public Tag(int tagId, String keyword) {
        this.tagId = tagId;
        this.keyword = keyword;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
