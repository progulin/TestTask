package com.splat.task.service;

import com.splat.task.AccountServiceImpl;
import com.splat.task.commons.Constants;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DbPersistService implements DbPersistInterface {
    private final static Logger log = Logger.getLogger(AccountServiceImpl.class);

    private final Connection connection;
    private final PreparedStatement statement;

    DbPersistService(Connection connection) throws SQLException {
        this.connection = connection;
        this.statement = connection.prepareStatement(Constants.MERGE_QUERY);
    }

    @Override
    public boolean persistAccount(int id, long value) {
        try {
            statement.setInt(1, id);
            statement.setLong(2, value);
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            log.error("Can not persist value", ex);
            return false;
        }
    }

    @Override
    public void submitAccount(Integer key, long value) throws SQLException {
        statement.setInt(1, key);
        statement.setLong(2, value);
        statement.addBatch();
    }

    @Override
    public void clearBatch() throws SQLException {
        statement.clearBatch();
    }

    @Override
    public void executeBatch() throws SQLException {
        statement.executeBatch();
    }

    @Override
    public Map<Integer, AtomicLong> getWholeDb() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery(Constants.FETCH_QUERY);
        Map<Integer, AtomicLong> result = new HashMap<>();
        while (rs.next()) {
            AtomicLong value = new AtomicLong(rs.getLong(2));
            Integer id = rs.getInt(1);
            result.put(id, value);
        }
        return result;
    }
}
