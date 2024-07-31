package com.spotinst.service.bl.cmds.dummy;

import com.spotinst.commons.cmd.BaseCmd;
import com.spotinst.commons.errors.dal.DalError;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.service.bl.errors.ErrorCodes;
import com.spotinst.service.bl.model.dummy.BlDummy;
import com.spotinst.service.bl.repos.SampleRepoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
public class GetDummyCmd extends BaseCmd<BlDummy> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetDummyCmd.class);

    public GetDummyCmd() {
        super(null, null);
    }

    public void execute(String dummyId) {
        LOGGER.info(String.format("Start fetching dummy [%s]...", dummyId));

        RepoGenericResponse<BlDummy> response = SampleRepoManager.Dummy.get(dummyId);
        // We can use the generic repository as well
        // RepoGenericResponse<BlDummy> response = SampleRepoManager.DummyRepo.get(dummyId);
        if (response.isRequestSucceed() == false) {
            String         errFormat = "Failed fetching dummy [%s]";
            String         errMsg    = String.format(errFormat, dummyId);
            List<DalError> dalErrors = response.getDalErrors();
            LOGGER.error(errMsg, dalErrors);
            throw new BlException(ErrorCodes.FAILED_TO_GET_DUMMY, "Failed to get dummy", dalErrors);
        }

        LOGGER.info(String.format("Finished fetching dummy [%s]", dummyId));

        this.setResult(response.getValue());
    }
}
