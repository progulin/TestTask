package com.splat.task.service;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface DbPersistInterface {
    boolean persistAccount(int id, long value);

    void submitAccount(Integer key, long value) throws SQLException;

    void clearBatch() throws SQLException;

    void executeBatch() throws SQLException;

    Map<Integer, AtomicLong> getWholeDb() throws SQLException;
}
