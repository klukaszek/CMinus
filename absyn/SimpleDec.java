package absyn;

// Handles declarations of variables that are not arrays (i.e int or bool)
public class SimpleDec extends VarDec{

  public Type type;
  public String name;

  public SimpleDec( int row, int col, Type type, String name ) {
    this.row = row;
    this.col = col;
    this.type = type;
    this.name = name;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }

  @Override
  public String toString() {
    return type + ": " + name;
  }

}
