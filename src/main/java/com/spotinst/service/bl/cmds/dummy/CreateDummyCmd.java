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
public class CreateDummyCmd extends BaseCmd<BlDummy> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDummyCmd.class);

    public CreateDummyCmd() {
        super(null, null);
    }

    public void execute(BlDummy blDummy) {
        LOGGER.info("Start creating a new dummy...");

        String id = ""; //generateId(IdPrefixTypeEnum.Dummy);
        blDummy.setId(id);

        RepoGenericResponse<BlDummy> response = SampleRepoManager.Dummy.create(blDummy);
        // We can use the generic repository as well
        // RepoGenericResponse<BlDummy> response = SampleRepoManager.DummyRepo.create(blDummy);
        if (response.isRequestSucceed() == false) {
            String         errMsg    = "Failed creating a new dummy";
            List<DalError> dalErrors = response.getDalErrors();
            LOGGER.error(errMsg, dalErrors);
            throw new BlException(ErrorCodes.FAILED_TO_CREATE_DUMMY, "Failed to create dummy", dalErrors);
        }

        LOGGER.info("Finished creating a new dummy");

        this.setResult(response.getValue());
    }
}
