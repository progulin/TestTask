package com.splat.task.service;

import com.splat.task.AccountServiceImpl;
import com.splat.task.commons.Settings;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class DbRefreshService extends Thread {
    private final static Logger log = Logger.getLogger(DbRefreshService.class);
    private final AccountServiceImpl accountService;
    private final DbPersistInterface persistService;
    private Map<Integer, AtomicLong> diffCache = new HashMap<>();
    private volatile boolean running = true;
    private int submitedQueryCounter = 0;

    public DbRefreshService(AccountServiceImpl accountService, DbPersistInterface persistService) {
        super();
        setDaemon(true);
        setName("DB persist service");

        this.persistService = persistService;
        this.accountService = accountService;
        try {
            diffCache = persistService.getWholeDb();
        } catch (SQLException ex) {
            log.error("Can not load db", ex);
        }
    }

    public Map<Integer, AtomicLong> getDiffCache() {
        return diffCache;
    }

    @Override
    public void run() {
        while (running) {
            LockSupport.parkNanos(Settings.getInstance().WRITE_CACHE_INTERVAL.value());
            try {
                Enumeration<Integer> keys = accountService.getCachedIds();

                while (keys.hasMoreElements()) {
                    Integer key = keys.nextElement();
                    long value = accountService.getCacheValue(key).longValue();
                    AtomicLong cached = diffCache.get(key);

                    if (cached == null) {
                        cached = new AtomicLong(0);
                        diffCache.put(key, cached);
                    }

                    if (cached.get() != value) {
                        cached.set(value);
                        persistService.submitAccount(key, value);
                        submitedQueryCounter++;

                        if (submitedQueryCounter == Settings.getInstance().WRITE_BATCH_SIZE.value()) {
                            persistService.executeBatch();
                            submitedQueryCounter = 0;
                        }

                    }
                }

                if (submitedQueryCounter > 0) {
                    submitedQueryCounter = 0;
                    persistService.executeBatch();
                }
            } catch (SQLException ex) {
                log.error("Can not save account value", ex);
            }
        }
    }

}
