package com.spotinst.service.commons.cachedStorage;

import com.spotinst.service.commons.utils.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nir Cohen
 * @since 09/01/2020
 */
public abstract class BaseSingleCachedStorage<ID, T, F> extends BaseCachedStorage<T, F> {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSingleCachedStorage.class);

    protected Map<ID, T> entitiesMap;
    //endregion

    //region Constructor
    public BaseSingleCachedStorage(String entityName, F filterToUse) {
        super(entityName, filterToUse);
    }
    //endregion

    //region Public Methods
    public T getEntity(@NotNull ID id) {
        T retVal;

        if (initialized == false) {
            refresh();
        }
        else {
            retVal = entitiesMap.get(id);

            if (retVal == null) {
                // id was not found in the map, refresh map
                LOGGER.warn(String.format("Could not find id %s in storage, refreshing", id));
                refresh();
            }
        }

        retVal = entitiesMap.get(id);

        return retVal;
    }
    //endregion

    //region Override Methods
    @Override
    protected List<T> getAllEntities() {
        return new ArrayList<>(entitiesMap.values());
    }

    @Override
    public void buildMap(List<T> entities) {
        entitiesMap = entities.stream().filter(ListUtils.buildUniquenessPredicate(this::getIdFromEntity))
                              .collect(Collectors.toMap(this::getIdFromEntity, Function.identity()));
    }
    //endregion

    //region Abstract Methods
    public abstract ID getIdFromEntity(T entity);
    //endregion
}
