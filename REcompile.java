class REcompile {

    private static String r = ""; // The expression we are compiling
    private static int i = 0; // Pointer into the expression
    private static int s = 0; // Which state are we building
    private static char[] ch; // Character of each state in FSM
    private static int[] n1; // Next 1 of each state in FSM
    private static int[] n2; // Next 2 of each state in FSM

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

        if (debugMode)
            System.out.println("Compiling: " + r);

        // Initialize FSM arrays to be the length of the expression since we need one
        // state per symbol in the expression except for parenthesis but it is not worth
        // scanning through to see how many of them there are.

        ch = new char[r.length() * 3];
        n1 = new int[r.length() * 3];
        n2 = new int[r.length() * 3];

        expression();

        showFSM();
    }

    private static int expression() {
        // Every expression starts with a single direction branching machine
        setState(s, (char) 1, s + 1, s + 1);
        int start = s;
        s++;

        alternation();

        if (i < r.length() && (isFactor() || r.charAt(i) == '(' || r.charAt(i) == '\\'))
            expression();

        return start;
    }

    private static int alternation() {
        int start = concatenation();
        int prev = start - 1;
        // Optionally do alternation with another alternation.
        if (i < r.length() && r.charAt(i) == '|') {
            i++;
            setState(s, (char) 1, start, s + 1);
            setState(prev, ch[prev], s, s);
            start = s;
            int endOfConcatenation = s - 1;
            s++;
            setState(s, (char) 1, s + 1, s + 1);
            s++;
            alternation();
            setState(endOfConcatenation, ch[endOfConcatenation], s, s);
            setState(s, (char) 1, s + 1, s + 1);
            s++;

        }

        return start;
    }

    private static int concatenation() {
        int start = repetition();

        // Optionally concatenates another concatenation
        if (i < r.length() && (isFactor() || r.charAt(i) == '(' || r.charAt(i) == '\\'))
            concatenation();

        return start;
    }

    private static int repetition() {
        int start = parenthesis();
        int prev = start - 1;

        // Optionally does a repetition
        if (i < r.length() && r.charAt(i) == '*') {
            i++;

            // Move start forward one, replacing it with a padding machine
            setState(s, ch[start], s + 1, s + 1); // After literal, jump to branching machine again
            int literal = s;
            s++;
            setState(start, (char) 1, s, s); // which points to the branching machine

            setState(s, (char) 1, literal, s + 1);
            int b = s;
            s++;
            // Update prev machine to point to branching machine
            setState(prev, ch[prev], b, b);

            // Finish off with single direction branching machine
            setState(s, (char) 1, s + 1, s + 1);
            s++;
        } else if (i < r.length() && r.charAt(i) == '+') {
            i++;
            setState(s, (char) 1, start, s + 1);
            s++;

            // Finish off with single direction branching machine
            setState(s, (char) 1, s + 1, s + 1);
            s++;
        } else if (i < r.length() && r.charAt(i) == '?') {
            i++;

            // Move start forward one, replacing it with a padding machine
            setState(s, ch[start], s + 2, s + 2); // After literal, jump to exit
            int literal = s;
            s++;
            setState(start, (char) 1, s, s); // which points to the branching machine

            setState(s, (char) 1, literal, s + 1);
            int b = s;
            s++;
            // Update prev machine to point to branching machine
            setState(prev, ch[prev], b, b);

            // Finish off with single direction branching machine
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
            start = expression();
            if (i >= r.length() || r.charAt(i) != ')')
                error();
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
        }
        return start;
    }

    private static int factor() {
        int start = -1;

        if (isLiteral() || r.charAt(i) == '.') {
            // Handle literal
            setState(s, r.charAt(i), s + 1, s + 1);
            start = s;

            s++;
            i++;
        }
        return start;
    }

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

    private static void setState(int st, char c, int ne1, int ne2) {
        ch[st] = c;
        n1[st] = ne1;
        n2[st] = ne2;
    }

    private static void showFSM() {
        if (debugMode)
            showFSMdebug();
        else
            for (int j = 0; j < s; j++) {
                System.out.println(j + " " + ch[j] + " " + n1[j] + " " + n2[j] + " ");
            }
    }

    private static void showFSMdebug() {
        System.out.println(" ___________");
        System.out.println("|s#|ch|n1|n2|");
        for (int j = 0; j < s; j++) {

            String stateNumber = j + " ";
            if (j > 9) {
                stateNumber = j + "";
            }

            String character = ch[j] + " ";
            if (ch[j] == (char) 1) {
                character += " ";
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