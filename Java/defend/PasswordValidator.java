/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

package defend;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class PasswordValidator {

    private static final int MIN_PASSWORD_LENGTH = 10;

    private static final String PASSWORD_TITLE =
            "Password:";

    private static final String RETYPE_PASSWORD_PROMPT =
            "Retype your password:";
    private static final String PASSWORD_PROMPT =
            "Enter a password including at least one uppercase latin " +
            "letter, lowercase latin letter, decimal digit, and special " +
            "character (any of the following: ~`!@#$%^&*()_=+-).\nThe " +
            "password may contain multiple of each of these but may not " +
            "contain any characters that are not listed above.\nThe " +
            "password must be at least " + MIN_PASSWORD_LENGTH + " and at " +
            "most " + InputManager.MAX_STRING_LENGTH + " characters.";

    private static final String PASSWORD_REGEX =
            "(?=.*[A-Z])" +
            "(?=.*[a-z])" +
            "(?=.*\\d)" +
            "(?=.*[~`!@#$%^&*()_=+-])" +
            "[A-Za-z\\d~`!@#$%^&*()_=+-]{" +
            MIN_PASSWORD_LENGTH + ',' + InputManager.MAX_STRING_LENGTH + "}";

    static void readAndVerify() {
        final PasswordHasher hasher = createPasswordHasher();
        assert hasher != null;

        final byte[][] saltAndHash = readAndSaltAndHash(hasher);
        assert saltAndHash != null;

        final int hashSize = saltAndHash[0].length;

        verifyPassword(
                hasher,
                saltAndHash,
                hashSize,
                writePasswordFile(saltAndHash)
        );
    }

    private static PasswordHasher createPasswordHasher() {
        try {
            return new PasswordHasher();
        } catch (NoSuchAlgorithmException e) {
            // Should only be encountered if bug in PasswordHasher or PBKDF2
            // not supported by implementation
            InputManager.unexpectedClose(e);
            return null; // Will never be reached
        }
    }

    private static byte[][] readAndSaltAndHash(final PasswordHasher theHasher) {
        try {
            return theHasher.saltAndHashWithNewSalt(
                    InputManager.readStringWithRegex(
                            PASSWORD_TITLE, PASSWORD_PROMPT,
                            PASSWORD_REGEX
                    )
            );
        } catch (InvalidKeySpecException e) {
            // Should only be encountered if serious problem preventing
            // password from ever being hashed
            InputManager.unexpectedClose(e);
            return null; // Will never be reached
        }
    }

    private static boolean writePasswordFile(final byte[][] theSaltAndHash) {
        try (FileOutputStream out =
                     new FileOutputStream(InputManager.getPasswordPath())) {
            out.write(theSaltAndHash[0]);
            out.write(theSaltAndHash[1]);

            // Password written to file, and file should be read from
            return true;
        } catch (IOException | SecurityException e) {
            InputManager.logError(e);

            // Password not written to file, and data in memory should be used
            // instead (backup)
            return false;
        }
    }

    private static void verifyPassword(final PasswordHasher theHasher,
                                       final byte[][] theSaltAndHash,
                                       final int theHashSize,
                                       final boolean theIsInFile) {
        final byte[][] saltAndHash = getSaltAndHashFromFileOrMemory(
                theIsInFile, theSaltAndHash, theHashSize
        );

        InputManager.readStringWithRegex(
                RETYPE_PASSWORD_PROMPT,
                PASSWORD_REGEX,
                (str) -> validateRetypedPassword(
                        str, theHasher, saltAndHash
                )
        );
    }

    private static byte[][] getSaltAndHashFromFileOrMemory(final boolean theIsInFile,
                                                           final byte[][] theSaltAndHash,
                                                           final int theHashSize) {
        final byte[][] saltAndHash = new byte[2][];
        if (!theIsInFile ||
            !readSaltAndHashFromFile(saltAndHash, theHashSize)) {
            // Password either couldn't be saved to file or couldn't be read
            // from file, so default to using salt and hash stored in memory
            saltAndHash[0] = theSaltAndHash[0];
            saltAndHash[1] = theSaltAndHash[1];
        }

        return saltAndHash;
    }

    private static boolean readSaltAndHashFromFile(final byte[][] theSaltAndHash,
                                                   final int theHashSize) {
        try {
            final byte[] combinedSaltAndHash = Files.readAllBytes(
                    new File(InputManager.getPasswordPath()).toPath()
            );
            theSaltAndHash[0] = new byte[theHashSize];
            theSaltAndHash[1] = new byte[theHashSize];

            System.arraycopy(
                    combinedSaltAndHash, 0,
                    theSaltAndHash[0], 0,
                    theHashSize
            );
            System.arraycopy(
                    combinedSaltAndHash, 32,
                    theSaltAndHash[1], 0,
                    theHashSize
            );

            return true; // File read successfully
        } catch (IOException e) {
            InputManager.logError(e);

            return false; // File could not be read
        }
    }

    private static boolean validateRetypedPassword(final String theRetypedPassword,
                                                   final PasswordHasher theHasher,
                                                   final byte[][] theSaltAndHash) {
        try {
            return Arrays.equals(
                    theSaltAndHash[1],
                    theHasher.saltAndHash(
                            theRetypedPassword, theSaltAndHash[0]
                    )
            );
        } catch (InvalidKeySpecException e) {
            // Only encountered if serious problem preventing password from
            // ever being hashed
            InputManager.unexpectedClose(e);
            return false; // Unreachable
        }
    }


    private static class PasswordHasher {

        private static final SecureRandom RANDOM = new SecureRandom();
        private static final int ITERATIONS = 1_048_576;

        private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
        private static final int HASH_SIZE = 32;
        private static final int KEY_LENGTH = HASH_SIZE * 8; // In bits

        private static final String BACKUP_ALGORITHM = "PBKDF2WithHmacSHA1";
        private static final int BACKUP_HASH_SIZE = 16;
        private static final int BACKUP_KEY_LENGTH = BACKUP_HASH_SIZE * 8;


        private final SecretKeyFactory myKeyFactory;
        private final int myHashSize;
        private final int myKeyLength;


        private PasswordHasher() throws NoSuchAlgorithmException {
            SecretKeyFactory keyFactory;
            int hashSize, keyLength;
            try {
                keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
                hashSize = HASH_SIZE;
                keyLength = KEY_LENGTH;
            } catch (NoSuchAlgorithmException e) {
                InputManager.logError(e);

                keyFactory = SecretKeyFactory.getInstance(BACKUP_ALGORITHM);
                hashSize = BACKUP_HASH_SIZE;
                keyLength = BACKUP_KEY_LENGTH;
            }

            myKeyFactory = keyFactory;
            myHashSize = hashSize;
            myKeyLength = keyLength;
        }

        private byte[][] saltAndHashWithNewSalt(final String thePassword)
                throws InvalidKeySpecException {
            final byte[][] saltAndHash = new byte[2][];
            saltAndHash[0] = generateSalt(myHashSize);
            saltAndHash[1] = saltAndHash(thePassword, saltAndHash[0]);

            return saltAndHash;
        }

        private byte[] saltAndHash(final String thePassword,
                           final byte[] theSalt)
                throws InvalidKeySpecException {
            return myKeyFactory.generateSecret(
                    new PBEKeySpec(
                            thePassword.toCharArray(),
                            theSalt,
                            ITERATIONS,
                            myKeyLength
                    )
            ).getEncoded();
        }

        private static byte[] generateSalt(final int theHashSize) {
            final byte[] salt = new byte[theHashSize];
            RANDOM.nextBytes(salt);

            return salt;
        }
    }
}
