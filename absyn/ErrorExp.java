package absyn;

public class ErrorExp extends Exp {
  public String msg;
  
  // Effectively, this is a placeholder for an error message.
  public ErrorExp( int row, int col ) {
    this.row = row;
    this.col = col;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit( this, level, isAddr );
  }
}
