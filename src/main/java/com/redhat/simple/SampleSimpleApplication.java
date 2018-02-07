/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.simple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class SampleSimpleApplication implements CommandLineRunner {

	@Value("${FTP_USER}")
	private String ftp_user;

	@Value("${FTP_PWD_MD5}")
	private String ftp_pwd;

	@Value("${FTP_HOME}")
	private String ftp_home;

	@Value("${FTP_PORT}")
	private String ftp_port;

	@Override
	public void run(String... args) {
		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();
		// set the port of the listenher
		factory.setPort(Integer.valueOf(ftp_port));
		// replace the default listener
		serverFactory.addListener("default", factory.createListener());

		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		try {
			// userManagerFactory.setFile(new File(this.getClass().getResource("/users.properties").toURI()));
			Properties props = new Properties();
			props.load(this.getClass().getResourceAsStream("/users.properties"));
			Properties parsedProps = new Properties();
			props.keySet().stream().forEach(k -> {
				String newValue = ((String) props.get(k)).replace("${FTP_PWD_MD5}", ftp_pwd);
				newValue = newValue.replace("${FTP_HOME}", ftp_home);
				parsedProps.put(((String) k).replace("${FTP_USER}", ftp_user), newValue);
			});
			File propFile = File.createTempFile("ftp-parsed-", ".props");
			parsedProps.store(new FileOutputStream(propFile), "");
			userManagerFactory.setFile(propFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		serverFactory.setUserManager(userManagerFactory.createUserManager());

		// start the server
		FtpServer server = serverFactory.createServer();
		try {
			server.start();
		} catch (FtpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SampleSimpleApplication.class, args);
	}
}
