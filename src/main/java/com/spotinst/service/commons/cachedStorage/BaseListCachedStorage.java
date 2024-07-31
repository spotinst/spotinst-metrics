package com.spotinst.service.commons.cachedStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Nir Cohen
 * @since 09/01/2020
 */
public abstract class BaseListCachedStorage<ID, ID2, T, F> extends BaseCachedStorage<T, F> {

    //region Members
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseListCachedStorage.class);

    protected Map<ID, List<T>> entitiesMap;
    //endregion

    //region Constructor
    public BaseListCachedStorage(String entityName, F filterToUse) {
        super(entityName, filterToUse);
    }
    //endregion

    //region Public Methods
    public List<T> getEntities(@NotNull ID id) {
        List<T> retVal;

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

    public T getEntity(@NotNull ID id, @NotNull ID2 id2) {
        T retVal;

        List<T> entities = entitiesMap.get(id);

        if (entities == null) {
            retVal = refreshAndGetEntityIfEntitiesExists(id, id2);
        }
        else {
            retVal = entities.stream().filter(e -> Objects.equals(getId2FromEntity(e), id2)).findFirst().orElse(null);

            if (retVal == null) {
                retVal = refreshAndGetEntityIfEntitiesExists(id, id2);
            }
        }

        return retVal;
    }

    private T refreshAndGetEntityIfEntitiesExists(@NotNull ID id, @NotNull ID2 id2) {
        T retVal = null;

        refresh();
        List<T> entities = entitiesMap.get(id);

        if (entities != null) {
            retVal = entities.stream().filter(e -> Objects.equals(getId2FromEntity(e), id2)).findFirst().orElse(null);
        }

        return retVal;
    }

    //endregion

    //region Override Methods

    @Override
    public void buildMap(List<T> entities) {
        entitiesMap = entities.stream().collect(Collectors.groupingBy(this::getIdFromEntity));
    }


    @Override
    protected List<T> getAllEntities() {
        return entitiesMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
    //endregion

    //region Abstract Methods
    public abstract ID getIdFromEntity(T entity);

    protected abstract ID2 getId2FromEntity(T entity);

    //endregion
}
