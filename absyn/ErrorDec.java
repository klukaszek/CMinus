package absyn;

public class ErrorDec extends VarDec {

  public ErrorDec( int row, int col, Type type, String name ) {
    this.row = row;
    this.col = col;
    this.type = type;
    this.name = name;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit( this, level, isAddr );
  }

  @Override
  public String toString() {
    return type + ": " + name;
  }

}

