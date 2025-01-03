package com.spotinst.service.bl.cmds.lookups;

import com.spotinst.commons.cmd.BaseCmd;
import com.spotinst.service.bl.model.BlOrganization;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.service.SampleAppContext;
import com.spotinst.service.bl.repos.SampleRepoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by aharontwizer on 4/12/16.
 */
public class LoadOrganizationsCmd extends BaseCmd<Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadOrganizationsCmd.class);

    public void execute() {
        LOGGER.info("Loading Organizations...");

        Boolean                                   retVal                = false;
        RepoGenericResponse<List<BlOrganization>> organizationsResponse = SampleRepoManager.Organization.getAll(null);

        if (organizationsResponse.isRequestSucceed() && organizationsResponse.getValue().size() > 0) {

            int    organizationsAmount = organizationsResponse.getValue().size();
            String format              = "Loaded [%s] organizations from database into application cache";
            String msg                 = String.format(format, organizationsAmount);
            LOGGER.info(msg);

            Map<BigInteger, BlOrganization> organizationMap = organizationsResponse.getValue().stream().collect(
                    Collectors.toMap(BlOrganization::getId, Function.identity()));
            SampleAppContext.getInstance().setOrganizations(organizationMap);
            retVal = true;
        }

        setResult(retVal);
    }
}
