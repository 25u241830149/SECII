package com.campushub.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("t_task")
public class Task {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("publisher_id")
    private Long publisherId;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("category")
    private Integer category;

    @TableField("location")
    private String location;

    @TableField("reward")
    private BigDecimal reward;

    @TableField("deadline_time")
    private OffsetDateTime deadlineTime;

    @TableField("item_image_url")
    private String itemImageUrl;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("team_total_members")
    private Integer teamTotalMembers;

    @TableField("team_current_members")
    private Integer teamCurrentMembers;

    @TableField("activity_time")
    private OffsetDateTime activityTime;

    @TableField("activity_note")
    private String activityNote;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private OffsetDateTime createTime;

    @TableField("update_time")
    private OffsetDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField(exist = false)
    private Double longitude;

    @TableField(exist = false)
    private Double latitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }

    public OffsetDateTime getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(OffsetDateTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        this.itemImageUrl = itemImageUrl;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getTeamTotalMembers() {
        return teamTotalMembers;
    }

    public void setTeamTotalMembers(Integer teamTotalMembers) {
        this.teamTotalMembers = teamTotalMembers;
    }

    public Integer getTeamCurrentMembers() {
        return teamCurrentMembers;
    }

    public void setTeamCurrentMembers(Integer teamCurrentMembers) {
        this.teamCurrentMembers = teamCurrentMembers;
    }

    public OffsetDateTime getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(OffsetDateTime activityTime) {
        this.activityTime = activityTime;
    }

    public String getActivityNote() {
        return activityNote;
    }

    public void setActivityNote(String activityNote) {
        this.activityNote = activityNote;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(OffsetDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
