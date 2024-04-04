/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

package defend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;

public class InputManager {

    static final int MAX_STRING_LENGTH = 50;

    private static final String LOG_FILENAME = "log.txt";
    private static final String PASSWORD_FILENAME = "pass.byte";

    private static final String BACKUP_PROGRAM_FOLDER =
            "tcss360\\defend";
    private static final String BACKUP_LOG_PATH =
            BACKUP_PROGRAM_FOLDER + '\\' + LOG_FILENAME;
    private static final String BACKUP_PASSWORD_PATH =
            BACKUP_PROGRAM_FOLDER + '\\' + PASSWORD_FILENAME;

    private static final String PROGRAM_FOLDER =
            System.getenv("APPDATA") + '\\' + BACKUP_PROGRAM_FOLDER;
    private static final String LOG_PATH =
            PROGRAM_FOLDER + '\\' + LOG_FILENAME;
    private static final String PASSWORD_PATH =
            PROGRAM_FOLDER + '\\' + PASSWORD_FILENAME;

    private static final String INVALID_INPUT =
            "Invalid input. Try again.\n";
    private static final String REDIRECTION_ATTEMPT =
            "File redirection is not allowed.";
    private static final String PROGRAM_FOLDER_ERROR =
            "The application cannot acquire the file system resources to run.";
    private static final String UNEXPECTED_CLOSE =
            "The application was forced to close unexpectedly.";

    private static Scanner INPUT = new Scanner(System.in);


    static boolean ensureNoRedirection() {
        // Require input and output to be connected to a terminal
        if (System.console() == null) {
            System.out.println(REDIRECTION_ATTEMPT);
            logError(REDIRECTION_ATTEMPT);

            return false;
        }

        return true;
    }

    static void printInputInvalid() {
        System.out.println(INVALID_INPUT);
    }

    static String readLineAndPrintBlankLine() {
        String line;
        try {
            line = INPUT.nextLine();
        } catch (NoSuchElementException e) {
            INPUT = new Scanner(System.in);
            line = null;
        }

        System.out.println();
        return line;
    }

    static String readStringWithRegex(final String theTitle,
                                      final String thePrompt,
                                      final String theRegex,
                                      final Predicate<String> theValidationFunction) {
        return readStringWithRegex(
                theTitle, thePrompt, theRegex, theValidationFunction, true
        );
    }

    static String readStringWithRegex(final String thePrompt,
                                      final String theRegex,
                                      final Predicate<String> theValidationFunction) {
        return readStringWithRegex(
                "", thePrompt, theRegex, theValidationFunction, false
        );
    }

    static String readStringWithRegex(final String theTitle,
                                      final String thePrompt,
                                      final String theRegex) {
        return readStringWithRegex(
                theTitle, thePrompt, theRegex, (str) -> true, true
        );
    }

    static String readStringWithRegex(final String theTitle,
                                      final String thePrompt,
                                      final String theRegex,
                                      final Predicate<String> theValidationFunction,
                                      final boolean theUsesTitle) {
        String result;

        if (theUsesTitle) {
            System.out.println(theTitle);
        }

        while (true) {
            System.out.println(thePrompt);

            result = readLineAndPrintBlankLine();
            if (result != null &&
                result.matches(theRegex) &&
                theValidationFunction.test(result)) {
                return result;
            }

            System.out.println(INVALID_INPUT);
        }
    }

    static String getPasswordPath() {
        return makeProgramDirectory() ? PASSWORD_PATH : BACKUP_PASSWORD_PATH;
    }

    static void logError(final Throwable theThrowable) {
        logError(theThrowable.toString());
    }

    static void unexpectedClose(final Throwable theThrowable) {
        logError(theThrowable);
        unexpectedClose();
    }

    private static String getLogPath() {
        return makeProgramDirectory() ? LOG_PATH : BACKUP_LOG_PATH;
    }

    private static boolean makeProgramDirectory() {
        final File dir = new File(PROGRAM_FOLDER);

        dir.mkdirs();
        if (!dir.exists()) {
            final File backupDir = new File(BACKUP_PROGRAM_FOLDER);

            backupDir.mkdirs();
            if (!backupDir.exists()) {
                programFolderFailure();
            }
            return false;
        }
        return true;
    }

    private static void logError(final String theError) {
        try (FileWriter fw = new FileWriter(getLogPath(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            pw.println();

            pw.print(new Date());
            pw.println(':');

            pw.println(theError);
        } catch (IOException e) {
            programFolderFailure();
        }
    }

    private static void programFolderFailure() {
        // Since assuming user can only interact with system through this
        // program, should never be encountered
        if (System.console() != null) {
            System.out.println(PROGRAM_FOLDER_ERROR);
        }

        unexpectedClose();
    }

    private static void unexpectedClose() {
        if (System.console() != null) {
            System.out.println(UNEXPECTED_CLOSE);
        }

        System.exit(-1);
    }
}
