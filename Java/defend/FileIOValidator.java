/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

package defend;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileIOValidator {

    // 127 KiB, to stay a bit below 128 KiB restriction on String size in some
    // JVMs and make sure the file can be read very quickly (to guarantee the
    // entire input file can be read in as soon as it is identified, hopefully
    // avoiding the situation of the file being deleted before it can be read)
    private static final int MAX_INPUT_FILE_SIZE = 1024 * 127;

    private static final String INPUT_FILE_TITLE =
            "Name of an existing input file of at most " +
            MAX_INPUT_FILE_SIZE + " bytes to read from:";
    private static final String OUTPUT_FILE_TITLE =
            "Name of a currently nonexistent output file to write to:";

    private static final String FILE_PROMPT =
            "Enter the name of a plain text file in the current directory.\n" +
            "No parts of the filepath other than the name of the file may " +
            "be specified.\nThe filename may include 1-50 alphanumeric " +
            "characters, periods, underscores, and hyphens, followed by .txt";

    private static final String FILE_REGEX =
            "[A-Za-z0-9._-]{1," + InputManager.MAX_STRING_LENGTH + "}\\.txt";

    private static final String FILE_DOES_NOT_EXIST =
            "The specified file does not exist.";
    private static final String FILE_IS_DIRECTORY =
            "The provided filename refers to a directory.";
    private static final String FILE_TOO_LARGE =
            "The specified file exceeds " + MAX_INPUT_FILE_SIZE + " bytes.";
    private static final String FILE_ALREADY_EXISTS =
            "The specified file already exists, and overwriting is not " +
            "allowed.";
    private static final String UNAUTHORIZED =
            "You do not have the authority to access that file.";
    private static final String FILE_BECAME_UNAVAILABLE =
            "The specified file could not be accessed.";

    private static final String FIRST_NAME_LABEL =
            "First name:";
    private static final String LAST_NAME_LABEL =
            "Last name:";
    private static final String FIRST_INT_LABEL =
            "First integer:";
    private static final String SECOND_INT_LABEL =
            "Second integer:";
    private static final String SUM_LABEL =
            "Sum:";
    private static final String PRODUCT_LABEL =
            "Product:";
    private static final String INPUT_FILE_NAME_LABEL =
            "Input file name:";
    private static final String INPUT_FILE_CONTENTS_LABEL =
            "Input file contents:";


    static List<String> readInputFile() {
        final List<String> nameAndContents = new ArrayList<>();
        String name;

        while (true) {
            name = InputManager.readStringWithRegex(
                    INPUT_FILE_TITLE, FILE_PROMPT,
                    FILE_REGEX,
                    FileIOValidator::validateInputFile
            );

            try {
                final List<String> lines = Files.readAllLines(
                        new File(name).toPath()
                );

                // The following calls should not throw IOException or
                // SecurityException, so no possibility of name or lines being
                // added multiple times
                nameAndContents.add(name);
                nameAndContents.addAll(lines);

                return nameAndContents;
            } catch (IOException | SecurityException e) {
                System.out.println(FILE_BECAME_UNAVAILABLE);
            }

            InputManager.printInputInvalid();
        }
    }

    static void readAndPrintToOutputFile(final String[] theName,
                                         final int[] theInts,
                                         final long theSum,
                                         final long theProduct,
                                         final List<String> theInputFile) {
        final String output = constructOutput(
                theName,
                theInts, theSum, theProduct,
                theInputFile
        );

        while (true) {
            try {
                Files.writeString(
                        new File(InputManager.readStringWithRegex(
                                OUTPUT_FILE_TITLE, FILE_PROMPT,
                                FILE_REGEX,
                                FileIOValidator::validateOutputFile
                        )).toPath(),
                        output,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE
                );
                return;
            } catch (FileAlreadyExistsException e) {
                System.out.println(FILE_ALREADY_EXISTS);
            } catch (IOException | SecurityException e) {
                System.out.println(FILE_BECAME_UNAVAILABLE);
            }

            InputManager.printInputInvalid();
        }
    }

    private static String constructOutput(final String[] theName,
                                          final int[] theInts,
                                          final long theSum,
                                          final long theProduct,
                                          final List<String> theInputFile) {
        return formatLabelAndValue(FIRST_NAME_LABEL, theName[0]) +
               formatLabelAndValue(LAST_NAME_LABEL, theName[1]) +
               '\n' +
               formatLabelAndValue(FIRST_INT_LABEL, theInts[0]) +
               formatLabelAndValue(SECOND_INT_LABEL, theInts[1]) +
               formatLabelAndValue(SUM_LABEL, theSum) +
               formatLabelAndValue(PRODUCT_LABEL, theProduct) +
               '\n' +
               formatLabelAndValue(
                       INPUT_FILE_NAME_LABEL, theInputFile.get(0)
               ) +
               formatLabelAndValue(
                       INPUT_FILE_CONTENTS_LABEL,
                       joinAllButFirstOfList(theInputFile)
               );
    }

    private static String formatLabelAndValue(final String theLabel,
                                              final String theValue) {
        return theLabel + '\n' +
               theValue + "\n\n";
    }

    private static String formatLabelAndValue(final String theLabel,
                                              final long theValue) {
        return formatLabelAndValue(theLabel, "" + theValue);
    }

    private static String joinAllButFirstOfList(final List<String> theList) {
        return String.join("\n", theList.subList(1, theList.size()));
    }

    private static boolean validateInputFile(final String theFilename) {
        final File file = new File(theFilename);

        try {
            if (!file.exists()) {
                System.out.println(FILE_DOES_NOT_EXIST);
            } else if (file.isDirectory()) {
                System.out.println(FILE_IS_DIRECTORY);
            } else if (Files.size(file.toPath()) > MAX_INPUT_FILE_SIZE) {
                System.out.println(FILE_TOO_LARGE);
            } else {
                return true;
            }
        } catch (SecurityException e) {
            System.out.println(UNAUTHORIZED);
        } catch (IOException | InvalidPathException e) {
            InputManager.logError(e);
        }

        return false;
    }

    private static boolean validateOutputFile(final String theFilename) {
        final File file = new File(theFilename);

        try {
            if (file.exists()) {
                System.out.println(FILE_ALREADY_EXISTS);
            } else {
                return true;
            }
        } catch (SecurityException e) {
            System.out.println(UNAUTHORIZED);
        }

        return false;
    }
}
