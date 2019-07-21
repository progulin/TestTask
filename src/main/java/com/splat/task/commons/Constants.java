package com.splat.task.commons;

public interface Constants {
    String DATABASE_UTL = "jdbc:h2:file:~/task";
    String MERGE_QUERY = "MERGE INTO account KEY(id) VALUES(?, ?)";
    String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS account(id INT PRIMARY KEY, amount BIGINT(0))";
    String FETCH_QUERY = "SELECT id, amount FROM ACCOUNT";
    int WEB_SERVER_PORT = 8080;
    int WRITE_BATCH_SIZE = 10;
    int WRITE_CACHE_INTERVAL = 1000;
    int RATE_TIME_MS = 1000;
}
