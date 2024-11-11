package com.spotinst.metrics.api.resources.tasks;

import com.spotinst.metrics.bl.cmds.lookups.LoadOrganizationsCmd;
import io.dropwizard.servlets.tasks.Task;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by aharontwizer on 11/22/15.
 */
public class ReloadOrganizationsTask extends Task {

    private static final String RELOAD_ORGANIZATIONS = "reloadOrganizations";

    public ReloadOrganizationsTask() {
        super(RELOAD_ORGANIZATIONS);
    }

    @Override
    public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
        LoadOrganizationsCmd cmd = new LoadOrganizationsCmd();
        cmd.execute();
    }
}
