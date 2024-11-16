package qengine.program;

import qengine.model.Dictionary;
import qengine.model.RDFAtom;
import qengine.parser.RDFAtomParser;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DictionaryTest {
    private static final String WORKING_DIR = "data/";
    private static final String SAMPLE_DATA_FILE = WORKING_DIR + "sample_data.nt";

    public static void main(String[] args) throws IOException {
        // Create a new dictionary
        Dictionary dictionary = new Dictionary();

        // Parse and encode RDF data
        System.out.println("=== Parsing and Encoding RDF Data ===");
        List<int[]> encodedTriples = parseAndEncodeRDFData(SAMPLE_DATA_FILE, dictionary);

        // Print dictionary statistics
        System.out.println("\n=== Dictionary Statistics ===");
        System.out.println("Total entries in dictionary: " + dictionary.size());

        // Test decoding some encoded values
        System.out.println("\n=== Testing Decode Function ===");
        testDecode(dictionary, encodedTriples);

        // Test specific strings
        System.out.println("\n=== Testing Specific Strings ===");
        testSpecificStrings(dictionary);
    }

    private static List<int[]> parseAndEncodeRDFData(String rdfFilePath, Dictionary dictionary) throws IOException {
        List<int[]> encodedTriples = new ArrayList<>();

        try (RDFAtomParser parser = new RDFAtomParser(new FileReader(rdfFilePath), RDFFormat.NTRIPLES)) {
            int count = 0;
            while (parser.hasNext()) {
                RDFAtom atom = parser.next();
                int[] encodedTriple = dictionary.encodeRDFAtom(atom);
                encodedTriples.add(encodedTriple);

                System.out.println("Triple #" + (++count) + ":");
                System.out.println("  Original: " + atom);
                System.out.println("  Encoded: [" + encodedTriple[0] + ", " +
                        encodedTriple[1] + ", " +
                        encodedTriple[2] + "]");
            }
            System.out.println("Total triples encoded: " + count);
        }
        return encodedTriples;
    }

    private static void testDecode(Dictionary dictionary, List<int[]> encodedTriples) {
        // Test decoding the first triple
        if (!encodedTriples.isEmpty()) {
            int[] firstTriple = encodedTriples.get(0);
            System.out.println("Decoding first triple:");
            System.out.println("  Subject: " + dictionary.decode(firstTriple[0]));
            System.out.println("  Predicate: " + dictionary.decode(firstTriple[1]));
            System.out.println("  Object: " + dictionary.decode(firstTriple[2]));
        }
    }

    private static void testSpecificStrings(Dictionary dictionary) {
        // Test some common strings from the sample data
        String[] testStrings = {
                "http://db.uwaterloo.ca/~galuc/wsdbm/User0",
                "http://schema.org/birthDate",
                "1988-09-24"
        };

        for (String str : testStrings) {
            int encoded = dictionary.encode(str);
            String decoded = dictionary.decode(encoded);
            System.out.println("Testing string: " + str);
            System.out.println("  Encoded: " + encoded);
            System.out.println("  Decoded: " + decoded);
            System.out.println("  Match: " + str.equals(decoded));
        }

        // Test containsString
        System.out.println("\nTesting containsString:");
        System.out.println("  Contains 'http://db.uwaterloo.ca/~galuc/wsdbm/User0': " +
                dictionary.containsString("http://db.uwaterloo.ca/~galuc/wsdbm/User0"));
        System.out.println("  Contains 'NonExistentString': " +
                dictionary.containsString("NonExistentString"));
    }
}
