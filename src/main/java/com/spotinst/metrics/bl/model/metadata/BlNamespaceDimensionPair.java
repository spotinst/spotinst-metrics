package com.spotinst.metrics.bl.model.metadata;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlNamespaceDimensionPair {
    private String namespace;
    private String dimension;

    public BlNamespaceDimensionPair() {}

    public BlNamespaceDimensionPair(String namespace, String dimension) {
        this.namespace = namespace;
        this.dimension = dimension;
    }

    public BlNamespaceDimensionPair(BlNamespaceDimensionPair pair) {
        this.namespace = pair.namespace;
        this.dimension = pair.dimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlNamespaceDimensionPair that = (BlNamespaceDimensionPair) o;

        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) {
            return false;
        }
        return dimension != null ? dimension.equals(that.dimension) : that.dimension == null;
    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + (dimension != null ? dimension.hashCode() : 0);
        return result;
    }
}