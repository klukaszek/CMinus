package absyn;

public class ReturnExp extends Exp {
  public Exp exp;
  
  public ReturnExp( int row, int col, Exp exp ) {
    this.row = row;
    this.col = col;
    this.exp = exp;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit( this, level, isAddr );
  }

  public int getReturnType() {
    return exp.dtype.type.type;
  }

  public String toString() {
    return "return " + exp.toString();
  }
}
