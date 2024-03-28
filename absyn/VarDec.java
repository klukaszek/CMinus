package absyn;

public abstract class VarDec extends Declaration {
  // 0: global, 1: function scope
  // Previously named "visibility" but changed to "nestLevel" to match lecture slides
  public int nestLevel;
  public int offset;
}
