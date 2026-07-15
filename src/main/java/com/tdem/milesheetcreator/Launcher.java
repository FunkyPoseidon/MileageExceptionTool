package com.tdem.milesheetcreator;

import java.io.File;
import java.io.PrintWriter;

public class Launcher {

    public static void main(String[] args) {

        try {
            MainApplication.main(args);

        } catch (Throwable t) {

            try {
                File log = new File(
                        System.getProperty("user.home"),
                        "MileageExceptionTool-error.txt"
                );

                PrintWriter writer = new PrintWriter(log);
                t.printStackTrace(writer);
                writer.close();

            } catch (Exception ignored) {
            }

            throw t;
        }
    }
}