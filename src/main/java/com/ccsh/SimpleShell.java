package com.ccsh;

import java.io.BufferedReader;
import java.io.File;
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
                    System.out.println("Exiting shell...");
                    break;
                } else if (command.equals("pwd")) {
                    System.out.println(System.getProperty("user.dir"));
                    continue;
                } else if (command.startsWith("cd")) {
                    String newDirectory = command.substring(3).trim();
                    changeDirectory(newDirectory);
                    continue;
                }

                // check for pipe support
                if (command.contains("|")) {
                    executePipedCommands(command);
                } else {
                    executeCommand(command);
                }
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        }
    }

    private static void executePipedCommands(String command) {
        String[] commands = command.split("\\|");
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process previousProcess = null;

        try {
            for (String cmd : commands) {
                String[] commandParts = cmd.trim().split(" ");
                processBuilder.command("cmd.exe", "/c", commandParts[0]);

                for (int i = 1; i < commandParts.length; i++) {
                    processBuilder.command().add(commandParts[i]);
                }

                Process process = processBuilder.start();

                if (previousProcess != null) {
                    InputStreamReader previousOutput = new InputStreamReader(previousProcess.getInputStream());
                    processBuilder.redirectInput(previousOutput);
                }

                if (previousProcess != null) {
                    previousProcess.waitFor();
                }

                previousProcess = process;

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            if (previousProcess != null) {
                previousProcess.waitFor();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing piped commands: ");
        }
    }

    private static void changeDirectory(String newDirectory) {
        File dir = new File(newDirectory);
        if (dir.exists() && dir.isDirectory()) {
            System.setProperty("user.dir", dir.getAbsolutePath());
            System.out.println("Changed directory to: " + dir.getAbsolutePath());
        } else {
            System.err.println("Error changing directory: " + newDirectory + " does not exist or is not a directory.");
        }
    }

    private static void executeCommand(String command) {
        try {
            String[] commandParts = command.split(" ");
            ProcessBuilder processBuilder = new ProcessBuilder();

            // set command and it's arguments
            processBuilder.command("cmd.exe", "/c", commandParts[0]);

            // add arguments to the command
            for (int i = 1; i < commandParts.length; i++) {
                processBuilder.command().add(commandParts[i]);
            }

            Process process = processBuilder.start();

            // read output from the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // wait for command to finish
            process.waitFor();

            // check for errors in the error stream
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Error: " + errorLine);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }
}