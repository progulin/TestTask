package com.splat.task.test;

import com.splat.task.statistic.AccountServiceStatistic;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestStatistic {
    private final static int THREAD_COUNT = 10;
    private final static long ITERATE_COUNT = 1000000;

    private void iterate(AccountServiceStatistic statistic) {
        for (int i = 0; i < ITERATE_COUNT; i++) {
            statistic.calcQuery(System.currentTimeMillis());
        }
    }

    @Test
    public void testConcurrency() throws InterruptedException {
        AccountServiceStatistic statistic = new AccountServiceStatistic(1000);
        ExecutorService exec = Executors.newFixedThreadPool(THREAD_COUNT);

        for(int cnt = 0 ; cnt < THREAD_COUNT; cnt++) {
            exec.execute(() -> iterate(statistic));
        }
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);

        Assert.assertEquals(ITERATE_COUNT * THREAD_COUNT, statistic.getCounter());
    }

    @Test
    public void testLeave() {
        AccountServiceStatistic statistic = new AccountServiceStatistic(1000);

        statistic.calcQuery(100001);
        statistic.calcQuery(100002);
        statistic.calcQuery(100003);

        Assert.assertEquals(3,  statistic.getRate(100003));
        Assert.assertEquals(3,  statistic.getRate(100009));
        Assert.assertEquals(2,  statistic.getRate(101001));
        Assert.assertEquals(1,  statistic.getRate(101002));
        Assert.assertEquals(0,  statistic.getRate(101003));
    }

}
