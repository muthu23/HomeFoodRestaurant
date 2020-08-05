package com.homefood.restaurant.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CancelReasons {

   /* @SerializedName("reason_list")
    @Expose
    private List<ReasonList> reasonList = null;

    public List<ReasonList> getReasonList() {
        return reasonList;
    }

    public void setReasonList(List<ReasonList> reasonList) {
        this.reasonList = reasonList;
    }*/

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
