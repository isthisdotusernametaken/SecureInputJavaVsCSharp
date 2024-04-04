/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

package defend;

import java.util.List;

public class Defend {

    public static void main(String[] args) {
        if (InputManager.ensureNoRedirection()) {
            final String[] name = NameValidator.read();

            final int[] ints = IntsValidator.read();
            final long sum = IntsValidator.add(ints);
            final long product = IntsValidator.multiply(ints);

            final List<String> input = FileIOValidator.readInputFile();

            PasswordValidator.readAndVerify();

            FileIOValidator.readAndPrintToOutputFile(
                    name, ints, sum, product, input
            );
        }
    }
}
