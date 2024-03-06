import absyn.*;

/* Many of the methods are similar or identical 
 * to the ones in ShowTreeVisitor.java from the sample project */
public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent(int level) {
    for (int i = 0; i < level * SPACES; i++)
      System.out.print(" ");
  }

  public void visit(ArrayDec dec, int level) {
    indent(level);
    System.out.println("Array Declaration: " + dec.toString());
    level++;
    dec.type.accept(this, level);
  ;}

  public void visit(AssignExp exp, int level) {
    indent(level);
    System.out.println("AssignExp:");
    level++;

    if (exp.var != null)
      exp.var.accept(this, level);

    if (exp.var != null)
      exp.exp.accept(this, level);
  }

  public void visit(BoolExp exp, int level) {
    indent(level);
    System.out.println("BoolExp: " + exp.toString());
  }

  public void visit(CondExp exp, int level) {
    indent(level);
    System.out.println("CondExp: " + exp.toString());
  }

  public void visit(CallExp exp, int level) {
    indent(level);
    System.out.println("CallExp: " + exp.func);
    level++;

    if (exp.args != null)
      exp.args.accept(this, level);
  }

  public void visit(CmpExp exp, int level) {
    indent(level);
    System.out.println("Compound Exp:");
    level++;

    if (exp.decList != null)
      exp.decList.accept(this, level);

    if (exp.expList != null)
      exp.expList.accept(this, level);
  }

  public void visit(Declaration dec, int level) {
    indent(level);
    System.out.println("Declaration:");
    level++;
    
    // Determine the type of declaration and call the appropriate accept method
    if (dec instanceof VarDec)
      ((VarDec) dec).accept(this, level);
    else if (dec instanceof FunDec)
      ((FunDec) dec).accept(this, level);
    else
      System.out.println("Unrecognized declaration at line " + dec.row + " and column " + dec.col);
  }

  public void visit(DecList decList, int level) {
    indent(level);
    System.out.println("DecList:");
    level++;

    while (decList != null) {
      decList.head.accept(this, level);
      decList = decList.tail;
    }
  }

  public void visit(ErrorDec dec, int level) {
    indent(level);
    System.out.println("Declaration: Error at line " + dec.row + " and column " + dec.col);
  }

  public void visit(ErrorExp exp, int level) {
    indent(level);
    System.out.println("Expression: Error at line " + exp.row + " and column " + exp.col);
  }

  public void visit(Exp exp, int level) {
    indent(level);
    System.out.println("Exp:");
    level++;
    
    // Determine the type of expression and call the appropriate accept method
    if (exp instanceof AssignExp)
      ((AssignExp) exp).accept(this, level);
    else if (exp instanceof IfExp)
      ((IfExp) exp).accept(this, level);
    else if (exp instanceof IntExp)
      ((IntExp) exp).accept(this, level);
    else if (exp instanceof OpExp)
      ((OpExp) exp).accept(this, level);
    else if (exp instanceof IterExp)
      ((IterExp) exp).accept(this, level);
    else if (exp instanceof CallExp)
      ((CallExp) exp).accept(this, level);
    else if (exp instanceof CmpExp)
      ((CmpExp) exp).accept(this, level);
    else
      System.out.println("Unrecognized expression at line " + exp.row + " and column " + exp.col);
  }

  public void visit(ExpList expList, int level) {
    while (expList != null) {
      expList.head.accept(this, level);
      expList = expList.tail;
    }
  }

  public void visit(FunDec dec, int level) {
    indent(level);
    System.out.println("Function Declaration: " + dec.type + " " + dec.name);
    level++;
    dec.params.accept(this, level);

    // Function prototypes can have no body so we need to check for null
    if (dec.body != null)
      dec.body.accept(this, level);
  }

  public void visit(IfExp exp, int level) {
    indent(level);
    System.out.println("IfExp:");
    level++;
    exp.cond.accept(this, level);

    if (exp.ifDo != null)
      exp.ifDo.accept(this, level);
    if (exp.elseDo != null)
    {
      indent(level-1);
      System.out.println("Else:");
      exp.elseDo.accept(this, level);
    }
  }

  public void visit(IndexVar var, int level) {
    indent(level);
    System.out.println("IndexVar: " + var.toString());
    level++;
    var.ind.accept(this, level);
  }

  public void visit(IntExp exp, int level) {
    indent(level);
    System.out.println("IntExp: " + exp.value);
  }

  public void visit(IterExp exp, int level) {
    indent(level);
    System.out.println("While Loop (IterExp):");
    level++;

    indent(level);
    System.out.println("Loop condition:");
    exp.cond.accept(this, level);

    indent(level);
    System.out.println("Loop body:");
    exp.body.accept(this, level);
  }

  public void visit(SimpleDec dec, int level) {
    indent(level);
    if (dec.type.type != Type.VOID)
      System.out.println("Simple Declaration: " + dec.type + " " + dec.name);
    else
      System.out.println("Simple Declaration: " + dec.type);

    level++;
    dec.type.accept(this, level);
  }

  public void visit(SimpleVar var, int level) {
    indent(level);
    System.out.println("SimpleVar: " + var.name);
  }

  public void visit(NilExp exp, int level) {
    indent(level);
    System.out.println("NilExp: " + exp.toString());
  }

  public void visit( OpExp exp, int level ) {
    indent( level );
    System.out.print( "OpExp: " ); 
    System.out.println( exp.toString() );

    level++;

    
    exp.left.accept( this, level );
    exp.right.accept( this, level );
  }

  public void visit(ReturnExp exp, int level) {
    indent(level);
    System.out.println("ReturnExp:");
    level++;
    
    // Important to check for null here because return statements can have no expression
    if (exp.exp != null)
      exp.exp.accept(this, level);
  }

  public void visit(Type type, int level) {
    indent(level);
    System.out.print("Type: ");
    switch (type.type) {
      case Type.INT:
        System.out.println("int");
        break;
      case Type.VOID:
        System.out.println("void");
        break;
      case Type.BOOL:
        System.out.println("bool");
        break;
      default:
        System.out.println("Unrecognized type at line " + type.row + " and column " + type.col);
    }
  }

  public void visit(Var var, int level) {
    indent(level);
    System.out.println("Var: " + var.name);
  }

  public void visit(VarDec dec, int level) {
    indent(level);
    System.out.println("Var Declaration: " + dec.name);
    level++;
    dec.type.accept(this, level);
  }

  public void visit(VarDecList decList, int level) {
    indent(level);
    System.out.println("VarDecList:");
    level++;
    
    while (decList != null) {
      decList.head.accept(this, level);
      decList = decList.tail;
    }
  }

  public void visit(VarExp exp, int level) {
    indent(level);
    System.out.println("VarExp:");

    level++;
    exp.var.accept(this, level);
  }

}
