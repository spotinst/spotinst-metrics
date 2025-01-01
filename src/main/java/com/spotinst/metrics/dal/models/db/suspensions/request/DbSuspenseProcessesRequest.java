package com.spotinst.metrics.dal.models.db.suspensions.request;

import com.spotinst.metrics.dal.models.db.suspensions.DbProcessSuspension;

import java.util.List;

/**
 * Created by ohadmuchnik on 14/06/2016.
 */
public class DbSuspenseProcessesRequest {

    private List<DbProcessSuspension> processes;

    public List<DbProcessSuspension> getProcesses() {
        return processes;
    }

    public void setProcesses(List<DbProcessSuspension> processes) {
        this.processes = processes;
    }
}
