package com.spotinst.metrics.dal.models.db.suspensions;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spotinst.commons.mapper.entities.base.PartialUpdateEntity;

import java.util.Date;

import static com.spotinst.dropwizard.common.constants.ServiceConstants.Filters.FILTER_NAME_PARTIAL_UPDATE;

@JsonFilter(FILTER_NAME_PARTIAL_UPDATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbProcessSuspension extends PartialUpdateEntity {

    private Integer id;
    private String  resourceId;
    private String  process;
    private String  source;
    private Boolean isPublic;
    private Date    expiredAt;
    private String  cloudProvider;

    public DbProcessSuspension() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public Date getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Date expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(String cloudProvider) {
        this.cloudProvider = cloudProvider;
    }
}
