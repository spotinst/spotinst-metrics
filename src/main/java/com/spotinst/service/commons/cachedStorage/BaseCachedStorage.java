package com.spotinst.service.commons.cachedStorage;

import com.spotinst.dropwizard.bl.repo.RepoGenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nir Cohen
 * @since 09/01/2020
 */
public abstract class BaseCachedStorage<T, F> {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCachedStorage.class);

    private   String  entityName;
    private   F       filterToUse;
    protected Boolean initialized;
    //endregion


    //region Constructor
    public BaseCachedStorage(String entityName, F filterToUse) {
        this.entityName = entityName;
        this.filterToUse = filterToUse;
        this.initialized = false;
    }
    //endregion

    //region Public Methods
    public Boolean refresh() {
        LOGGER.info(String.format("Refreshing cached storage for type %s", this.getClass().getSimpleName()));
        RepoGenericResponse<List<T>> entitiesListResponse = fetchAll(filterToUse);

        if (entitiesListResponse.isRequestSucceed()) {
            List<T> entities = entitiesListResponse.getValue();
            buildMap(entities);
            initialized = true;
            LOGGER.info(
                    String.format("Finished refreshing cached storage for type %s", this.getClass().getSimpleName()));
        }
        else {
            LOGGER.error(String.format("Failed to fetch %s entities from the repo. Errors: %s", entityName,
                                       entitiesListResponse.getDalErrors()));
        }

        return true;
    }

    public abstract RepoGenericResponse<List<T>> fetchAll(F filterToUse);

    public abstract void buildMap(List<T> entities);

    public List<T> getAll() {
        if (initialized == false) {
            refresh();
        }

        List<T> entities = getAllEntities();
        List<T> retVal   = new ArrayList<>(entities);

        return retVal;
    }
    //endregion

    //region Protected Methods
    protected abstract List<T> getAllEntities();
    //endregion
}
