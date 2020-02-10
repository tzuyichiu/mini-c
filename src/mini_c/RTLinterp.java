package mini_c;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Interprète de code RTL */
public class RTLinterp implements RTLVisitor {

  private Map<String, RTLfun> funs;
  private Map<Register, Long> regs;
  private Memory mem;
  private Label next;
  
  /** interprète un programme RTL donné, à partir de la fonction "main" */
  RTLinterp(RTLfile file) {
    this.funs = new HashMap<String, RTLfun>();
    for (RTLfun f: file.funs)
      this.funs.put(f.name, f);
    this.mem = new Memory();
    call("main", new LinkedList<Register>());
  }

  private long call(String name, List<Register> rl) {
    RTLfun f = this.funs.get(name);
    assert f != null; // programme bien typé
    Map<Register, Long> saved_regs = this.regs, new_regs = new HashMap<>();
    for (Register r: f.locals) new_regs.put(r, 0L);
    assert f.formals.size() == rl.size(); // programme bien typé
    Iterator<Register> actuals = rl.iterator();
    for (Register param: f.formals)
      new_regs.put(param, get(actuals.next()));
    this.regs = new_regs;
    this.next = f.entry;
    while (!this.next.equals(f.exit)) {
      RTL i = f.body.graph.get(this.next);
      if (i == null) throw new Error("no RTL instruction at label " + this.next);
      i.accept(this);
    }
    long res = this.regs.containsKey(f.result) ? get(f.result) : 0L;
    this.regs = saved_regs;
    return res;
  }
  
  private void set(Register r, long v) {
    this.regs.put(r, v);
  }
  private void set(Register r, boolean b) {
    this.regs.put(r, b ? 1L : 0L);
  }
  private long get(Register r) {
    if (!this.regs.containsKey(r)) throw new Error("unknown register " + r);
    return this.regs.get(r);
  }
  
  @Override
  public void visit(Rconst o) {
    set(o.r, o.i);
    this.next = o.l;
  }

  @Override
  public void visit(Rload o) {
    long p = get(o.r1);
    set(o.r2, this.mem.get(p, o.i));
    this.next = o.l;
  }

  @Override
  public void visit(Rstore o) {
    long p = get(o.r2);
    long v = get(o.r1);
    this.mem.set(p, o.i, v);
    this.next = o.l;
  }

  @Override
  public void visit(Rmunop o) {
    long v = get(o.r);
    if (o.m instanceof Maddi)
      set(o.r, v + ((Maddi)o.m).n);
    else if (o.m instanceof Msetei)
      set(o.r, v == ((Msetei)o.m).n);
    else // Msetnei
      set(o.r, v != ((Msetnei)o.m).n);
    this.next = o.l;
  }

  @Override
  public void visit(Rmbinop o) {
    long v1 = get(o.r1);
    if (o.m == Mbinop.Mmov)
      set(o.r2, v1);
    else {
      long v2 = get(o.r2);
      switch (o.m) {
      case Madd: set(o.r2, v2 + v1); break;
      case Msub: set(o.r2, v2 - v1); break;
      case Mmul: set(o.r2, v2 * v1); break;
      case Mdiv: set(o.r2, v2 / v1); break;
      case Msete: set(o.r2, v2 == v1); break;
      case Msetne: set(o.r2, v2 != v1); break;
      case Msetl: set(o.r2, v2 < v1); break;
      case Msetle: set(o.r2, v2 <= v1); break;
      case Msetg: set(o.r2, v2 > v1); break;
      case Msetge: set(o.r2, v2 >= v1); break;
      default: assert false; // Mmov déjà traité
      }
    }
    this.next = o.l;
  }

  @Override
  public void visit(Rmubranch o) {
    long v = get(o.r);
    boolean b;
    if      (o.m instanceof Mjz  ) b = v == 0L;
    else if (o.m instanceof Mjnz ) b = v != 0L;
    else if (o.m instanceof Mjlei) b = v <= ((Mjlei)o.m).n;
    else /*  o.m instanceof Mjgi */b = v > ((Mjgi)o.m).n;
    this.next = b ? o.l1 : o.l2;
  }

  @Override
  public void visit(Rmbbranch o) {
    long v1 = get(o.r1);
    long v2 = get(o.r2);
    boolean b = true; // parce que le compilo Java n'est pas assez malin
    switch (o.m) {
    case Mjl : b = v2 <  v1; break;
    case Mjle: b = v2 <= v1; break;
    }
    this.next = b ? o.l1 : o.l2;
  }

  @Override
  public void visit(Rcall o) {
    switch (o.s) {
    case "sbrk":
      set(o.r, this.mem.malloc((int)get(o.rl.get(0))));
      break;
    case "putchar":
      long n = get(o.rl.get(0));
      System.out.print((char)n);
      set(o.r, n);
      break;
    default:
      set(o.r, call(o.s, o.rl));
    }
    this.next = o.l;
  }

  @Override
  public void visit(Rgoto o) {
    this.next = o.l;
  }

  @Override
  public void visit(RTLfun o) {
    assert false; // inutilisé
  }

  @Override
  public void visit(RTLfile o) {
    assert false; // inutilisé
  }
  
}
