package com.spotinst.service.bl.model.common;

/**
 * @author Stas Radchenko
 * @since 2019-08-07
 */
public class BlSqlDBJavaGroupMapping {
    //region Members
    private String id;
    private String sqlName;
    private String dbJavaGroupName;
    //endregion

    //region Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public String getDbJavaGroupName() {
        return dbJavaGroupName;
    }

    public void setDbJavaGroupName(String dbJavaGroupName) {
        this.dbJavaGroupName = dbJavaGroupName;
    }
    //endregion

}
