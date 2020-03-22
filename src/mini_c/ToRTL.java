package mini_c;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

class ToRTL implements Visitor {
	
	private RTLgraph rtlGraph; // graph being constructed
	private RTLfun rtlFun;
	private RTLfile rtlFile;
	private Label lastFresh;
	private Register r;
	private HashMap<String,Register> var2regs;
	private HashMap<String,Register> arg2regs;
	
	ToRTL() {
		this.rtlGraph = new RTLgraph();
		this.var2regs = new HashMap<>();
		this.arg2regs = new HashMap<>();
	}
	
	public RTLfile translate(File tree) {
		tree.accept(this);
		return this.rtlFile;
	}

	@Override
	public void visit(Unop n) {}

	@Override
	public void visit(Binop n) {}

	@Override
	public void visit(String n) {}

	@Override
	public void visit(Tint n) {}

	@Override
	public void visit(Tstructp n) {}

	@Override
	public void visit(Tvoidstar n) {}

	@Override
	public void visit(Ttypenull n) {}

	@Override
	public void visit(Structure n) {}

	@Override
	public void visit(Field n) {}

	@Override
	public void visit(Decl_var n) {
		Register r = new Register();
		this.rtlFun.locals.add(r);
		this.var2regs.put(n.name, r);
	}

	@Override
	public void visit(Expr n) {
		n.accept(this);
	}

	@Override
	public void visit(Econst n) {
		this.lastFresh = this.rtlGraph.add(
            new Rconst(n.i, this.r, this.lastFresh));
	}

	@Override
	public void visit(Eaccess_local n) {
		Register r1 = this.var2regs.get(n.i);
		if (r1 == null) r1 = this.arg2regs.get(n.i);
		this.lastFresh = this.rtlGraph.add(
            new Rmbinop(Mbinop.Mmov, r1, this.r, this.lastFresh));
	}

	@Override
	public void visit(Eaccess_field n) {

		Register r1 = new Register();
		this.lastFresh = this.rtlGraph.add(
            new Rload(r1, n.f.offset, this.r, this.lastFresh));
		this.r = r1;
		n.e.accept(this);
		
	}

	@Override
	public void visit(Eassign_local n) {
		Register r1 = this.var2regs.get(n.i);
		
		if (r1 == null) {
			r1 = this.arg2regs.get(n.i);
		}
		this.lastFresh = this.rtlGraph.add(
            new Rmbinop(Mbinop.Mmov, this.r, r1, this.lastFresh));
		n.e.accept(this);
	}

	@Override
	public void visit(Eassign_field n) {
		Register r1 = new Register();
		Label l1 = this.rtlGraph.add(
            new Rstore(r1, this.r, n.f.offset, this.lastFresh));
		this.lastFresh = l1;
		n.e1.accept(this);
		this.r = r1;
		n.e2.accept(this);
	}

	@Override
	public void visit(Eunop n) {
		switch (n.u) {
        case Uneg:
            
            Register r1 = new Register();
            
            /** 3. We compute this.r - r1 */
            this.lastFresh = this.rtlGraph.add(
                new Rmbinop(Mbinop.Msub, r1, this.r, this.lastFresh));
            
            /** 2. We store $0 inside this.r (an unused register) */
            this.lastFresh = this.rtlGraph.add(
                new Rconst(0, this.r, this.lastFresh));
            
            /** 1. We store the result of n.e inside r1 */
            this.r = r1;
            n.e.accept(this);
            break;
        
        case Unot:
            /** if 0 then 1 else 0 */
            this.lastFresh = this.rtlGraph.add(
                new Rmunop(new Msetei(0), this.r, this.lastFresh));
            n.e.accept(this);
            break;
		}
	}

	@Override
	public void visit(Ebinop n) {
		
        Mbinop mb = null;
        Mubranch mub = null;
        int c = -1;
		switch (n.b) {
        case Beq : mb = Mbinop.Msete;       break;
        case Bneq: mb = Mbinop.Msetne;      break;
        case Blt : mb = Mbinop.Msetl;       break;
        case Ble : mb = Mbinop.Msetle;      break;
        case Bgt : mb = Mbinop.Msetg;       break;
        case Bge : mb = Mbinop.Msetge;      break;
        case Badd: mb = Mbinop.Madd;        break;
        case Bsub: mb = Mbinop.Msub;        break;
        case Bmul: mb = Mbinop.Mmul;        break;
        case Bdiv: mb = Mbinop.Mdiv;        break;
        case Band: mub = new Mjz();  c = 0; break;
        case Bor : mub = new Mjnz(); c = 1; break;
		}
        
		/** 
         * Beq, Bneq, Blt, Ble, Bgt, Bge, Badd, Bsub, Bmul, Bdiv
		 * |!| Careful here: we want to compute e1 op e2 with r2 <- r2 op r1
		 * so we have to put e1 in r2 and e2 in r1 
		 */
        if (c == -1) {
			Register r1 = new Register();
			Register r2 = this.r;
			this.lastFresh = this.rtlGraph.add(
                new Rmbinop(mb, r1, r2, this.lastFresh));
			this.r = r1;
			n.e2.accept(this);
			this.r = r2;
			n.e1.accept(this);
        }
        /** Band, Bor (lazy) */
		else {
			Label l_true = this.rtlGraph.add(
                new Rconst(c, this.r, this.lastFresh));
			Label l_false = this.rtlGraph.add(
                new Rconst(1-c, this.r, this.lastFresh));
			Label if_e1_false = this.rtlGraph.add(
                new Rmubranch(mub, this.r, l_true, l_false));
			this.lastFresh = if_e1_false;
			n.e2.accept(this);	
			this.lastFresh = this.rtlGraph.add(
                new Rmubranch(mub, this.r, l_true, this.lastFresh));
			n.e1.accept(this);
        }
	}

	@Override
	public void visit(Ecall n) {
		LinkedList<Register> rl = new LinkedList<>();
		for (Expr args: n.el) {
			rl.add(new Register());
		}
		
		this.lastFresh = this.rtlGraph.add(
            new Rcall(this.r, n.i, rl, this.lastFresh));

		Iterator<Register> listIter = rl.listIterator();
		for (Expr args: n.el) {
			this.r = listIter.next();
			args.accept(this);
		}
	}

	@Override
	public void visit(Esizeof n) {
		this.lastFresh = this.rtlGraph.add(
            new Rconst(n.s.size, this.r, this.lastFresh));
	}

	@Override
	public void visit(Sskip n) {
		// do nothing
	}

	@Override
	public void visit(Sexpr n) {
		n.e.accept(this);
	}

	@Override
	public void visit(Sif n) {
		Label prev_l = this.lastFresh;
        n.s1.accept(this);
        Label truel = this.lastFresh;
        this.lastFresh = prev_l;
        n.s2.accept(this);
        Label falsel = this.lastFresh;
    
        Register r1 = new Register();
        this.r = r1;
        this.lastFresh = this.rtlGraph.add(
            new Rmubranch(new Mjnz(), r1, truel, falsel));
        n.e.accept(this);
	}

	@Override
	public void visit(Swhile n) {
        
        /** 
         * Since we execute the statements in the reverse order, lastFresh is
         * the label of the statement just after the while loop: stored in quitl
         */
        Label quitl = this.lastFresh;

        // An independent label for goto at the end of the loop
        Label gotol = new Label();
        this.lastFresh = gotol; // n.s will thus be directed to gotol
        n.s.accept(this);

        // now lastFresh contains the label of beginning of the loop
        Register r1 = new Register();
        this.r = r1;
        this.lastFresh = this.rtlGraph.add(
            new Rmubranch(new Mjnz(), r1, this.lastFresh, quitl));

        n.e.accept(this);
        this.rtlGraph.graph.put(gotol, new Rgoto(this.lastFresh));
        // should "goto" here!! (we evaluate again the Expr)
	}

	@Override
	public void visit(Sblock n) {
		for (Decl_var dv: n.dl) {
			dv.accept(this);
		}
        // Process statements in reverse order
		Iterator<Stmt> itr = n.sl.descendingIterator();
		while (itr.hasNext()) {
			itr.next().accept(this);
		}
	}

	@Override
	public void visit(Sreturn n) {
		this.lastFresh = this.rtlFun.exit;
		this.r = this.rtlFun.result;
		n.e.accept(this);
	}

	@Override
	public void visit(Decl_fun n) {
		this.rtlGraph = new RTLgraph();
		this.rtlFun = new RTLfun(n.fun_name);
		for (Decl_var dvar: n.fun_formals) {
			Register r = new Register();
			this.rtlFun.formals.add(r);
			this.arg2regs.put(dvar.name, r);
		}
		this.rtlFun.result = new Register();
		this.rtlFun.locals = new HashSet<>();		
		this.rtlFun.exit = new Label();
		n.fun_body.accept(this);
		this.rtlFun.entry = this.lastFresh;
		this.rtlFun.body = this.rtlGraph;	
	}

	@Override
	public void visit(File n) {
		this.rtlFile = new RTLfile();
		for (Decl_fun f: n.funs) {
			f.accept(this);
			this.rtlFile.funs.add(this.rtlFun);
			this.var2regs = new HashMap<>();
			this.arg2regs = new HashMap<>();
		}		
	}
	
}