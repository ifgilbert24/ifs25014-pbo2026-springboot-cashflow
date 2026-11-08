package org.delcom.ifs25014pbo2026springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loader ringan untuk file {@code .env} di working directory.
 *
 * <p>Setiap baris {@code KEY=VALUE} (yang bukan komentar {@code #} dan bukan kosong)
 * disalin ke {@link System#setProperty(String, String)} jika key tersebut belum ada
 * di system properties maupun environment variable OS. Dengan demikian nilai dari OS
 * selalu menang atas {@code .env}, dan Spring Boot dapat membacanya melalui placeholder
 * standar {@code ${KEY:default}} di {@code application.yaml}.</p>
 */
public final class DotEnv {

    private DotEnv() {
    }

    public static void load() {
        load(Path.of(".env"));
    }

    public static void load(Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path);
            for (String rawLine : lines) {
                parseLine(rawLine);
            }
        } catch (IOException _) {
            // Abaikan jika file tidak dapat dibaca; gunakan nilai default Spring.
        }
    }

    static void parseLine(String rawLine) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        int eq = line.indexOf('=');
        if (eq <= 0) {
            return;
        }
        String key = line.substring(0, eq).trim();
        String value = stripQuotes(line.substring(eq + 1).trim());
        if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }

    static String stripQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}

