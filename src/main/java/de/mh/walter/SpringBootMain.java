package de.mh.walter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootMain {

    //TODO: use utf-8 as table encoding
    //TODO: extract register validation
    //TODO: extract auth need path check
    //TODO: user auth keys -> remove old ones?
    //TODO: user auth keys -> show in interface?
    public static void main(String[] args) {
        SpringApplication.run(SpringBootMain.class, args);
    }

}
