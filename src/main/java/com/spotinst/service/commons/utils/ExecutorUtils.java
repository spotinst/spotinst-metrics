package com.spotinst.service.commons.utils;

import com.spotinst.service.SampleAppContext;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {
    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorUtils.class);
    //endregion

    //region Public Methods
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        shutdownAndAwaitTermination(pool, Long.MAX_VALUE);
    }

    public static void shutdownAndAwaitTermination(ExecutorService pool, Long waitTimeSeconds) {
        pool.shutdown();

        try {
            Date startingTime = SampleAppContext.getInstance().getSystemClock().getCurrentTime();
            pool.awaitTermination(waitTimeSeconds, TimeUnit.SECONDS);
            Date now = SampleAppContext.getInstance().getSystemClock().getCurrentTime();

            Duration executorDuration = new Duration(startingTime.getTime(), now.getTime());

            LOGGER.info(String.format("ExecutorService %s took %s seconds to finish", pool.getClass().getSimpleName(),
                                      executorDuration.getStandardSeconds()));
        }
        catch (InterruptedException ex) {
            pool.shutdownNow();
            LOGGER.error(String.format("Exception while awaiting for termination. Exception: %s", ex));
        }
    }
    //endregion
}
