package absyn;

public class NormalVar extends Var {
  public String name;

  public NormalVar( int row, int col, String name ) {
    this.row = row;
    this.col = col;
    this.name = name;
    this.declaration = null;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
