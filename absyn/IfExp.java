package absyn;

public class IfExp extends Exp {
  public Exp cond;
  public Exp ifDo;
  public Exp elseDo;

  public IfExp( int row, int col, Exp cond, Exp ifDo, Exp elseDo ) {
    this.row = row;
    this.col = col;
    this.cond = cond;
    this.ifDo = ifDo;
    this.elseDo = elseDo;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit( this, level, isAddr );
  }
}
