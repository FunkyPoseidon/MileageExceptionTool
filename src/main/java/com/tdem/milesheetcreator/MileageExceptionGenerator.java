package com.tdem.milesheetcreator;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class MileageExceptionGenerator {

    public static File generate(
            File inputFile,
            File outputFolder,
            Config config,
            Consumer<String> statusUpdate
    ) {

        try {
            return MilesSheetCreator.generate(
                    inputFile,
                    outputFolder,
                    config,
                    statusUpdate
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}