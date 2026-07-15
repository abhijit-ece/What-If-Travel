package com.travelsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@SpringBootApplication
public class TravelSimulatorApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(TravelSimulatorApplication.class, args);
    }

    private static void loadDotEnv() {
        File dotEnv = new File(".env");
        if (!dotEnv.exists()) {
            dotEnv = new File("../.env");
        }

        if (dotEnv.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dotEnv))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        System.setProperty(key, value);
                    }
                }
                System.out.println("Loaded .env configuration successfully.");
            } catch (Exception e) {
                System.err.println("Could not parse .env file: " + e.getMessage());
            }
        } else {
            System.out.println(".env file not found. System environment variables will be used.");
        }
    }
}
