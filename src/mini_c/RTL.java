package mini_c;

/** Register Transfer Language (RTL) */

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** le type des instructions RTL */
abstract class RTL {
  abstract void accept(RTLVisitor v);
  abstract Label[] succ();
  }

/** charge une constante dans un registre */
class Rconst extends RTL {
  final int i;
  final Register r;
  final Label l;
  
  Rconst(int i, Register r, Label l) { this.i = i; this.r = r; this.l = l;  }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return "mov $" + i + " " + r + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

/** instruction mov i(r1), r2 */
class Rload extends RTL { 
  final Register r1;
  final int i;
  final Register r2;
  final Label l;
  
  Rload(Register r1, int i, Register r2, Label l) { 
	this.r1 = r1; this.i = i;
    this.r2 = r2; this.l = l;  }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return "mov " + i + "(" + r1 + ") " + r2 + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

/** instruction mov r1, i(r2) */
class Rstore extends RTL {
  final Register r1;
  final Register r2;
  final int i;
  final Label l;
  
  Rstore(Register r1, Register r2, int i, Label l) { this.r1 = r1;
    this.r2 = r2; this.i = i; this.l = l;  }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return "mov " + r1 + " " + i + "(" + r2 + ") " + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

/** opération x86-64 unaire */
class Rmunop extends RTL {
  Munop m;
  Register r;
  Label l;
  
  Rmunop(Munop m, Register r, Label l) { this.m = m; this.r = r; this.l = l; }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

/** opération x86-64 binaire 
 *  attention au sens : c'est {@code r2 <- r2 m r1} */
class Rmbinop extends RTL {
  Mbinop m;
  Register r1;
  Register r2;
  Label l;
  
  Rmbinop(Mbinop m, Register r1, Register r2, Label l) { this.m = m;
    this.r1 = r1; this.r2 = r2; this.l = l;  }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r1 + " " + r2 + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

/** instruction x86-64 de branchement (unaire) */
class Rmubranch extends RTL {
  Mubranch m;
  Register r;
  Label l1;
  Label l2;
  
  Rmubranch(Mubranch m, Register r, Label l1, Label l2) { this.m = m;
    this.r = r; this.l1 = l1; this.l2 = l2;  }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r + " --> " + l1 + ", " + l2; }
  Label[] succ() { return new Label[] { l1, l2 }; }
  }

/** instruction x86-64 de branchement (binaire)
 *  attention au sens : c'est le résultat de {@code r2 cmp r1} */
class Rmbbranch extends RTL {
  Mbbranch m;
  Register r1;
  Register r2;
  Label l1;
  Label l2;
  
  Rmbbranch(Mbbranch m, Register r1, Register r2, Label l1, Label l2) {
    this.m = m; this.r1 = r1; this.r2 = r2; this.l1 = l1; this.l2 = l2;  }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return m + " " + r1 + " " + r2 + " --> " + l1 + ", " + l2; }
  Label[] succ() { return new Label[] { l1, l2 }; }
  }

/** appel de fonction */
class Rcall extends RTL {
  Register r;
  String s;
  List<Register> rl;
  Label l;
  
  Rcall(Register r, String s, List<Register> rl, Label l) { this.r = r;
    this.s = s; this.rl = rl; this.l = l;  }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return r + " <- call " + s + rl + " --> " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

/** saut inconditionnel */
class Rgoto extends RTL {
  Label l;
  
  Rgoto(Label l) { this.l = l;  }
  
  void accept(RTLVisitor v) { v.visit(this); }
  public String toString() { return "goto " + l; }
  Label[] succ() { return new Label[] { l }; }
  }

/** une fonction RTL */

class RTLfun {
  /** nom de la fonction */
  String name;
  /** paramètres formels */
  List<Register> formals;
  /** résultat de la fonction */
  Register result;
  /** ensemble des variables locales */
  Set<Register> locals;
  /** point d'entrée dans le graphe */
  Label entry;
  /** point de sortie dans le graphe */
  Label exit;
  /** le graphe de flot de contrôle */
  RTLgraph body;
  
  RTLfun(String name) {
    this.name = name;
    this.formals = new LinkedList<>();
    this.locals = new HashSet<>();
    }
  
  void accept(RTLVisitor v) { v.visit(this); }
  
  /** pour débugger */
  void print() {
    System.out.println("== RTL ==========================");
    System.out.println(result + " " + name + formals);
    System.out.println("  entry  : " + entry);
    System.out.println("  exit   : " + exit);
    System.out.println("  locals : " + locals);
    body.print(entry);
  }
}

/** un programme RTL */

class RTLfile {
  List<RTLfun> funs;
  
  RTLfile() {
    this.funs = new LinkedList<RTLfun>();  }
  
  void accept(RTLVisitor v) { v.visit(this); }

  /** pour débugger */
  void print() {
	for (RTLfun fun: this.funs)
	  fun.print();
  }
}

/** graphe de flot de contrôle (d'une fonction)
 * 
 * c'est un dictionnaire qui associe une instruction de type RTL
 * à une étiquette de type Label
 */
class RTLgraph {
	Map<Label, RTL> graph = new HashMap<Label, RTL>();
	
	/** ajoute une nouvelle instruction dans le graphe
	  * et renvoie son étiquette */
	Label add(RTL instr) {
		Label l = new Label();
		graph.put(l, instr);
		return l;
	}
	
	// imprime le graphe par un parcours en profondeur
	private void print(Set<Label> visited, Label l) {
		if (visited.contains(l)) return;
		visited.add(l);
		RTL r = this.graph.get(l);
		if (r == null) return; // c'est le cas pour exit
		System.out.println("  " + String.format("%3s", l) + ": " + r);
		for (Label s: r.succ()) print(visited, s);
	}
	
	/** imprime le graphe (pour debugger) */
	void print(Label entry) {
		print(new HashSet<Label>(), entry);
	}
}

/** visiteur pour parcourir la forme RTL
 * (pour la suite du compilateur)
 */

interface RTLVisitor {
  public void visit(Rconst o);
  public void visit(Rload o);
  public void visit(Rstore o);
  public void visit(Rmunop o);
  public void visit(Rmbinop o);
  public void visit(Rmubranch o);
  public void visit(Rmbbranch o);
  public void visit(Rcall o);
  public void visit(Rgoto o);
  public void visit(RTLfun o);
  public void visit(RTLfile o);
  }

/** un visiteur du code RTL qui ne fait rien */
class EmptyRTLVisitor implements RTLVisitor {
  public void visit(Rconst o) {}
  public void visit(Rload o) {}
  public void visit(Rstore o) {}
  public void visit(Rmunop o) {}
  public void visit(Rmbinop o) {}
  public void visit(Rmubranch o) {}
  public void visit(Rmbbranch o) {}
  public void visit(Rcall o) {}
  public void visit(Rgoto o) {}
  public void visit(RTLfun o) {}
  public void visit(RTLfile o) {}
  }
