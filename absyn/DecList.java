package absyn;

public class DecList extends Absyn {
  public Declaration head;
  public DecList tail;

  public DecList( Declaration head, DecList tail ) {
    this.head = head;
    this.tail = tail;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
    visitor.visit( this, level, isAddr );
  }
}
