package com.splat.task;

import com.splat.task.commons.Constants;
import com.splat.task.server.AccountServer;
import com.splat.task.service.DbPersistServiceFactory;
import com.splat.task.service.DbPersistServiceFactoryInterface;
import com.splat.task.service.DbRefreshService;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.Desktop;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestServer {
    private final static Logger log = Logger.getLogger(TestServer.class);

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender());
        Logger.getRootLogger().setLevel(Level.ALL);
        BasicConfigurator.configure();

        try {
            org.h2.tools.Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();
            org.h2.tools.Server.createWebServer("-web", "-webDaemon", "-webAllowOthers", "-webPort", "8082").start();
            initDatabase();
            startBrowse();
        } catch (Exception ex) {
            log.error("Can not init database", ex);
            System.exit(1);
        }

        DbPersistServiceFactoryInterface factory = new DbPersistServiceFactory();
        AccountServiceImpl service = new AccountServiceImpl(factory);

        DbRefreshService dbRefreshService = new DbRefreshService(service, factory.createService());
        service.restoreCache(dbRefreshService.getDiffCache());
        dbRefreshService.start();

        AccountServer server = new AccountServer(service);
        server.start();
    }

    private static void startBrowse() {
        try {
            Desktop.getDesktop().browse(URI.create("http://localhost:8082/"));
        } catch (Exception ex) {
            log.error("Can not open url", ex);
        }
    }

    private static void initDatabase() throws SQLException {
        log.info("Init database....");
        Connection connection = DriverManager.getConnection(Constants.DATABASE_UTL);
        Statement st = connection.createStatement();
        st.executeUpdate(Constants.CREATE_TABLE_QUERY);
    }

}
