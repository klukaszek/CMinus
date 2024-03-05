package absyn;

public class AssignExp extends Exp {
  public Var var;
  public Exp exp;

  public AssignExp( int row, int col, Var var, Exp exp ) {
    this.row = row;
    this.col = col;
    this.var = var;
    this.exp = exp;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
