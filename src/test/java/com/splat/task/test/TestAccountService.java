package com.splat.task.test;

import com.splat.task.AccountServiceImpl;
import com.splat.task.service.DbPersistInterface;

import com.splat.task.service.DbPersistServiceFactoryInterface;
import org.junit.Assert;
import org.junit.Test;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import static org.mockito.Mockito.*;

public class TestAccountService {

    @Test
    public void testCalc() {
        final DbPersistServiceFactoryInterface factory = mock(DbPersistServiceFactoryInterface.class);
        final DbPersistInterface persist = mock(DbPersistInterface.class);
        when(factory.createService()).thenReturn(persist);

        final AccountServiceImpl service = new AccountServiceImpl(factory);
        service.addAmount(1, 2L);
        service.addAmount(1, 3L);
        service.addAmount(1, -1L);
        service.addAmount(2, 100L);
        service.addAmount(2, 0L);

        verify(persist, times(1)).persistAccount(1, 2);
        verify(persist, times(1)).persistAccount(1, 5);
        verify(persist, times(1)).persistAccount(1, 4);
        verify(persist, times(2)).persistAccount(2, 100);

        Assert.assertEquals(5, service.getStatistic().getCounter());
        Assert.assertEquals(100, service.getAmount(2).longValue());
        Assert.assertEquals(4, service.getAmount(1).longValue());
    }

    @Test
    public void testCacheLoad() {
        final DbPersistServiceFactoryInterface factory = mock(DbPersistServiceFactoryInterface.class);
        final DbPersistInterface persist = mock(DbPersistInterface.class);
        when(factory.createService()).thenReturn(persist);

        Map<Integer, AtomicLong> data = new HashMap();
        data.put(100, new AtomicLong(1000));
        data.put(1, new AtomicLong(1500));
        data.put(123, new AtomicLong(1));

        final AccountServiceImpl service = new AccountServiceImpl(factory);
        service.restoreCache(data);

        service.addAmount(100, 1L);
        service.addAmount(1, 1L);
        service.addAmount(123, 1L);

        verify(persist, times(1)).persistAccount(100, 1001);
        verify(persist, times(1)).persistAccount(1, 1501);
        verify(persist, times(1)).persistAccount(123, 2);

        Assert.assertEquals(3, service.getStatistic().getCounter());
        Assert.assertEquals(3, getEnumerationSize(service.getCachedIds()));
    }

    private long getEnumerationSize(Enumeration enumeration) {
        long counter = 0;
        while(enumeration.hasMoreElements()) {
            counter++;
            enumeration.nextElement();
        }
        return counter;
    }

}
