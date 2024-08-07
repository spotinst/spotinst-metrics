package com.spotinst.metrics.bl.model;

public class BlMetric {
    private String name;
    private Double value;
    private String unit;
    private Long   count;

    public BlMetric() {
    }

    public BlMetric(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    public BlMetric(String name, Double value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public BlMetric(String name, Double value, String unit, Long count) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.count = count;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    //    @Override
    //    public boolean equals(Object o) {
    //        if (this == o) {
    //            return true;
    //        }
    //        if (o == null || getClass() != o.getClass()) {
    //            return false;
    //        }
    //
    //        BlMetric blMetric = (BlMetric) o;
    //
    //        if (name != null ? !name.equals(blMetric.name) : blMetric.name != null) {
    //            return false;
    //        }
    //        if (value != null ? !value.equals(blMetric.value) : blMetric.value != null) {
    //            return false;
    //        }
    //        return unit != null ? unit.equals(blMetric.unit) : blMetric.unit == null;
    //    }

    //    @Override
    //    public int hashCode() {
    //        int result = name != null ? name.hashCode() : 0;
    //        result = 31 * result + (value != null ? value.hashCode() : 0);
    //        result = 31 * result + (unit != null ? unit.hashCode() : 0);
    //        return result;
    //    }
}
