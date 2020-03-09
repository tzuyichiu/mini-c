package mini_c;

/** Location Transfer Language (LTL) */

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/** une opérande = un registre ou un emplacement de pile
 *  (résultat de l'allocation de registres) */

abstract class Operand {}

/** une opérande qui est un emplacement de pile */
class Spilled extends Operand {
  int n; /** position par rapport à %rbp */
  
  Spilled(int n) { this.n = n; }
  
  @Override
  public String toString() { return n + "(%rbp)"; }
  
  @Override
  public boolean equals(Object that) {
    if (that instanceof Reg) return false;
    return ((Spilled)that).n == this.n;
  }
}

/** une opérande qui est un registre (physique) */
class Reg extends Operand {
  Register r;

  Reg(Register r) { this.r = r; }
  
  @Override
  public String toString() { return r.toString(); }
  
  @Override
  public boolean equals(Object that) {
    if (that instanceof Spilled) return false;
    return ((Reg)that).r.equals(this.r);
  }
}

/** instruction LTL */

abstract class LTL {
  abstract void accept(LTLVisitor v);
  abstract Label[] succ();
  }

/** les mêmes que dans ERTL */

class Lload extends LTL {
  public Register r1;
  public int i;
  public Register r2;
  public Label l;
  Lload(Register r1, int i, Register r2, Label l) { this.r1 = r1; this.i = i;
    this.r2 = r2; this.l = l;  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return "mov " + i + "(" + r1 + ") " + r2 + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
 }

class Lstore extends LTL {
  public Register r1;
  public Register r2;
  public int i;
  public Label l;
  Lstore(Register r1, Register r2, int i, Label l) { this.r1 = r1;
    this.r2 = r2; this.i = i; this.l = l;  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return "mov " + r1 + " " + i + "(" + r2 + ") " + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

class Lgoto extends LTL {
  public Label l;
  Lgoto(Label l) { this.l = l;  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return "goto " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

class Lreturn extends LTL {
  Lreturn() {  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return "return"; }
  Label[] succ() { return new Label[] { }; }
  }

/** les mêmes que dans ERTL, mais avec Operand à la place de Register */

class Lconst extends LTL {
  public int i;
  public Operand o;
  public Label l;
  Lconst(int i, Operand o, Label l) { this.i = i; this.o = o; this.l = l;  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return "mov $" + i + " " + o + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

class Lmunop extends LTL {
  public Munop m;
  public Operand o;
  public Label l;
  Lmunop(Munop m, Operand o, Label l) { this.m = m; this.o = o; this.l = l;
     }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + o + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

class Lmbinop extends LTL {
  public Mbinop m;
  public Operand o1;
  public Operand o2;
  public Label l;
  Lmbinop(Mbinop m, Operand o1, Operand o2, Label l) { this.m = m;
    this.o1 = o1; this.o2 = o2; this.l = l;  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + o1 + " " + o2 + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

class Lmubranch extends LTL {
  public Mubranch m;
  public Operand r;
  public Label l1;
  public Label l2;
  Lmubranch(Mubranch m, Operand r, Label l1, Label l2) { this.m = m;
    this.r = r; this.l1 = l1; this.l2 = l2;  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r + " --> " + l1 + ", " + l2; }
  Label[] succ() { return new Label[] { l1, l2 }; }
  }

class Lmbbranch extends LTL {
  public Mbbranch m;
  public Operand r1;
  public Operand r2;
  public Label l1;
  public Label l2;
  Lmbbranch(Mbbranch m, Operand r1, Operand r2, Label l1, Label l2) {
    this.m = m; this.r1 = r1; this.r2 = r2; this.l1 = l1; this.l2 = l2;  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r1 + " " + r2 + " --> " + l1 + ", " + l2; }
  Label[] succ() { return new Label[] { l1, l2 }; }
  }

class Lpush extends LTL {
  public Operand o;
  public Label l;
  public Lpush(Operand o, Label l) { this.o = o; this.l = l; }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return "push " + o + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
}

/** légèrement modifiée */

class Lcall extends LTL {
  public String s;
  public Label l;
  Lcall(String s, Label l) { this.s = s; this.l = l;  }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return "call " + s + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

/* nouveau */

class Lpop extends LTL {
  public Register r;
  public Label l;
  public Lpop(Register r, Label l) { this.r = r; this.l = l; }
  void accept(LTLVisitor v) { v.visit(this); }
  public String toString() { return "pop " + r + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
}

/** une fonction LTL */

class LTLfun {
  /** nom de la fonction */
  public String name;
  /** point d'entrée dans le graphe */
  public Label entry;
  /** le graphe de flot de contrôle */
  public LTLgraph body;
  
  LTLfun(String name) {
    this.name = name;
  }
  void accept(LTLVisitor v) { v.visit(this); }

  /** pour débugger */
  void print() {
    System.out.println("== LTL ==========================");
    System.out.println(name + "()");
    System.out.println("  entry  : " + entry);
    body.print(entry);
  }
}

class LTLfile {
  public LinkedList<LTLfun> funs;
  LTLfile() {
    this.funs = new LinkedList<LTLfun>();
  }
  void accept(LTLVisitor v) { v.visit(this); }
  
  /** pour débugger */
  void print() {
    for (LTLfun fun: this.funs)
      fun.print();
  }
}

/** graphe de flot de contrôle (d'une fonction)
 * 
 * c'est un dictionnaire qui associe une instruction de type RTL
 * à une étiquette de type Label
 */
class LTLgraph {
  Map<Label, LTL> graph = new HashMap<Label, LTL>();
  
  /** ajoute une nouvelle instruction dans le graphe
    * et renvoie son étiquette */
  Label add(LTL instr) {
    Label l = new Label();
    graph.put(l, instr);
    return l;
  }
  
  void put(Label l, LTL instr) {
    graph.put(l, instr);
  }
  
  // imprime le graphe par un parcours en profondeur
  private void print(Set<Label> visited, Label l) {
    if (visited.contains(l)) return;
    visited.add(l);
    LTL r = this.graph.get(l);
    if (r == null) return; // c'est le cas pour exit
    System.out.println("  " + String.format("%3s", l) + ": " + r);
    for (Label s: r.succ()) print(visited, s);
  }
  
  /** imprime le graphe (pour debugger) */
  void print(Label entry) {
    print(new HashSet<Label>(), entry);
  }
}

interface LTLVisitor {
  public void visit(Lload o);
  public void visit(Lstore o);
  public void visit(Lmubranch o);
  public void visit(Lmbbranch o);
  public void visit(Lgoto o);
  public void visit(Lreturn o);
  public void visit(Lconst o);
  public void visit(Lmunop o);
  public void visit(Lmbinop o);
  public void visit(Lpush o);
  public void visit(Lpop o);
  public void visit(Lcall o);
  public void visit(LTLfun o);
  public void visit(LTLfile o);
  }

class EmptyLTLVisitor implements LTLVisitor {
  public void visit(Lload o) {}
  public void visit(Lstore o) {}
  public void visit(Lmubranch o) {}
  public void visit(Lmbbranch o) {}
  public void visit(Lgoto o) {}
  public void visit(Lreturn o) {}
  public void visit(Lconst o) {}
  public void visit(Lmunop o) {}
  public void visit(Lmbinop o) {}
  public void visit(Lpush o) {}
  public void visit(Lpop o) {}
  public void visit(Lcall o) {}
  public void visit(LTLfun o) {}
  public void visit(LTLfile o) {}
  }
