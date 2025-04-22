import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Mapping the regex patterns to the anonymised text using Randomised IDs
public class MedAnony {
    private static final Random rand = new Random();
    private static final String[] nameLetters = {"H", "J", "K"};
    private static final String[] partLetters = {"Q", "R"};
    private static final String[] ageLetters = {"M", "N"};
    private static final String[] addrLetters = {"F", "G", "I"};

    private static String generateID(String prefix, String[] letters) {
        int num = rand.nextInt(100);
        String letter = letters[rand.nextInt(letters.length)];
        return prefix + String.format("%02d", num) + letter;
    }

    // My Regex pattern starts here
    // 
    // Anonymisation of Addresses, Names, and Ages
    public static String anonymiseText(String text) {
        // Step 1 -First Anonymise the Addresses to match any pattern both UK and US
        String addrPattern = "\\b(\\d{1,4} [A-Za-z]+ (Street|Avenue|Lane|Road|Ave|St|Rd|Penny Lane)(, [A-Za-z ]+(, [A-Z]{2}|, [A-Z][A-Za-z]+)?)?)\\b";
        Matcher addrMatcher = Pattern.compile(addrPattern).matcher(text);
        StringBuffer tempText = new StringBuffer(); // using StringBuffer to hold the modified text
        Set<String> addrIDsUsed = new HashSet<>();
        while (addrMatcher.find()) {
            String addrID;
            do {
                addrID = generateID("L", addrLetters);
            } while (addrIDsUsed.contains(addrID));
            addrIDsUsed.add(addrID);
            addrMatcher.appendReplacement(tempText, addrID);
        }
        addrMatcher.appendTail(tempText);
        text = tempText.toString(); 

        // Step 2 - Anonymise Title and Full names
        // Whether Title and Full Names, or title and Fname/Lname)
        String fullnamePattern = "(?i)\\b(Mr\\.?|Mrs\\.?|Ms\\.?|Dr\\.?|MR\\.?|MRS\\.?|MS\\.?|DR\\.?|miss|mister|doctor)\\.?\\s+([A-Z][a-z]+)(?:\\s+([A-Z][a-z]+))?\\b";
        Matcher fullnameMatcher = Pattern.compile(fullnamePattern).matcher(text);
        Set<String> fNames = new HashSet<>(); // holds firstnames
        Set<String> lNames = new HashSet<>(); // holds lastnames
        Set<String> nameIDsUsed = new HashSet<>(); // holds used name IDs
        tempText = new StringBuffer();
        while (fullnameMatcher.find()) {
            String fName = fullnameMatcher.group(2);
            String lName = fullnameMatcher.group(3); 
            if (lName != null) {
                fNames.add(fName);
                lNames.add(lName);
            } else {
                fNames.add(fName);
            }
            String fullnameID;
            do {
                fullnameID = generateID("J", nameLetters);
            } while (nameIDsUsed.contains(fullnameID));
            nameIDsUsed.add(fullnameID);
            fullnameMatcher.appendReplacement(tempText, fullnameID);
        }
        fullnameMatcher.appendTail(tempText);
        text = tempText.toString(); // updates the text with the anonymised names

        // Step 3 - Anonymise only Fnames / LNames (no title)
        String noTitlePattern = "\\b([A-Z][a-z]+)\\b";
        Matcher noTitleMatcher = Pattern.compile(noTitlePattern).matcher(text);
        tempText = new StringBuffer();
        while (noTitleMatcher.find()) {
            String name = noTitleMatcher.group(1);
            if (fNames.contains(name) || lNames.contains(name)) {
                String partID = generateID("R", partLetters);
                noTitleMatcher.appendReplacement(tempText, partID);
            } else {
                noTitleMatcher.appendReplacement(tempText, name);
            }
        }
        noTitleMatcher.appendTail(tempText);
        text = tempText.toString();

        // Step 4 - Age Anonymisation
        // To Match various age format e.g "45 years", "30-year-old", "aged 50", 10 yr old.
        String agePattern = "\\b(\\d{1,3}(-year-old| years?| yr old)|aged\\s+\\d{1,3})\\b";
        Matcher ageMatcher = Pattern.compile(agePattern).matcher(text);     // using the regex pattern to match age
        tempText = new StringBuffer();
        Set<String> ageIDsUsed = new HashSet<>();   // holds used age IDs
        while (ageMatcher.find()) {
            String ageID;
            do {
                ageID = generateID("M", ageLetters);
            } while (ageIDsUsed.contains(ageID));
            ageIDsUsed.add(ageID);
            ageMatcher.appendReplacement(tempText, ageID);
        }
        ageMatcher.appendTail(tempText);
        text = tempText.toString();

        return text;
    }

    public static void main(String[] args) {
        try {
            String filePath = "/home/Amarachi379/PatientNotes.txt"; // user input file path
            String text = Files.readString(Paths.get(filePath));
            String anonymised = anonymiseText(text);
            String outputPath = "/home/Amarachi379/AnonymisedNotes.txt"; // output file path
            Files.writeString(Paths.get(outputPath), "Original:\n" + text + "\n\nAnonymised:\n" + anonymised);
            System.out.println("Anonymised results saved to " + outputPath); // save the anonymised text to a file
        } catch (Exception e) {
            System.out.println("Error processing file: " + e.getMessage());
        }
    }
}