package absyn;

public class FunDec extends Declaration {

  public VarDecList params;
  public Exp body;
  public int argc;

  public FunDec( int row, int col, Type type, String name, VarDecList params, Exp body ) {
    this.row = row;
    this.col = col;
    this.type = type;
    this.name = name;
    this.params = params;
    this.body = body;
    this.argc = 0;
    
    // Count the number of arguments for the function definition
    while ( params != null ) {
      this.argc++;
      params = params.tail;
    }
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }

  @Override
  public String toString() {
    return "function '" + this.type + " " + this.name + "()' with " + this.argc + " argument(s)";
  }
}
