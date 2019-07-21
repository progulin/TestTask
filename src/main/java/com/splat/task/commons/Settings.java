package com.splat.task.commons;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.Properties;

public class Settings {
    private final static Logger log = Logger.getLogger(Settings.class);

    private static Settings instance = null;

    public final Setting<Integer> WEB_SERVER_PORT;
    public final Setting<Integer> WRITE_BATCH_SIZE;
    public final Setting<Integer> WRITE_CACHE_INTERVAL;
    public final Setting<Integer> RATE_TIME_MS;
    public final Setting<Boolean> USE_WRITE_CACHE;

    public class Setting<T> {
        private T value;

        private Setting(String value, T defaultValue, Class<T> clazz) {
            this.value = parseValue(value, defaultValue, clazz);
        }

        public T value() {
            return value;
        }

        public void value(T value) {
            log.info("Change settings from " + this.value + " to " + value);
            this.value = value;
        }

        private T parseValue(String value, T defaultValue, Class<T> clazz) {
            try {
                if (clazz == Boolean.class)
                    return clazz.cast(Boolean.parseBoolean(value));
                if (clazz == Integer.class)
                    return clazz.cast(Integer.parseInt(value));
                if (clazz == String.class)
                    return clazz.cast(value);
            } catch (Exception ex) {
                System.out.println("Can not parse value: " + value + " of type " + clazz.getName() + " using default value " + defaultValue);
            }
            return defaultValue;
        }
    }

    public Settings(Properties config) {
        WEB_SERVER_PORT = new Setting(config.getProperty("web_server_port"), Constants.WEB_SERVER_PORT, Integer.class);
        WRITE_BATCH_SIZE = new Setting(config.getProperty("write_batch_size"), Constants.WRITE_BATCH_SIZE, Integer.class);
        WRITE_CACHE_INTERVAL = new Setting(config.getProperty("write_cache_interval"), Constants.WRITE_CACHE_INTERVAL, Integer.class);
        USE_WRITE_CACHE = new Setting(config.getProperty("use_write_cache"), Boolean.FALSE, Boolean.class);
        RATE_TIME_MS = new Setting(config.getProperty("rate_time_ms"), Constants.RATE_TIME_MS, Integer.class);
    }

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings(readConfigFile());
        }
        return instance;
    }

    private static Properties readConfigFile() {
        Properties config = new Properties();
        try (FileInputStream f = new FileInputStream(System.getProperty("CONFIG", "config.properties"))) {
            config.load(f);
        } catch (Exception ex) {
            Logger.getRootLogger().error("Can not read config file", ex);
        }
        return config;
    }

}
