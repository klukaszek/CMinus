# CIS*4650 Compilers Checkpoint 1

## Author: Kyle Lukaszek
## ID: 1113798

### Compilation

To compile the project type in the following command.

`bash
make`

This will generate the following:

- Scanner 
- CM Executable

To test the scanner:

`java -cp /usr/share/java/cup.jar Scanner < file.cm`

To test the C Minus parser:

`java -cp /usr/share/java/cup.jar  CM file.cm [-a: print AST][-s: semantics and symbols]`

The -a flag prints an AST

The -s flag shows the semantic analysis and the building of the symbol table.

To redirect the output:

`java -cp /usr/share/java/cup.jar  CM file.cm -a > output.txt`

To remove the files:
`make clean`

### Documentation

The documentation and discussion of the work done for checkpoint 2 are in *C2_Doc.pdf*

### Tests

I only provided 3 tests because I am already late in submitting the assignment and testing extensively would take more time than I have.

The features that are being tested are listed at the top of each test file.
