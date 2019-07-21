package com.splat.task;

import com.splat.task.commons.Settings;
import com.splat.task.service.DbPersistInterface;
import com.splat.task.service.DbPersistServiceFactoryInterface;
import com.splat.task.statistic.AccountServiceStatistic;
import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public final class AccountServiceImpl implements AccountService {
    private final static Logger log = Logger.getLogger(AccountServiceImpl.class);

    private final ConcurrentHashMap<Integer, LongAdder> cache = new ConcurrentHashMap<>();
    private final AccountServiceStatistic statistic;
    private final ThreadLocal<DbPersistInterface> dbPersistServiceThreadLocal;

    public AccountServiceImpl(DbPersistServiceFactoryInterface factory) {
        dbPersistServiceThreadLocal = ThreadLocal.withInitial(factory::createService);
        statistic = new AccountServiceStatistic(Settings.getInstance().RATE_TIME_MS.value());
    }

    public Long getAmount(Integer id) {
        statistic.calcQuery(System.currentTimeMillis());
        LongAdder field = cache.get(id);
        if (field == null) {
            return 0L;
        }
        return field.sum();
    }

    public void addAmount(Integer id, Long value) {
        statistic.calcQuery(System.currentTimeMillis());
        LongAdder field = cache.get(id);
        if (field == null) {
            LongAdder newField = new LongAdder();
            LongAdder prevField = cache.putIfAbsent(id, newField);
            if (prevField != null) {
                field = prevField;
            } else {
                field = newField;
            }
        }
        if (Settings.getInstance().USE_WRITE_CACHE.value()) {
            field.add(value);
        } else {
            synchronized (field) {
                dbPersistServiceThreadLocal.get().persistAccount(id, field.sum() + value);
                field.add(value);
            }
        }
    }

    public Enumeration<Integer> getCachedIds() {
        return cache.keys();
    }

    public void restoreCache(Map<Integer, AtomicLong> data) {
        for (Integer key : data.keySet()) {
            LongAdder adder = new LongAdder();
            adder.add(data.get(key).get());
            cache.put(key, adder);
        }
    }

    public AccountServiceStatistic getStatistic() {
        return statistic;
    }

    public LongAdder getCacheValue(Integer id) {
        return cache.get(id);
    }
}
