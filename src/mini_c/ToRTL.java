package mini_c;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

class ToRTL implements Visitor {
	
	private RTLgraph graph; // graphe en cours de construction
	private RTLfun fun;
	private RTLfile file;
	private Label l;
	private Register r;
	private HashMap<String,Register> var2regs;
	private HashMap<String,Register> arg2regs;
	
	ToRTL() {
		this.graph = new RTLgraph();
		this.var2regs = new HashMap<>();
		this.arg2regs = new HashMap<>();
	}
	
	public RTLfile translate(File tree) {
		tree.accept(this);
		return file;
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
		this.fun.locals.add(r);
		this.var2regs.put(n.name, r);
	}

	@Override
	public void visit(Expr n) {
		n.accept(this);
	}

	@Override
	public void visit(Econst n) {
		this.l = this.graph.add(new Rconst(n.i, this.r, this.l));
	}

	@Override
	public void visit(Eaccess_local n) {
		Register r1 = this.var2regs.get(n.i);
		if (r1 == null) {
			r1 = this.arg2regs.get(n.i);
		}
		this.l = this.graph.add(new Rmbinop(Mbinop.Mmov, r1, this.r, this.l));
	}

	@Override
	public void visit(Eaccess_field n) {

		Register r1 = new Register();
		this.l = this.graph.add(new Rload(r1, n.f.offset, this.r, this.l));
		this.r = r1;
		n.e.accept(this);
		
	}

	@Override
	public void visit(Eassign_local n) {
		Register r1 = this.var2regs.get(n.i);
		
		if (r1 == null) {
			r1 = this.arg2regs.get(n.i);
		}
		this.l = this.graph.add(new Rmbinop(Mbinop.Mmov, this.r, r1, this.l));
		n.e.accept(this);
	}

	@Override
	public void visit(Eassign_field n) {
		Register r1 = new Register();
		Label l1 = this.graph.add(new Rstore(r1, this.r, n.f.offset, this.l));
		this.l = l1;
		n.e1.accept(this);
		this.r = r1;
		n.e2.accept(this);
	}

	@Override
	public void visit(Eunop n) {
		switch (n.u) {
        case Uneg:
            
            // We create a node "L1: sub r1 r2" (L1 and r1 fresh), and r2 
            // given recursively by the previous visitor.
            // Then we create another node to put "L2: mov $0 r2" (L2 fresh)
            // Finally we recursively process the n.e expression with 
            // references to r1 and L2 (stored in this.r and this.l)

            Register r1 = new Register();
            Register r2 = this.r;
            this.l = this.graph.add(new Rmbinop(Mbinop.Msub, r1, r2, this.l)); 
            // the returned label is now L1 (see above). Stored in this.l
            
            this.l = this.graph.add(new Rconst(0, r2, this.l)); 
            // the returned label is now L2
            this.r = r1;
            n.e.accept(this);
            break;
        
        case Unot:		
            this.l = this.graph.add(new Rmunop(new Msetei(0), this.r, this.l));
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
        
        // not Band, Bor
		/* 
		 * |!| Careful here : we want to compute e1 op e2 with r2 <- r2 op r1
		 * so we have to put e1 in r2 and e2 in r1 
		 */
        if (c == -1) {
			Register r1 = new Register();
			Register r2 = this.r;
			this.l = this.graph.add(new Rmbinop(mb, r1, r2, this.l));
			this.r = r1;
			n.e2.accept(this);
			this.r = r2;
			n.e1.accept(this);
        }
        // Band, Bor
		else {
			Label l_true = this.graph.add(new Rconst(c, this.r, this.l));
			Label l_false = this.graph.add(new Rconst(1-c, this.r, this.l));
			Label if_e1_false = this.graph.add(
                new Rmubranch(mub, this.r, l_true, l_false));
			this.l = if_e1_false;
			n.e2.accept(this);	
			this.l = this.graph.add(new Rmubranch(mub, this.r, l_true, this.l));
			n.e1.accept(this);
        }
	}

	@Override
	public void visit(Ecall n) {
		LinkedList<Register> rl = new LinkedList<>();
		for (Expr args: n.el) {
			rl.add(new Register());
		}
		
		this.l = this.graph.add(new Rcall(this.r, n.i, rl, this.l));

		Iterator<Register> listIter = rl.listIterator();
		for (Expr args: n.el) {
			this.r = listIter.next();
			args.accept(this);
		}
	}

	@Override
	public void visit(Esizeof n) {
		
		this.l = this.graph.add(new Rconst(n.s.size, this.r, this.l));
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
		Label prev_l = this.l;
        n.s1.accept(this);
        Label truel = this.l;
        this.l = prev_l;
        n.s2.accept(this);
        Label falsel = this.l;
    
        Register r1 = new Register();
        this.r = r1;
        this.l = this.graph.add(new Rmubranch(new Mjnz(), r1, truel, falsel));
        n.e.accept(this);
	}

	@Override
	public void visit(Swhile n) {
        
        // Since we execute the statements in the reverse order, this.l is
        // the label of the statement just after the while loop: stored in quitl
        Label quitl = this.l;

        // An independent label for goto at the end of the loop
        Label gotol = new Label();
        this.l = gotol; // n.s will thus be directed to gotol
        n.s.accept(this);

        // now this.l contains the label of beginning of the loop
        Register r1 = new Register();
        this.r = r1;
        this.l = this.graph.add(new Rmubranch(new Mjnz(), r1, this.l, quitl));

        n.e.accept(this);
        this.graph.graph.put(gotol, new Rgoto(this.l));
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
		this.l = this.fun.exit;
		this.r = this.fun.result;
		n.e.accept(this);
	}

	@Override
	public void visit(Decl_fun n) {
		this.graph = new RTLgraph();
		this.fun = new RTLfun(n.fun_name);
		for (Decl_var dvar: n.fun_formals) {
			Register r = new Register();
			this.fun.formals.add(r);
			this.arg2regs.put(dvar.name, r);
		}
		this.fun.result = new Register();
		this.fun.locals = new HashSet<>();		
		this.fun.exit = new Label();
		n.fun_body.accept(this);
		this.fun.entry = this.l;
		this.fun.body = this.graph;	
	}

	@Override
	public void visit(File n) {
		this.file = new RTLfile();
		for (Decl_fun f: n.funs) {
			f.accept(this);
			this.file.funs.add(this.fun);
			this.var2regs = new HashMap<>();
			this.arg2regs = new HashMap<>();
		}		
	}
	
}