/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

using System.Security;
using System.Text.RegularExpressions;

namespace Defend483CS
{

    internal class InputManager
    {

        internal const int _maxStringLength = 50;

        private const string _logFilename =
            "log.txt";
        private const string _passwordFilename =
            "pass.byte";

        private const string _backupProgramFolder =
            @"tcss360\defend";
        private const string _backupLogPath =
            _backupProgramFolder + @"\" + _logFilename;
        private const string _backupPasswordPath =
            _backupProgramFolder + @"\" + _passwordFilename;

        private static readonly string _programFolder =
            Path.Combine(
                Environment.GetFolderPath(
                    Environment.SpecialFolder.ApplicationData
                ),
                _backupProgramFolder
            );
        private static readonly string _logPath =
            Path.Combine(_programFolder, _logFilename);
        private static readonly string _passwordPath =
            Path.Combine(_programFolder, _passwordFilename);

        private static readonly string _invalidInput =
            "Invalid input. Try again." + Environment.NewLine;
        private const string _redirectionAttempt =
            "File redirection is not allowed.";
        private const string _programFolderError =
            "The application cannot acquire the file system resources to run.";
        private const string _inputTooLong =
            "The provided input was too long.";


        internal static bool EnsureNoRedirection()
        {
            if (Console.IsOutputRedirected)
            {
                // Don't attempt to print to console, since may be redirected
                // to unreliable output that may cause other exception
                LogError(_redirectionAttempt);
                return false;
            }
            if (Console.IsInputRedirected)
            {
                Console.WriteLine(_redirectionAttempt);
                LogError(_redirectionAttempt);

                return false;
            }

            return true;
        }

        internal static (T, T) ReadTwo<T>(in string theTitle,
                                          in string theFirstSubtitle,
                                          in string theSecondSubtitle,
                                          in Func<string, T> theReadFunction)
        {
            Console.WriteLine(theTitle);

            T first = theReadFunction(theFirstSubtitle);
            T second = theReadFunction(theSecondSubtitle);

            return (first, second);
        }

        internal static string ReadStringUntilValid(in string thePrompt,
                                                    in string theRegex = "^.*$",
                                                    in string? theTitle = null,
                                                    in Predicate<string>? theValidationFunction = null)
        {
            string? result;
            Predicate<string> validationFunction = theValidationFunction ??
                                                   ((str) => true);

            if (!string.IsNullOrEmpty(theTitle))
            {
                Console.WriteLine(theTitle);
            }

            while (true)
            {
                Console.WriteLine(thePrompt);

                result = ReadLineAndPrintBlankLine();
                if (result != null &&
                    Regex.IsMatch(result, theRegex) &&
                    validationFunction(result))
                {
                    return result;
                }

                Console.WriteLine(_invalidInput);
            }
        }

        internal static string GetPasswordPath()
        {
            return MakeProgramDirectory() ? _passwordPath : _backupPasswordPath;
        }

        internal static void LogError(in Exception theException)
        {
            LogError(theException.ToString());
        }

        internal static void CloseUnexpectedly(in Exception theException,
                                               in ExitCode theExitCode)
        {
            LogError(theException);
            CloseUnexpectedly(theExitCode);
        }

        private static string? ReadLineAndPrintBlankLine()
        {
            string? line = null;

            try
            {
                line = Console.ReadLine(); // May return null w/o exception
                Console.WriteLine();
            }
            catch (Exception e) when (e is OutOfMemoryException or
                                      ArgumentOutOfRangeException)
            {
                Console.WriteLine(); // Exception before first cw, so again
                Console.WriteLine(_inputTooLong);
            }
            catch (IOException e)
            {
                // No redirection, so should only temporarily occur unless
                // serious problem in environment (so expect to not close
                // program)
                LogError(e);
                // Slight formatting discrepancy with no cw, but
                // cw excluded to avoid printing large number of
                // lines if repeated IOExceptions without ever
                // prompting user for input
            }

            return line;
        }

        private static string GetLogPath()
        {
            return MakeProgramDirectory() ? _logPath : _backupLogPath;
        }

        private static bool MakeProgramDirectory()
        {
            if (MakeProgramDirectory(_programFolder))
            {
                return true;
            }
            if (MakeProgramDirectory(_backupProgramFolder))
            {
                return false;
            }

            // Neither folder can be used, so nowhere to preserve error
            // information after program exits
            ProgramFolderFailure();
            return false; // Unreachable
        }

        private static bool MakeProgramDirectory(in string theDirectory)
        {
            try
            {
                if (Directory.CreateDirectory(theDirectory).Exists)
                {
                    return true;
                }
            }
            catch (Exception e) when (e is IOException or
                                      UnauthorizedAccessException or
                                      ArgumentException or
                                      NotSupportedException)
            {// Indicate failure by returning false. To avoid recursive loop
            }// when errors cannot be logged, don't attempt to log error

            // Return false if directory doesn't exist for some reason
            // according to DirectoryInfo.Exists or if exception thrown by
            // CreateDirectory otherwise indicates specified directory not
            // available
            return false;
        }

        private static void LogError(in string theError)
        {
            try
            {
                File.AppendAllText(
                    GetLogPath(),
                    Environment.NewLine +
                    DateTime.Now.ToString("F") + ':' + Environment.NewLine +
                    theError + Environment.NewLine
                );
            }
            catch (Exception e) when (e is IOException or
                                      UnauthorizedAccessException or
                                      ArgumentException or
                                      NotSupportedException or
                                      SecurityException)
            {
                // Preliminary check for at least one program folder option
                // working avoids this in as many situations as possible (short
                // of simply continuing to let the program run when errors
                // occur and cannot be logged)
                ProgramFolderFailure();
            }
        }

        private static void ProgramFolderFailure()
        {
            // If user can only interact with program, should never be
            // encountered

            // Brief, not very revealing message written to console instead of
            // error log since error log unavailable. Check for redirection
            // needed since may be called from EnsureNoRedirection
            if (!Console.IsOutputRedirected)
            {
                Console.WriteLine(_programFolderError);
            }

            CloseUnexpectedly(ExitCode.FileSystemError);
        }

        private static void CloseUnexpectedly(in ExitCode theExitCode)
        {
            Environment.Exit((int) theExitCode);
        }
    }
}
