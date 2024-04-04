/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

using System.Security;
using System.Security.Cryptography;

namespace Defend483CS
{
    internal class PasswordValidator
    {

        private const int _minPasswordLength = 10;

        private const string _title =
            "Password:";

        private const string _promptRetype =
            "Retype your password:";
        private static readonly string _prompt =
            "Enter a password including at least one uppercase latin " +
            "letter, lowercase latin letter, decimal digit, and special " +
            "character (any of the following: ~`!@#$%^&*()_=+-)." +
            Environment.NewLine + "The password may contain multiple of " +
            "each of these but may not contain any characters that are not " +
            "listed above." + Environment.NewLine + "The password must be " +
            "at least " + _minPasswordLength + " and at most " +
            InputManager._maxStringLength + " characters.";

        private static readonly string _regex =
            "^(?=.*[A-Z])" +
            "(?=.*[a-z])" +
            @"(?=.*\d)" +
            "(?=.*[~`!@#$%^&*()_=+-])" +
            @"[A-Za-z\d~`!@#$%^&*()_=+-]{" +
            _minPasswordLength + ',' + InputManager._maxStringLength + "}$";

        private const int _hashSize = 64;
        private const int _iterations = 1_048_576;
        private static readonly HashAlgorithmName _algorithm =
            HashAlgorithmName.SHA512;

        internal static void ReadAndStoreAndVerify()
        {
            (byte[] salt, byte[] hash) = ReadAndSaltAndHash();

            ReadAndVerify(salt, hash, WriteToPasswordFile(salt, hash));
        }

        private static (byte[], byte[]) ReadAndSaltAndHash()
        {
            Rfc2898DeriveBytes saltAndHasher = SaltAndCreateHasher(
                InputManager.ReadStringUntilValid(_prompt, _regex, _title)
            );

            return (saltAndHasher.Salt, saltAndHasher.GetBytes(_hashSize));
        }

        private static Rfc2898DeriveBytes SaltAndCreateHasher(in string thePassword)
        {
            try
            {
                return new Rfc2898DeriveBytes(
                    thePassword,
                    _hashSize,
                    _iterations,
                    _algorithm
                );
            }
            catch (Exception e) when (e is ArgumentException or
                                      CryptographicException)
            {
                // Salt size set manually and checked, and SHA512 supported
                // from .NET Core 1.0 to 7.0, so should never be reached
                InputManager.CloseUnexpectedly(
                    e, ExitCode.CryptographyError
                );

                // Unreachable, avoids incorrect compiler warnings of null in
                // client
                return new Rfc2898DeriveBytes("", 0);
            }
        }

        private static bool WriteToPasswordFile(in byte[] theSalt,
                                                in byte[] theHash)
        {
            try
            {
                File.WriteAllBytes(
                    InputManager.GetPasswordPath(),
                    theSalt.Concat(theHash).ToArray()
                );

                // Password in file, so can read from file
                return true;
            }
            catch (Exception e) when (e is ArgumentException or
                                        IOException or
                                        UnauthorizedAccessException or
                                        NotSupportedException or
                                        SecurityException)
            {
                InputManager.LogError(e);

                // Indicate password couldn't be written to file, so revert to
                // using data in memory to avoid premature exit
                return false;
            }
        }

        private static void ReadAndVerify(in byte[] theSalt,
                                          in byte[] theHash,
                                          in bool theIsInFile)
        {
            (byte[] salt, byte[] hash) = LoadFromMemoryOrPasswordFile(
                theSalt, theHash, theIsInFile
            );

            InputManager.ReadStringUntilValid(
                _promptRetype,
                _regex,
                theValidationFunction: (str) =>
                    ValidateRetypedPassword(str, salt, hash)
            );
        }

        private static (byte[], byte[]) LoadFromMemoryOrPasswordFile(in byte[] theSalt,
                                                                     in byte[] theHash,
                                                                     in bool theIsInFile)
        {
            if (!theIsInFile ||
                !LoadFromPasswordFile(out byte[] salt, out byte[] hash))
            {
                (salt, hash) = (theSalt, theHash);
            }

            return (salt, hash);
        }

        private static bool LoadFromPasswordFile(out byte[] salt,
                                                 out byte[] hash)
        {
            try
            {
                byte[] saltAndHash = File.ReadAllBytes(
                    InputManager.GetPasswordPath()
                );

                // Same size used for salt and hash, so can split at center
                int center = saltAndHash.Length / 2;
                (salt, hash) = (
                    saltAndHash[..center],
                    saltAndHash[center..]
                );

                return true; // Successfully loaded into given vars
            }
            catch (Exception e) when (e is ArgumentException or
                                        IOException or
                                        UnauthorizedAccessException or
                                        FileNotFoundException or
                                        NotSupportedException or
                                        SecurityException)
            {
                // Unused
                (salt, hash) = (Array.Empty<byte>(), Array.Empty<byte>());

                return false; // Revert to copies in memory
            }
        }

        private static bool ValidateRetypedPassword(in string theRetypedPasssword,
                                                    in byte[] theSalt,
                                                    in byte[] theHash)
        {
            try
            {
                return theHash.SequenceEqual(new Rfc2898DeriveBytes(
                    theRetypedPasssword,
                    theSalt,
                    _iterations,
                    _algorithm
                ).GetBytes(_hashSize));
            }
            catch (Exception e) when (e is ArgumentException or
                                      CryptographicException)
            {
                // SHA512 supported from .NET Core 1.0 to 7.0, so should never
                // be reached
                InputManager.CloseUnexpectedly(
                    e, ExitCode.CryptographyError
                );

                return false; // Unreachable
            }
        }
    }
}
