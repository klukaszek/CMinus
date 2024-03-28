package absyn;

public class Type extends Absyn {
  
  public final static int VOID = 0;
  public final static int INT = 1;
  public final static int BOOL = 2;
  public final static int UNKNOWN = 3;

  // This is used for the output function
  public final static int ANY = 4;

  public int type;

  public Type( int row, int col, int type) {
    this.row = row;
    this.col = col;
    this.type = type;
  }

  public Type UNKNOWN_TYPE(int row, int col) {
    return new Type(row, col, UNKNOWN);
  }  

  public void accept( AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit( this, level, isAddr );
  }

  @Override
  public String toString() {
    switch (this.type) {
      case VOID:
        return "void";
      case INT:
        return "int";
      case BOOL:
        return "bool";
      case ANY:
        return "any";
      default:
        return "unknown";
    }
  }
}
