package com.splat.task.service;

import com.splat.task.commons.Constants;
import org.apache.log4j.Logger;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DbPersistServiceFactory implements DbPersistServiceFactoryInterface {
    private final static Logger log = Logger.getLogger(DbPersistServiceFactory.class);

    @Override
    public DbPersistService createService() {
        try {
            return new DbPersistService(DriverManager.getConnection(Constants.DATABASE_UTL));
        } catch (SQLException ex) {
            log.error("Can not create persist service", ex);
            System.exit(1);
        }
        return null;
    }
}
