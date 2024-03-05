
/*
 *
 *  CM Compiler Entry Point
 *  
 *  Author: Kyle Lukaszek
 *  ID: 1113798
 *  Class: CIS4650
 *
 *  ======================
 *  CM Compiler is a compiler for the C- language, a subset of the C language.
 *  The compiler is implemented in Java and uses JFlex for lexical analysis and CUP for grammar rules and syntax.
 *  
 *  To scan and parse a C- program, run the following command:
 *  java CM <filename.cm> [-a]
 *  
 *  -a Flag: Prints Abstract Syntax Tree of parsed program.
 *
 */

import absyn.*;

public class CM {
  public static void main(String[] args) throws Exception {

    if (args.length == 0) {
      System.out.println("Usage: java CM <filename.cm> [-a]");
      return;
    }

    try {
      parser p = new parser(new Lexer(new java.io.FileReader(args[0])));
      Absyn result = (Absyn) (p.parse().value);

      for (int i = 1; i < args.length; i++) {
        if (args[i].equals("-a")) {
          p.PrintAST(true);
        }
      }

      if (parser.printAST == true) {
        System.out.println("The abstract syntax tree is:");
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0);
      }
      System.out.println("Parsing completed successfully!"); // Handle the parsing result

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
