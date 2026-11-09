package org.delcom.ifs25014pbo2026springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
public class App implements AppShellConfigurator, VaadinServiceInitListener {

    private static final String HIGH_CONTRAST_CSS = """
            :root, html, body {
                --lumo-primary-text-color: #1E40AF !important;
                --lumo-primary-color: #1E40AF !important;
                --lumo-success-text-color: #166534 !important;
                --lumo-success-color: #15803D !important;
                --lumo-error-text-color: #991B1B !important;
                --lumo-error-color: #991B1B !important;
                --lumo-body-text-color: #0F172A !important;
                --lumo-header-text-color: #0F172A !important;
                --lumo-secondary-text-color: #334155 !important;
                --lumo-tertiary-text-color: #334155 !important;
                --lumo-disabled-text-color: #475569 !important;
                --lumo-contrast-50pct: #475569 !important;
                --lumo-contrast-60pct: #334155 !important;
                --lumo-contrast-70pct: #1E293B !important;
                --lumo-contrast-80pct: #0F172A !important;
                --lumo-contrast-90pct: #0F172A !important;
                --lumo-contrast: #0F172A !important;
                --vaadin-input-field-label-color: #0F172A !important;
                --vaadin-input-field-focused-label-color: #1E40AF !important;
                --vaadin-input-field-hovered-label-color: #0F172A !important;
                --vaadin-input-field-placeholder-color: #475569 !important;
                --vaadin-input-field-helper-color: #334155 !important;
                --vaadin-input-field-value-color: #0F172A !important;
                --vaadin-input-field-background: #FFFFFF !important;
                --vaadin-input-field-border-color: #475569 !important;
                --vaadin-button-tertiary-text-color: #0F172A !important;
                background-color: #F8FAFC;
                color: #0F172A;
            }
            input::placeholder,
            textarea::placeholder,
            vaadin-text-field::part(value)::placeholder,
            vaadin-combo-box::part(value)::placeholder {
                color: #475569 !important;
                opacity: 1 !important;
            }
            vaadin-text-field::part(label),
            vaadin-combo-box::part(label) {
                color: #0F172A !important;
                font-weight: 600 !important;
                opacity: 1 !important;
            }
            vaadin-text-field::part(input-field),
            vaadin-combo-box::part(input-field) {
                min-height: 44px;
                background-color: #FFFFFF !important;
                border: 1px solid #475569 !important;
                color: #0F172A !important;
            }
            vaadin-text-field::part(clear-button),
            vaadin-combo-box::part(clear-button),
            vaadin-combo-box::part(toggle-button) {
                color: #334155 !important;
                opacity: 1 !important;
            }
            vaadin-grid::part(header-cell) {
                color: #0F172A !important;
                background-color: #F1F5F9 !important;
                font-weight: 700 !important;
            }
            vaadin-grid::part(body-cell) {
                color: #0F172A !important;
                background-color: #FFFFFF !important;
            }
            a[href], button, summary, select, label[for],
            [role="button"], [role="link"], [role="tab"], [role="menuitem"],
            [role="option"], [role="switch"], vaadin-button, vaadin-combo-box {
                cursor: pointer;
            }
            :disabled, [aria-disabled="true"], vaadin-button[disabled] {
                cursor: not-allowed;
            }
            vaadin-button {
                min-height: 44px;
                font-weight: 600;
            }
            vaadin-button:focus-visible,
            vaadin-text-field:focus-within,
            vaadin-combo-box:focus-within {
                outline: 2px solid #1E40AF;
                outline-offset: 2px;
            }
            @media (prefers-reduced-motion: reduce) {
                *, *::before, *::after {
                    animation-duration: 0.01ms !important;
                    animation-iteration-count: 1 !important;
                    transition-duration: 0.01ms !important;
                    scroll-behavior: auto !important;
                }
            }
            """;

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle("Cashflow: Catatan Keuangan");
        settings.addMetaTag("author", "ifs25014");
        settings.addMetaTag("robots", "index, follow");
        settings.addInlineWithContents(
                Inline.Position.PREPEND,
                HIGH_CONTRAST_CSS,
                Inline.Wrapping.STYLESHEET
        );
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(App::customizeIndexHtml);
    }

    static void customizeIndexHtml(IndexHtmlResponse response) {
        Document document = response.getDocument();
        document.outputSettings().charset("UTF-8");
        Element html = document.selectFirst("html");
        if (html != null) {
            html.attr("lang", "id");
        }
        Element outlet = document.getElementById("outlet");
        if (outlet != null) {
            outlet.tagName("main");
            outlet.attr("role", "main");
            outlet.attr("aria-label", "Konten Utama Aplikasi Cashflow");
        }
    }

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
