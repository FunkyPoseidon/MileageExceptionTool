package com.tdem.milesheetcreator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Config {
    private int carCount;
    private List<String> exceptions;
    private boolean secondTransfer;

    public Config (int carCount, List<String> exceptions, boolean secondTransfer) {
        this.carCount = carCount;
        this.exceptions = exceptions;
        this.secondTransfer = secondTransfer;
    }

    public Config(int carCount, ArrayList<String> exceptions) {
        this.carCount = carCount;
        this.exceptions = exceptions;
    }

    public int getCarCount() {
        return carCount;
    }

    public String getExceptions() {
        return String.join(", ", exceptions);
    }

    public boolean isSecondTransfer() {
        return secondTransfer;
    }

    //gets permanent content from config and delimits it. returns only string version of content inside
    public List<String> getFromConfig() {
        createDefaultConfigIfNeeded();

        List<String> fileContents = new ArrayList<>();
        File file = getConfigFile();
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("File could not be found.");
            System.exit(9);
        }

        while(fileScanner.hasNextLine()) {
            fileContents.add(fileScanner.nextLine());
        }

        List<String> delimitedFileContents = new ArrayList<>();
        for(String str : fileContents) {
            Scanner strScanner = new Scanner(str);
            strScanner.useDelimiter(":");

            while(strScanner.hasNext()) {
                delimitedFileContents.add(strScanner.next().trim());
            }
        }

        //removes front header portion of text
        delimitedFileContents.remove(0);
        delimitedFileContents.remove(1);

        return delimitedFileContents;
    }

    public void writeToConfig() {
        File file = getConfigFile();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("Total Cars: " + carCount);
            bw.newLine();
            bw.write("Mile Exceptions: " + getExceptions());

        } catch (IOException e) {
            System.out.println("File could not be found.");
            System.exit(9);
        }
    }

    public static File getConfigFile() {
        File folder = new File(
                System.getProperty("user.home"),
                ".mileageexceptiontool"
        );

        if(!folder.exists()) {
            folder.mkdir();
        }

        return new File(folder, "config.txt");
    }

    private static void createDefaultConfigIfNeeded() {

        File configFile = getConfigFile();

        if (configFile.exists()) {
            return;
        }

        new Config(
                60,                     // default car count
                List.of("Bap", "T/O#2", "ER", "Med", "Uber"),    // default exceptions
                false                   // second transfer
        ).writeToConfig();
    }
}
