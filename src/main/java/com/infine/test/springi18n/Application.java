package com.infine.test.springi18n;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Locale;
import java.util.ResourceBundle;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            String lang = args[0] != null ? args[0] : "fr";
            String country = args[1] != null ? args[1] : "FR";
            Locale locale = new Locale(lang, country);

            JSONResourceBundleControl control = new JSONResourceBundleControl(ctx);
            final String bundleName = "file:./i18n/TestBundle";
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, control);
            System.out.println("key1 " + bundle.getString("key1"));
            System.out.println("key2 " + bundle.getString("key2"));

            Thread.sleep(1000);
            bundle = ResourceBundle.getBundle(bundleName, locale, control);
            System.out.println("key1 " + bundle.getString("key1"));
        };
    }

}
