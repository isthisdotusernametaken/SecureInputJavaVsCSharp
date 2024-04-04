Team Members:
	AJ Downey
	Joshua Barbee
	Wei Wei Chien


Shortcomings:
	If the log file cannot be opened when an error needs to be logged or if the Java implementation does not support either of the possible algorithms specified for PBKDF2, the program will exit before completing (but will still not display exception information to the user).
	Neither solution can detect or recover from the application being forcibly terminated (such as with Ctrl+C).


Compilation Instructions
	Java:
		With Java 17 or later installed (older versions may or may not run) and environment variables updated as needed
		Open a terminal in the "Java" folder (immediately above the "defend" folder)
		Enter "javac defend/*.java" and then "java defend/Defend"
			The condition in the if-statement in Defend.main would need to be replaced with "true" to bypass the check for file redirection in order to run in some IDEs
	C#:
		With .NET 6.0 or later installed (older versions may or may not run) and environment variables updated as needed
		Open a terminal in the "CS/run" folder
		Enter "dotnet Defend483CS.dll"
			The .cs files may be able to be compiled and run as-is in an IDE using a new enough version of .NET


Reasoning behind the use of InputManager.ReadStringUntilValid and the validation predicate provided to it (described for C# solution, but also to some degree for Java solution) 
	In each case of receiving input, we need a loop of (1) prompting the user for input, (2) accepting input from the user, (3) validating the input with regex and/or a custom validation method, and (4) either returning the validated input and exiting the loop or communicating to the user that the input was invalid and continuing the loop.
	Also, the validated input usually needs to be processed in some way, and any issues that were not detected in the validation step could cause the processing step to fail. In such a case, if recovering from the failure and continuing the expected behavior of the program is possible, we need to then go back to the input loop.
	To avoid lengthy corrections for slight formatting changes between these cases (to avoid externally providing extra information about what went wrong internally) and to draw the clearer line that the input loop is not exited until the input is guaranteed to be suitable for its purpose, the processing step is incorporated into the validation function that is passed to the method with the input loop.
	If any output other than the entered string is required from the processing step, the validation method gains an out-mode parameter, and a local method with the valid Predicate signature (passed in as a validation function instead of the actual validation function) is created to call the validation method and update a local variable with the resulting value.


What we defended against in our code:
	Because standard IO redirected, not enough input or input too large, or unable to print output
		Program does not start if file redirection detected
		(may not be necessary, if assuming program is not started by user)
	Int overflow
		Each int cast to long before operation, and result kept in long (-2^31 * -2^31 = 2^62 < 2^63 - 1, so even largest-magnitude ints fit in long when multiplied)
	File system problems
		Backup folder for log and password if security issue or other problem makes intended folder unusable
		Backup strategy of using salt and hash stored in memory if salt and hash cannot be written or read from file (error logged, but application continues)
	Java implementation not supporting PBKDF2 with SHA256
		Backup older algorithm of PBKDF2 with SHA1 supported by more implementations if intended algorithm fails
			Not in C# solution, since SHA512 supported even in much older versions
	Invalid names (too short, too long, with invalid chars)
		Regex
	Invalid filenames
		Regex
	Invalid passwords
		Regex
	Invalid input files (security issue, not readable text)
		Catching IO and security exceptions and requiring user to provide new input
	Input file becoming unavailable between checking and reading or same file specified for input and output
		Reading entire file into memory while checking
		Overwriting file without output not allowed
	Input file nonexistent, directory, or possibly too large to keep in memory
		Using validation methods (in Java, with File and Files classes; in C#, with File and Directory classes) for each of these scenarios and requiring user to reenter if invalid
	User traversing the file system (including reading log file to see what errors occurred)
		Can only read from or write to text files in the current directory
			Enforced by regex not allowing characters (/, \) or overall paths (.. — since no slashes and must end in .txt) that can specify a parent or child directory
	User overwriting important/own text file
		Require user to reenter if validation method or exception (in Java, with File.exists and FileAlreadyExistsException; in C#, with File.Exists and Directory.Exists) indicates file already exists
	Easy to crack password
		Using CSPRNG (in Java, from SecureRandom; in C#, from Cryptography.RandomNumberGenerator)
		Using PBKDF2 (in Java, with SHA256 in SecretKeyFactory and salt and hash 32-bytes each; in C#, with SHA512 in Rfc2898DeriveBytes and salt and hash 64 bytes each) with > 1,000,000 iterations
	Other problems when reading in input from console or parsing input
		Reading entire line in for each input, preventing incorrect empty input or extra input
		Handling input closed with Ctrl+Z/D (in Java, with NoSuchElementException and new Scanner; in C# with checking read result for null)
		Catching and safely recovering from all exceptions
			If possible to successfully complete after exception, normal execution resumse
			If no longer possible to successfuly complete, then log error, display generic error message to user, and safely exit