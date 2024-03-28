package absyn;

public class CondExp extends Exp {
  
  public final static int AND = 0;
  public final static int OR = 1;
  public final static int NOT = 2;

  public Exp left;
  public int op;
  public Exp right;

  public CondExp( int row, int col, Exp left, int op, Exp right ) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.op = op;
    this.right = right;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
    visitor.visit( this, level, isAddr );
  }

  @Override
  public String toString() {
    switch (op) {
      case AND:
        return left.toString() + " && " + right.toString();
      case OR:
        return left.toString() + " || " + right.toString();
      case NOT:
        return "~" + left.toString();
      default:
        return "Unknown operator at line " + row + " and column " + col;
    }
  }
}
