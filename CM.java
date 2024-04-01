
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
import java.util.ArrayList;

public class CM {

  enum Flag {
    AST,
    SEMANTIC,
    CODEGEN
  }

  public static void main(String[] args) throws Exception {

    if (args.length == 0) {
      System.out.println("Usage: java CM <filename.cm> [-a][-s][-c]");
      return;
    }

    ArrayList<Flag> flags = new ArrayList<Flag>();

    try {
      parser p = new parser(new Lexer(new java.io.FileReader(args[0])));
      Absyn result = (Absyn) (p.parse().value);

      for (int i = 1; i < args.length; i++) {
        if (args[i].equals("-a")) {
          flags.add(Flag.AST);
        }
        if (args[i].equals("-s")) {
          flags.add(Flag.SEMANTIC);
        }
        if (args[i].equals("-c")) {
          flags.add(Flag.CODEGEN);
        }
      }
      
      if (flags.contains(Flag.AST)) {
        System.out.println("The abstract syntax tree is:");
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0, false);
      }

      if (flags.contains(Flag.SEMANTIC)) {
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(result);
      }

      if (flags.contains(Flag.CODEGEN)) {
        CodeGenerator generator = new CodeGenerator();
        generator.visit(result, args[0]);
      }

      System.out.println("\nParsing completed");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
