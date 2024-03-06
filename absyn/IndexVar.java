package absyn;

// IndexVar keeps track of the array variable and the index expression
public class IndexVar extends Var {
  public Exp ind;

  public IndexVar( int row, int col, String name, Exp ind ) {
    this.row = row;
    this.col = col;
    this.name = name;
    this.ind = ind;
    this.declaration = null;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }

  @Override
  public String toString() {
    return name + "[" + ind.toString() + "]";
  }
}
