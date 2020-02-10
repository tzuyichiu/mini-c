package mini_c;

import java.util.HashMap;
import java.util.HashSet;

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
		Register r = var2regs.get(n.i);
		this.l = this.graph.add(new Rload(r,0,this.r,this.l));
	}

	@Override
	public void visit(Eaccess_field n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Eassign_local n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Eassign_field n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Eunop n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Ebinop n) {
		// TODO Auto-generated method stub
		
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
		this.l = new Label();
		this.r = new Register();
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
		for(Stmt s : n.sl) {
			s.accept(this);
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