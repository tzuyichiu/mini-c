package mini_c;

import java.util.LinkedList;

class ToLTL implements ERTLVisitor {
	private Coloring coloring; // coloriage de la fonction en cours de traduction
	int size_locals; // taille pour les variables locales
	LTLgraph graph;  // graphe en cours de construction
	LinkedList<Label> visited_labels;
	LTLfun ltlFun;
	ERTLgraph ertlGraph;
	LTLfile ltlFile;
	Label ERTL_label;
	Label last_fresh;
	
	// Prepares to accept ERTL that is at label l in ERTLgraph, or not if the ERTL is null.
	private void checkAndAccept(Label l) {
		this.ERTL_label = l;
		ERTL ertl = this.ertlGraph.graph.get(l);
		visited_labels.add(l);
		if (ertl != null) ertl.accept(this);
	}
	
	private Operand getColor(Register r) {
		if (r.isHW()) return new Reg(r);
		else return this.coloring.colors.get(r);
	}
	
	private boolean isSpilled(Operand o) {
		if (o instanceof Spilled) return true;
		else return false;
	}
	
	LTLfile translate(ERTLfile ef) {
		ef.accept(this);
		return this.ltlFile;
	}
	
	@Override
	public void visit(ERconst o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		this.graph.put(myLabel, new Lconst(o.i, this.getColor(o.r), o.l));
	}
	@Override
	public void visit(ERload o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		Operand r1Color = this.getColor(o.r1);
		Operand r2Color = this.getColor(o.r2);
		if (!this.isSpilled(r1Color) && !this.isSpilled(r2Color)) {			
			this.graph.put(myLabel, new Lload(((Reg) r1Color).r, o.i, ((Reg) r2Color).r, o.l));
		}
		else if (!this.isSpilled(r1Color) && this.isSpilled(r2Color)) {
			this.last_fresh = this.graph.add(
					new Lmbinop(Mbinop.Mmov, new Reg(Register.tmp1), r2Color, o.l));
			this.graph.put(myLabel, new Lload(((Reg) r1Color).r, o.i, Register.tmp1, this.last_fresh));
		}
		else if (this.isSpilled(r1Color) && !this.isSpilled(r2Color)) {
			this.last_fresh = this.graph.add(
					new Lload(Register.tmp1, o.i, ((Reg) r2Color).r, o.l));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, r1Color, new Reg(Register.tmp1), this.last_fresh));
		} else {
			this.last_fresh = this.graph.add(new Lmbinop(Mbinop.Mmov, new Reg(Register.tmp2), r2Color, o.l));
			this.last_fresh = this.graph.add(
					new Lload(Register.tmp1, o.i, Register.tmp2, this.last_fresh));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, r1Color, new Reg(Register.tmp1), this.last_fresh));
		}
	}
	@Override
	public void visit(ERstore o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		Operand r1Color = this.getColor(o.r1);
		Operand r2Color = this.getColor(o.r2);
		if (!this.isSpilled(r1Color) && !this.isSpilled(r2Color)) {			
			this.graph.put(myLabel, new Lstore(((Reg) r1Color).r, ((Reg) r2Color).r, o.i, o.l));
		}
		else if (!this.isSpilled(r1Color) && this.isSpilled(r2Color)) {
			this.last_fresh = this.graph.add(
					new Lstore(((Reg) r1Color).r, Register.tmp1, o.i, o.l));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, r2Color, new Reg(Register.tmp1), this.last_fresh));
		}
		else if (this.isSpilled(r1Color) && !this.isSpilled(r2Color)) {
			this.last_fresh = this.graph.add(
					new Lstore(Register.tmp1, ((Reg) r2Color).r, o.i, o.l));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, r1Color, new Reg(Register.tmp1), this.last_fresh));
		} else {
			this.last_fresh = this.graph.add(
					new Lstore(Register.tmp1, Register.tmp2, o.i, o.l));
			this.last_fresh = this.graph.add(
					new Lmbinop(Mbinop.Mmov, r2Color, new Reg(Register.tmp2), this.last_fresh));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, r1Color, new Reg(Register.tmp1), this.last_fresh));
		}
	}
	@Override
	public void visit(ERmunop o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		this.graph.put(myLabel, new Lmunop(o.m, this.getColor(o.r), o.l));
	}
	@Override
	public void visit(ERmbinop o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		Operand r1Color = this.getColor(o.r1);
		Operand r2Color = this.getColor(o.r2);
		// 1. mov x x translated to goto
		if (o.m.equals(Mbinop.Mmov) && r1Color.equals(r2Color)) {
			this.graph.put(myLabel, new Lgoto(o.l));
		}
		// 2. imul x y if y spilled : put y in tmp register
		else if (o.m.equals(Mbinop.Mdiv) && this.isSpilled(r2Color)) {
			this.last_fresh = this.graph.add(
					new Lmbinop(Mbinop.Mdiv, r1Color, new Reg(Register.tmp1), o.l));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, r2Color, new Reg(Register.tmp1), this.last_fresh));
		}
		// 3. Generally, at least one of the two r1 and r2 should not be spilled
		else if (!this.isSpilled(r1Color) || !this.isSpilled(r2Color)) {			
			this.graph.put(myLabel, new Lmbinop(o.m, r1Color, r2Color, o.l));
		}
		else {
			this.last_fresh = this.graph.add(
					new Lmbinop(o.m, new Reg(Register.tmp1), r2Color, o.l));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, r1Color, new Reg(Register.tmp1), this.last_fresh));
		}
	}
	@Override
	public void visit(ERmubranch o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l1);
		checkAndAccept(o.l2);
		this.graph.put(myLabel, new Lmubranch(o.m, this.getColor(o.r), o.l1, o.l2));
	}
	@Override
	public void visit(ERmbbranch o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l1);
		checkAndAccept(o.l2);
		Operand r1Color = this.getColor(o.r1);
		Operand r2Color = this.getColor(o.r2);
		if (!this.isSpilled(r1Color) || !this.isSpilled(r2Color)) {			
			this.graph.put(myLabel, new Lmbbranch(o.m, r1Color, r2Color, o.l1, o.l2));
		}
		else {
			this.last_fresh = this.graph.add(
					new Lmbbranch(o.m, new Reg(Register.tmp1), r2Color, o.l1, o.l2));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, r1Color, new Reg(Register.tmp1), this.last_fresh));
		}
	}
	@Override
	public void visit(ERgoto o) {
		Label myLabel = this.ERTL_label;
		if (!visited_labels.contains(o.l)) checkAndAccept(o.l);
		this.graph.put(myLabel, new Lgoto(o.l));
	}
	@Override
	public void visit(ERcall o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		this.graph.put(myLabel, new Lcall(o.s, o.l));
	}
	@Override
	public void visit(ERalloc_frame o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		if (this.size_locals != 0) {			
			this.last_fresh = this.graph.add(new Lmunop(new Maddi(-8*this.size_locals), new Reg(Register.rsp), o.l));
			this.last_fresh = this.graph.add(new Lmbinop(Mbinop.Mmov, new Reg(Register.rsp), new Reg(Register.rbp), this.last_fresh));
			this.graph.put(myLabel, new Lpush(new Reg(Register.rbp), this.last_fresh));
		}
		else {
			this.graph.put(myLabel, new Lgoto(o.l));
		}
	}
	@Override
	public void visit(ERdelete_frame o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		if (this.size_locals != 0) {
			this.last_fresh = this.graph.add(new Lpop(Register.rbp, o.l));
			this.graph.put(myLabel, new Lmbinop(Mbinop.Mmov, new Reg(Register.rbp), new Reg(Register.rsp), this.last_fresh));
		}
		else {
			this.graph.put(myLabel, new Lgoto(o.l));
		}
	}
	@Override
	public void visit(ERget_param o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		Operand rColor = this.getColor(o.r);
		if (!this.isSpilled(rColor)) {
			this.graph.put(myLabel, new Lload(Register.rbp, o.i, ((Reg) rColor).r, o.l));
		}
		else {
			// o.r is on the stack, we have to use one more instruction
			this.last_fresh = this.graph.add(
					new Lmbinop(Mbinop.Mmov, new Reg(Register.tmp1), rColor, o.l));
			this.graph.put(myLabel, new Lload(Register.rbp, o.i, Register.tmp1, this.last_fresh));
		}
	}
	@Override
	public void visit(ERpush_param o) {
		Label myLabel = this.ERTL_label;
		checkAndAccept(o.l);
		Operand rColor = this.getColor(o.r);
		if (!this.isSpilled(rColor)) {			
			this.graph.put(myLabel, new Lpush(rColor, o.l));
		}
		else {
			// o.r is on the stack, we have to use one more instruction
			this.last_fresh = this.graph.add(
					new Lpush(new Reg(Register.tmp1), o.l));
			this.graph.put(myLabel, new Lmbinop(
					Mbinop.Mmov, rColor, new Reg(Register.tmp1), this.last_fresh));
		}
	}
	@Override
	public void visit(ERreturn o) {
		this.graph.put(this.ERTL_label, new Lreturn());
	}
	@Override
	public void visit(ERTLfun o) {
		this.ltlFun = new LTLfun(o.name);
		this.ltlFun.entry = o.entry;
		this.coloring = new Coloring (new Interference (new Liveness(o.body)));
		this.size_locals = this.coloring.nlocals;
		this.graph = new LTLgraph();
		this.ertlGraph = o.body;
		visited_labels = new LinkedList<>();
		checkAndAccept(o.entry);
		this.ltlFun.body = this.graph;
	}
	@Override
	public void visit(ERTLfile o) {
		this.ltlFile = new LTLfile();
		for (ERTLfun ertlFun : o.funs) {
			ertlFun.accept(this);
			ltlFile.funs.add(this.ltlFun);
		}
	}

}
