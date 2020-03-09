package mini_c;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/** Interprète de code LTL */
public class LTLinterp  implements LTLVisitor {

  private Map<String, LTLfun> funs;
  private Machine mem;
  private Label next;
  
  /** interprète un programme RTL donné, à partir de la fonction "main" */
  LTLinterp(LTLfile file) {
    this.funs = new HashMap<String, LTLfun>();
    for (LTLfun f: file.funs)
      this.funs.put(f.name, f);
    this.mem = new Machine();
    call("main");
  }

  private void call(String name) {
    LTLfun f = this.funs.get(name);
    assert f != null; // programme bien typé
    this.mem.push(0l); // adresse de retour fictive
    this.next = f.entry;
    while (true) {
      LTL i = f.body.graph.get(this.next);
      if (i == null) throw new Error("no LTL instruction at label " + this.next);
      if (i instanceof Lreturn) break;
      i.accept(this);
    }
    this.mem.pop(); // dépile l'adresse de retour fictive
  }
  
  long get(Register r) { return this.mem.get(r); }
  void set(Register r, long v) { this.mem.set(r,  v); }
  void set(Register r, boolean v) { this.mem.set(r,  v); }
  
  private void set(Operand o, long v) {
    if (o instanceof Reg)
      set(((Reg)o).r, v);
    else {
      int ofs = ((Spilled)o).n;
      this.mem.store(get(Register.rbp), ofs, v);
    }
  }
  private void set(Operand o, boolean b) {
    set(o, b ? 1l: 0l);
  }
  private long get(Operand o) {
    if (o instanceof Reg)
      return get(((Reg)o).r);
    else {
      int ofs = ((Spilled)o).n;
      return this.mem.load(get(Register.rbp), ofs);
    }
  }
  
  @Override
  public void visit(Lconst o) {
    set(o.o, o.i);
    this.next = o.l;
  }

  @Override
  public void visit(Lload o) {
    long p = get(o.r1);
    set(o.r2, this.mem.load(p, o.i));
    this.next = o.l;
  }

  @Override
  public void visit(Lstore o) {
    long p = get(o.r2);
    long v = get(o.r1);
    this.mem.store(p, o.i, v);
    this.next = o.l;
  }

  @Override
  public void visit(Lmunop o) {
    long v = get(o.o);
    if (o.m instanceof Maddi)
      set(o.o, v + ((Maddi)o.m).n);
    else if (o.m instanceof Msetei)
      set(o.o, v == ((Msetei)o.m).n);
    else // Msetnei
      set(o.o, v != ((Msetnei)o.m).n);
    this.next = o.l;
  }

  @Override
  public void visit(Lmbinop o) {
    long v1 = get(o.o1);
    if (o.m == Mbinop.Mmov)
      set(o.o2, v1);
    else {
      long v2 = get(o.o2);
      switch (o.m) {
      case Madd: set(o.o2, v2 + v1); break;
      case Msub: set(o.o2, v2 - v1); break;
      case Mmul: set(o.o2, v2 * v1); break;
      case Mdiv:
        if (!o.o2.equals(new Reg(Register.rax))) throw new Error("div: r2 must be %rax");
        set(o.o2, v2 / v1); break;
      case Msete: set(o.o2, v2 == v1); break;
      case Msetne: set(o.o2, v2 != v1); break;
      case Msetl: set(o.o2, v2 < v1); break;
      case Msetle: set(o.o2, v2 <= v1); break;
      case Msetg: set(o.o2, v2 > v1); break;
      case Msetge: set(o.o2, v2 >= v1); break;
      default: assert false; // Mmov déjà traité
      }
    }
    this.next = o.l;
  }

  @Override
  public void visit(Lmubranch o) {
    long v = get(o.r);
    boolean b;
    if      (o.m instanceof Mjz  ) b = v == 0L;
    else if (o.m instanceof Mjnz ) b = v != 0L;
    else if (o.m instanceof Mjlei) b = v <= ((Mjlei)o.m).n;
    else /*  o.m instanceof Mjgi */b = v > ((Mjgi)o.m).n;
    this.next = b ? o.l1 : o.l2;
  }

  @Override
  public void visit(Lmbbranch o) {
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
  public void visit(Lcall o) {
    switch (o.s) {
    case "sbrk":
      set(Register.result, this.mem.malloc((int)get(Register.rdi)));
      break;
    case "putchar":
      long n = get(Register.rdi);
      System.out.print((char)n);
      set(Register.result, n);
      break;
    default:
      call(o.s);
    }
    this.next = o.l;
  }

  @Override
  public void visit(Lgoto o) {
    this.next = o.l;
  }

  @Override
  public void visit(LTLfun o) {
    assert false; // inutilisé
  }

  @Override
  public void visit(LTLfile o) {
    assert false; // inutilisé
  }

 @Override
  public void visit(Lpush o) {
    this.mem.push(get(o.o));
    this.next = o.l;
  }

  @Override
  public void visit(Lreturn o) {
    assert false; // inutilisé
  }

  @Override
  public void visit(Lpop o) {
    set(o.r, this.mem.pop());
    this.next = o.l;
  }
  
}
