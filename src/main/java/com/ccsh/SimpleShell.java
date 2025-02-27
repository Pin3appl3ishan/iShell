package com.ccsh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SimpleShell {
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command;

        while (true) {
            System.out.println("ccsh> ");
            try {
                command = reader.readLine().trim();
                if (command.isEmpty()) continue;
                if (command.equals("exit")) {
                    break;
                }
                executeCommand(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }
}