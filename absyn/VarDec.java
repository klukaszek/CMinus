package absyn;

public abstract class VarDec extends Declaration {
  // 0: global, 1: function scope, 2: block scope
  public int visibility;
}
