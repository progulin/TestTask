package com.splat.task.test;

import com.splat.task.AccountServiceImpl;
import com.splat.task.commons.Settings;
import com.splat.task.service.DbPersistInterface;
import com.splat.task.service.DbPersistServiceFactoryInterface;
import com.splat.task.service.DbRefreshService;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.*;

public class TestDbRefreshService {

    private void sleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ex) {
            Assert.fail("Unexpected exception");
        }
    }

    @Test
    public void testWithCache() throws SQLException {
        Settings.getInstance().USE_WRITE_CACHE.value(true);
        Settings.getInstance().WRITE_CACHE_INTERVAL.value(100);

        final Map<Integer, AtomicLong> data = new HashMap();
        data.put(100, new AtomicLong(1000));
        data.put(101, new AtomicLong(1500));
        data.put(102, new AtomicLong(1));

        DbPersistServiceFactoryInterface factory = mock(DbPersistServiceFactoryInterface.class);
        DbPersistInterface persist = mock(DbPersistInterface.class);
        when(factory.createService()).thenReturn(persist);
        when(persist.getWholeDb()).thenReturn(data);

        AccountServiceImpl service = new AccountServiceImpl(factory);
        DbRefreshService refresher = new DbRefreshService(service, persist);

        service.restoreCache(refresher.getDiffCache());
        refresher.start();
        sleep(1000);

        try {
            verify(persist, never()).clearBatch();
            verify(persist, never()).executeBatch();

            service.addAmount(100, 50L);
            service.addAmount(101, 50L);
            sleep(200);

            verify(persist, times(1)).submitAccount(100, 1050);
            verify(persist, times(1)).submitAccount(101, 1550);
            verify(persist, times(1)).executeBatch();
        } catch (SQLException ex) {
            Assert.fail("Not expected exception");
        }
    }

    @Test
    public void testWithoutCache() throws SQLException {
        Settings.getInstance().USE_WRITE_CACHE.value(false);

        final Map<Integer, AtomicLong> data = new HashMap();
        data.put(100, new AtomicLong(1000));
        data.put(101, new AtomicLong(1500));
        data.put(102, new AtomicLong(1));

        final DbPersistServiceFactoryInterface factory = mock(DbPersistServiceFactoryInterface.class);
        final DbPersistInterface persist = mock(DbPersistInterface.class);
        when(factory.createService()).thenReturn(persist);
        when(persist.getWholeDb()).thenReturn(data);

        final AccountServiceImpl service = new AccountServiceImpl(factory);

        service.addAmount(100, 50L);
        verify(persist, times(1)).persistAccount(100, 50);
        service.addAmount(101, 50L);
        verify(persist, times(1)).persistAccount(101, 50);
        service.addAmount(101, 50L);
        verify(persist, times(1)).persistAccount(101, 100);
    }

}
