package com.spotinst.service.commons.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by aharontwizer on 8/9/15.
 */
public class ListUtils {

    public static <T> Predicate<T> buildUniquenessPredicate(Function<? super T, Object> uniqueProperty) {
        Predicate<T> retVal;
        Set<Object>  existenceByObj = ConcurrentHashMap.newKeySet();
        retVal = t -> {
            boolean result;
            Object  key = uniqueProperty.apply(t);
            result = existenceByObj.add(key);
            return result;
        };
        return retVal;
    }

    public static <T> Map<Integer, List<T>> breakToChunksByChunkSizePercentage(List<T> sourceList,
                                                                               int chunkSizePercentage) {
        //TODO: write unit tests

        Map<Integer, List<T>> result = null;

        if (sourceList != null) {
            Double chunkSize   = Math.ceil((sourceList.size() * chunkSizePercentage) / 100f);
            Double numOfChunks = Math.ceil(sourceList.size() / chunkSize);

            result = new HashMap<>();

            for (int i = 0, j = 0; i < sourceList.size(); i++, j++) {
                List<T> currentChunkItems;
                T       sourceItem  = sourceList.get(i);
                int     chunkNumber = (j % numOfChunks.intValue()) + 1;

                if (result.containsKey(chunkNumber) == false) {
                    result.put(chunkNumber, new LinkedList<>());
                }

                currentChunkItems = result.get(chunkNumber);
                currentChunkItems.add(sourceItem);
            }
        }

        return result;
    }

    public static <T> Map<Integer, List<T>> breakToChunksByMaxChunkSize(List<T> sourceList, int maxChunkSize) {
        //TODO: write unit tests

        Map<Integer, List<T>> result = null;

        if (sourceList != null) {
            result = new HashMap<>();

            for (int currentSourceItemIndex = 0, currentChunk = 1;
                 currentSourceItemIndex < sourceList.size(); currentSourceItemIndex++) {
                List<T> currentChunkItems;
                T       sourceItem = sourceList.get(currentSourceItemIndex);

                if (result.containsKey(currentChunk) == false) {
                    result.put(currentChunk, new LinkedList<>());
                }

                currentChunkItems = result.get(currentChunk);
                currentChunkItems.add(sourceItem);

                if (currentChunkItems.size() == maxChunkSize) {
                    currentChunk++;
                }
            }
        }

        return result;
    }

    //region randomShuffle

    /**
     * randomShuffleNelements receives a list<T> an Integer 'finalNumber' stating what is the sub list we desire and a boolean.
     * it returns a sublist of 'finalNumber' random elements
     * the bollean returnExactNumber will choose the filter we use, for performance we can use it as false
     * as long we do not need an exact number.
     */
    public static <T> List<T> randomShuffleNelements(List<T> sourceList, Integer finalNumber,
                                                     Boolean returnExactNumber) {
        //TODO: write unit tests
        List<T> newList;
        if (returnExactNumber == true) {
            // new list will have exactly 'finalNumber' of elements in it.
            newList = RandomUtils.randomSelectExactN(sourceList, finalNumber);
        }
        else {
            // new list will have roughly 'finalNumber' of elements in it => can be MORE or LESS.
            newList = RandomUtils.randomSelectRoughN(sourceList, finalNumber);
        }
        return newList;
    }

    //end region
}
