package absyn;

public class BoolExp extends Exp {
  
  public final static int AND = 0;
  public final static int OR = 1;
  public final static int NOT = 2;

  public Exp left;
  public int op;
  public Exp right;

  public BoolExp( int row, int col, Exp left, int op, Exp right ) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.op = op;
    this.right = right;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
