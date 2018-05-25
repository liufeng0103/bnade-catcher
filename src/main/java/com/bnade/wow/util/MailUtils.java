package com.bnade.wow.util;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailUtils {

	private static final Logger logger = LoggerFactory.getLogger(MailUtils.class);

	private final static String EMAIL_HOSTNAME = ConfigUtils.getProperty("email_hostname");
	private final static String EMAIL_USERNAME = ConfigUtils.getProperty("email_username");
	private final static String EMAIL_PASSWORD = ConfigUtils.getProperty("email_password");
	private final static int EMAIL_PORT = Integer.valueOf(ConfigUtils.getProperty("email_port"));
	private final static String EMAIL_NAME = ConfigUtils.getProperty("email_name");
	private final static String EMAIL_FROM = ConfigUtils.getProperty("email_from");
	private final static boolean EMAIL_IS_SSL = "true".equalsIgnoreCase(ConfigUtils.getProperty("email_is_ssl"));

	public static void sendHtmlEmail(String subject, String msg, String to) {
		try {
			HtmlEmail email = new HtmlEmail();
			email.setHostName(EMAIL_HOSTNAME);
			email.setSmtpPort(EMAIL_PORT);
			email.setAuthenticator(new DefaultAuthenticator(EMAIL_USERNAME, EMAIL_PASSWORD));
			email.setSSLOnConnect(EMAIL_IS_SSL);
			email.setFrom(EMAIL_FROM, EMAIL_NAME);
			email.setSubject(subject);
			email.setHtmlMsg(msg);
			email.setCharset("utf-8");
			email.setTextMsg("您的邮箱不支持html邮件");
			email.addTo(to);
			email.send();
			logger.info("email sent success to {}", to);
		} catch (EmailException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public static void sendSimpleEmail(String subject, String msg, String to) {
		try {
			Email email = new SimpleEmail();
			email.setHostName(EMAIL_HOSTNAME);
			email.setAuthenticator(new DefaultAuthenticator(EMAIL_USERNAME, EMAIL_PASSWORD));
			email.setSSLOnConnect(EMAIL_IS_SSL);
			email.setFrom(EMAIL_FROM, EMAIL_NAME);
			email.setSmtpPort(EMAIL_PORT);
			email.setSubject(subject);
			email.setMsg(msg);
			email.addTo(to);
			email.send();
			logger.info("email sent success to {}", to);
		} catch (EmailException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		MailUtils.sendSimpleEmail("test", "test", "liufeng0103@163.com");
//		MailUtils.sendHtmlEmail("test", "<a href='http://www.bnade.com'>test1</a>", "liufeng0103@163.com");
	}

}
