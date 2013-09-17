package com.vng.esb.config;

import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ConfigUtils {
	private static Properties prop = new Properties();
	private static  String propFile = "config.properties";
	private static final Logger logger = LogManager.getLogger(ConfigUtils.class);
	
	public static synchronized String get(final String key) {
		return get("config.properties", key);
	}

	public static synchronized String get(final String configFile, final String key) {
		try {
			if (!propFile.equalsIgnoreCase(configFile)) {
				propFile = configFile;
				prop = new Properties();
			}

			if (propFile == null) {
				propFile = "config.properties";
			}

			if (prop == null) {
				prop = new Properties();
			}

			prop.load(ConfigUtils.class.getClassLoader().getResourceAsStream(
					propFile));
			return prop.getProperty(key);
		} catch (Exception e) {
			logger.error("Get Property error", e);
			return null;
		}
	}
}
