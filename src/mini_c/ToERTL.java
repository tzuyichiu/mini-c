package mini_c;
import java.util.LinkedList;
import java.util.HashSet;

public class ToERTL implements RTLVisitor {
	
	private ERTLfile ertlfile;
	private ERTLgraph ertlgraph;
	private ERTLfun ertlfun;
	private RTLgraph rtlgraph;
    private Label l;
	
	ToERTL() {
        this.ertlfile = new ERTLfile();
	}
	
	ERTLfile translate(RTLfile tree) {
		tree.accept(this);
		return this.ertlfile;
	}

	@Override
	public void visit(Rconst o) {
		RTL rtl = this.rtlgraph.graph.get(o.l);
        if (rtl != null) rtl.accept(this);

		this.l = this.ertlgraph.add(new ERconst(o.i, o.r, this.l));
	}

	@Override
	public void visit(Rload o) {
		RTL rtl = this.rtlgraph.graph.get(o.l);
        if (rtl != null) rtl.accept(this);

		this.l = this.ertlgraph.add(new ERload(o.r1, o.i, o.r2, this.l));
	}

	@Override
	public void visit(Rstore o) {
		RTL rtl = this.rtlgraph.graph.get(o.l);
        if (rtl != null) rtl.accept(this);

		this.l = this.ertlgraph.add(new ERstore(o.r1, o.r2, o.i, this.l));
	}

	@Override
	public void visit(Rmunop o) {
		RTL rtl = this.rtlgraph.graph.get(o.l);
        if (rtl != null) rtl.accept(this);

		this.l = this.ertlgraph.add(new ERmunop(o.m, o.r, this.l));
	}

	@Override
	public void visit(Rmbinop o) {
		RTL rtl = this.rtlgraph.graph.get(o.l);
        if (rtl != null) rtl.accept(this);
		
		if (o.m.equals(Mbinop.Mdiv)) {
			this.l = this.ertlgraph.add(
				new ERmbinop(Mbinop.Mmov, Register.rax, o.r2, this.l));
			this.l = this.ertlgraph.add(
				new ERmbinop(Mbinop.Mdiv, o.r1, Register.rax, this.l));
			this.l = this.ertlgraph.add(
				new ERmbinop(Mbinop.Mmov, o.r2, Register.rax, this.l));
		}
		else {			
			this.l = this.ertlgraph.add(new ERmbinop(o.m, o.r1, o.r2, this.l));
		}
	}

	@Override
	public void visit(Rmubranch o) {
		
		RTL rtl1 = this.rtlgraph.graph.get(o.l1);
        if (rtl1 != null) rtl1.accept(this);
        
        Label l1 = this.l;

        RTL rtl2 = this.rtlgraph.graph.get(o.l2);
		if (rtl2 != null) rtl2.accept(this);
		
        Label l2 = this.l;
        
        this.l = this.ertlgraph.add(
			new ERmubranch(o.m, o.r, l1, l2));
	}

	@Override
	public void visit(Rmbbranch o) {
		
		RTL rtl1 = this.rtlgraph.graph.get(o.l1);
        if (rtl1 != null) rtl1.accept(this);
        
        Label l1 = this.l;

        RTL rtl2 = this.rtlgraph.graph.get(o.l2);
		if (rtl2 != null) rtl2.accept(this);
        else this.l = this.ertlgraph.add(new ERreturn());
        
        Label l2 = this.l;
        
		this.l = this.ertlgraph.add(
			new ERmbbranch(o.m, o.r1, o.r2, l1, l2));
	}

	@Override
	public void visit(Rcall o) {
		RTL rtl = this.rtlgraph.graph.get(o.l);
        if (rtl != null) rtl.accept(this);
		
		int n_args = o.rl.size();
		int k = n_args;
		
		// 5. If n > 6, depilate 8×(n−6) bytes from the stack
		if (n_args > 6) {
			k = 6;
			Register r1 = new Register();
			this.l = this.ertlgraph.add(new ERmbinop(
				Mbinop.Msub, r1, Register.rsp, this.l));
			this.l = this.ertlgraph.add(new ERconst(8*(n_args-6), r1, this.l));
		}
		
		// 4. Copy %rax in r
		this.l = this.ertlgraph.add(new ERmbinop(
			Mbinop.Mmov, Register.rax, o.r, this.l));
		
		// 3. Call f(k)
		this.l = this.ertlgraph.add(new ERcall(o.s, k, this.l));
		
		// 2. If n > 6, pass the other arguments on the stack with push_param
		for (int i=n_args-1; i>=6; i--) {
			this.l = this.ertlgraph.add(new ERpush_param(o.rl.get(i), this.l));
		}
		
		// 1. Pass the min(n, 6) arguments inside corresponding register
		for (int i=k-1; i>=0; i--) {
			this.l = this.ertlgraph.add(new ERmbinop(
				Mbinop.Mmov, o.rl.get(i), Register.parameters.get(i), this.l));
		}
	}

	@Override
	public void visit(Rgoto o) {
		RTL rtl = this.rtlgraph.graph.get(o.l);
        if (rtl != null) rtl.accept(this);
        else this.l = this.ertlgraph.add(new ERreturn());
		this.l = this.ertlgraph.add(new ERgoto(this.l));
	}

	@Override
	public void visit(RTLfun o) {

        HashSet<Register> locals = new HashSet<>();
        LinkedList<Register> callee_saved = new LinkedList<>();

        // 4. Return
        this.l = this.ertlgraph.add(new ERreturn());
        
        // 3. Free the activation table
        this.l = this.ertlgraph.add(new ERdelete_frame(this.l));

        // 2. Retreive the callee-saved registers
        for (int i=0; i<Register.callee_saved.size(); i++) {
            Register new_reg = new Register();
            callee_saved.add(new_reg);
			this.l = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, callee_saved.get(i), 
                Register.callee_saved.get(i), this.l));
            locals.add(new_reg);
        }
        
        // 1. Copy the return value into %rax
        this.l = this.ertlgraph.add(new ERmbinop(
            Mbinop.Mmov, o.result, Register.rax, this.l));
            
        // *** Time for recursion ***
        this.rtlgraph = o.body;
		RTL rtl = this.rtlgraph.graph.get(o.entry);
        if (rtl != null) rtl.accept(this);
        // *** End of recursion ***

        int n_args = o.formals.size();
        int k = n_args;
        if (n_args > 6) k = 6;

        // 4. Copy the other parameters into pseudo-registers
		for (int i=n_args-1; i>=6; i--) {
			this.l = this.ertlgraph.add(
                new ERget_param(i-6, o.formals.get(i), this.l));
		}
		
		// 3. Pass the min(n, 6) parameters into pseudo-register
		for (int i=k-1; i>=0; i--) {
			this.l = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, Register.parameters.get(i), 
                o.formals.get(i), this.l));
		}

        // 2. Save the callee-saved registers
        for (int i=0; i<Register.callee_saved.size(); i++) {
			this.l = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, Register.callee_saved.get(i), 
                callee_saved.get(i), this.l));
		}

        // 1. Alloc the activation table with alloc_frame
        this.l = this.ertlgraph.add(new ERalloc_frame(this.l));
        
        this.ertlfun = new ERTLfun(o.name, o.formals.size());
        this.ertlfun.body = this.ertlgraph;
        this.ertlfun.locals = locals;
		this.ertlfun.entry = this.l;
	}

	@Override
	public void visit(RTLfile o) {
		for (RTLfun fun: o.funs) {
            this.ertlgraph = new ERTLgraph();
            fun.accept(this);
			this.ertlfile.funs.add(this.ertlfun);
		}
	}
}
