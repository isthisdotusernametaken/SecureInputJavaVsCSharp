/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

package defend;

public class IntsValidator {

    private static final String INTS_TITLE =
            "2 integers:";

    private static final String FIRST_INT =
            "First integer:";
    private static final String SECOND_INT =
            "Second integer:";
    private static final String INTS_PROMPT =
            "Enter one integer in the range [" + Integer.MIN_VALUE + ", " +
            Integer.MAX_VALUE + "].\nThe integer can include a sign (+ or " +
            "-) and cannot include commas:";

    static int[] read() {
        final int[] ints = new int[2];

        System.out.println(INTS_TITLE);
        ints[0] = readInt(FIRST_INT);
        ints[1] = readInt(SECOND_INT);

        return ints;
    }

    static long add(final int[] theInts) {
        return ((long) theInts[0]) + theInts[1];
    }

    static long multiply(final int[] theInts) {
        return ((long) theInts[0]) * theInts[1];
    }

    private static int readInt(final String thePrePrompt) {
        while (true) {
            System.out.println(thePrePrompt);
            System.out.println(INTS_PROMPT);

            try {
                return Integer.parseInt(
                        InputManager.readLineAndPrintBlankLine()
                );
            } catch (NumberFormatException e) {
                InputManager.printInputInvalid();
            }
        }
    }
}
