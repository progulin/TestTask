package com.splat.task.statistic;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class AccountServiceStatistic {
    private final static Logger log = Logger.getLogger(AccountServiceStatistic.class);

    private AtomicLong counter = new AtomicLong(0);
    private volatile long rate = 0;
    private final long[] buckets;
    private final long timeMs;
    private volatile long lastTimestamp = 0;

    public AccountServiceStatistic(int timeMs) {
        buckets = new long[timeMs];
        this.timeMs = timeMs;
    }

    public synchronized void calcQuery(long timestamp) {
        rollUp(timestamp);
        buckets[(int) (timestamp % timeMs)]++;

        rate++;
        counter.incrementAndGet();
    }

    private void rollUp(long timestamp) {
        if (timestamp < lastTimestamp) {
            return;
        }
        long sum = 0;
        if (lastTimestamp != 0) {
            int counter = 0;
            for (long i = (lastTimestamp - timeMs) + 1; i < (timestamp - timeMs) + 1; i++) {
                if (counter > timeMs) break;
                long r = buckets[(int) (i % timeMs)];
                buckets[(int) (i % timeMs)] = 0;
                sum += r;
                counter++;
            }
            rate -= sum;
        }
        lastTimestamp = timestamp;
    }

    public synchronized long getRate(long timestamp) {
        rollUp(timestamp);
        return rate;
    }

    public long getCounter() {
        return counter.get();
    }

    public synchronized void clear() {
        counter.set(0);
        Arrays.fill(buckets, 0);
        rate = 0;
        lastTimestamp = System.currentTimeMillis();
    }

}
