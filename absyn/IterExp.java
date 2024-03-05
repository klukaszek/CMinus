package absyn;

public class IterExp extends Exp {
  public Exp cond;
  public Exp body;
  
  // Get the conditional expression and the body of code within the loop to execute
  public IterExp( int row, int col, Exp cond, Exp body ) {
    this.row = row;
    this.col = col;
    this.cond = cond;
    this.body = body;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
