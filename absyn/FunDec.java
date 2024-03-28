package absyn;

public class FunDec extends Declaration {

  public VarDecList params;
  public Exp body;
  public int argc;
  public int funAddr;

  public FunDec( int row, int col, Type type, String name, VarDecList params, Exp body ) {
    this.row = row;
    this.col = col;
    this.type = type;
    this.name = name;
    this.params = params;
    this.body = body;
    this.argc = 0;
    // Set the function address to -1 to indicate that it has not been set
    this.funAddr = -1;
    
    // Count the number of arguments for the function definition
    while ( params != null ) {
      this.argc++;
      params = params.tail;
    }
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit( this, level, isAddr );
  }

  @Override
  public String toString() {
    return "function '" + this.type + " " + this.name + "()' with " + this.argc + " argument(s)";
  }
}
