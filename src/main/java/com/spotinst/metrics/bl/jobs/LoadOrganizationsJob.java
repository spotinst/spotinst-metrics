package com.spotinst.metrics.bl.jobs;

import com.spotinst.dropwizard.common.scheduling.OnLocalOnly;
import com.spotinst.metrics.bl.cmds.lookups.LoadOrganizationsCmd;
import io.dropwizard.jobs.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnLocalOnly(jobName = "LoadOrganizationsJob", value = "0 0 */1 * * ?")
public class LoadOrganizationsJob extends Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadOrganizationsJob.class);

    @Override
    public void doJob(JobExecutionContext context) throws JobExecutionException {

        LOGGER.info("****** Starting LoadOrganizationsJob Job *********");

        try {
            LoadOrganizationsCmd cmd = new LoadOrganizationsCmd();
            cmd.execute();
        } catch (Exception ex) {
            LOGGER.error("Failed at LoadOrganizationsJob", ex);
        }

        LOGGER.info("****** Finished LoadOrganizationsJob Job *********");
    }
}
