/*
 * Team 3
 * AJ Downey, Joshua Barbee, Wei Wei Chien
 */

namespace Defend483CS
{
    internal class IntsValidator
    {

        private const string _title =
            "2 integers:";

        private const string _firstIntTitle =
            "First integer:";
        private const string _secondIntTitle =
            "Second integer:";
        private static readonly string _prompt =
            "Enter one integer in the range [" + int.MinValue + ", " +
            int.MaxValue + "]." + Environment.NewLine + "The integer can " +
            "include a sign (+ or -) and cannot include commas:";

        internal static (int, int, long, long) ReadAndAddAndMultiply()
        {
            (int first, int second) = InputManager.ReadTwo<int>(
                _title,
                _firstIntTitle, _secondIntTitle,
                ReadInt
            );

            return (
                first, second,
                Add(first, second), Multiply(first, second)
            );
        }

        // Param not in-mode ref for compiler to allow passing
        // as Func<string, int> param
        private static int ReadInt(string theSubtitle)
        {
            int result = 0; // Guaranteed to be reassigned
            InputManager.ReadStringUntilValid(
                _prompt,
                theTitle: theSubtitle,
                theValidationFunction: TryParseWrapper
            );

            return result;

            // Can't use in param since used as method group
            bool TryParseWrapper(string theString) =>
                int.TryParse(theString, out result);
        }

        private static long Add(in int theFirstInt, in int theSecondInt)
        {
            return ((long)theFirstInt) + theSecondInt;
        }

        private static long Multiply(in int theFirstInt, in int theSecondInt)
        {
            return ((long)theFirstInt) * theSecondInt;
        }
    }
}
