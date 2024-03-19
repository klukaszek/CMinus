package absyn;

public class OpExp extends Exp {
  
  public final static int PLUS = 0;
  public final static int MINUS = 1;
  public final static int MUL = 2;
  public final static int DIV = 3;
  public final static int EQ = 4;
  public final static int NE = 5;
  public final static int LT = 6;
  public final static int LE = 7;
  public final static int GT = 8;
  public final static int GE = 9;
  public final static int UNKNOWN = 10;

  public Exp left;
  public int op;
  public Exp right;
  public Boolean isRel = false;

  public OpExp( int row, int col, Exp left, int op, Exp right ) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.op = op;
    this.right = right;
    
    // Check if the operation uses a relational operator
    if (op == EQ || op == NE || op == LT || op == LE || op == GT || op == GE)
      isRel = true;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }

  @Override
  public String toString() {
    
    String leftString = left.toString();

    if (left instanceof OpExp || left instanceof CallExp)
      leftString = "(" + leftString + ")";

    String rightString = right.toString();
    if (right instanceof OpExp || right instanceof CallExp)
      rightString = "(" + rightString + ")";


    switch (op) {
      case PLUS:
        return  leftString + " + " + rightString;
      case MINUS:
        return leftString + " - " + rightString;
      case MUL:
        return leftString + " * " + rightString;
      case DIV:
        return leftString + " / " + rightString;
      case EQ:
        return "(" + left.toString() + ") == (" + right.toString() + ")";
      case NE:
        return "(" + left.toString() + ") != (" + right.toString() + ")";
      case LT:
        return "(" + left.toString() + ") < (" + right.toString() + ")";
      case LE:
        return "(" + left.toString() + ") <= (" + right.toString() + ")";
      case GT:
        return "(" + left.toString() + ") > (" + right.toString() + ")";
      case GE:
        return "(" + left.toString() + ") >= (" + right.toString() + ")";
      default:
        return "Unknown operator expression";
    }
  }

}
