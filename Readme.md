# CIS*4650 Compilers Checkpoint 3

## Author: Kyle Lukaszek
## ID: 1113798

### Compilation

To compile the project type in the following command.

`make`

This will generate the following:

- Scanner 
- CM Executable

To test the scanner:

`java -cp /usr/share/java/cup.jar:. Scanner < file.cm`

To test the C Minus parser:

`java -cp /usr/share/java/cup.jar:. CM file.cm [-a: print AST][-s: semantics and symbols][-c: compile]`

The -a flag prints an AST

The -s flag shows the semantic analysis and the building of the symbol table.

The -c flag compiles the AST to machine code for the provided Turing Machine Simulator.

To redirect the output:

`java -cp /usr/share/java/cup.jar:. CM file.cm -a > output.txt`

To remove the files:
`make clean`

#### Using TMSimulator

To generate machine code execute the following command:

`java -cp /usr/share/java/cup.jar:. CM file.cm -c > output.tm`

Then go to the TMSimulator directory and compile the simulator:

```
cd TMSimulator/
make
```

This will create an executable "tm".

To test generated code:

`./tm <path to .tm file>`

### Documentation

The documentation and discussion of the work done for checkpoint 2 are in *C2_Doc.pdf*

### Tests

See [1-10.cm] for more info about the tests.
