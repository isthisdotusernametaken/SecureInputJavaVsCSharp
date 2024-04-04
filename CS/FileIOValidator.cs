/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

using System.Security;

namespace Defend483CS
{
    internal class FileIOValidator
    {

        // 127 KiB, to avoid problems when loading entire file into memory
        private const int _maxInputFileSize = 1024 * 127;

        private static readonly string _inputTitle =
            "Name of an existing input file of at most " + _maxInputFileSize +
            " bytes to read from:";
        private const string _outputTitle =
            "Name of a currently nonexistent output file to write to:";

        private const string _prompt =
            "Enter the name of a plain text file in the current directory.\n" +
            "No parts of the filepath other than the name of the file may " +
            "be specified.\nThe filename may include 1-50 alphanumeric " +
            "characters, periods, underscores, and hyphens, followed by .txt";

        private static readonly string _regex =
            "^[A-Za-z0-9._-]{1," + InputManager._maxStringLength + @"}\.txt$";

        private const string _fileInaccessibleOrDoesNotExist =
            "The specified file does not exist or you do not have " +
            "permission to access it.";
        private const string _fileIsDirectory =
            "The provided filename refers to a directory.";
        private static readonly string _fileTooLarge =
            "The specified file exceeds " + _maxInputFileSize + " bytes.";
        private const string _fileInaccessibleOrAlreadyExists =
            "The specified file is inaccessible or already exists, and " +
            "overwriting is not allowed.";
        private const string _unauthorized =
            "You do not have the authority to access that file.";

        private const string _firstNameLabel =
            "First name:";
        private const string _lastNameLabel =
            "Last name:";
        private const string _firstIntLabel =
            "First integer:";
        private const string _secondIntLabel =
            "Second integer:";
        private const string _sumLabel =
            "Sum:";
        private const string _productLabel =
            "Product:";
        private const string _inputFileNameLabel =
            "Input file name:";
        private const string _inputFileContentsLabel =
            "Input file contents:";

        internal static (string, string) ReadInputFile()
        {
            string contents = ""; // Guaranteed to be overwritten
            string name = InputManager.ReadStringUntilValid(
                _prompt,
                _regex,
                _inputTitle,
                ValidateAndReadInputFileWrapper
            );

            return (name, contents);

            bool ValidateAndReadInputFileWrapper(string theFilename) =>
                ValidateAndReadInputFile(theFilename, out contents);
        }

        internal static void ReadFilenameAndPrintToFile(in string theFirstName,
                                                        in string theLastName,
                                                        in int theFirstInt,
                                                        in int theSecondInt,
                                                        in long theSum,
                                                        in long theProduct,
                                                        in string theInputFileName,
                                                        in string theInputFileContents)
        {
            string output = ConstructOutput(
                theFirstName, theLastName,
                theFirstInt, theSecondInt, theSum, theProduct,
                theInputFileName, theInputFileContents
            );

            InputManager.ReadStringUntilValid(
                _prompt,
                _regex,
                _outputTitle,
                (str) => ValidateAndWriteOutputFile(str, output)
            );
        }

        private static bool ValidateAndReadInputFile(in string theFilename,
                                                     out string theContents)
        {
            try
            {
                if (ValidateInputFileUnchecked(theFilename))
                {
                    theContents = File.ReadAllText(theFilename);
                    return true;
                }
            }
            catch (Exception e) when (e is SecurityException or
                                      UnauthorizedAccessException)
            {
                Console.WriteLine(_unauthorized);
            }
            catch (Exception e) when (e is ArgumentException or
                                      PathTooLongException or
                                      NotSupportedException)
            {
                // All these invalid char/path cases are avoided by regex, but
                // even if regex modified to allow these problems, can recover
                // and simply accept input again (after general invalid input
                // message in ReadStringUntilValid)
            }
            catch (IOException)
            {
                Console.WriteLine(_fileInaccessibleOrDoesNotExist);
            }

            theContents = ""; // Unused, since returning false indicates fail
            return false;
        }

        private static bool ValidateInputFileUnchecked(in string theFilename)
        {
            if (Directory.Exists(theFilename))
            {
                Console.WriteLine(_fileIsDirectory);
            }
            else if (!File.Exists(theFilename))
            {
                Console.WriteLine(_fileInaccessibleOrDoesNotExist);
            }
            else if (new FileInfo(theFilename).Length > _maxInputFileSize)
            {
                Console.WriteLine(_fileTooLarge);
            }
            else
            {
                return true;
            }

            return false;
        }

        private static string ConstructOutput(in string theFirstName,
                                              in string theLastName,
                                              in int theFirstInt,
                                              in int theSecondInt,
                                              in long theSum,
                                              in long theProduct,
                                              in string theInputFileName,
                                              in string theInputFileContents)
        {
            return FormatLabelAndValue(_firstNameLabel, theFirstName) +
                   FormatLabelAndValue(_lastNameLabel, theLastName) +
                   Environment.NewLine +
                   FormatLabelAndValue(_firstIntLabel, theFirstInt.ToString()) +
                   FormatLabelAndValue(_secondIntLabel, theSecondInt.ToString()) +
                   FormatLabelAndValue(_sumLabel, theSum.ToString()) +
                   FormatLabelAndValue(_productLabel, theProduct.ToString()) +
                   Environment.NewLine +
                   FormatLabelAndValue(_inputFileNameLabel, theInputFileName) +
                   FormatLabelAndValue(
                       _inputFileContentsLabel, theInputFileContents
                   );
        }

        private static string FormatLabelAndValue(in string theLabel,
                                                  in string theValue)
        {
            return theLabel + Environment.NewLine +
                   theValue + Environment.NewLine + Environment.NewLine;
        }

        private static bool ValidateAndWriteOutputFile(string theFilename,
                                                       in string theOutput)
        {
            if (File.Exists(theFilename))
            {
                Console.WriteLine(_fileInaccessibleOrAlreadyExists);
            }
            else if (Directory.Exists(theFilename))
            {
                Console.WriteLine(_fileIsDirectory);
            }
            else
            {
                return TryWriteOutputFile(theFilename, theOutput);
            }

            return false;
        }

        private static bool TryWriteOutputFile(in string theFilename,
                                               in string theOutput)
        {
            try
            {
                File.WriteAllText(theFilename, theOutput);

                return true;
            }
            catch (Exception e) when (e is SecurityException or
                                      UnauthorizedAccessException)
            {
                Console.WriteLine(_unauthorized);
            }
            catch (Exception e) when (e is ArgumentException or
                                      PathTooLongException or
                                      NotSupportedException)
            {
                // All avoided by regex, and if not, are still recoverable (see
                // where these are caught in ValidateAndReadInputFile).
                // Handled in caller's caller when returned false detected
            }
            catch (IOException)
            {
                Console.WriteLine(_fileInaccessibleOrAlreadyExists);
            }

            return false;
        }
    }
}
