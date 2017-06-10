package com.bnade.wow.util;

import java.io.File;
import java.util.ResourceBundle;

public class ConfigUtils {

	private static ResourceBundle bundle = null;

	private static String BUNDLENAME = "bnade"; // 配置文件为classpath下的bnade.properties

	private static ResourceBundle getResourceBundle() {
		if (bundle == null) {
			// 如果在classpath目录中存在dev目录，则使用dev中的配置文件
			if (new File(ConfigUtils.class.getClassLoader().getResource("").getFile() + "dev").exists()) {
				bundle = ResourceBundle.getBundle("dev/" + BUNDLENAME);
			} else {
				bundle = ResourceBundle.getBundle(BUNDLENAME);
			}
		}
		return bundle;
	}

	public static String getProperty(String name, String defaultValue) {
		if (name == null)
			return null;
		String value = null;
		try {
			value = getResourceBundle().getString(name).trim();
			if (value != null && value.length() == 0) {
				value = defaultValue;
			}
		} catch (Throwable ex) {
			value = defaultValue;
			System.out.println("Key: " + name
					+ " not found, set to default value: " + defaultValue);
		}
		return value;
	}

	public static String getProperty(String name) {
		return getProperty(name, null);
	}

	public static void main(String[] args) {
		
		System.out.println(ConfigUtils.class.getClassLoader().getResource("").getFile());
		System.out.println(new File(ConfigUtils.class.getClassLoader().getResource("").getFile() + "dev").exists());
		System.out.println(ConfigUtils.getProperty("email_from"));
	}
}
