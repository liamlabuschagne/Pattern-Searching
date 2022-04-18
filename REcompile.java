class REcompile {

    private static String r = "";
    private static int i = 0;

    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("Usage: java REcompile.java \"[regular expression]\"");
        }
        r = args[0];
        System.out.println("Compiling: " + r);
        expression();
    }

    private static void expression() {
        term();
        if (i == r.length()) {
            return;
        }
        expression();
    }

    private static void term() {
        closure();
        if (i >= r.length())
            return;
        if (r.charAt(i) == '|') {
            i++;
            term();
            return;
        }
    }

    private static void closure() {
        factor();
        if (i >= r.length())
            return;
        if (r.charAt(i) == '*' || r.charAt(i) == '+' || r.charAt(i) == '?') {
            i++;
            return;
        }
    }

    private static void factor() {

        if (r.charAt(i) == '(') {
            i++;
            expression();
            return;
        }

        if (r.charAt(i) == ')') {
            i++;
            return;
        }

        if (isLiteral() || r.charAt(i) == '.') {
            i++;
            return;
        }

        if (r.charAt(i) == '\\' && !isLiteral(r.charAt(i + 1))) {
            i += 2;
            return;
        }

        error();
    }

    private static boolean isLiteral() {
        if (i == r.length()) {
            error();
        }
        return isLiteral(r.charAt(i));
    }

    private static boolean isLiteral(char c) {
        String nonLiterals = "()*+?|\\";
        return !nonLiterals.contains(String.valueOf(c));
    }

    private static void error() {
        System.err.println("Malformed expression near position: " + i);
        System.exit(0);
    }
}