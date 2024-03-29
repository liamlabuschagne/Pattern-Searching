/*
    This part was done by Liam Labuschagne ID: 1575313
*/

class REcompile {

    private static String r = ""; // The expression we are compiling
    private static int i = 0; // Pointer into the expression
    private static int s = 0; // Which state are we building
    private static char[] ch; // Character of each state in FSM
    private static int[] n1; // Next 1 of each state in FSM
    private static int[] n2; // Next 2 of each state in FSM
    private static boolean inNestedExpression = false;

    // debug mode toggle
    private static boolean debugMode = false;

    public static void main(String args[]) {
        if (args.length == 0 || args.length > 2) {
            System.err.println("Usage: java REcompile.java \"[REGULAR EXPRESSION]\"\n-d\tDebug Mode");
            return;
        }

        if (args.length == 2) {
            debugMode = true;
        }

        r = args[0];

        // Initialize FSM arrays to be the length of the expression since we need one
        // state per symbol in the expression except for parenthesis but it is not worth
        // scanning through to see how many of them there are.

        ch = new char[r.length() * 3];
        n1 = new int[r.length() * 3];
        n2 = new int[r.length() * 3];

        // Pad the start of the expression with a single direction branching machine
        setState(s, (char) 1, s + 1, s + 1);
        s++;

        // Convert all [abc] to (a|b|c)
        preProcess();

        if (debugMode)
            System.out.println("Compiling: " + r);

        // Start the compilation
        expression();

        // Send FSM description to standard out
        showFSM();
    }

    private static void preProcess() {
        // Find non-escaped [

        int start = 0;
        int end = 0;
        boolean inside = false;

        for (int j = 0; j < r.length(); j++) {
            if (r.charAt(j) == '[' && (j == 0 || (r.charAt(j - 1) != '\\'))) {
                start = j;
                inside = true;
            }

            if (inside && r.charAt(j) == ']' && r.charAt(j - 1) != '\\') {
                end = j;
                inside = false;
                if (start + 1 == end)
                    error();
                String processed = alternationBracketsToExpression(r.substring(start + 1, end));
                r = r.substring(0, start) + processed + r.substring(end + 1);
            }
        }
    }

    private static String alternationBracketsToExpression(String alternationString) {

        String processed = "(";
        boolean hasClosingSquareBracket = false;
        for (int j = 0; j < alternationString.length(); j++) {

            if (alternationString.charAt(j) == ']') {
                // Flag this and move on
                hasClosingSquareBracket = true;
                continue;
            } else if (!isLiteral(alternationString.charAt(j)) || alternationString.charAt(j) == '.') {
                processed += "\\";
            }
            processed += alternationString.charAt(j);
            if ((j + 1) < alternationString.length()) {
                processed += "|";
            }
        }

        if (hasClosingSquareBracket) {
            // Prepend it
            processed = processed.substring(0, 1) + "\\]|" + processed.substring(1);
        }

        processed += ")";

        return processed;
    }

    private static int expression() {
        // We can't start an expression with )
        if (i < r.length() && r.charAt(i) == ')')
            error();

        int start = alternation();

        if (i == 0)
            start = 0;

        // Optionally concatenate another expression
        if (i < r.length() && (isFactor() || r.charAt(i) == '(' || r.charAt(i) == '\\'))
            expression();

        return start;
    }

    private static int alternation() {
        int prev = s - 1;
        int start = concatenation();
        int t1 = start;
        // Optionally do alternation with another alternation.
        if (i < r.length() && r.charAt(i) == '|') {
            i++;

            int endOfT1 = s - 1;

            // Build bm (t1 and t2 which is next)
            setState(s, (char) 1, t1, s + 1);
            int bm = s;
            start = bm; // This machine starts at branching machine
            s++;

            // Set prev to bm
            if (n1[prev] == n2[prev])
                setState(prev, ch[prev], bm, bm);
            else
                setState(prev, ch[prev], n1[prev], bm);

            // Build t2
            alternation();

            // Set end of t1 to next
            setState(endOfT1, ch[endOfT1], s, s);

        }

        return start;
    }

    private static int concatenation() {
        int start = repetition();

        // Optionally concatenates another concatenation
        if (i < r.length() && (isFactor() || r.charAt(i) == '(' || r.charAt(i) == '\\'))
            concatenation();
        // The only cases where we wouldn't concatenate are: eos or alternation
        else if (i < r.length() && r.charAt(i) != ')' && r.charAt(i) != '|')
            error();
        else if ((i + 1) == r.length() && r.charAt(i) == '|') // Check we aren't ending on |
            error();
        // Check we aren't ending on ) when not in a nested expression
        else if (i < r.length() && !inNestedExpression && r.charAt(i) == ')')
            error();

        return start;
    }

    private static int repetition() {
        int prev = s - 1;
        int start = parenthesis();
        int t1 = start;
        // Optionally does a repetition
        if (i < r.length() && r.charAt(i) == '*') {
            i++;

            // Build bm (t1 and next)
            setState(s, (char) 1, t1, s + 1);
            int bm = s;
            start = bm;
            s++;

            // Set prev to bm
            if (n1[prev] == n2[prev])
                setState(prev, ch[prev], bm, bm);
            else
                setState(prev, ch[prev], n1[prev], bm);

            // Pad end with sin dir bm
            setState(s, (char) 1, s + 1, s + 1);
            s++;

        } else if (i < r.length() && r.charAt(i) == '+') {
            i++;

            // Build bm to choose between t1 and next
            setState(s, (char) 1, t1, s + 1);
            s++;

            // Start is still t1 since this is 1 or more.

            // Finish off with single direction branching machine
            setState(s, (char) 1, s + 1, s + 1);
            s++;
        } else if (i < r.length() && r.charAt(i) == '?') {
            i++;

            // Build bm (t1 and next)
            setState(s, (char) 1, t1, s + 1);
            int bm = s;
            start = bm;
            s++;

            // Set t1 to be next (only allow one visit)
            setState(t1, ch[t1], s, s);

            // Set prev to bm
            if (n1[prev] == n2[prev])
                setState(prev, ch[prev], bm, bm);
            else
                setState(prev, ch[prev], n1[prev], bm);

            // Pad with sin dir bm
            setState(s, (char) 1, s + 1, s + 1);
            s++;
        }
        return start;
    }

    private static int parenthesis() {
        int start = -1;

        if (i < r.length())
            start = escape();

        // Or it does a nested expression (E)
        if (i < r.length() && r.charAt(i) == '(') {
            i++;
            inNestedExpression = true;
            if (i >= r.length() || r.charAt(i) == ')')
                error(); // Empty expression
            start = expression();
            if (i >= r.length() || r.charAt(i) != ')')
                error(); // Didn't close parenthesis
            inNestedExpression = false;
            i++;
        }

        return start;
    }

    private static int escape() {
        int start = -1;

        if (r.charAt(i) == '\\') {
            // Handle escaped non-literal
            i++; // Move past the \

            // Create a machine as if it is a literal
            setState(s, r.charAt(i), s + 1, s + 1);
            start = s;
            s++;
            i++;
        } else if (isLiteral()) {
            start = factor();
        } else if (r.charAt(i) != '(') { // the only other possibility is that we are starting a nested expression
            error(); // Malformed expression
        }
        return start;
    }

    private static int factor() {
        int start = -1;

        if (!isLiteral() && r.charAt(i) != '.')
            error();

        char c = r.charAt(i);

        if (r.charAt(i) == '.') {
            c = (char) 2; // Ascii code 2 for wildcard
        }

        // Handle literal
        setState(s, c, s + 1, s + 1);
        start = s;

        s++;
        i++;
        return start;
    }

    // Some utility functions used in lookahead's

    private static boolean isFactor() {
        if (i >= r.length())
            error();
        return (isLiteral(r.charAt(i)) || r.charAt(i) == '.');
    }

    private static boolean isLiteral() {
        if (i >= r.length())
            error();
        return isLiteral(r.charAt(i));
    }

    private static boolean isLiteral(char c) {
        String nonLiterals = "()*+?|\\";
        return !nonLiterals.contains(String.valueOf(c));
    }

    // Report malformed expression and terminate without finishing recursion
    private static void error() {
        error("");
    }

    private static void error(String from) {
        System.err.println("Malformed expression near position: " + i + from);

        String padding = "";
        for (int j = 0; j < i; j++) {
            padding += " ";
        }
        System.out.println(r);
        System.out.println(padding + "^");

        System.exit(0);
    }

    // Utility function to update all three arrays simultaneously
    private static void setState(int st, char c, int ne1, int ne2) {
        ch[st] = c;
        n1[st] = ne1;
        n2[st] = ne2;
    }

    // Output FSM description to standard out
    private static void showFSM() {
        if (debugMode)
            showFSMdebug();
        else
            for (int j = 0; j < s; j++) {
                System.out.println(j + " " + ch[j] + " " + n1[j] + " " + n2[j]);
            }
    }

    // Nice debugging display format
    private static void showFSMdebug() {
        System.out.println(" -----------");
        System.out.println("|s#|ch|n1|n2|");
        for (int j = 0; j < s; j++) {

            String stateNumber = j + " ";
            if (j > 9) {
                stateNumber = j + "";
            }

            String character = ch[j] + " ";
            if (ch[j] == (char) 1) {
                character = "br";
            }
            if (ch[j] == (char) 2) {
                character = "wi";
            }

            String nextOne = n1[j] + " ";
            if (n1[j] > 9) {
                nextOne = n1[j] + "";
            }

            String nextTwo = n2[j] + " ";
            if (n2[j] > 9) {
                nextTwo = n2[j] + "";
            }

            System.out.println("|" + stateNumber + "|" + character + "|" + nextOne + "|" + nextTwo + "|");
        }
        System.out.println(" -----------");
    }
}