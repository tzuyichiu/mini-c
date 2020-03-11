package mini_c;
import java.util.LinkedList;
import java.util.Set;

public class ToERTL implements RTLVisitor {
	
	private ERTLfile ertlFile;
	private ERTLgraph ertlGraph; // graph being constructed
	private ERTLfun ertlFun;
	private RTLgraph rtlGraph; // RTL code being translated
	private LinkedList<Label> visitedLabels;
    private Label lastFresh;
    private Label rtlLabel;
	
	ToERTL() {
        this.ertlFile = new ERTLfile();
	}
	
	ERTLfile translate(RTLfile tree) {
		tree.accept(this);
		return this.ertlFile;
	}
	
	
	/*
	 * Rule: - RTL_Label gives the label of the visited RTL in RTLgraph
	 * 		 - lastFresh is the latest fresh label generated
	 */
	@Override
	public void visit(Rconst o) {
		Label myLabel = this.rtlLabel;
		RTL rtl = this.rtlGraph.graph.get(o.l);
		this.rtlLabel = o.l;
		visitedLabels.add(o.l);
        if (rtl != null) rtl.accept(this);
		
		this.ertlGraph.put(myLabel, new ERconst(o.i, o.r, o.l));
	}

	@Override
	public void visit(Rload o) {
		Label myLabel = this.rtlLabel;
		RTL rtl = this.rtlGraph.graph.get(o.l);
		this.rtlLabel = o.l;
		visitedLabels.add(o.l);
        if (rtl != null) rtl.accept(this);
        
		this.ertlGraph.put(myLabel, new ERload(o.r1, o.i, o.r2, o.l));
	}

	@Override
	public void visit(Rstore o) {
		Label myLabel = this.rtlLabel;
		RTL rtl = this.rtlGraph.graph.get(o.l);
		this.rtlLabel = o.l;
		visitedLabels.add(o.l);
        if (rtl != null) rtl.accept(this);

		this.ertlGraph.put(myLabel, new ERstore(o.r1, o.r2, o.i, o.l));
	}

	@Override
	public void visit(Rmunop o) {
		Label myLabel = this.rtlLabel;
		RTL rtl = this.rtlGraph.graph.get(o.l);
		this.rtlLabel = o.l;
		visitedLabels.add(o.l);
        if (rtl != null) rtl.accept(this);

		this.ertlGraph.put(myLabel, new ERmunop(o.m, o.r, o.l));
	}

	@Override
	public void visit(Rmbinop o) {
		Label myLabel = this.rtlLabel;
		RTL rtl = this.rtlGraph.graph.get(o.l);
		this.rtlLabel = o.l;
		visitedLabels.add(o.l);
        if (rtl != null) rtl.accept(this);
		
		if (o.m.equals(Mbinop.Mdiv)) {
			this.lastFresh = this.ertlGraph.add(
				new ERmbinop(Mbinop.Mmov, Register.rax, o.r2, o.l));
			this.lastFresh = this.ertlGraph.add(
				new ERmbinop(Mbinop.Mdiv, o.r1, Register.rax, this.lastFresh));
			this.ertlGraph.put(myLabel,
				new ERmbinop(Mbinop.Mmov, o.r2, Register.rax, this.lastFresh));
		}
		else {			
			this.ertlGraph.put(myLabel, new ERmbinop(o.m, o.r1, o.r2, o.l));
		}
	}

	@Override
	public void visit(Rmubranch o) {
		
		Label myLabel = this.rtlLabel;
		
		RTL rtl = this.rtlGraph.graph.get(o.l1);
		this.rtlLabel = o.l1;
		visitedLabels.add(o.l1);
        if (rtl != null) rtl.accept(this);

        RTL rtl2 = this.rtlGraph.graph.get(o.l2);
		this.rtlLabel = o.l2;
		visitedLabels.add(o.l2);
        if (rtl2 != null) rtl2.accept(this);
		
        this.ertlGraph.put(myLabel,
			new ERmubranch(o.m, o.r, o.l1, o.l2));
	}

	@Override
	public void visit(Rmbbranch o) {
		
		Label myLabel = this.rtlLabel;
		
		RTL rtl = this.rtlGraph.graph.get(o.l1);
		this.rtlLabel = o.l1;
		visitedLabels.add(o.l1);
        if (rtl != null) rtl.accept(this);
        
		RTL rtl2 = this.rtlGraph.graph.get(o.l2);
		this.rtlLabel = o.l2;
		visitedLabels.add(o.l2);
		Label l2 = o.l2;
        if (rtl2 != null) rtl2.accept(this);
        
		this.ertlGraph.put(myLabel,
			new ERmbbranch(o.m, o.r1, o.r2, o.l1, l2));
	}

	@Override
	public void visit(Rcall o) {

		Label myLabel = this.rtlLabel;
		RTL rtl = this.rtlGraph.graph.get(o.l);
		this.rtlLabel = o.l;
		visitedLabels.add(o.l);
        if (rtl != null) rtl.accept(this);
		
		int n_args = o.rl.size();
		int k = n_args;
		
		// 5. If n > 6, pop 8×(n−6) bytes from the stack
		if (n_args > 6) {
			k = 6;
			Register r1 = new Register();
			this.lastFresh = this.ertlGraph.add(new ERmbinop(
                Mbinop.Msub, r1, Register.rsp, o.l));
			this.lastFresh = this.ertlGraph.add(
                new ERconst(8*(n_args-6), r1, this.lastFresh));
		} else {
			// If no 5. phase, transfer control to o.l
			this.lastFresh = o.l;
		}
		
		// 4. Copy %rax in r
		this.lastFresh = this.ertlGraph.add(new ERmbinop(
			Mbinop.Mmov, Register.rax, o.r, this.lastFresh));
		
		if (n_args > 0) {
			// 3. Call f(k)
			this.lastFresh = this.ertlGraph.add(
                new ERcall(o.s, k, this.lastFresh));
		
			// 2. If n > 6, pass the other arguments on the stack
			for (int i=n_args-1; i>=6; i--) {
				this.lastFresh = this.ertlGraph.add(
                    new ERpush_param(o.rl.get(i), this.lastFresh));
			}
			
			// 1. Pass the min(n, 6) arguments inside corresponding register
			for (int i=k-1; i>=1; i--) {
				this.lastFresh = this.ertlGraph.add(new ERmbinop(
                    Mbinop.Mmov, o.rl.get(i), 
                    Register.parameters.get(i), this.lastFresh));
			}
			this.ertlGraph.put(myLabel, new ERmbinop(
                Mbinop.Mmov, o.rl.get(0), 
                Register.parameters.get(0), this.lastFresh));
		} else {
			// No arguments, tranfert control directly to 3.
			this.ertlGraph.put(myLabel,
				new ERcall(o.s, k, this.lastFresh));
		}
	}

	@Override
	public void visit(Rgoto o) {
		Label myLabel = this.rtlLabel;
		if(!visitedLabels.contains(o.l)) {			
			RTL rtl = this.rtlGraph.graph.get(o.l);
			this.rtlLabel = o.l;
			visitedLabels.add(o.l);
			if (rtl != null) rtl.accept(this);
		}
		this.ertlGraph.put(myLabel, new ERgoto(o.l));
	}

	@Override
	public void visit(RTLfun o) {

        Set<Register> locals = o.locals;
        LinkedList<Register> callee_saved = new LinkedList<>();

        // 4. Return
        this.lastFresh = this.ertlGraph.add(new ERreturn());
        
        // 3. Free the activation table
        this.lastFresh = this.ertlGraph.add(
            new ERdelete_frame(this.lastFresh));

        // 2. Retreive the callee-saved registers
        for (int i=0; i<Register.callee_saved.size(); i++) {
            Register new_reg = new Register();
            callee_saved.add(new_reg);
			this.lastFresh = this.ertlGraph.add(new ERmbinop(
                Mbinop.Mmov, callee_saved.get(i), 
                Register.callee_saved.get(i), this.lastFresh));
            locals.add(new_reg);
        }
        
        // 1. Copy the return value into %rax
        this.ertlGraph.put(o.exit, new ERmbinop(
            Mbinop.Mmov, o.result, Register.rax, this.lastFresh));
        
        // *** Time for recursion ***
        visitedLabels = new LinkedList<>();
        this.rtlGraph = o.body;
		RTL rtl = this.rtlGraph.graph.get(o.entry);
		this.rtlLabel = o.entry;
		visitedLabels.add(o.entry);
        if (rtl != null) rtl.accept(this);
        // *** End of recursion ***

        int n_args = o.formals.size();
        int k = n_args;
        if (n_args > 6) k = 6;
        
        // Cheat on lastFresh meaning: force control to be passed to o.entry
        this.lastFresh = o.entry;
        
        // 4. Copy the other parameters into pseudo-registers
		for (int i=6; i <= n_args-1; i++) {
			this.lastFresh = this.ertlGraph.add(new ERget_param(
                8*(n_args+1-i), o.formals.get(i), this.lastFresh));
		}
		
		// 3. Pass the min(n, 6) parameters into pseudo-register
		for (int i=k-1; i>=0; i--) {
			this.lastFresh = this.ertlGraph.add(new ERmbinop(
                Mbinop.Mmov, Register.parameters.get(i), 
                o.formals.get(i), this.lastFresh));
		}

        // 2. Save the callee-saved registers
        for (int i=0; i<Register.callee_saved.size(); i++) {
			this.lastFresh = this.ertlGraph.add(new ERmbinop(
                Mbinop.Mmov, Register.callee_saved.get(i), 
                callee_saved.get(i), this.lastFresh));
		}

        // 1. Alloc the activation table with alloc_frame
        this.lastFresh = this.ertlGraph.add(
            new ERalloc_frame(this.lastFresh));
        
        this.ertlFun = new ERTLfun(o.name, o.formals.size());
        this.ertlFun.body = this.ertlGraph;
        this.ertlFun.locals = locals;
		this.ertlFun.entry = this.lastFresh;
	}

	@Override
	public void visit(RTLfile o) {
		for (RTLfun fun: o.funs) {
            this.ertlGraph = new ERTLgraph();
            fun.accept(this);
			this.ertlFile.funs.add(this.ertlFun);
		}
	}
}