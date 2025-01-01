package com.spotinst.metrics.bl.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlDimensionsValuesRequest {
    private String       accountId;
    private String       namespace;
    private List<String> dimensionKeys;

    public BlDimensionsValuesRequest() {}
}
