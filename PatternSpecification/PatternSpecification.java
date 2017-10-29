import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.*;

/**
 * Class PatternSpecification provides methods for constructing regular expressions based on a pattern
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
        String extractSpecRegex = "\\%\\{(.*?)\\}";
        Pattern p = Pattern.compile(extractSpecRegex);
        Matcher m = p.matcher(spec);
        while(m.find()){
            ptrn.add(m.group(1));
        }
        //Gracefully exit if no token capture sequence extracted.
        if(ptrn.isEmpty()){
            System.out.println("Command line argument must contain a token capture sequence." + "\n" +
                               "The following are accepted: %{#}, %{#G} or %{#S#}");
            System.exit(0);
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
        String result = patternSpec;
        //Iterate through the token capture sequences extracted from the command line argument.
        for(int i = 0; i < spec.size(); i++){
            //Check whether the the arraylist element is empty.
            if(!spec.get(i).isEmpty()){
                int specLength = spec.get(i).length();
                //Simplest case %{#}, replace the number with a simple regular expression.
                if(isNumeric(spec.get(i))){
                    result = result.replaceAll("\\%\\{(" + spec.get(i) + ")\\}", normalNumber);
                    /*Greedy case %{#G}, Check whether last character of sequence is 'G'
                     * and the preceeding number is a non-negative number.
                     */
                } else if (spec.get(i).charAt(specLength - 1) == 'G' && isNumeric(spec.get(i).substring(0, specLength - 1))) {
                    result = result.replaceAll("\\%\\{(" + spec.get(i) + ")\\}", greedy);
                    /*
                     * Space limitation case %{#S#} - Iterate through the sequences characters extracting the preceding and
                     * proceeding numbers adjacent to the 'S' character.
                     * Following that, as long as both numbers are non negative integers, build up the space limitation regex
                     * so it represents the number of spaces specified by the token capture sequence.
                     */
                } else {
                    
                    for(int j = 0; j < specLength; j++){
                        int numberOfSpaces = 0;
                        if(spec.get(i).charAt(j) == 'S'){
                            
                            try {
                                numberOfSpaces = Integer.parseInt(spec.get(i).substring(j+1));
                                int precedingNumber = Integer.parseInt(spec.get(i).substring(0, j));
                            } catch(NumberFormatException e){
                                return patternSpec;
                            }
                            
                            String valueOfSpaces = spec.get(i).substring(j+1);
                            String numberBeforeS = spec.get(i).substring(0, j);
                            StringBuilder spaceRegex = new StringBuilder();
                            spaceRegex.append(spaceLimitationStart);
                            
                            for(int k = 0; k < numberOfSpaces; k++){
                                spaceRegex.append(spaceLimtationMiddle);
                            }
                            
                            result = result.replaceAll("\\%\\{(" + numberBeforeS + "S" + valueOfSpaces + ")\\}",
                                                       spaceRegex.toString());
                        }
                        
                    }
                }
                //ArryList element is empty gracefully exit.
            } else {
                System.out.println("Specification not defined." + "\n" +
                                   "Use the token capture sequences as follows: %{#}, %{#G} or %{#S#}");
                System.exit(0);
            }
        }
        
        
        
        return result;
    }
    
    
    /**
     * Matches lines read in from stdin to the regex created from the specification.
     * @param line2BeMatched the line read from stdin to be matched against the regex
     * @param regex the regex to match the line against.
     * @return the string line2BeMatched if it matched the regex.
     */
    private static boolean match(String line2BeMatched, String regex){
        
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line2BeMatched);
        if(m.matches()){
            return true;
        } else {
            return false;
        }
        
    }
    
    public static void main(String[] args){
        String argString = returnArgumentString(args);
        String regex = createRegex(convertSpecification(argString), argString);
        
        if(regex == argString) {
            
            System.out.println("Specification is incorrect, should be either: %{#}, %{#G} or %{#S#}." + "\n"
                               + "All numbers must be non-negative integers.");
            System.exit(0);
            
        }
        InputStreamReader isReader = new InputStreamReader(System.in);
        BufferedReader bufReader = new BufferedReader(isReader);
        String readLine = "";
        
        try {
            while(bufReader.ready()){
                
                readLine = bufReader.readLine();
                
                if(match(readLine, regex)){
                    System.out.println(readLine);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}


