package com.spotinst.service.bl.cmds.dummy;

import com.spotinst.commons.errors.dal.DalError;
import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.bl.BlException;
import com.spotinst.service.bl.cmds.common.BaseCmd;
import com.spotinst.service.bl.errors.ErrorCodes;
import com.spotinst.service.bl.model.dummy.BlDummy;
import com.spotinst.service.bl.repos.SampleRepoManager;
import com.spotinst.service.dal.models.db.dummy.DummyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zachi.nachshon on 1/15/17.
 */
public class GetAllDummiesCmd extends BaseCmd<List<BlDummy>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAllDummiesCmd.class);

    public GetAllDummiesCmd() {
        super();
    }

    public void execute(String accountId, DummyFilter filter) {
        LOGGER.info(String.format("Start fetching all dummies by filter under account [%s]...", accountId));

        RepoGenericResponse<List<BlDummy>> response = SampleRepoManager.Dummy.getAll(filter);
        // We can use the generic repository as well
        // RepoGenericResponse<List<BlDummy>> response = SampleRepoManager.DummyRepo.getAll(filter);
        if (response.isRequestSucceed() == false) {
            String         errFormat = "Failed fetching dummies by filter under account [%s]";
            String         errMsg    = String.format(errFormat, accountId);
            List<DalError> dalErrors = response.getDalErrors();
            LOGGER.error(errMsg, dalErrors);
            throw new BlException(ErrorCodes.FAILED_TO_GET_DUMMY, "Failed to get dummies", dalErrors);
        }

        LOGGER.info(String.format("Finished fetching all dummies by filter under account [%s]", accountId));

        this.setResult(response.getValue());
    }
}
