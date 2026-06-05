package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;


@Configuration
public class Config {

    @Bean
    public RestClient restClient(){
        return RestClient.builder().build();
    }

}
