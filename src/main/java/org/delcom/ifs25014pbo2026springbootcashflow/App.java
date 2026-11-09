package org.delcom.ifs25014pbo2026springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point aplikasi Spring Boot dan konfigurasi shell HTML Vaadin untuk Cashflow.
 */
@SpringBootApplication
@Viewport("width=device-width, initial-scale=1")
@Meta(
        name = "description",
        content = "Aplikasi web pencatatan arus kas (Cashflow) untuk mengelola pemasukan, pengeluaran, dan saldo keuangan secara akurat."
)
@Meta(name = "theme-color", content = "#0F172A")
public class App implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle("Cashflow: Catatan Keuangan");
        settings.addMetaTag("author", "ifs25014");
        settings.addMetaTag("robots", "index, follow");
    }

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
