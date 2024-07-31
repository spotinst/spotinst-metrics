package com.spotinst.service.bl.cmds.dummy;

import com.spotinst.commons.cmd.BaseCmd;
import com.spotinst.commons.errors.dal.DalError;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.service.bl.errors.ErrorCodes;
import com.spotinst.service.bl.repos.SampleRepoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zachi.nachshon on 4/6/17.
 */
public class DeleteDummyCmd extends BaseCmd<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDummyCmd.class);

    public DeleteDummyCmd() {
        super(null, null);
    }

    public void execute(String dummyId) {
        LOGGER.info(String.format("Start deleting dummy [%s]...", dummyId));

        RepoGenericResponse<Boolean> response = SampleRepoManager.Dummy.delete(dummyId);
        // We can use the generic repository as well
        // RepoGenericResponse<Boolean> response = SampleRepoManager.DummyRepo.delete(dummyId);
        if (response.isRequestSucceed() == false) {
            String         errFormat = "Failed deleting dummy [%s]";
            String         errMsg    = String.format(errFormat, dummyId);
            List<DalError> dalErrors = response.getDalErrors();
            LOGGER.error(errMsg, dalErrors);
            throw new BlException(ErrorCodes.FAILED_TO_DELETE_DUMMY, "Failed to delete dummy", dalErrors);
        }

        LOGGER.info(String.format("Finished deleting dummy [%s]", dummyId));

        this.setResult(response.getValue());
    }
}
