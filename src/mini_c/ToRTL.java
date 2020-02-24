package mini_c;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Iterator;

class ToRTL implements Visitor {
	
	private RTLgraph graph; // graphe en cours de construction
	private RTLfun fun;
	private RTLfile file;
	private Label l;
	private Register r;
	private HashMap<String,Register> var2regs; 
	
	ToRTL() {
		this.graph = new RTLgraph();
		this.var2regs = new HashMap<>();
	}
	
	public RTLfile translate(File tree) {
		tree.accept(this);
		return file;
	}

	@Override
	public void visit(Unop n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Binop n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(String n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Tint n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Tstructp n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Tvoidstar n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Ttypenull n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Structure n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Field n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Decl_var n) {
		Register r = new Register();
		this.fun.locals.add(r);
		var2regs.put(n.name,r);
	}

	@Override
	public void visit(Expr n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Econst n) {
		this.l = this.graph.add(new Rconst(n.i,this.r,this.l));
	}

	@Override
	public void visit(Eaccess_local n) {
		Register r1 = var2regs.get(n.i);
		System.out.println(this.l);
		this.l = this.graph.add(new Rmbinop(Mbinop.Mmov,r1,this.r,this.l));
		System.out.println(this.l);
	}

	@Override
	public void visit(Eaccess_field n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Eassign_local n) {
		Register r1 = new Register();
		Register r2 = var2regs.get(n.i);
		this.l = this.graph.add(new Rmbinop(Mbinop.Mmov,r1,r2,this.l));
		this.r = r1;
		n.e.accept(this);
	}

	@Override
	public void visit(Eassign_field n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Eunop n) {
		switch(n.u) {
		case Uneg:
			/*
			 * We create a node "L1 : sub r1 r2" with L1 fresh and r1 fresh, and r2 given recursively by the previous visitor
			 * Then we create another node to put "L2 : mov $0 r2" with L2 fresh
			 * Finally we recursively process the n.e expression with references to register r1 and label L2 
			 * (stored in this.r and this.l)
			 */
			Register r1 = new Register();
			Register r2 = this.r;
			this.l = this.graph.add(new Rmbinop(Mbinop.Msub,r1,r2,this.l)); // After the operation, the returned label is L1 (see above). We store it in this.l.
			this.l = this.graph.add(new Rconst(0,r2,this.l)); // After the operation, the returned label is L2.		
			this.r = r1;
			n.e.accept(this);
			break;
		case Unot:		
			this.l = this.graph.add(new Rmunop(new Msetei(0),this.r,this.l));
			n.e.accept(this);
			break;
		}
		
	}

	@Override
	public void visit(Ebinop n) {
		
		Mbinop mb = null;
		switch(n.b) {
			case Beq: mb = Mbinop.Msete; break;
			case Bneq: mb = Mbinop.Msetne; break;
			case Blt: mb = Mbinop.Msetl; break;
			case Ble: mb = Mbinop.Msetle; break;
			case Bgt: mb = Mbinop.Msetg; break;
			case Bge: mb = Mbinop.Msetge; break;
			case Badd: mb = Mbinop.Madd; break;
			case Bsub: mb = Mbinop.Msub; break;
			case Bmul: mb = Mbinop.Mmul; break;
			case Bdiv: mb = Mbinop.Mdiv; break;
			case Band: break;
			case Bor: break;
		}
		if(n.b != Binop.Band && n.b != Binop.Bor) {
			Register r1 = new Register();
			Register r2 = this.r;
			this.l = this.graph.add(new Rmbinop(mb,r1,r2,this.l));
			this.r = r1;
			n.e1.accept(this);
			this.r = r2;
			n.e2.accept(this);
		}
		//TODO Handle cases Band and Bor
	}

	@Override
	public void visit(Ecall n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Esizeof n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Sskip n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Sexpr n) {
		n.e.accept(this);
	}

	@Override
	public void visit(Sif n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Swhile n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Sblock n) {
		for(Decl_var dv : n.dl) {
			dv.accept(this);
		}
		//Process statements in reverse order
		Iterator<Stmt> itr = n.sl.descendingIterator();
		while(itr.hasNext()) {
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
		for(Decl_var dvar : n.fun_formals) {
			Register r = new Register();
			this.fun.formals.add(r);
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
		for(Decl_fun f : n.funs) {
			f.accept(this);
			this.file.funs.add(this.fun);
		}		
	}
	
}