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

  private static final int SPACES = 4;

  private static boolean silent = false;

  // Define an enum for the scope type (global or local)
  private enum ScopeType {
    GLOBAL, LOCAL
  }

  public static Boolean errorSemanticAnalysis = false;

  // We store each symbol as a mapped key to a list of nodes that represent the
  // symbol
  private HashMap<String, ArrayList<NodeType>> symbolTable;

  public SemanticAnalyzer(boolean isSilent) {
    
    // Set the silent flag to suppress output
    silent = isSilent;

    symbolTable = new HashMap<String, ArrayList<NodeType>>();

    // Predefined functions stated in the sematic requirements of the specifications
    FunDec input = new FunDec(-1, -1, new Type(-1, -1, Type.INT), "input", null, null);

    VarDecList outputParams = new VarDecList(new SimpleDec(-1, -1, new Type(-1, -1, Type.ANY), "output"), null);
    FunDec output = new FunDec(-1, -1, new Type(-1, -1, Type.VOID), "output", outputParams, null);

    log("Predefined functions:");

    // Add the predefined functions to the symbolTable
    insertSymbol(input, ScopeType.GLOBAL.ordinal());
    insertSymbol(output, ScopeType.GLOBAL.ordinal());
  }

  // This is identical to calling the accept method on the root node, I just
  // wanted to make it explicit
  public void analyze(Absyn rootNode) {
    rootNode.accept(this, ScopeType.GLOBAL.ordinal(), false);
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
            warn(dec.row, dec.col, "Redeclaration of symbol: " + dec.name);
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

    // We do this so we don't print any symbols of NULL (such as having void as the
    // only parameter of a function)
    if (dec.name != null) {
      indent(level);
      log("Inserting symbol (" + dec + ")");
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

    // Iterate through the symbolTable and remove any symbols that are in the
    // current scope
    while (iterator.hasNext()) {
      key = iterator.next();

      // Using the key, get the list of nodes that represent the symbol
      list = symbolTable.get(key);

      // Iterate through the list of nodes that represent the symbol
      for (int i = 0; i < list.size(); i++) {
        current = list.get(i);

        // If the current node is in the expected scope, remove it from the list
        if (current.level == level) {
          // Ignore any symbols with the name null (such as having void as the only
          // parameter of a function)
          if (current.name != null) {
            indent(level);
            log("Removing Symbol: (" + current + ")");
          }

          // Remove the symbol from the list
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
      String type = typeToString(exp.getReturnType());

      error(exp.row, exp.col,
          "Return type '" + type + "' does not match function. '" + name + "()' expected return type: " + typeToString(expectedType));
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
  //
  // I use a quiet flag to suppress error messages when checking for return when
  // recursively calling this function
  private boolean checkFunctionReturn(String name, ExpList expList, int returnType, boolean quiet) {

    assert (returnType == Type.VOID || returnType == Type.INT || returnType == Type.BOOL);
    assert (expList != null);

    Exp current = null;
    boolean hasReturn = false;
    boolean condReturn = false;
    boolean iterReturn = false;

    // Iterate through the expression list and check for return statements (can be
    // nested in if or while statements)
    while (expList != null) {

      current = expList.head;

      if (current instanceof ReturnExp) {
        hasReturn = true;
        checkReturnType(name, (ReturnExp) current, returnType);

      } else if (hasReturn) {
        if (!quiet)
          error(current.row, current.col, "Unreachable code");

      } else if (current instanceof IfExp) {
        // This will only be true if the if statement and the else statement both
        // contain a return statement
        // Otherwise, we continue to check for a return expression in the function scope
        condReturn = checkIfElseContainsReturn(name, (IfExp) current, returnType);

      } else if (current instanceof IterExp) {

        iterReturn = checkIterContainsReturn(name, (IterExp) current, returnType);

      }

      // Get next expression list
      expList = expList.tail;
    }

    if (condReturn) {
      hasReturn = true;
    }

    // Our only iteration statement is a while loop, so we have to make sure that
    // the function also has a return statement outside of the loop in case the loop
    // is never executed
    if (iterReturn && !hasReturn) {
      if (!quiet)
        error(current.row, current.col, "Function " + name
            + " does not have a return statement outside of while loop. Function may not return type: "
            + typeToString(returnType));
    }
    // If the expected return type is not void and there is no return statement,
    // throw an error because the function is missing a return statement
    else if (returnType != Type.VOID && !hasReturn) {
      if (!quiet)
        error(current.row, current.col,
            "Function " + name
                + " may not return properly. Make sure all branches return, or function has general return statement. Expected return type: "
                + typeToString(returnType));
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
      ifHasReturn = checkFunctionReturn(name, ifBlock.expList, returnType, true);
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
      elseHasReturn = checkFunctionReturn(name, elseBlock.expList, returnType, true);
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
      hasReturn = checkFunctionReturn(name, ((CmpExp) iterExp.body).expList, returnType, true);

    } else if (iterExp.body instanceof ReturnExp) {
      hasReturn = true;
      checkReturnType(name, (ReturnExp) iterExp.body, returnType);
    }

    return hasReturn;
  }

  // Generic error message, this should be printed no matter if silent is true or false
  private void error(int row, int col, String message) {
    System.err.println("\nError at line " + (row + 1) + ":" + (col + 1) + " - " + message);
    System.err.println();
  }
  
  // Generic warning message, this should not be printed if silent is true
  private void warn(int row, int col, String message) {
    System.err.println("\nWarning at line " + (row + 1) + ":" + (col + 1) + " - " + message);
    System.err.println();
  }

  public String typeToString(int type) {
    switch (type) {
      case Type.VOID:
        return "void";
      case Type.INT:
        return "int";
      case Type.BOOL:
        return "bool";
      default:
        return "unknown";
    }
  }

  /* ----------------- Visitor Methods and Helpers ----------------- */

  private void enterScope(String msg) {
    log("Entering new scope: (" + msg + ")");
  }

  private void exitScope(String msg) {
    log("Exiting scope: (" + msg + ")");
  }

  private void log (String msg) {
    if (!silent) System.out.println(msg);
  }

  // Indent based on current scope level
  private void indent(int level) {
    if (silent) return;
    for (int i = 0; i < SPACES * level; i++) {
      System.out.print("  ");
    }
  }

  public void visit(ArrayDec decArr, int level, boolean isAddr) {

    if (decArr == null) {
      return;
    }

    if (decArr.type.type == Type.VOID) {
      error(decArr.row, decArr.col, "Invalid array declaration. Array type cannot be void.");
    }

    if (!(decArr.size instanceof IntExp)) {
      error(decArr.row, decArr.col, "Invalid array size. Array size must be an integer.");
      // Ensure that the array size is greater than 0
    } else {
      IntExp size = (IntExp) decArr.size;
      if (size.value < 0) {
        error(decArr.row, decArr.col, "Array size must be greater than 0");
      } else if (((IntExp) decArr.size).value == 0) {
        warn(decArr.row, decArr.col,
            "Array size not defined. Make sure this is a function parameter and not a variable declaration.");
      }
    }

    // Set the visibility of the array declaration based on the current scope level
    if (level - 1 > ScopeType.GLOBAL.ordinal()) {
      decArr.nestLevel = ScopeType.LOCAL.ordinal();
    } else {
      decArr.nestLevel = ScopeType.GLOBAL.ordinal();
    }

    // Add the array declaration to the symbolTable
    insertSymbol(decArr, level);
  }

  public void visit(AssignExp aExp, int level, boolean isAddr) {

    if (aExp == null) {
      return;
    }

    // Check left hand side of the assignment expression
    if (aExp.var != null) {
      aExp.var.accept(this, level, isAddr);
      aExp.exp.accept(this, level, isAddr);

      Declaration leftDec = lookupSymbol(aExp.var.name, false, aExp.row, aExp.col);

      if (leftDec.type == null) {
        aExp.dtype = new ErrorDec(aExp.row, aExp.col, null, "Invalid assignment expression. Symbol not found.");
        return;
      }

      // Get type of left hand side of the assignment expression, and the type of the
      // associated expression
      Type varType = leftDec.type;
      Type expType;
      
      if (aExp.exp.dtype == null) {
        expType = new Type(aExp.exp.row, aExp.exp.col, Type.UNKNOWN);
      } else {
        expType = aExp.exp.dtype.type;
      }

      if (varType == null || expType == null) {
        error(aExp.row, aExp.col, "Invalid assignment expression.");
        aExp.dtype = new ErrorDec(aExp.row, aExp.col, null, "Invalid assignment expression. Type not found.");
        return;
      }

      // Check if the types of the left hand side and right hand side of the
      // assignment expression match
      if (varType.type != expType.type) {
        error(aExp.row, aExp.col,
            "Type mismatch in assignment expression. Expected type: " + varType + " | Actual type: " + expType);
      }

      // Assign the assignment expression a type declaration so that any lookups calls
      // can get the type of the expression
      aExp.dtype = new SimpleDec(aExp.row, aExp.col, varType, null);
    } else {
      aExp.exp.accept(this, level, isAddr);
      error(aExp.row, aExp.col, "Invalid assignment expression");
      aExp.dtype = new ErrorDec(aExp.row, aExp.col, null, null);
    }
  }

  public void visit(BoolExp exp, int level, boolean isAddr) {
    if (exp != null) {
      exp.dtype = new SimpleDec(exp.row, exp.col, new Type(exp.row, exp.col, Type.BOOL), null);
    } else {
      error(exp.row, exp.col, "Invalid boolean expression: NULL");
      exp.dtype = new ErrorDec(exp.row, exp.col, null, "Invalid boolean expression");
    }
  }

  public void visit(CallExp cExp, int level, boolean isAddr) {

    if (cExp == null) {
      return;
    }

    // If the function call is properly defined, we need to accept the arguments and
    // check them properly
    if (cExp.args != null && cExp.args.head != null) {
      cExp.args.accept(this, level, isAddr);
    }

    // Lookup the function call in the symbolTable to check if it exists
    Declaration dec = lookupSymbol(cExp.func, true, cExp.row, cExp.col);

    // If the function call is valid, we need to check if the arguments match the
    // function's expected parameters
    // If the function call is invalid, we need to print an error message
    if (dec instanceof FunDec) {
      FunDec funDec = (FunDec) dec;
      cExp.dtype = funDec;

      // Get arguments so we can iterate through them and check the function
      // parameters against the arguments

      // Expected parameters
      VarDecList funArgs = funDec.params;

      // Passed parameters
      ExpList pList = cExp.args;

      int numArgs = 0;

      // Iterate through passed parameters and check if they match the expected arg
      // types / count
      while (pList != null && pList.head != null) {
        numArgs++;

        // If the function call has more arguments than the function's expected print an
        // error and break
        // Otherwise, we check the types of the arguments and the function's parameters
        if (funArgs == null || funArgs.head == null || funDec.argc == 0) {
          error(cExp.row, cExp.col, "Invalid number of arguments for function call: " + cExp.func + " Expected: "
              + funDec.argc + " | Actual: " + numArgs);
          break;
        } else {

          // As long as the function has parameters, we need to check if the types of the
          // passed arguments
          if (pList.head.dtype != null) {
            Type argType = funArgs.head.type;
            Type passedType = pList.head.dtype.type;

            // Check if the types of the arguments and the function's parameters match
            if (argType.type == Type.ANY) {
              // Do nothing
            } else if (passedType.type != argType.type) {
              error(cExp.row, cExp.col, "Invalid argument type for function call: " + cExp.func + " Expected: "
                  + argType + " | Actual: " + passedType);
            }
          }

          // Get next parameter and expected argument
          pList = pList.tail;
          funArgs = funArgs.tail;
        }
      }

      // If the function call has less arguments than the function's expected print an
      // error and assign
      if (funArgs != null && funArgs.head != null && funDec.argc != 0) {
        error(cExp.row, cExp.col, "Invalid number of arguments for function call: " + cExp.func + " Expected: "
            + funDec.argc + " | Actual: " + numArgs);
      }

      // Some error occurred when looking up the function definition in the
      // symbolTable
    } else {
      error(cExp.row, cExp.col, "Invalid function call: " + cExp.func);
    }

  }

  public void visit(CmpExp exp, int level, boolean isAddr) {

    if (exp == null) {
      error(exp.row, exp.col, "Invalid compound expression: NULL");
      return;
    }

    // If the compound expression is properly defined, we traverse the declaration
    // list and expression list
    if (exp.decList != null) {
      exp.decList.accept(this, level, isAddr);
    }

    if (exp.expList != null) {
      exp.expList.accept(this, level, isAddr);
    }
  }

  public void visit(CondExp exp, int level, boolean isAddr) {
    // We must check if the condition is either an integer or a boolean
    if (exp.dtype == null) {
      error(exp.row, exp.col, "Invalid conditional expression: NULL");
      return;
    }
    
    if ((exp.dtype.type.type != Type.INT && exp.dtype.type.type != Type.BOOL)) {
      error(exp.row, exp.col, "Invalid condition type: " + exp.dtype.type);
      return;
    }
  }

  public void visit(DecList decList, int level, boolean isAddr) {

    if (decList == null) {
      return;
    }

    indent(level);
    enterScope("global");

    // Traverse the declaration list and visit each declaration node accordingly
    while (decList != null) {
      decList.head.accept(this, level + 1, isAddr);
      decList = decList.tail;
    }

    removeScopeFromTable(level);
    indent(level);
    exitScope("global");
  }

  public void visit(ErrorDec dec, int level, boolean isAddr) {
    insertSymbol(dec, level);
  }

  public void visit(ErrorExp exp, int level, boolean isAddr) {
    exp.dtype = new ErrorDec(exp.row, exp.col, null, "Invalid expression");
  }

  public void visit(ExpList exp, int level, boolean isAddr) {
    if (exp == null) {
      return;
    }

    // Traverse the expression list and visit each expression node accordingly
    while (exp != null) {
      exp.head.accept(this, level, isAddr);
      exp = exp.tail;
    }
  }

  public void visit(FunDec dec, int level, boolean isAddr) {

    if (dec == null) {
      error(dec.row, dec.col, "Invalid function declaration: NULL");
      return;
    }

    // Insert any function declaration into the symbolTable since it could be a
    // prototype
    insertSymbol(dec, level);

    // If the function declaration has a body, we need to traverse the parameters
    // and the body
    // Then we check if the function has a proper return statement
    if (!(dec.body instanceof NilExp) && dec.body != null) {
      indent(level);
      enterScope("function " + dec.name);

      if (dec.params != null) {
        // If the function head is simply "void", we know there are no parameters
        if (dec.params.head.type.type != Type.VOID) {
          dec.params.accept(this, level + 1, isAddr);
        }
      }

      dec.body.accept(this, level + 1, isAddr);
      // We need to check if the function has a return statement
      checkFunctionReturn(dec.name, ((CmpExp) dec.body).expList, dec.type.type, false);

      removeScopeFromTable(level + 1);
      indent(level);
      exitScope("function " + dec.name);
    } else {
      removeScopeFromTable(level + 1);
    }
  }

  public void visit(IfExp exp, int level, boolean isAddr) {

    if (exp == null) {
      error(exp.row, exp.col, "Invalid if expression: NULL");
      return;
    }

    if (exp.cond != null) {
      exp.cond.accept(this, level, isAddr);
      indent(level);
      enterScope("if statement");
    }

    if (exp.ifDo != null) {
      exp.ifDo.accept(this, level + 1, isAddr);

      // Once we traverse the if statement, we need to remove the scope from the table
      removeScopeFromTable(level + 1);
      indent(level);
      exitScope("if statement");
    }

    if (exp.elseDo != null) {
      indent(level);
      enterScope("else statement");
      exp.elseDo.accept(this, level + 1, isAddr);

      // Once we traverse the else statement, we need to remove the scope from the
      // table
      removeScopeFromTable(level + 1);
      indent(level);
      exitScope("else statement");
    }

  }

  public void visit(IndexVar var, int level, boolean isAddr) {

    if (var == null) {
      error(var.row, var.col, "Invalid index variable: NULL");
      return;
    }

    // Verify that the index variable is properly defined
    var.ind.accept(this, level, isAddr);

    if (var.ind.dtype == null) {
      error(var.row, var.col, "Invalid index variable.");
      return;
    }

    int varType = var.ind.dtype.type.type;

    if (varType != Type.INT) {
      error(var.row, var.col, "Invalid index type. Expected: int | Actual: " + typeToString(varType));
    }

    // Assign the index variable a type declaration so that any lookups calls can
    // get the type of the expression
    var.declaration = (VarDec) lookupSymbol(var.name, false, var.row, var.col);

    if (var.declaration.type == null) {
      error(var.row, var.col, "Invalid index variable. Symbol '" + var.declaration.name + "' not found.");
      return;
    }

    // Check if the index is within bounds
    if (var.ind instanceof IntExp) {
      IntExp ind = (IntExp) var.ind;
      IntExp size = (IntExp) ((ArrayDec) var.declaration).size;

      if (ind.value < 0) {
        error(ind.row, ind.col, "Invalid array index. Index must be >= 0.");
      } else if (ind.value > size.value) {
        error(ind.row, ind.col, "Index out of bounds. Index: " + ind.value + " Array size: "
            + size.value);
      }
    }
  }

  public void visit(IterExp exp, int level, boolean isAddr) {

    if (exp == null) {
      error(exp.row, exp.col, "Invalid iteration expression: NULL");
      return;
    }

    if (exp.cond == null) {
      error(exp.row, exp.col, "Invalid iteration condition: NULL");
    }

    // Check the condition of the while loop
    exp.cond.accept(this, level, isAddr);

    if (exp.body != null) {
      indent(level);
      enterScope("while loop");
      exp.body.accept(this, level + 1, isAddr);

      // Once we traverse the while loop, we need to remove the scope from the table
      removeScopeFromTable(level + 1);
      indent(level);
      exitScope("while loop");
    }

  }

  public void visit(IntExp exp, int level, boolean isAddr) {
    if (exp != null) {
      exp.dtype = new SimpleDec(exp.row, exp.col, new Type(exp.row, exp.col, Type.INT), null);
    } else {
      error(exp.row, exp.col, "Invalid integer expression: NULL");
    }
  }

  public void visit(NilExp exp, int level, boolean isAddr) {
    if (exp != null) {
      exp.dtype = new SimpleDec(exp.row, exp.col, new Type(exp.row, exp.col, Type.VOID), null);
    } else {
      error(exp.row, exp.col, "Invalid nil expression: NULL");
    }
  }

  public void visit(OpExp exp, int level, boolean isAddr) {
    if (exp == null) {
      error(exp.row, exp.col, "Invalid operation expression: NULL");
      return;
    }

    // Traverse the tree to the left of the operator
    if (exp.left != null) {
      exp.left.accept(this, level, isAddr);
    }

    // Traverse the tree to the right of the operator
    if (exp.right != null) {
      exp.right.accept(this, level, isAddr);
    }

    // If the operation expression is a relational operator, we set type to boolean
    // Otherwise, we need to check if the types of the left and right hand side of
    // the operator match
    if (exp.isRel) {
      exp.dtype = new SimpleDec(exp.row, exp.col, new Type(exp.row, exp.col, Type.BOOL), null);
    } else {
      Type leftType = exp.left.dtype.type;

      if (exp.right.dtype == null) {
        exp.dtype = new ErrorDec(exp.row, exp.col, null, "Invalid operation expression");
        return;
      }

      Type rightType = exp.right.dtype.type;

      // Assign a type declaration to the operation expression so that any lookup
      // calls return the type of the expression

      if (leftType == null || rightType == null) {
        error(exp.row, exp.col, "Invalid expression.");
        exp.dtype = new ErrorDec(exp.row, exp.col, null, "Invalid expression");
        return;
      }

      if (leftType.type == rightType.type) {
        exp.dtype = exp.left.dtype;
      } else {
        error(exp.row, exp.col, "Invalid operation expression. Expected type: " + leftType);
        exp.dtype = new ErrorDec(exp.row, exp.col, null, "Invalid operation expression");
      }
    }
  }

  public void visit(ReturnExp exp, int level, boolean isAddr) {
    if (exp == null) {
      error(exp.row, exp.col, "Invalid return expression: NULL");
      return;
    }

    // Assign the return expression a type declaration so that any lookups calls can
    // get the return type
    if (exp.exp != null) {
      exp.exp.accept(this, level, isAddr);
      exp.dtype = exp.exp.dtype;
    }
  }

  public void visit(SimpleDec dec, int level, boolean isAddr) {
    if (dec == null) {
      error(dec.row, dec.col, "Invalid simple declaration: NULL");
      return;
    }

    if (dec.type.type == Type.VOID) {
      error(dec.row, dec.col, "Invalid simple declaration. Type cannot be void.");
    }

    // Set the visibility of the simple declaration based on the current scope level
    if (level - 1 > ScopeType.GLOBAL.ordinal()) {
      dec.nestLevel = ScopeType.LOCAL.ordinal();
    } else {
      dec.nestLevel = ScopeType.GLOBAL.ordinal();
    }

    // Add the simple declaration to the symbolTable
    insertSymbol(dec, level);
  }

  public void visit(SimpleVar var, int level, boolean isAddr) {
    if (var == null) {
      error(var.row, var.col, "Invalid simple variable: NULL");
      return;
    }

    var.declaration = (VarDec) lookupSymbol(var.name, false, var.row, var.col);

    if (var.declaration.type == null) {
      error(var.row, var.col, "Symbol '" + var.name + "' is not defined.");
      return;
    }
  }

  public void visit(Type type, int level, boolean isAddr) {
    // Do nothing
  }

  public void visit(VarDec dec, int level, boolean isAddr) {
    // Do nothing
  }

  public void visit(VarDecList decList, int level, boolean isAddr) {

    // Traverse the variable declaration list and visit each declaration node
    // accordingly
    while (decList != null) {

      if (decList.head != null) {
        decList.head.accept(this, level, isAddr);
      }
      decList = decList.tail;
    }

  }

  public void visit(VarExp exp, int level, boolean isAddr) {
    // If the variable expression is properly defined, we need traverse the
    // expression tree
    if (exp != null) {
      exp.var.accept(this, level, isAddr);

      // Get declaration type of the variable expression from the symbolTable
      exp.dtype = lookupSymbol(exp.var.name, false, exp.row, exp.col);
    } else {
      error(exp.row, exp.col, "Invalid variable expression: NULL");
      exp.dtype = new ErrorDec(exp.row, exp.col, null, "Invalid variable expression");
    }
  }

}
