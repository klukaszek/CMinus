package absyn;

public class CallExp extends Exp {
  public String func;
  public ExpList args;
  public int numArgs;

  public CallExp( int row, int col, String func, ExpList args ) {
    this.row = row;
    this.col = col;
    this.func = func;
    this.args = args;
    this.numArgs = this.countArgs( args );
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit( this, level, isAddr );
  }

  private int countArgs( ExpList args ) {
    if( args == null ) {
      return 0;
    }
    return 1 + countArgs( args.tail );
  }
}
