package com.spotinst.metrics.bl.repos.interfaces;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.metrics.bl.model.BlMetricCreateRequest;

public interface IMetricRepo {
    RepoGenericResponse<Boolean> create(BlMetricCreateRequest request);
}
