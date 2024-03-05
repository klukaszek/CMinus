package absyn;

public interface AbsynVisitor {

  public void visit( ArrayDec decArr, int level );

  public void visit( AssignExp exp, int level );

  public void visit(DecList decList, int level);

  public void visit( ExpList exp, int level );

  public void visit( IntExp exp, int level );

  public void visit( NormalDec dec, int level );

  public void visit( VarDec dec, int level );

  public void visit( VarDecList decList, int level );

  public void visit( ErrorDec dec, int level );

  public void visit( ErrorExp exp, int level );

  public void visit( Type type, int level );

  public void visit( FunDec dec, int level );
  
  public void visit( CmpExp exp, int level );

  public void visit( IfExp exp, int level );

  public void visit( IterExp exp, int level );

  public void visit( ReturnExp exp, int level );

  public void visit( OpExp exp, int level );

  public void visit( BoolExp exp, int level );

  public void visit( CallExp exp, int level );

  public void visit( NormalVar var, int level );

  public void visit( ArrayVar var, int level );

  public void visit( VarExp exp, int level );

}
