import absyn.*;

// Node type specified in 8-TypeChecking
// Needs it's own file because Java things
public class NodeType {
  public String name;
  public Declaration dec;
  public int level;

  public NodeType(String name, Declaration dec, int level) {
    this.name = name;
    this.dec = dec;
    this.level = level;
  }
  
  // Override to just return the actual declaration string
  public String toString() {
    return dec.toString();
  }
}
