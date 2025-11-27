package com.argus.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ArgusServerApplication {
	private int serverPort;
    public static void main(String[] args) {
        SpringApplication.run(ArgusServerApplication.class, args);
    }
    
    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) {
    	this.serverPort = event.getWebServer().getPort();
        System.out.println("🚀 Argus Server iniciado em http://localhost:" + serverPort);
    }
}
