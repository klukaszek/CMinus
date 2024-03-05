package absyn;

// ArrayVar keeps track of the array variable and the index expression
public class ArrayVar extends Var {
  public Exp ind;

  public ArrayVar( int row, int col, String name, Exp ind ) {
    this.row = row;
    this.col = col;
    this.name = name;
    this.ind = ind;
    this.declaration = null;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
