package absyn;

// Handles declarations of arrays (i.e int[] or bool[])
// This class is a subclass of VarDec, which is a subclass of declarations
public class ArrayDec extends VarDec {

  // We need to store the size of the array as an IntExp instead of an int because
  // the size of the array can be a variable
  public Exp size;

  public ArrayDec(int row, int col, Type type, String name, Exp size) {
    this.row = row;
    this.col = col;
    this.type = type;
    this.name = name;
    this.size = size;
  }

  public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
    visitor.visit(this, level, isAddr);
  }

  @Override
  public String toString() {
    if (this.size instanceof IntExp) {
      return type + ": " + name + "[" + ((IntExp) this.size).value + "]";
    }
    return type + ": " + name + "[ invalid size ]";
  }

}
