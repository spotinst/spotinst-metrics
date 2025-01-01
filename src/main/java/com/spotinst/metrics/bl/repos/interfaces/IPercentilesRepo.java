package com.spotinst.metrics.bl.repos.interfaces;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.metrics.bl.model.request.BlPercentilesRequest;
import com.spotinst.metrics.bl.model.response.BlPercentilesResponse;

public interface IPercentilesRepo {
    RepoGenericResponse<BlPercentilesResponse> getPercentiles(BlPercentilesRequest request,
                                                                    String index);
}
