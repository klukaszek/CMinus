# CIS*4650 Compilers Checkpoint 1

## Author: Kyle Lukaszek
## ID: 1113798

### Compilation

To compile the project type in the following command.

`bash

make

`

This will generate the following:

- Scanner 
- CM Executable

To test the scanner:

`bash

java Scanner < file.cm

`

To test the C Minus parser

`bash

java CM file.cm [-a: print AST]

`

The -a flag prints an AST

### Documentation

The documentation and discussion of the work done for checkpoint 1 are in *C1_Doc.pdf*

### Tests

I only provided 3 tests because I am already late in submitting the assignment and testing extensively would take more time than I have.

The features that are being tested are listed at the top of each test file.
