package com.spotinst.metrics.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by zachi.nachshon on 1/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ApiDimensionsValuesRequest {

    @NotNull
    private String namespace;

    @Size(min = 1)
    private List<String> dimensionKeys;

    public ApiDimensionsValuesRequest() {
    }
}
