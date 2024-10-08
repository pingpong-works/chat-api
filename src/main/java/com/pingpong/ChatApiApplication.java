package com.pingpong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ChatApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(ChatApiApplication.class, args);
  }
}
