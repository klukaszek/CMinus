package absyn;

public class CmpExp extends Exp {
  public VarDecList decList;
  public ExpList expList;
  
  public CmpExp(int row, int col, VarDecList decList, ExpList expList) {
    this.row = row;
    this.col = col;
    this.decList = decList;
    this.expList = expList;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit( this, level, isAddr );
  }
}
