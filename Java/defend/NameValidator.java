/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

package defend;

public class NameValidator {

    private static final String NAME_TITLE =
            "Name:";

    private static final String FIRST_NAME =
            "First name:";
    private static final String LAST_NAME =
            "Last name:";
    private static final String NAME_PROMPT =
            "Enter 1 to " + InputManager.MAX_STRING_LENGTH + " characters.\n" +
            "Uppercase and lowercase latin letters, apostrophes, and " +
            "hyphens are allowed:";

    private static final String NAME_REGEX =
            "[A-Za-z'-]{1," + InputManager.MAX_STRING_LENGTH + "}";

    static String[] read() {
        final String[] firstAndLastName = new String[2];

        System.out.println(NAME_TITLE);
        firstAndLastName[0] = readName(FIRST_NAME);
        firstAndLastName[1] = readName(LAST_NAME);

        return firstAndLastName;
    }

    private static String readName(final String thePrePrompt) {
        return InputManager.readStringWithRegex(
                thePrePrompt, NAME_PROMPT,
                NAME_REGEX
        );
    }
}
