/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

namespace Defend483CS
{
    internal class NameValidator
    {

        private const string _title =
            "Name:";

        private const string _firstNameTitle =
            "First name:";
        private const string _lastNameTitle =
            "Last name:";
        private static readonly string _prompt =
            "Enter 1 to " + InputManager._maxStringLength + " characters." +
            Environment.NewLine + "Uppercase and lowercase latin letters, " +
            "apostrophes, and hyphens are allowed:";

        private static readonly string _regex =
            "^[A-Za-z'-]{1," + InputManager._maxStringLength + "}$";

        internal static (string, string) Read()
        {
            return InputManager.ReadTwo<string>(
                _title,
                _firstNameTitle, _lastNameTitle,
                (str) => InputManager.ReadStringUntilValid(
                    _prompt, _regex, str
                )
            );
        }
    }
}
