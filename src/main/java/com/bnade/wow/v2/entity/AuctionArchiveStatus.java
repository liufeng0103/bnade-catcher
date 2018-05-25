package com.bnade.wow.v2.entity;

/**
 * 归档job状态
 *
 * Created by liufeng0103@163.com on 2018/1/2.
 */
public class AuctionArchiveStatus {

    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 0;

    public AuctionArchiveStatus() {
    }

    public AuctionArchiveStatus(Integer realmId, String archiveDate, Integer status, String message) {
        this.realmId = realmId;
        this.archiveDate = archiveDate;
        this.status = status;
        this.message = message;
    }

    private Integer id;
    private Integer realmId;
    private String archiveDate;
    private Integer status;
    private String message;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRealmId() {
        return realmId;
    }

    public void setRealmId(Integer realmId) {
        this.realmId = realmId;
    }

    public String getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(String archiveDate) {
        this.archiveDate = archiveDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AuctionArchiveStatus{" +
                "id=" + id +
                ", realmId=" + realmId +
                ", archiveDate='" + archiveDate + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
