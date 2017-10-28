import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.*;

/**
 * Class ConvertPatterns provides methods for constructing regular expressions based on a pattern
 * specification.
 * @author Nicholas Taylor
 * @version 27/10/17
 *
 */
public class PatternSpecification {

	public static final String start = "^";
	public static final String end = "$";
	public static final String spaceLimitationStart = "\\\\b\\\\w+\\\\b";
	public static final String spaceLimtationMiddle = "\\\\s\\\\b\\\\w+\\\\b";
	public static final String normalNumber = "([\\\\w\\\\s]+)";
	public static final String greedy = "(.*?)";

	private static String returnArgumentString(String[] args){
		String input = "";
		for(int i = 0; i < args.length; i++){
			if(i == 0){
				input += args[i];
			} else {
				input += " " + args[i];
			}
		}
		return input;

	}
	/**
	 * Converts a specification string into an array list containing the numbers and letters corresponding to
	 * the inner part of: %{ }.
	 * @param spec The string provided as command line argument - specification string.
	 * @return An ArrayList containing the inner parts of %{ }.
	 */
	private static ArrayList<String> convertSpecification(String spec){
		ArrayList<String> ptrn = new ArrayList<String>();
		String testSpec = "\\%\\{(.*?)\\}";
		Pattern p = Pattern.compile(testSpec);
		Matcher m = p.matcher(spec);
		while(m.find()){
			ptrn.add(m.group(1));
		}
		return ptrn;
	}
	/**
	 * Helper method which checks to see whether a string is a non-negative integer.
	 * @param s the string to be checked.
	 * @return true if the string is a non-negative number, false otherwise.
	 */
	private static boolean isNumeric(String s){
		try {
			int check = Integer.parseInt(s);
			if (check < 0){
				return false;
			}
		} catch (NumberFormatException e){
			return false;
		}
		return true;
	}
	/**
	 * Creates a regular expression that represents the pattern specification.
	 * @param spec the list containing the seperate specification parts.
	 * @param patternSpec the original pattern specification to base the regular expression on.
	 * @return a regular expression representing the pattern specification.
	 */
	private static String createRegex(ArrayList<String> spec, String patternSpec){
		//3 possibilities : %{#}, %{#G}, %{#S#}
		String result = patternSpec;


		for(int i = 0; i < spec.size(); i++){
			int specLength = spec.get(i).length();
			//Simplest case - one number.

			if(specLength == 1 && isNumeric(spec.get(i))){
				result = result.replaceAll("\\%\\{(\\d)\\}", normalNumber);
			//Second case, with greedy token capture modifier.
			} else if (specLength == 2 && isNumeric(spec.get(i).substring(0, 1)) && spec.get(i).charAt(1) == 'G'){
				result = result.replaceAll("\\%\\{(\\dG)\\}", greedy);

			} else if (specLength == 3 && isNumeric(spec.get(i).substring(0, 1)) && spec.get(i).charAt(1) == 'S') {
				StringBuilder spaceRegex = new StringBuilder();
				spaceRegex.append(spaceLimitationStart);
				int numberOfSpaces = Integer.parseInt(spec.get(i).substring(2, 3));

				for(int j = 0; j < numberOfSpaces; j++){
					spaceRegex.append(spaceLimtationMiddle);
				}
				result = result.replaceAll("\\%\\{(\\dS\\d)\\}", spaceRegex.toString());


			} else {
				System.out.println("Specification is incorrect, should be either: %{#}, %{#G} or %{#S#}." + "\n"
									+ "All numbers must be non-negative integers.");
				System.exit(0);
			}
		}
		result = start + result + end;
		return result;
	}
	/**
	 * Matches lines read in from stdin to the regex created from the specification.
	 * @param line2BeMatched the line read from stdin to be matched against the regex
	 * @param regex the regex to match the line against.
	 * @return the string line2BeMatched if it matched the regex.
	 */
	private static String match(String line2BeMatched, String regex){

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(line2BeMatched);
		if(m.matches()){
			return line2BeMatched;
		} else {
			return "";
		}

	}

	public static void main(String[] args){
		String argString = returnArgumentString(args);
		String result = createRegex(convertSpecification(argString), argString);
		InputStreamReader isReader = new InputStreamReader(System.in);
		BufferedReader bufReader = new BufferedReader(isReader);

		String readLine = "";
		try {
			while(bufReader.ready()){

				readLine = bufReader.readLine();
				String matchedString = match(readLine, result);
				if(matchedString != ""){
					System.out.println(matchedString);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
