/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

namespace Defend483CS
{
    internal class Defend
    {

        static void Main(string[] args)
        {
            if (InputManager.EnsureNoRedirection())
            {
                (string firstName, string lastName) = NameValidator.Read();

                (int firstInt, int secondInt, long sum, long product) =
                    IntsValidator.ReadAndAddAndMultiply();

                (string inputFileName, string inputFileContents) =
                    FileIOValidator.ReadInputFile();

                PasswordValidator.ReadAndStoreAndVerify();

                FileIOValidator.ReadFilenameAndPrintToFile(
                    firstName, lastName,
                    firstInt, secondInt, sum, product,
                    inputFileName, inputFileContents
                );
            }
        }
    }
}

