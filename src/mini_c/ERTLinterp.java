package mini_c;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/** Interprète de code ERTL */
public class ERTLinterp implements ERTLVisitor {

  private Map<String, ERTLfun> funs;
  private Machine mem;
  private Label next;
  
  /** interprète un programme RTL donné, à partir de la fonction "main" */
  ERTLinterp(ERTLfile file) {
    this.funs = new HashMap<String, ERTLfun>();
    for (ERTLfun f: file.funs)
      this.funs.put(f.name, f);
    this.mem = new Machine();
    call("main");
  }

  private void call(String name) {
    ERTLfun f = this.funs.get(name);
    assert f != null; // programme bien typé
    this.mem.push(0L); // adresse de retour fictive
    HashMap<Register, Long> saved_regs = this.mem.regs, new_regs = new HashMap<>();
    for (Register r: f.locals) new_regs.put(r, 0L);
    this.mem.regs = new_regs;
    this.next = f.entry;
    while (true) {
      ERTL i = f.body.graph.get(this.next);
      if (i == null) throw new Error("no ERTL instruction at label " + this.next);
      if (i instanceof ERreturn) break;
      i.accept(this);
    }
    this.mem.pop(); // dépile l'adresse de retour fictive
    this.mem.regs = saved_regs;
  }
  
  long get(Register r) { return this.mem.get(r); }
  void set(Register r, long v) { this.mem.set(r,  v); }
  void set(Register r, boolean v) { this.mem.set(r,  v); }

  @Override
  public void visit(ERconst o) {
    this.mem.set(o.r, o.i);
    this.next = o.l;
  }

  @Override
  public void visit(ERload o) {
    long p = get(o.r1);
    set(o.r2, this.mem.load(p, o.i));
    this.next = o.l;
  }

  @Override
  public void visit(ERstore o) {
    long p = get(o.r2);
    long v = get(o.r1);
    this.mem.store(p, o.i, v);
    this.next = o.l;
  }

  @Override
  public void visit(ERmunop o) {
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
  public void visit(ERmbinop o) {
    long v1 = get(o.r1);
    if (o.m == Mbinop.Mmov)
      set(o.r2, v1);
    else {
      long v2 = get(o.r2);
      switch (o.m) {
      case Madd: set(o.r2, v2 + v1); break;
      case Msub: set(o.r2, v2 - v1); break;
      case Mmul: set(o.r2, v2 * v1); break;
      case Mdiv:
        if (!o.r2.equals(Register.rax)) throw new Error("div: r2 must be %rax");
        set(o.r2, v2 / v1); break;
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
  public void visit(ERmubranch o) {
    long v = get(o.r);
    boolean b;
    if      (o.m instanceof Mjz  ) b = v == 0L;
    else if (o.m instanceof Mjnz ) b = v != 0L;
    else if (o.m instanceof Mjlei) b = v <= ((Mjlei)o.m).n;
    else /*  o.m instanceof Mjgi */b = v > ((Mjgi)o.m).n;
    this.next = b ? o.l1 : o.l2;
  }

  @Override
  public void visit(ERmbbranch o) {
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
  public void visit(ERcall o) {
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
  public void visit(ERgoto o) {
    this.next = o.l;
  }

  @Override
  public void visit(ERTLfun o) {
    assert false; // inutilisé
  }

  @Override
  public void visit(ERTLfile o) {
    assert false; // inutilisé
  }

  @Override
  public void visit(ERalloc_frame o) {
    this.mem.push_register(Register.rbp);
    set(Register.rbp, get(Register.rsp));
    this.next = o.l;
  }

  @Override
  public void visit(ERdelete_frame o) {
    this.mem.pop_in_register(Register.rbp);
    this.next = o.l;
  }

  @Override
  public void visit(ERget_param o) {
    set(o.r, this.mem.load(get(Register.rbp), o.i));
    this.next = o.l;
  }

  @Override
  public void visit(ERpush_param o) {
    this.mem.push(get(o.r));
    this.next = o.l;
  }

  @Override
  public void visit(ERreturn o) {
    // rien à faire ici
  }
  
}
