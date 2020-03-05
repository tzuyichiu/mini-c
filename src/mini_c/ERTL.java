package mini_c;

/** Explicit Register Transfer Language (ERTL) */

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/** instruction ERTL */

/** les mêmes que dans RTL */

abstract class ERTL {
  abstract void accept(ERTLVisitor v);
  abstract Label[] succ();
  
  abstract Set<Register> def();
  abstract Set<Register> use();

  protected static Set<Register> emptySet = new HashSet<>();
  protected static Set<Register> singleton(Register r) {
    Set<Register> s = new HashSet<>(); s.add(r); return s; }
  protected static Set<Register> pair(Register r1, Register r2) {
    Set<Register> s = singleton(r1); s.add(r2); return s; }
  protected static Set<Register> triple(Register r1, Register r2, Register r3) {
    Set<Register> s = pair(r1, r2); s.add(r3); return s; }
}

class ERconst extends ERTL {
  public int i;
  public Register r;
  public Label l;
  ERconst(int i, Register r, Label l) { this.i = i; this.r = r; this.l = l;
  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "mov $" + i + " " + r + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return singleton(r); }
  @Override Set<Register> use() { return emptySet; }
}

class ERload extends ERTL {
  public Register r1;
  public int i;
  public Register r2;
  public Label l;
  ERload(Register r1, int i, Register r2, Label l) { this.r1 = r1;
    this.i = i; this.r2 = r2; this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "mov " + i + "(" + r1 + ") " + r2 + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return singleton(r2); }
  @Override Set<Register> use() { return singleton(r1); }
}

class ERstore extends ERTL {
  public Register r1;
  public Register r2;
  public int i;
  public Label l;
  ERstore(Register r1, Register r2, int i, Label l) { this.r1 = r1;
    this.r2 = r2; this.i = i; this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "mov " + r1 + " " + i + "(" + r2 + ") " + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return emptySet; }
  @Override Set<Register> use() { return pair(r1, r2); }
}

class ERmunop extends ERTL {
  public Munop m;
  public Register r;
  public Label l;
  ERmunop(Munop m, Register r, Label l) { this.m = m; this.r = r; this.l = l;
     }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return singleton(r); }
  @Override Set<Register> use() { return singleton(r); }
}

class ERmbinop extends ERTL {
  public Mbinop m;
  public Register r1;
  public Register r2;
  public Label l;
  ERmbinop(Mbinop m, Register r1, Register r2, Label l) { this.m = m;
    this.r1 = r1; this.r2 = r2; this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r1 + " " + r2 + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { 
    if (m == Mbinop.Mdiv) {
      assert (r2.equals(Register.rax));
      return pair(Register.rax, Register.rdx);
    } else
      return singleton(r2); }
  @Override Set<Register> use() {
    if (m == Mbinop.Mdiv) return triple(Register.rax, Register.rdx, r1);
    else if (m == Mbinop.Mmov) return singleton(r1);
    else return pair(r1, r2); }
}

class ERmubranch extends ERTL {
  public Mubranch m;
  public Register r;
  public Label l1;
  public Label l2;
  ERmubranch(Mubranch m, Register r, Label l1, Label l2) { this.m = m;
    this.r = r; this.l1 = l1; this.l2 = l2;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r + " --> " + l1 + ", " + l2; }
  Label[] succ() { return new Label[] { l1, l2 }; }
  @Override Set<Register> def() { return emptySet; }
  @Override Set<Register> use() { return singleton(r); }
}

class ERmbbranch extends ERTL {
  public Mbbranch m;
  public Register r1;
  public Register r2;
  public Label l1;
  public Label l2;
  ERmbbranch(Mbbranch m, Register r1, Register r2, Label l1, Label l2) {
    this.m = m; this.r1 = r1; this.r2 = r2; this.l1 = l1; this.l2 = l2;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r1 + " " + r2 + " --> " + l1 + ", " + l2; }
  Label[] succ() { return new Label[] { l1, l2 }; }
  @Override Set<Register> def() { return emptySet; }
  @Override Set<Register> use() { return pair(r1, r2); }
}

class ERgoto extends ERTL {
  public Label l;
  ERgoto(Label l) { this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "goto " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return emptySet; }
  @Override Set<Register> use() { return emptySet; }
}

/* modifiée */

class ERcall extends ERTL {
  public String s;
  /** nombre d'arguments passés dans des registres */
  public int i;
  public Label l;
  ERcall(String s, int i, Label l) { this.s = s; this.i = i; this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "call " + s + "(" + i + ") --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return new HashSet<>(Register.caller_save); }
  @Override Set<Register> use() {
    Set<Register> s = new HashSet<>();
    int k = i;
    assert (k <= Register.parameters.size());
    for (Register r: Register.parameters) {
      if (k-- == 0) break;
      s.add(r);
    }
    return s;
  }
}

/* nouvelles instructions */

class ERalloc_frame extends ERTL {
  public Label l;
  ERalloc_frame(Label l) { this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "alloc_frame --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return emptySet; }
  @Override Set<Register> use() { return emptySet; }
}

class ERdelete_frame extends ERTL {
  public Label l;
  ERdelete_frame(Label l) { this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "delete_frame --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return emptySet; }
  @Override Set<Register> use() { return emptySet; }
}

class ERget_param extends ERTL {
  public int i;
  public Register r;
  public Label l;
  ERget_param(int i, Register r, Label l) { this.i = i; this.r = r;
    this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "mov stack(" + i + ") " + r + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return singleton(r); }
  @Override Set<Register> use() { return emptySet; }
}

class ERpush_param extends ERTL {
  public Register r;
  public Label l;
  ERpush_param(Register r, Label l) { this.r = r; this.l = l;  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "push " + r + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  @Override Set<Register> def() { return emptySet; }
  @Override Set<Register> use() { return singleton(r); }
}

class ERreturn extends ERTL {
  ERreturn() {  }
  void accept(ERTLVisitor v) { v.visit(this); }
  public String toString() { return "return"; }
  Label[] succ() { return new Label[] { }; }
  @Override Set<Register> def() { return emptySet; }
  @Override Set<Register> use() {
    Set<Register> s = new HashSet<>(Register.callee_saved);
    s.add(Register.rax);
    return s;
  }
}

/** une fonction ERTL */

class ERTLfun {
  /** nom de la fonction */
  public String name;
  /** nombre total d'arguments */
  public int formals;
  /** ensemble des variables locales */
  public Set<Register> locals;
  /** point d'entrée dans le graphe */
  public Label entry;
  /** le graphe de flot de contrôle */
  public ERTLgraph body;
  
  ERTLfun(String name, int formals) {
    this.name = name; this.formals = formals; this.locals = new HashSet<>();
  }
  void accept(ERTLVisitor v) { v.visit(this); }

  /** pour débugger */
  void print() {
    System.out.println("== ERTL =========================");
    System.out.println(name + "(" + formals + ")");
    System.out.println("  entry  : " + entry);
    System.out.println("  locals : " + locals);
    body.print(entry);
  }
}

class ERTLfile {
  public LinkedList<ERTLfun> funs;
  ERTLfile() {
    this.funs = new LinkedList<>();  }
  void accept(ERTLVisitor v) { v.visit(this); }

  /** pour débugger */
  void print() {
    for (ERTLfun fun: this.funs)
      fun.print();
  }
}

/** graphe de flot de contrôle (d'une fonction)
 * 
 * c'est un dictionnaire qui associe une instruction de type RTL
 * à une étiquette de type Label
 */
class ERTLgraph {
  Map<Label, ERTL> graph = new HashMap<Label, ERTL>();
  
  /** ajoute une nouvelle instruction dans le graphe
    * et renvoie son étiquette */
  Label add(ERTL instr) {
    Label l = new Label();
    graph.put(l, instr);
    return l;
  }
  
  void put(Label l, ERTL instr) {
    graph.put(l, instr);
  }
  
  // imprime le graphe par un parcours en profondeur
  private void print(Set<Label> visited, Label l) {
    if (visited.contains(l)) return;
    visited.add(l);
    ERTL r = this.graph.get(l);
    if (r == null) return; // c'est le cas pour exit
    System.out.println("  " + String.format("%3s", l) + ": " + r);
    for (Label s: r.succ()) print(visited, s);
  }
  
  /** imprime le graphe (pour debugger) */
  void print(Label entry) {
    print(new HashSet<Label>(), entry);
  }
}

interface ERTLVisitor {
  public void visit(ERconst o);
  public void visit(ERload o);
  public void visit(ERstore o);
  public void visit(ERmunop o);
  public void visit(ERmbinop o);
  public void visit(ERmubranch o);
  public void visit(ERmbbranch o);
  public void visit(ERgoto o);
  public void visit(ERcall o);
  public void visit(ERalloc_frame o);
  public void visit(ERdelete_frame o);
  public void visit(ERget_param o);
  public void visit(ERpush_param o);
  public void visit(ERreturn o);
  public void visit(ERTLfun o);
  public void visit(ERTLfile o);
  }

class EmptyERTLERTLVisitor implements ERTLVisitor {
  public void visit(ERconst o) {}
  public void visit(ERload o) {}
  public void visit(ERstore o) {}
  public void visit(ERmunop o) {}
  public void visit(ERmbinop o) {}
  public void visit(ERmubranch o) {}
  public void visit(ERmbbranch o) {}
  public void visit(ERgoto o) {}
  public void visit(ERcall o) {}
  public void visit(ERalloc_frame o) {}
  public void visit(ERdelete_frame o) {}
  public void visit(ERget_param o) {}
  public void visit(ERpush_param o) {}
  public void visit(ERreturn o) {}
  public void visit(ERTLfun o) {}
  public void visit(ERTLfile o) {}
  }
