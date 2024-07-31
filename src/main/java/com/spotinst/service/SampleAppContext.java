package com.spotinst.service;

import com.spotinst.dropwizard.common.context.BaseAppContext;
import com.spotinst.messaging.clients.consumer.IMessagingConsumer;
import com.spotinst.messaging.clients.producer.IMessagingProducer;
import com.spotinst.service.bl.model.BlOrganization;
import com.spotinst.service.bl.model.common.BlSqlDBJavaGroupMapping;
import com.spotinst.service.commons.configuration.DbServiceGroupConfig;
import com.spotinst.service.commons.configuration.SampleConfiguration;

import java.math.BigInteger;
import java.util.Map;

public class SampleAppContext extends BaseAppContext<SampleConfiguration, BlOrganization> {

    private static SampleAppContext instance;

    private SampleAppContext() {
    }

    public static SampleAppContext getInstance() {
        if (instance == null) {
            synchronized (SampleAppContext.class) {
                if (instance == null) {
                    instance = new SampleAppContext();
                }
            }
        }
        return instance;
    }

    //region Members
    private Map<BigInteger, BlOrganization>      organizations;
    private Map<String, BlSqlDBJavaGroupMapping> sqlDBJavaGroupMapping;
    private Map<String, DbServiceGroupConfig>    dbJavaGroupLbUrlMapping;
    private IMessagingProducer                   sqsProducer;
    private IMessagingProducer                   snsProducer;
    private IMessagingConsumer                   sqsConsumer;
    //endregion

    //region Getters and Setters

    @Override
    public Map<BigInteger, BlOrganization> getOrganizations() {
        return organizations;
    }

    @Override
    public void setOrganizations(Map<BigInteger, BlOrganization> organizations) {
        this.organizations = organizations;
    }

    public IMessagingProducer getSqsProducer() {
        return sqsProducer;
    }

    public void setSqsProducer(IMessagingProducer sqsProducer) {
        this.sqsProducer = sqsProducer;
    }

    public IMessagingConsumer getSqsConsumer() {
        return sqsConsumer;
    }

    public void setSqsConsumer(IMessagingConsumer sqsConsumer) {
        this.sqsConsumer = sqsConsumer;
    }

    public IMessagingProducer getSnsProducer() {
        return snsProducer;
    }

    public void setSnsProducer(IMessagingProducer snsProducer) {
        this.snsProducer = snsProducer;
    }

    public Map<String, BlSqlDBJavaGroupMapping> getSqlDBJavaGroupMapping() {
        return sqlDBJavaGroupMapping;
    }

    public void setSqlDBJavaGroupMapping(Map<String, BlSqlDBJavaGroupMapping> sqlDBJavaGroupMapping) {
        this.sqlDBJavaGroupMapping = sqlDBJavaGroupMapping;
    }

    public Map<String, DbServiceGroupConfig> getDbJavaGroupLbUrlMapping() {
        return dbJavaGroupLbUrlMapping;
    }

    public void setDbJavaGroupLbUrlMapping(Map<String, DbServiceGroupConfig> dbJavaGroupLbUrlMapping) {
        this.dbJavaGroupLbUrlMapping = dbJavaGroupLbUrlMapping;
    }
    //endregion
}
