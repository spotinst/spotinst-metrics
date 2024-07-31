package com.spotinst.service.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by dvirkatz on 24/11/2016.
 */
public class RandomUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomUtils.class);

    /**
     * @param coll          - that's the input, some collection of elements.
     * @param numOfElements - the amount of elements you want to receive - NOTE: you will get about this number back,
     *                      this is can be helpful in case the list is very large and you dont need exact number.
     * @return a random list of about numOfElements
     */
    public static <E> List<E> randomSelectRoughN(Collection<E> coll, Integer numOfElements) {

        List<E> returnedList =
                coll.stream().filter(randomPredicate(coll.size(), numOfElements)).collect(Collectors.toList());
        return returnedList;
    }

    /**
     * @param coll          - that's the input, some collection of elements.
     * @param numOfElements - the amount of elements you want to receive
     * @return a random list of about numOfElements
     */
    public static <E> List<E> randomSelectExactN(Collection<E> coll, Integer numOfElements) {
        List<E> returnedList =
                coll.stream().filter(new Selector(coll.size(), numOfElements)).collect(Collectors.toList());
        return returnedList;
    }

    /**
     * Returns a predicate that evaluates to true with a probability
     * of toChoose/total.
     */
    private static Predicate<Object> randomPredicate(Integer total, Integer toChoose) {
        Random random = new Random();
        return obj -> random.nextInt(total) < toChoose;
    }

    /**
     * A stateful predicate that, given a total number
     * of items and the number to choose, will return 'true'
     * the chosen number of times distributed randomly
     * across the total number of calls to its test() method.
     */
    private static class Selector implements Predicate<Object> {
        private   Integer total;  // total number items remaining
        protected Integer remain; // number of items remaining to select
        protected Random  random = new Random();

        Selector(Integer total, Integer remain) {
            this.total = total;
            this.remain = remain;
        }

        @Override
        public synchronized boolean test(Object obj) {
            Boolean rc;
            if (total < 0) {
                System.out.println(("Error while sampling, total < 0"));
                rc = false;
            }
            else {
                if (random.nextInt(total--) < remain) {
                    remain--;
                    rc = true;
                }
                else {
                    rc = false;
                }

            }
            return rc;
        }
    }
}
