# Pattern Searching
This project aims to allow searching through text using a regular expression pattern. It does this in two steps:
1. Compile the regular expression into a FSM description, output to standard out.
2. Search an input file provided through a command line argument using the FSM description provided through standard in.

## Compiler
### Grammar
The grammar of the regular expression compiler is as follows:
```
E → T
E → TE
T → F
T → F*
T → F+
T → F?
T → F|T
F → .
F → l
F → \n
F → (E)

Where:

E is a regular expression
T is a term
F is a factor
l is a literal
n is a non-literal
. is any literal
* is 0 or more
+ is 1 or more
? is 0 or 1
| is alternation
() can be used to raise precedence of an expression
\ is an escape character
```

### Pre-Processing
The compiler will also convert the following syntax using a pre-processor:

`[l+n+]` to `(l|l|\n|\n)` for however many literals and non-literals there are except in the case of `']'` which will be moved to the front and escaped: `[\]]`

## Search