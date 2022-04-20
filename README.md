# Pattern Searching
This project aims to allow searching through text using a regular expression pattern. It does this in two steps:
1. Compile the regular expression into a FSM description, output to standard out.
2. Search an input file provided through a command line argument using the FSM description provided through standard in.

## Compiler
### Grammar
The grammar of the regular expression compiler is as follows:
```
E → A
E → AE
A → C
A → C|A
C → R
C → RC
R → P
R → P*
R → P+
R → P?
P → X
P → (X)
X → F
X → \n
F → .
F → l

Where:

E is a regular expression
A is an alternation
C is concatenation
R is repetition
P is parenthesis
X is an escaped character
n is a non-literal
F is a factor
. is any literal
l is a literal
* is 0 or more
+ is 1 or more
? is 0 or 1
| is alternation
() can be used to raise precedence of an expression
\ is an escape character
```

### Precedence
The order of precedence is as follows:
1. escaped characters (i.e. symbols preceded by \)
2. parentheses (i.e. the most deeply nested regexps have the highest precedence)
3. repetition/option operators (i.e. *, + and ?)
4. concatenation
5. alternation (i.e. | and [ ])

### Pre-Processing
The compiler will also convert the following syntax using a pre-processor:

`[l+n+]` to `(l|l|\n|\n)` for however many literals and non-literals there are except in the case of `']'` which will be moved to the front and escaped: `[\]]`

## Search