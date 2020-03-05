package mini_c;

public class ToERTL implements RTLVisitor {
	
	private ERTLfile file;
	private ERTLgraph graph;
	private Label l;
	private Register r;
	
	ToERTL() {
		this.file = new ERTLfile();
		this.graph = new ERTLgraph();
	}
	
	ERTLfile translate(RTLfile tree) {
		tree.accept(this);
		return file;
	}

	@Override
	public void visit(Rconst o) {
		this.l = this.graph.add(new ERconst(o.i,o.r,o.l));
		this.r = o.r;
	}

	@Override
	public void visit(Rload o) {
		this.l = this.graph.add(new ERload(o.r1,o.i,o.r2,o.l));
		this.r = o.r2;
	}

	@Override
	public void visit(Rstore o) {
		this.l = this.graph.add(new ERstore(o.r1,o.r2,o.i,o.l));
		this.r = o.r2;
	}

	@Override
	public void visit(Rmunop o) {
		this.l = this.graph.add(new ERmunop(o.m,o.r,o.l));
		this.r = o.r;
	}

	@Override
	public void visit(Rmbinop o) {
		if(o.m.equals(Mbinop.Mdiv)) {
			Label l1 = this.graph.add(new ERmbinop(Mbinop.Mmov, Register.rax, o.r2,o.l));
			Label l2 = this.graph.add(new ERmbinop(Mbinop.Mdiv, o.r1, Register.rax, l1));
			this.l = this.graph.add(new ERmbinop(Mbinop.Mmov, o.r2, Register.rax, l2));
		}
		else {			
			this.l = this.graph.add(new ERmbinop(o.m,o.r1,o.r2,o.l));
		}
		this.r = o.r2;
	}

	@Override
	public void visit(Rmubranch o) {
		this.l = this.graph.add(new ERmubranch(o.m,o.r,o.l1,o.l2));
		this.r = o.r;
	}

	@Override
	public void visit(Rmbbranch o) {
		this.l = this.graph.add(new ERmbbranch(o.m,o.r1,o.r2,o.l1,o.l2));
	}

	@Override
	public void visit(Rcall o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Rgoto o) {
		this.l = this.graph.add(new ERgoto(o.l));
	}

	@Override
	public void visit(RTLfun o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(RTLfile o) {
		for(RTLfun fun : o.funs) {
			fun.accept(this);
		}
	}

}
