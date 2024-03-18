/*
 * File: SemanticAnalyzer.java
 * Author: Kyle Lukaszek
 * ID: 1113798
 * Assignment: CIS4650 Compilers C2
 * Description: This file contains the methods and classes used to build
 * the symbol table and perform semantic analysis on the abstract syntax tree.
 */

import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class SemanticAnalyzer implements AbsynVisitor {

  private static final String NEW_SCOPE = "Entering new scope";
  private static final String EXIT_SCOPE = "Exiting scope";
  private static final int SPACES = 4;

  // Define an enum for the scope type (global or local)
  private enum ScopeType {
    GLOBAL, LOCAL
  }

  public static Boolean errorSemanticAnalysis = false;

  // We store each symbol as a mapped key to a list of nodes that represent the
  // symbol
  private HashMap<String, ArrayList<NodeType>> symbolTable;

  public SemanticAnalyzer() {
    symbolTable = new HashMap<String, ArrayList<NodeType>>();

    // Predefined functions stated in the sematic requirements of the specifications
    VarDecList inputParams = new VarDecList(new SimpleDec(-1, -1, new Type(-1, -1, Type.VOID), null), null);
    FunDec input = new FunDec(-1, -1, new Type(-1, -1, Type.INT), "input", inputParams, null);

    VarDecList outputParams = new VarDecList(new SimpleDec(-1, -1, new Type(-1, -1, Type.VOID), "output"), null);
    FunDec output = new FunDec(-1, -1, new Type(-1, -1, Type.VOID), "output", outputParams, null);

    // Add the predefined functions to the symbolTable
    insertSymbol(input, ScopeType.GLOBAL.ordinal());
    insertSymbol(output, ScopeType.GLOBAL.ordinal());
  }

  // This is identical to calling the accept method on the root node, I just
  // wanted to make it explicit
  public void analyze(Absyn rootNode) {

    rootNode.accept(this, ScopeType.GLOBAL.ordinal());

  }

  // Primary function used to build the symbol symbol and check for redeclarations
  private void insertSymbol(Declaration dec, int level) {

    NodeType node = new NodeType(dec.name, dec, level);

    // Check if the symbol already exists in the symbol symbolTable
    // If the scope is the same, it is a redeclaration
    // If the scope is different, it is a new declaration in a nested scope
    if (symbolTable.containsKey(dec.name)) {

      ArrayList<NodeType> list = symbolTable.get(dec.name);
      NodeType current;

      for (int i = 0; i < list.size(); i++) {

        current = list.get(i);

        // Check if the symbol scope does not conflict with the current scope
        if (current.level >= level) {

          // If the symbol is in the same scope, it is a redeclaration
          if (node.level == current.level && node.name.equals(current.name)) {
            error(dec.row, dec.col, "Redeclaration of symbol " + dec.name);
          }

          list.add(i, node);
          break;

          // Scope is most nested
        } else if (i == list.size() - 1) {
          list.add(node);
          break;
        }
      }

      // If the symbol does not exist in the table, add it
    } else {
      ArrayList<NodeType> list = new ArrayList<NodeType>();
      list.add(node);
      symbolTable.put(dec.name, list);
    }
  }

  // Function to retrieve a symbol from the symbolTable
  private Declaration lookupSymbol(String name, Boolean isFunc, int row, int col) {

    // Check if the symbol exists in the symbolTable
    if (symbolTable.containsKey(name)) {

      // If it does exist, we need to get the list of nodes that represent the symbol
      ArrayList<NodeType> list = symbolTable.get(name);
      NodeType current;

      // Iterate through nodes in the list and return the first instance of the symbol
      for (int i = 0; i < list.size(); i++) {

        current = list.get(i);

        // If we are looking for a function, we need to check if the current node is a
        // function declaration
        // Otherwise, we return the current node since it is the first instance of the
        // symbol in the symbolTable
        if (isFunc && !(current.dec instanceof FunDec)) {
          continue;
        } else {
          return current.dec;
        }
      }
    }

    // If the symbol does not exist in the symbolTable, return an error
    errorSemanticAnalysis = true;
    error(row, col, "Symbol " + name + " not found");
    return new ErrorDec(row, col, null, "Symbol " + name + " not found");
  }

  // Scopes are removed from the symbolTable when they are exited to prevent the
  // same symbol from being declared in a nested scope
  private void removeScopeFromTable(int level) {
    
    // Create an iterator to iterate through the symbolTable keys
    Iterator<String> iterator = symbolTable.keySet().iterator();
    String key;
    ArrayList<NodeType> list;
    NodeType current;

    // Iterate through the symbolTable and remove any symbols that are in the current scope
    while (iterator.hasNext()) {
      key = iterator.next();

      // Using the key, get the list of nodes that represent the symbol
      list = symbolTable.get(key);
      
      // Iterate through the list of nodes that represent the symbol
      for (int i = 0; i < list.size(); i++) {
        current = list.get(i);
        
        // If the current node is in the expected scope, remove it from the list
        if (current.level == level) {
          indent(level);
          System.out.println(current);
          list.remove(i);
          i--;
        }
      }

      // If the list is empty, remove the key from the symbolTable
      if (list.isEmpty()) {
        iterator.remove();
      }
    }
  }

  // Check if the expression being returned matches a function's expected return
  // type
  // This function is called from checkFunctionReturnType()
  private void checkReturnType(String name, ReturnExp exp, int expectedType) {

    if (exp.getReturnType() != expectedType) {
      error(exp.row, exp.col, "Return type does not match function " + name + " expected return type: " + expectedType);
    }

  }

  // Check if non-void function has a return statement
  // If the function has a return type, it must have a valid return statement
  // If the function has a void return type, it cannot have a return statement
  // This function also checks if the return expression is the last expression in
  // it's scope block
  // This also checks if the function contains any unreachable code
  // This function can be called recursively to check for return statements in
  // nested scopes
  private boolean checkFunctionReturn(String name, ExpList expList, int returnType) {

    assert (returnType != Type.VOID || returnType != Type.INT || returnType != Type.BOOL);
    assert (expList != null);

    Exp current = null;
    boolean hasReturn = false;
    boolean iterReturn = false;

    // Iterate through the expression list and check for return statements (can be
    // nested in if or while statements)
    while (expList != null) {

      current = expList.head;

      if (current instanceof ReturnExp) {
        hasReturn = true;
        checkReturnType(name, (ReturnExp) current, returnType);

      } else if (hasReturn) {
        error(current.row, current.col, "Unreachable code");

      } else if (current instanceof IfExp) {
        // This will only be true if the if statement and the else statement both
        // contain a return statement
        // Otherwise, we continue to check for a return expression in the function scope
        hasReturn = checkIfElseContainsReturn(name, (IfExp) current, returnType);

      } else if (current instanceof IterExp) {

        boolean localIterReturn = checkIterContainsReturn(name, (IterExp) current, returnType);

        // Only set iterReturn to true if it is not already true
        if (!iterReturn && localIterReturn) {
          iterReturn = true;
        }

      }

      // Get next expression list
      expList = expList.tail;
    }

    // Our only iteration statement is a while loop, so we have to make sure that
    // the function also has a return statement outside of the loop in case the loop
    // is never executed
    if (iterReturn && !hasReturn) {
      error(current.row, current.col, "Function " + name
          + " does not have a return statement outside of while loop. Function may not return type: " + returnType);
    }

    // If the expected return type is not void and there is no return statement,
    // throw an error because the function is missing a return statement
    if (returnType != Type.VOID && !hasReturn) {
      error(current.row, current.col,
          "Function " + name + " does not have a return statement. Expected return type: " + returnType);
    }

    return hasReturn;
  }

  // Check if the if statement has a return statement
  // If it does, the function still needs a return statement outside of the if
  // statement in the event the if statement is not executed
  // If the if statement has an else statement, the function needs to return from
  // both the if and else statements to be valid
  private boolean checkIfElseContainsReturn(String name, IfExp ifExp, int returnType) {

    boolean ifHasReturn = false;
    boolean elseHasReturn = false;

    // Check if the if statement is an entire if block.
    // If it is, we need to check if the if block has a return statement
    if (ifExp.ifDo instanceof CmpExp) {
      CmpExp ifBlock = (CmpExp) ifExp.ifDo;
      ifHasReturn = checkFunctionReturn(name, ifBlock.expList, returnType);
      // If the if statement is only 1 expression, we need to check if the expression
      // is a return statement
    } else if (ifExp.ifDo instanceof ReturnExp) {
      ifHasReturn = true;
      checkReturnType(name, (ReturnExp) ifExp.ifDo, returnType);
    }

    // Check if the else statement is an entire else block.
    // If it is, we need to check if the else block has a return statement.
    if (ifExp.elseDo instanceof CmpExp) {
      CmpExp elseBlock = (CmpExp) ifExp.elseDo;
      elseHasReturn = checkFunctionReturn(name, elseBlock.expList, returnType);
      // If the else statement is only 1 expression, we need to check if the
      // expression is a return statement
    } else if (ifExp.elseDo instanceof ReturnExp) {
      elseHasReturn = true;
      checkReturnType(name, (ReturnExp) ifExp.elseDo, returnType);
    }

    // As long as both the if and else statements have a return statement, the
    // function has a valid return statement,
    // There may still be unreachable code, but the function has a valid return
    // statement
    return ifHasReturn && elseHasReturn;
  }

  // Check if the iteration statement has a return statement, very similar to the
  // if statement check
  private boolean checkIterContainsReturn(String name, IterExp iterExp, int returnType) {

    boolean hasReturn = false;

    if (iterExp.body == null) {
      error(iterExp.row, iterExp.col, "Function " + name + " contains an empty while loop.");

    } else if (iterExp.body instanceof CmpExp) {
      hasReturn = checkFunctionReturn(name, ((CmpExp) iterExp.body).expList, returnType);

    } else if (iterExp.body instanceof ReturnExp) {
      hasReturn = true;
      checkReturnType(name, (ReturnExp) iterExp.body, returnType);
    }

    return hasReturn;
  }

  // Generic error message
  private void error(int row, int col, String message) {
    System.err.println("Error at line " + (row + 1) + ":" + (col + 1) + " - " + message);
  }

  /* ----------------- Visitor Methods and Helpers ----------------- */

  private void enterScope() {
    System.out.println(NEW_SCOPE);
  }

  private void exitScope() {
    System.out.println(EXIT_SCOPE);
  }

  // Indent based on current scope level
  private void indent(int level) {
    for (int i = 0; i < SPACES * level; i++) {
      System.out.print("  ");
    }
  }

  public void visit(ArrayDec decArr, int level) {
  }

  public void visit(AssignExp exp, int level) {
  }

  public void visit(BoolExp exp, int level) {
  }

  public void visit(CallExp exp, int level) {
  }

  public void visit(CmpExp exp, int level) {
  }

  public void visit(CondExp exp, int level) {
  }

  public void visit(DecList decList, int level) {
  }

  public void visit(ErrorDec dec, int level) {
  }

  public void visit(ErrorExp exp, int level) {
  }

  public void visit(ExpList exp, int level) {
  }

  public void visit(FunDec dec, int level) {
  }

  public void visit(IfExp exp, int level) {
  }

  public void visit(IndexVar var, int level) {
  }

  public void visit(IterExp exp, int level) {
  }

  public void visit(IntExp exp, int level) {
  }

  public void visit(NilExp exp, int level) {
  }

  public void visit(OpExp exp, int level) {
  }

  public void visit(ReturnExp exp, int level) {
  }

  public void visit(SimpleDec dec, int leel) {
  }

  public void visit(SimpleVar var, int level) {
  }

  public void visit(Type type, int level) {
  }

  public void visit(VarDec dec, int level) {
  }

  public void visit(VarDecList decList, int level) {
  }

  public void visit(VarExp exp, int level) {
  }

}
