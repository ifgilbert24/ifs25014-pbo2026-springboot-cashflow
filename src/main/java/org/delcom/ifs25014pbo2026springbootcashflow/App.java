package org.delcom.ifs25014pbo2026springbootcashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point aplikasi Spring Boot + Vaadin untuk Catatan Keuangan (Cashflow).
 *
 * <p>Komposisi objek domain, usecase, dan adapter didefinisikan di
 * {@code config.AppConfig}; tampilan web tersedia pada route {@code /} melalui
 * {@code framework.view.FinanceView}.</p>
 */
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}

