package absyn;

public interface AbsynVisitor {

  public void visit( ArrayDec decArr, int level, boolean isAddr);

  public void visit( AssignExp exp, int level, boolean isAddr);

  public void visit( BoolExp exp, int level, boolean isAddr); 

  public void visit( CallExp exp, int level, boolean isAddr);

  public void visit( CmpExp exp, int level, boolean isAddr);

  public void visit( CondExp exp, int level, boolean isAddr);

  public void visit( DecList decList, int level, boolean isAddr);

  public void visit( ErrorDec dec, int level, boolean isAddr);

  public void visit( ErrorExp exp, int level, boolean isAddr);

  public void visit( ExpList exp, int level, boolean isAddr);

  public void visit( FunDec dec, int level, boolean isAddr);

  public void visit( IfExp exp, int level, boolean isAddr);

  public void visit( IndexVar var, int level, boolean isAddr);

  public void visit( IterExp exp, int level, boolean isAddr);

  public void visit( IntExp exp, int level, boolean isAddr);

  public void visit( NilExp exp, int level, boolean isAddr);

  public void visit( OpExp exp, int level, boolean isAddr);

  public void visit( ReturnExp exp, int level, boolean isAddr);

  public void visit( SimpleDec dec, int leel, boolean isAddr);

  public void visit( SimpleVar var, int level, boolean isAddr);

  public void visit( Type type, int level, boolean isAddr);

  public void visit( VarDec dec, int level, boolean isAddr);

  public void visit( VarDecList decList, int level, boolean isAddr);

  public void visit( VarExp exp, int level, boolean isAddr);
  
}
