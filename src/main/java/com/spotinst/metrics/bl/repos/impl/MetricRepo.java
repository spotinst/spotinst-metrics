package com.spotinst.metrics.bl.repos.impl;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import com.spotinst.dropwizard.common.exceptions.ExceptionHelper;
import com.spotinst.dropwizard.common.exceptions.dal.DalException;
import com.spotinst.metrics.bl.model.BlMetricCreateRequest;
import com.spotinst.metrics.bl.repos.interfaces.IMetricRepo;
import com.spotinst.metrics.commons.converters.MetricConverter;
import com.spotinst.metrics.dal.dao.elastic.MetricDAO;
import com.spotinst.metrics.dal.models.elastic.ElasticMetricDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tal.Geva
 * @since 07/08/2024
 */
public class MetricRepo implements IMetricRepo {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricRepo.class);
    //endregion

    //region Override Methods
    @Override
    public RepoGenericResponse<Boolean> create(BlMetricCreateRequest request) {
        RepoGenericResponse<Boolean> retVal;

        List<ElasticMetricDocument> dalMetrics =
                request.getMetricDocuments().stream().map(MetricConverter::blToDal).collect(Collectors.toList());
        try {
            MetricDAO dao       = new MetricDAO();
            Boolean   isCreated = dao.create(dalMetrics);

            retVal = new RepoGenericResponse<>(isCreated);
        }
        catch (IOException ex) {
            LOGGER.error("Failed to create metrics in ES. Error: {}", ex.getMessage());

            DalException dalException = new DalException(ex.getMessage(), ex.getCause());
            retVal = ExceptionHelper.handleDalException(dalException);
        }

        return retVal;
    }
}
