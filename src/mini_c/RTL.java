package mini_c;

/** Register Transfer Language (RTL) */

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** the type of RTL instructions */
abstract class RTL {
    abstract void accept(RTLVisitor v);
    abstract Label[] succ();
}

/** stock a constant in a register */
class Rconst extends RTL {
    final int i;
    final Register r;
    final Label l;

    Rconst(int i, Register r, Label l) { this.i = i; this.r = r; this.l = l; }

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
    public String toString() { 
        return "mov " + i + "(" + r1 + ") " + r2 + " --> " + l; 
    }
    Label[] succ() { return new Label[] { l }; }
}

/** instruction mov r1, i(r2) */
class Rstore extends RTL {
    final Register r1;
    final Register r2;
    final int i;
    final Label l;

    Rstore(Register r1, Register r2, int i, Label l) { 
        this.r1 = r1; this.r2 = r2; this.i = i; this.l = l;
    }

    void accept(RTLVisitor v) { v.visit(this); }
    public String toString() { 
        return "mov " + r1 + " " + i + "(" + r2 + ") " + " --> " + l; 
    }
    Label[] succ() { return new Label[] { l }; }
}

/** unary operation x86-64 */
class Rmunop extends RTL {
    Munop m;
    Register r;
    Label l;

    Rmunop(Munop m, Register r, Label l) { 
        this.m = m; this.r = r; this.l = l; 
    }

    void accept(RTLVisitor v) { v.visit(this); }
    public String toString() { return m + " " + r + " --> " + l; }
    Label[] succ() { return new Label[] { l }; }
}

/** binary operation x86-64
 *  be careful of the order: it's the result of {@code r2 <- r2 m r1} */
class Rmbinop extends RTL {
    Mbinop m;
    Register r1;
    Register r2;
    Label l;

    Rmbinop(Mbinop m, Register r1, Register r2, Label l) {
        this.m = m; this.r1 = r1; this.r2 = r2; this.l = l;
    }

    void accept(RTLVisitor v) { v.visit(this); }
    public String toString() { 
        return m + " " + r1 + " " + r2 + " --> " + l;
    }
    Label[] succ() { return new Label[] { l }; }
}

/** unary branching instruction x86-64 */
class Rmubranch extends RTL {
    Mubranch m;
    Register r;
    Label l1;
    Label l2;

    Rmubranch(Mubranch m, Register r, Label l1, Label l2) { 
        this.m = m; this.r = r; this.l1 = l1; this.l2 = l2;
    }

    void accept(RTLVisitor v) { v.visit(this); }
    public String toString() { 
        return m + " " + r + " --> " + l1 + ", " + l2;
    }
    Label[] succ() { return new Label[] { l1, l2 }; }
}

/** binary branching instruction x86-64
 *  be careful of the order: it's the result of {@code r2 cmp r1} */
class Rmbbranch extends RTL {
    Mbbranch m;
    Register r1;
    Register r2;
    Label l1;
    Label l2;

    Rmbbranch(Mbbranch m, Register r1, Register r2, Label l1, Label l2) {
        this.m = m; this.r1 = r1; this.r2 = r2; this.l1 = l1; this.l2 = l2;
    }

    void accept(RTLVisitor v) { v.visit(this); }
    public String toString() { 
        return m + " " + r1 + " " + r2 + " --> " + l1 + ", " + l2; 
    }
    Label[] succ() { return new Label[] { l1, l2 }; }
}

/** function call */
class Rcall extends RTL {
    Register r;
    String s;
    List<Register> rl;
    Label l;

    Rcall(Register r, String s, List<Register> rl, Label l) { 
        this.r = r; this.s = s; this.rl = rl; this.l = l;
    }

    void accept(RTLVisitor v) { v.visit(this); }
    public String toString() { return r + " <- call " + s + rl + " --> " + l; }
    Label[] succ() { return new Label[] { l }; }
}

/** inconditionnal jump */
class Rgoto extends RTL {
    Label l;

    Rgoto(Label l) { this.l = l; }

    void accept(RTLVisitor v) { v.visit(this); }
    public String toString() { return "goto " + l; }
    Label[] succ() { return new Label[] { l }; }
}

/** a RTL function */

class RTLfun {
    /** name of the function */
    String name;
    /** formal parameters */
    List<Register> formals;
    /** resultat of the function */
    Register result;
    /** set of local variables */
    Set<Register> locals;
    /** entry point of the graph */
    Label entry;
    /** exit point of the graph */
    Label exit;
    /** control-flow graph */
    RTLgraph body;

    RTLfun(String name) {
        this.name = name;
        this.formals = new LinkedList<>();
        this.locals = new HashSet<>();
    }

    void accept(RTLVisitor v) { v.visit(this); }
  
    /** to debug */
    void print() {
        System.out.println("== RTL ==========================");
        System.out.println(result + " " + name + formals);
        System.out.println("  entry  : " + entry);
        System.out.println("  exit   : " + exit);
        System.out.println("  locals : " + locals);
        body.print(entry);
    }
}

/** a RTL program */

class RTLfile {
    List<RTLfun> funs;

    RTLfile() {
    this.funs = new LinkedList<RTLfun>(); }

    void accept(RTLVisitor v) { v.visit(this); }

    /** to debug */
    void print() {
    for (RTLfun fun: this.funs)
        fun.print();
    }
}

/** control-flow graph (of a function)
 * 
 * it's a dictionary associating an RTL instruction to a Label
 */
class RTLgraph {
	Map<Label, RTL> graph = new HashMap<Label, RTL>();
	
	// add a new instruction to the graphe and return its label
	Label add(RTL instr) {
		Label l = new Label();
		graph.put(l, instr);
		return l;
	}
	
	// print the graph by a depth-first search
	private void print(Set<Label> visited, Label l) {
		if (visited.contains(l)) return;
		visited.add(l);
		RTL r = this.graph.get(l);
		if (r == null) return; // it's the case of exit
		System.out.println("  " + String.format("%3s", l) + ": " + r);
		for (Label s: r.succ()) print(visited, s);
	}
	
	// print the graph (to debug)
	void print(Label entry) {
		print(new HashSet<Label>(), entry);
	}
}

/** visitor to run through a RTLGraph (for the following of the compiler)
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

/** a visitor of RTL code that does nothing */
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
