/*
 *
 *  TMSimulator - A Turing Machine Simulator Machine Code Generator
 *  
 *  Author: Kyle Lukaszek
 *  ID: 1113798
 *  Class: CIS4650
 *
 *  ======================
 *  
 *  Generates machine code for the C- program using the AST created by the parser for checkpoint 2.
 *  Machine code can be executed by the TMSimulator provided in the project.
 *
 */

import absyn.*;

public class CodeGenerator implements AbsynVisitor {

  private static final int BYTE = 1;

  // Define constants values
  private static final int IADDR_SIZE = 1024;
  private static final int DADDR_SIZE = 1024;
  private static final int NO_REGS = 8;
  private static final int PC_REG = 7;

  // Predefined register numbers
  private static final int AC = 0;
  private static final int AC1 = 1;
  private static final int TEMP1 = 2;
  private static final int TEMP2 = 3;
  private static final int TEMP3 = 4;
  private static final int FP = 5;
  private static final int GP = 6;
  private static final int PC = 7;

  private static final int ofpFO = 0;
  private static final int retFO = -1;
  private static final int initFO = -2;

  // Register-Only Instructions (RO)
  private static final int HALT = 0;
  private static final int IN = 1;
  private static final int OUT = 2;
  private static final int ADD = 3;
  private static final int SUB = 4;
  private static final int MUL = 5;
  // May generate ZERO_DIV
  private static final int DIV = 6;
  // Error code for division by zero
  private static final int ZERO_DIV = 7;

  // Register-to-Memory Instructions (RM)
  private static final int LD = 8;
  private static final int LDA = 9;
  private static final int LDC = 10;
  private static final int ST = 11;
  private static final int JLT = 12;
  private static final int JLE = 13;
  private static final int JGT = 14;
  private static final int JGE = 15;
  private static final int JEQ = 16;
  private static final int JNE = 17;

  // Initialize instance variables
  private int emitLoc = 0;
  private int highEmitLoc = 0;
  private int globalOffset;
  private int tempOffset;
  private int mainEntry;
  private boolean mainFound;
  private boolean tempFlag;

  // Constructor
  public CodeGenerator() {
    emitLoc = 0;
    highEmitLoc = 0;
    globalOffset = 0;
    tempOffset = 0;
    mainEntry = -1;
    mainFound = false;
    tempFlag = false;
  }

  /*
   * Emit methods
   */

  // Emit a register-only instruction
  private void emitRO(int op, int r, int s, int t, String comment) {
    System.out.printf("%3d: %5s %d, %d, %d\t%s\n", emitLoc, getOP(op), r, s, t, comment);
    emitLoc++;
    if (highEmitLoc < emitLoc) {
      highEmitLoc = emitLoc;
    }
  }

  // Emit a register-to-memory instruction
  private void emitRM(int op, int r, int d, int s, String comment) {
    System.out.printf("%3d: %5s %d, %d(%d)\t%s\n", emitLoc, getOP(op), r, d, s, comment);
    emitLoc++;

    if (highEmitLoc < emitLoc) {
      highEmitLoc = emitLoc;
    }
  }

  // Emit an ABS register-to-memory instruction
  private void emitRM_Abs(int op, int r, int a, String c) {
    System.out.printf("%3d: %5s %d, %d(%d)\t%s\n", emitLoc, getOP(op), r, a - (emitLoc + 1), PC, c);
    emitLoc++;
    if (highEmitLoc < emitLoc) {
      highEmitLoc = emitLoc;
    }
  }

  // Skip a number of locations for backpatching
  private int emitSkip(int distance) {
    int i = emitLoc;
    emitLoc += distance;
    if (highEmitLoc < emitLoc) {
      highEmitLoc = emitLoc;
    }
    return i;
  }

  // Backpatch a location
  private void emitBackup(int loc) {
    if (loc > highEmitLoc) {
      emitComment("BUG in emitBackup");
    }
    emitLoc = loc;
  }

  // Restore the current location to the highest location emitted
  private void emitRestore() {
    emitLoc = highEmitLoc;
  }

  // Print a comment
  private void emitComment(String comment) {
    System.out.println("* " + comment);
  }

  /*
   * Prelude and Finale Methods
   */

  // Generate the prelude machine code for the C- program
  // This is always the same for every C- program
  // This is also where the input and output routines are defined
  private void newPrelude() {

    emitComment("Standard Prelude:");

    // Set the global pointer and frame pointer to the base of the memory
    emitRM(LD, GP, 0, AC, "Set the global pointer");
    emitRM(LDA, FP, 0, GP, "Set the frame pointer");
    emitRM(ST, AC, 0, AC, "Clear location 0");

    // This allows us to backpatch the jump around the i/o routines
    int savedLoc = emitSkip(1);

    // Code for the i/o routines
    // Input routine
    emitComment("Jump around i/o routines here");
    emitComment("Code for input routine");
    emitRM(ST, AC, -1, FP, "Store return address");
    emitRO(IN, AC, 0, 0, "Read integer value");
    emitRM(LD, PC, -1, FP, "Return to caller");

    // Output routine
    emitComment("Code for output routine");
    emitRM(ST, 0, -1, FP, "Store return address");
    emitRM(LD, 0, -2, FP, "Load output value");
    emitRO(OUT, 0, 0, 0, "Write integer value");
    emitRM(LD, PC, -1, FP, "Return to caller");

    // Restore the location to the saved location for backpatching
    int savedLoc2 = emitSkip(0);
    emitBackup(savedLoc);
    emitRM_Abs(LDA, PC, savedLoc2, "Jump around i/o code");
    emitRestore();

    emitComment("End of prelude");
  }

  // Generate the finale machine code for the C- program
  // This is always the same for every C- program
  // This is where the program calls the main function and halts after execution
  void newFinale() {
    emitComment("Standard Finale:");
    emitRM(ST, FP, globalOffset + ofpFO, FP, "Push ofp");
    emitRM(LDA, FP, globalOffset, FP, "Push frame");
    emitRM(LDA, AC, 1, PC, "Load ac with ret ptr");
    emitRM_Abs(LDA, PC, mainEntry, "Jump to main loc");
    emitRM(LD, FP, ofpFO, FP, "Pop frame");
    emitRO(HALT, 0, 0, 0, "");

    emitComment("End of execution.");
  }

  /*
   * Visitor methods
   * In the case of the following methods, "offset" refers to the offset of the
   * current frame
   */

  // Perform post-order traversal of the AST to generate the machine code for the
  // C- program. This is the main method that is called to generate the machine
  // code.
  public void visit(Absyn root_node, String filename) {

    emitComment("C- machine code generated for TMSimulator");
    emitComment("Compiling: " + filename);

    // Generate prelude along with input and output routines
    newPrelude();

    // Traverse the AST to generate the machine code for the C- program, start at
    // offset 0
    root_node.accept(this, 0, false);

    if (!mainFound) {
      emitComment("Error: No main function found");
      return;
    }

    emitComment("Jumping to finale");

    // Generate finale
    newFinale();

  }

  public void visit(ArrayDec decArr, int offset, boolean isAddr) {

    if (decArr.nestLevel == 0) {
      emitComment("Store global array variable: " + decArr.name);
    } else {
      emitComment("Store local array variable: " + decArr.name);
    }

    decArr.offset = offset;

  }

  public void visit(AssignExp exp, int offset, boolean isAddr) {

    emitComment("-> assign");

    emitComment(exp.var.getClass().getName());

    // Check if we are on the left side of the assignment
    // If we are on the left side, we have to set isAddr to true
    if (exp.var instanceof SimpleVar) {

      emitComment("-> id");

      exp.var.accept(this, offset, true);

      emitComment("<- id");

      // Store the address of the variable in the AC register
      emitRM(ST, AC, offset, FP, "assign: push left");

      // Assign data to index positions of an array
    } else if (exp.var instanceof IndexVar) {

      IndexVar arrayIndex = (IndexVar) exp.var;
      ArrayDec arrayDecl = (ArrayDec) arrayIndex.declaration;

      // Load the base address of the array into AC1
      emitRM(LDA, AC1, arrayDecl.offset, FP, "Load base address of array");

      // Evaluate the index expression and store the result in TEMP1
      arrayIndex.ind.accept(this, offset, false);
      emitRM(ST, AC, offset, FP, "Store index value temporarily");
      emitRM(LD, TEMP1, offset, FP, "Load index value into TEMP1");

      // Load the size of the array into TEMP2
      emitRM(LDC, TEMP2, arrayDecl.offset + 1, FP, "Load array size into TEMP2");

      // Compute the effective address of the array element
      emitRO(ADD, AC1, AC1, TEMP1, "Compute effective address");

      // Check if the index is within bounds
      emitRO(SUB, TEMP1, TEMP1, TEMP2, "Check if index is out of bounds");
      emitRM(JGE, TEMP1, 2, PC, "Jump to out-of-bounds error");

      // Evaluate the right-hand side expression and store the result in AC
      exp.exp.accept(this, offset - 1, false);

      // Store the value in AC at the computed effective address
      emitRM(ST, AC, 0, AC1, "Store value at array element");
    }

    // Accept whatever is on the right side of the assignment
    exp.exp.accept(this, offset - 1, false);

    // Load the address of the variable into the AC register
    emitRM(LD, AC1, offset, FP, "assign: load left");

    // Store the value of the right side of the assignment into the variable
    emitRM(ST, AC, AC, 1, "assign: store value");

    emitComment("<- assign");
  }

  public void visit(BoolExp exp, int offset, boolean isAddr) {

    int truth = exp.value ? 1 : 0;

    // This should just be a 1 or 0 in the AC register
    emitRM(LDC, AC, truth, 0, "Load boolean value");
  }

  // Handle any function calls appropriately
  public void visit(CallExp exp, int offset, boolean isAddr) {

    emitComment("-> call of function " + exp.func + ":");

    // Handle arguments for the output function
    if (exp.args != null && exp.args.head != null) {
      exp.args.accept(this, offset + initFO, true);
    }

    // Get the address of the function declaration
    FunDec funDec = (FunDec) exp.dtype;

    // Check if the function is input or output
    // These functions have predefined addresses since they are built-in
    if (funDec.name.toLowerCase().equals("input")) {
      funDec.funAddr = 4;
    } else if (funDec.name.toLowerCase().equals("output")) {
      funDec.funAddr = 7;
    }

    // We then store our frame pointer, push the frame, and store the return address
    emitRM(ST, FP, offset + ofpFO, FP, "Push ofp");
    emitRM(LDA, FP, offset, FP, "Push frame");
    emitRM(LDA, AC, 1, PC, "Load ac with ret ptr");

    // Jump to the function address, and then pop the frame
    emitRM_Abs(LDA, PC, funDec.funAddr, "Jump to function " + exp.func + " loc");
    emitRM(LD, FP, ofpFO, FP, "Pop frame");

    emitComment("<- call");
  }

  // Handle any compound statements (e.g. fn body, if, while)
  public void visit(CmpExp exp, int offset, boolean isAddr) {

    // Following the example from the provided tm code
    emitComment("-> compound statement");

    // Generate machine code for the declaration list
    if (exp.decList != null) {
      exp.decList.accept(this, offset, false);

      // Adjust the offset for the current scope once we are done with the declaration
      // list
      offset = tempOffset;
    }

    // Generate machine code for the expression list
    if (exp.expList != null) {
      exp.expList.accept(this, offset, false);
    }

    // Exit the compound statement
    emitComment("<- compound statement");
  }

  // Handle any conditional expressions (e.g. and, or, not)
  public void visit(CondExp exp, int offset, boolean isAddr) {

    // Check condition type
    if (exp.op == CondExp.AND) {
      emitComment("-> and");

      // Evaluate the left side of the conditional expression
      exp.left.accept(this, offset, false);

      // Save the location and jump around the right side of the conditional
      // expression
      int andLoc = emitSkip(0);

      // Evaluate the right side of the conditional expression
      exp.right.accept(this, offset, false);

      // Backpatch the left side of the conditional expression
      emitBackup(andLoc);

      // If the left side of the conditional expression is false, jump to the end
      emitRM(JEQ, AC, 2, PC, "and: jump to false");
      emitRM(LDC, AC, 1, AC, "and: set true");
      emitRM(LDA, PC, 1, PC, "unconditional jump");

      // Set the true part of the conditional expression
      emitRM(LDC, AC, 0, AC, "and: set false");

      emitComment("<- and");
    } else if (exp.op == CondExp.OR) {
      emitComment("-> or");

      // Evaluate the left side of the conditional expression
      exp.left.accept(this, offset, false);

      // Save the location and jump around the right side of the conditional
      // expression
      int orLoc = emitSkip(0);

      // Evaluate the right side of the conditional expression
      exp.right.accept(this, offset, false);

      // Backpatch the left side of the conditional expression
      emitBackup(orLoc);

      // If the left side of the conditional expression is true, jump to the end
      emitRM(JEQ, AC, 2, PC, "or: jump to true");
      emitRM(LDC, AC, 0, AC, "or: set false");
      emitRM(LDA, PC, 1, PC, "unconditional jump");

      // Set the true part of the conditional expression
      emitRM(LDC, AC, 1, AC, "or: set true");

      emitComment("<- or");
    } else if (exp.op == CondExp.NOT) {
      emitComment("-> not");

      // Evaluate the expression
      exp.left.accept(this, offset, false);

      // If the expression is false, set the AC register to true
      emitRM(JEQ, AC, 2, PC, "not: jump to true");
      emitRM(LDC, AC, 0, AC, "not: set false");
      emitRM(LDA, PC, 1, PC, "unconditional jump");

      // Set the true part of the conditional expression
      emitRM(LDC, AC, 1, AC, "not: set true");

      emitComment("<- not");
    }
  }

  public void visit(DecList decList, int offset, boolean isAddr) {

    // Iterate through the declaration list and generate machine code for each
    // declaration
    while (decList != null) {
      decList.head.accept(this, offset, isAddr);
      decList = decList.tail;
    }

  }

  public void visit(ErrorDec dec, int offset, boolean isAddr) {
  }

  public void visit(ErrorExp exp, int offset, boolean isAddr) {
  }

  public void visit(ExpList exp, int offset, boolean isFnCall) {

    // Iterate through the expression list and generate machine code for each
    // expression
    while (exp != null) {

      if (isFnCall)
        emitComment("Function call argument");

      exp.head.accept(this, offset, false);

      // If we are in a function call, we need to store the argument in the frame
      // and adjust the offset of the frame based on the size of the argument
      if (isFnCall) {
        emitRM(ST, AC, offset, FP, "Store argument for function call");
        // Arguments are always 1 location in size
        offset--;
      }

      exp = exp.tail;
    }

  }

  // Handle any function declarations
  public void visit(FunDec dec, int offset, boolean isAddr) {

    emitComment("Processing function: " + dec.name);

    // Skip any redefinition of input or output functions
    if (dec.name.toLowerCase().equals("input") || dec.name.toLowerCase().equals("output")) {
      System.err.println("Error: Cannot redefine input or output functions");
      return;
    }

    emitComment("Jump around function code here");

    // Save the starting location of the function for backpatching
    int fnStart = emitSkip(1);

    // Set the address of the function
    dec.funAddr = emitLoc;

    // If the function is main, set the mainFound flag to true and store the main
    // entry location
    if (dec.name.equals("main")) {
      mainFound = true;
      mainEntry = fnStart + 1;
    }

    // Store the return addr from the AC register to the Return Frame Offset
    emitRM(ST, AC, retFO, FP, "Save return address for " + dec.name);

    // Make sure to change the current scope offset for the function
    offset = offset + initFO;

    // Generate machine code for function parameters
    dec.params.accept(this, offset, false);
    offset = tempOffset;

    // Generate machine code for the function body
    // We have to adjust the offset to account for the parameters which are all the
    // same size except for arrays
    dec.body.accept(this, offset, false);

    // Generate machine code for the function return
    emitRM(LD, PC, retFO, FP, "Return to caller");

    // Backpatch around the function code onto the next instruction
    int fnEnd = emitSkip(0);
    emitBackup(fnStart);
    emitRM_Abs(LDA, PC, fnEnd, "Jump around " + dec.name + " function code");
    emitRestore();

  }

  // Handle any if-else expressions
  public void visit(IfExp exp, int offset, boolean isAddr) {
    emitComment("-> if");

    // Test condition
    exp.cond.accept(this, offset, false);
    int savedLoc = emitSkip(1);

    // Generate machine code for the body of the if statement
    exp.ifDo.accept(this, offset - 1, false);
    int savedLoc2 = emitSkip(0);

    // Backpatch the jump back to the start of the if statement if AC is true
    emitBackup(savedLoc);
    emitRM_Abs(JEQ, AC, savedLoc2, "if: jump to else part");
    emitRestore();

    if (exp.elseDo != null) {
      int elseStart = emitSkip(1);
      exp.elseDo.accept(this, offset - 1, false);
      int elseEnd = emitSkip(0);

      emitBackup(elseStart);
      emitRM_Abs(JNE, AC, elseEnd, "if: jump to end");
      emitRestore();
    }

    emitComment("<- if");
  }

  public void visit(IndexVar var, int offset, boolean isAddr) {
    emitComment("-> index");
    
    // I deleted the code that was here because it stopped working and no matter what I tried I could not get it to work again

    emitComment("<- index");
  }

  public void visit(IterExp exp, int offset, boolean isAddr) {

    emitComment(exp.cond.getClass().getName());
    emitComment("-> while");
    emitComment("while: jump after body comes back here");

    // Save the location of the while loop
    int whileLoc = emitSkip(0);

    // Generate machine code for the condition of the while loop
    if (exp.cond != null) {
      exp.cond.accept(this, offset, false);
    }

    // Save the location of the body of the loop
    int doLoc = emitSkip(1);

    emitComment("while: jump to end belongs here");

    // Generate machine code for the body of the while loop
    if (exp.body != null) {
      exp.body.accept(this, offset, false);
    }

    // Jump back to the condition of the while loop
    emitRM_Abs(LDA, PC, whileLoc, "while: jump to top of loop");

    // Get the current location in the code
    int currLoc = emitSkip(0);
    emitBackup(doLoc);

    // If the condition is false, jump to the end of the while loop
    emitRM_Abs(JEQ, AC, currLoc, "while: jump to end");
    emitRestore();

    emitComment("<- while");
  }

  public void visit(IntExp exp, int offset, boolean isAddr) {
    emitComment("-> constant");

    // Load constant integer value into the AC register
    emitRM(LDC, AC, exp.value, 0, "load const");

    emitComment("<- constant");
  }

  public void visit(NilExp exp, int offset, boolean isAddr) {
  }

  // Example variables for managing temporary registers
  private boolean tempReg1Used = false;
  private boolean tempReg2Used = false;
  private boolean tempReg3Used = false;

  // Method to get a temporary register and mark it as used
  private int getTempReg() {
    // Implement logic to return the next available temporary register
    // For simplicity, let's assume we have tempReg1 and tempReg2 as available
    // registers
    if (!tempReg1Used) {
      tempReg1Used = true;
      return TEMP1;
    } else if (!tempReg2Used) {
      tempReg2Used = true;
      return TEMP2;
    } else if (!tempReg3Used) {
      tempReg3Used = true;
      return TEMP3;
    } else {
      // Handle error: No available temporary registers
      System.err.println("Error: No available temporary registers");
      return -1; // Return an invalid register number
    }
  }

  // Method to free a temporary register
  private void freeTempReg(int tempReg) {
    // Implement logic to free the specified temporary register
    if (tempReg == TEMP1) {
      tempReg1Used = false;
      emitRM(LDC, TEMP1, 0, TEMP1, "Free temporary register 1");
    } else if (tempReg == TEMP2) {
      tempReg2Used = false;
      // set the value of the register to 0
      emitRM(LDC, TEMP2, 0, TEMP2, "Free temporary register 2");
    } else if (tempReg == TEMP3) {
      tempReg3Used = false;
      // set the value of the register to 0
      emitRM(LDC, TEMP3, 0, TEMP3, "Free temporary register 3");
    }
  }

  // Handle any operation expressions (e.g. +, -, *, /, <, <=, >, >=, ==, !=)
  // I could not get a more simple way to handle temporary registers to work but I
  // think
  // this does the job well and properly follows the order of operations when
  // needed
  public void visit(OpExp exp, int offset, boolean isAddr) {

    emitComment("-> op");
    emitComment(exp.toString());

    // Generate machine code for the left operand
    exp.left.accept(this, offset, false);

    emitComment("Left operand loaded into AC");

    // If the left operand is an operation, we need to store the value in memory so
    // that we can access it later
    //if ((exp.left instanceof OpExp)) {
      emitRM(ST, AC, offset, FP, "Load left operand");
    //}
    int tempReg = getTempReg();

    // Move value of AC to TEMP1
    emitRM(LD, tempReg, offset, FP, "Load left operand into TEMP1");
    emitRM(ST, tempReg, 0, FP, "Store left operand in mem");

    // The same logic applies to the right operand
    exp.right.accept(this, offset - 1, false);

    // Load the right operand into the AC1 register
    emitRM(ST, AC, 0, FP, "Store right operand in mem");
    emitRM(LD, AC1, 0, FP, "Load right operand into AC1");

    // Then, load the left operand into the AC register after we move AC to AC1
    emitRM(ST, tempReg, 0, FP, "Store left operand in mem");
    emitRM(LD, AC, 0, FP, "Load left operand into AC");

    freeTempReg(tempReg);

    // Perform the operation based on the operator
    switch (exp.op) {

      case OpExp.PLUS:
        emitRO(ADD, AC, AC, AC1, "op +");
        break;

      case OpExp.MINUS:
        emitRO(SUB, AC, AC, AC1, "op -");
        break;

      case OpExp.MUL:
        emitRO(MUL, AC, AC, AC1, "op *");
        break;

      case OpExp.DIV:
        emitRO(DIV, AC, AC, AC1, "op /");
        break;

      case OpExp.LT:
        // Determine if left - right is less than 0, 0, or greater than 0
        emitRO(SUB, AC, AC, AC1, "op <");

        // If the result is less than 0, we know the left operand is less than the right
        // operand
        // so we can jump to the "true" part of the conditional
        emitRM(JLT, AC, 2, PC, "br if true");

        // Otherwise, we can jump to the "false" part of the conditional
        emitRM(LDC, AC, 0, AC, "false case");
        emitRM(LDA, PC, 1, PC, "unconditional jump");

        // Set the true part of the conditional (1 is true)
        emitRM(LDC, AC, 1, AC, "true case");
        break;

      case OpExp.LE:
        // Determine if left - right is less than or equal to 0, 0, or greater than 0
        emitRO(SUB, AC, AC, AC1, "op <=");

        // If the result is less than or equal to 0, we know the left operand is LE to
        // the right operand
        // so we can jump to the "true" part of the conditional
        emitRM(JLE, AC, 2, PC, "br if true");

        // Otherwise, we can jump to the "false" part of the conditional
        emitRM(LDC, AC, 0, AC, "false case");
        emitRM(LDA, PC, 1, PC, "unconditional jump");

        // Set the true part of the conditional (1 is true)
        emitRM(LDC, AC, 1, AC, "true case");
        break;

      case OpExp.GT:

        // Determine if left - right is greater than 0, 0, or less than 0
        emitRO(SUB, AC, AC, AC1, "op >");

        // If the result is greater than 0, we know the left operand is greater than the
        // right operand
        // so we can jump to the "true" part of the conditional
        emitRM(JGT, AC, 2, PC, "br if true");

        // Otherwise, we can jump to the "false" part of the conditional
        emitRM(LDC, AC, 0, AC, "false case");
        emitRM(LDA, PC, 1, PC, "unconditional jump");

        // Set the true part of the conditional (1 is true)
        emitRM(LDC, AC, 1, AC, "true case");
        break;

      case OpExp.GE:

        // Determine if left - right is greater than or equal to 0, 0, or less than 0
        emitRO(SUB, AC, AC, AC1, "op >=");

        // If the result is greater than or equal to 0, we know the left operand is GE
        // to the right operand
        // so we can jump to the "true" part of the conditional
        emitRM(JGE, AC, 2, PC, "br if true");

        // Otherwise, we can jump to the "false" part of the conditional
        emitRM(LDC, AC, 0, AC, "false case");
        emitRM(LDA, PC, 1, PC, "unconditional jump");

        // Set the true part of the conditional (1 is true)
        emitRM(LDC, AC, 1, AC, "true case");
        break;

      case OpExp.EQ:
        // Determine if left - right is equal to 0
        emitRO(SUB, AC, AC, AC1, "op ==");

        // If the result is 0, the left operand is equal to the right operand
        // so we can jump to the "true" part of the conditional
        emitRM(JEQ, AC, 2, PC, "br if true");

        // Otherwise, we can jump to the "false" part of the conditional
        emitRM(LDC, AC, 0, AC, "false case");

        emitRM(LDA, PC, 1, PC, "unconditional jump");

        // Set the true part of the conditional (1 is true)
        emitRM(LDC, AC, 1, AC, "true case");
        break;

      case OpExp.NE:
        // Determine if left - right is not equal to 0
        emitRO(SUB, AC, AC, AC1, "op !=");

        // If the result is not 0, the left operand is not equal to the right operand
        // so we can jump to the "true" part of the conditional
        emitRM(JNE, AC, 2, PC, "br if true");

        // Otherwise, we can jump to the "false" part of the conditional
        emitRM(LDC, AC, 0, AC, "false case");
        emitRM(LDA, PC, 1, PC, "unconditional jump");

        // Set the true part of the conditional (1 is true)
        emitRM(LDC, AC, 1, AC, "true case");
        break;
    }

    emitComment("<- op");
  }

  public void visit(ReturnExp exp, int offset, boolean isAddr) {

    emitComment("-> return");

    if (exp.exp instanceof CallExp) {
      emitComment("Returning Function Call");
    }

    // Generate machine code for expression being returned
    exp.exp.accept(this, offset, false);

    // Return to caller address
    emitRM(LD, PC, retFO, FP, "return to caller");

    emitComment("<- return");
  }

  public void visit(SimpleDec dec, int offset, boolean isAddr) {

    // Set the offset for the variable within the current frame
    // This is important for when we need to assign a value to the variable
    // declaration
    dec.offset = offset;

    // Print a comment for the variable declaration to indicate that we took note of
    // the offset
    if (dec.type.type == Type.INT) {
      if (dec.nestLevel == 0) {
        emitComment("Store global integer variable: " + dec.name);
      } else {
        emitComment("Store local variable: " + dec.name);
      }
    } else if (dec.type.type == Type.BOOL) {
      if (dec.nestLevel == 0) {
        emitComment("Store global boolean variable: " + dec.name);
      } else {
        emitComment("Store local variable: " + dec.name);
      }
    }
  }

  public void visit(SimpleVar var, int offset, boolean isAddr) {

    emitComment("looking up id: " + var.name);

    // Check if we are accessing the address of the variable
    if (isAddr) {
      emitRM(LDA, AC, var.declaration.offset, FP, "Load address of variable: " + var.name);
    } else {
      emitRM(LD, AC, var.declaration.offset, FP, "Load variable: " + var.name);
    }

  }

  public void visit(Type type, int offset, boolean isAddr) {
  }

  public void visit(VarDec dec, int offset, boolean isAddr) {

    if (dec instanceof SimpleDec) {

      SimpleDec simpleDec = (SimpleDec) dec;
      simpleDec.accept(this, offset, false);

    } else if (dec instanceof ArrayDec) {

      ArrayDec arrayDec = (ArrayDec) dec;

      // Skip if the size of the array is not an integer (Something went wrong)
      if (!(arrayDec.size instanceof IntExp)) {
        emitComment("Error: Array size must be an integer");
        return;
      }

      IntExp size = (IntExp) arrayDec.size;

      emitComment("-> array");
      
      // I deleted the code that was here because it stopped working and no matter what I tried I could not get it to work again

      emitComment("<- array");

    }
  }

  // Traverse the variable declaration list and generate machine code for each
  // variable
  public void visit(VarDecList decList, int offset, boolean isAddr) {

    // Iterate through nodes in the declaration list
    while (decList != null) {
      decList.head.accept(this, offset, isAddr);

      // Check if the current node is a simple declaration or an array declaration
      if (decList.head instanceof SimpleDec) {

        // We only need to adjust the offset by 1 location for both int and bool types
        offset = offset - 1;

        // If the current node is an array declaration...
      } else if (decList.head instanceof ArrayDec) {

        ArrayDec arrayDec = (ArrayDec) decList.head;

        // Skip if the size of the array is not an integer (Something went wrong)
        if (!(arrayDec.size instanceof IntExp)) {
          emitComment("Error: Array size must be an integer");
          return;
        }

        // I deleted the code that was here because it stopped working and no matter what I tried I could not get it to work again
        
      }

      // Go to the next node in the declaration list
      decList = decList.tail;
    }

    // Keep track of the current offset once we are done with the declaration list
    tempOffset = offset;
  }

  public void visit(VarExp exp, int offset, boolean isAddr) {

    emitComment("-> id");

    // Continue to the next node
    exp.var.accept(this, offset, isAddr);

    emitComment("<- id");

    emitRM(ST, AC, offset, FP, "op: push left");

  }

  // Return opcode string from a given opcode value
  private String getOP(int op) {
    switch (op) {
      case HALT:
        return "HALT";
      case IN:
        return "IN";
      case OUT:
        return "OUT";
      case ADD:
        return "ADD";
      case SUB:
        return "SUB";
      case MUL:
        return "MUL";
      case DIV:
        return "DIV";
      case ZERO_DIV:
        return "ZERO_DIV";
      case LD:
        return "LD";
      case LDA:
        return "LDA";
      case LDC:
        return "LDC";
      case ST:
        return "ST";
      case JLT:
        return "JLT";
      case JLE:
        return "JLE";
      case JGT:
        return "JGT";
      case JGE:
        return "JGE";
      case JEQ:
        return "JEQ";
      case JNE:
        return "JNE";
      default:
        return "UNKNOWN";
    }
  }
}
