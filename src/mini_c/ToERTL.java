package mini_c;
import java.util.LinkedList;
import java.util.Set;

public class ToERTL implements RTLVisitor {
	
	private ERTLfile ertlfile;
	private ERTLgraph ertlgraph;
	private ERTLfun ertlfun;
	private RTLgraph rtlgraph;
	private LinkedList<Label> visited_labels;
    private Label last_fresh;
    private Label RTL_label;
	
	ToERTL() {
        this.ertlfile = new ERTLfile();
	}
	
	ERTLfile translate(RTLfile tree) {
		tree.accept(this);
		return this.ertlfile;
	}
	
	
	/*
	 * Rule: - RTL_Label gives the label of the visited RTL in RTLgraph
	 * 		  - last_fresh is the latest fresh label generated
	 */
	@Override
	public void visit(Rconst o) {
		Label myLabel = this.RTL_label;
		RTL rtl = this.rtlgraph.graph.get(o.l);
		this.RTL_label = o.l;
		visited_labels.add(o.l);
        if (rtl != null) rtl.accept(this);
		
		this.ertlgraph.put(myLabel, new ERconst(o.i, o.r, o.l));
	}

	@Override
	public void visit(Rload o) {
		Label myLabel = this.RTL_label;
		RTL rtl = this.rtlgraph.graph.get(o.l);
		this.RTL_label = o.l;
		visited_labels.add(o.l);
        if (rtl != null) rtl.accept(this);
        
		this.ertlgraph.put(myLabel, new ERload(o.r1, o.i, o.r2, o.l));
	}

	@Override
	public void visit(Rstore o) {
		Label myLabel = this.RTL_label;
		RTL rtl = this.rtlgraph.graph.get(o.l);
		this.RTL_label = o.l;
		visited_labels.add(o.l);
        if (rtl != null) rtl.accept(this);

		this.ertlgraph.put(myLabel, new ERstore(o.r1, o.r2, o.i, o.l));
	}

	@Override
	public void visit(Rmunop o) {
		Label myLabel = this.RTL_label;
		RTL rtl = this.rtlgraph.graph.get(o.l);
		this.RTL_label = o.l;
		visited_labels.add(o.l);
        if (rtl != null) rtl.accept(this);

		this.ertlgraph.put(myLabel, new ERmunop(o.m, o.r, o.l));
	}

	@Override
	public void visit(Rmbinop o) {
		Label myLabel = this.RTL_label;
		RTL rtl = this.rtlgraph.graph.get(o.l);
		this.RTL_label = o.l;
		visited_labels.add(o.l);
        if (rtl != null) rtl.accept(this);
		
		if (o.m.equals(Mbinop.Mdiv)) {
			this.last_fresh = this.ertlgraph.add(
				new ERmbinop(Mbinop.Mmov, Register.rax, o.r2, o.l));
			this.last_fresh = this.ertlgraph.add(
				new ERmbinop(Mbinop.Mdiv, o.r1, Register.rax, this.last_fresh));
			this.ertlgraph.put(myLabel,
				new ERmbinop(Mbinop.Mmov, o.r2, Register.rax, this.last_fresh));
		}
		else {			
			this.ertlgraph.put(myLabel, new ERmbinop(o.m, o.r1, o.r2, o.l));
		}
	}

	@Override
	public void visit(Rmubranch o) {
		
		Label myLabel = this.RTL_label;
		
		RTL rtl = this.rtlgraph.graph.get(o.l1);
		this.RTL_label = o.l1;
		visited_labels.add(o.l1);
        if (rtl != null) rtl.accept(this);

        RTL rtl2 = this.rtlgraph.graph.get(o.l2);
		this.RTL_label = o.l2;
		visited_labels.add(o.l2);
        if (rtl2 != null) rtl2.accept(this);
		
        this.ertlgraph.put(myLabel,
			new ERmubranch(o.m, o.r, o.l1, o.l2));
	}

	@Override
	public void visit(Rmbbranch o) {
		
		Label myLabel = this.RTL_label;
		
		RTL rtl = this.rtlgraph.graph.get(o.l1);
		this.RTL_label = o.l1;
		visited_labels.add(o.l1);
        if (rtl != null) rtl.accept(this);
        
		RTL rtl2 = this.rtlgraph.graph.get(o.l2);
		this.RTL_label = o.l2;
		visited_labels.add(o.l2);
		Label l2 = o.l2;
        if (rtl2 != null) rtl2.accept(this);
        
		this.ertlgraph.put(myLabel,
			new ERmbbranch(o.m, o.r1, o.r2, o.l1, l2));
	}

	@Override
	public void visit(Rcall o) {

		Label myLabel = this.RTL_label;
		RTL rtl = this.rtlgraph.graph.get(o.l);
		this.RTL_label = o.l;
		visited_labels.add(o.l);
        if (rtl != null) rtl.accept(this);
		
		int n_args = o.rl.size();
		int k = n_args;
		
		// 5. If n > 6, pop 8×(n−6) bytes from the stack
		if (n_args > 6) {
			k = 6;
			Register r1 = new Register();
			this.last_fresh = this.ertlgraph.add(new ERmbinop(
                Mbinop.Msub, r1, Register.rsp, o.l));
			this.last_fresh = this.ertlgraph.add(
                new ERconst(8*(n_args-6), r1, this.last_fresh));
		} else {
			// If no 5. phase, transfer control to o.l
			this.last_fresh = o.l;
		}
		
		// 4. Copy %rax in r
		this.last_fresh = this.ertlgraph.add(new ERmbinop(
			Mbinop.Mmov, Register.rax, o.r, this.last_fresh));
		
		if (n_args > 0) {
			// 3. Call f(k)
			this.last_fresh = this.ertlgraph.add(
                new ERcall(o.s, k, this.last_fresh));
		
			// 2. If n > 6, pass the other arguments on the stack
			for (int i=n_args-1; i>=6; i--) {
				this.last_fresh = this.ertlgraph.add(
                    new ERpush_param(o.rl.get(i), this.last_fresh));
			}
			
			// 1. Pass the min(n, 6) arguments inside corresponding register
			for (int i=k-1; i>=1; i--) {
				this.last_fresh = this.ertlgraph.add(new ERmbinop(
                    Mbinop.Mmov, o.rl.get(i), 
                    Register.parameters.get(i), this.last_fresh));
			}
			this.ertlgraph.put(myLabel, new ERmbinop(
                Mbinop.Mmov, o.rl.get(0), 
                Register.parameters.get(0), this.last_fresh));
		} else {
			// No arguments, tranfert control directly to 3.
			this.ertlgraph.put(myLabel,
				new ERcall(o.s, k, this.last_fresh));
		}
	}

	@Override
	public void visit(Rgoto o) {
		Label myLabel = this.RTL_label;
		if(!visited_labels.contains(o.l)) {			
			RTL rtl = this.rtlgraph.graph.get(o.l);
			this.RTL_label = o.l;
			visited_labels.add(o.l);
			if (rtl != null) rtl.accept(this);
		}
		this.ertlgraph.put(myLabel,new ERgoto(o.l));
	}

	@Override
	public void visit(RTLfun o) {

        Set<Register> locals = o.locals;
        LinkedList<Register> callee_saved = new LinkedList<>();

        // 4. Return
        this.last_fresh = this.ertlgraph.add(new ERreturn());
        
        // 3. Free the activation table
        this.last_fresh = this.ertlgraph.add(
            new ERdelete_frame(this.last_fresh));

        // 2. Retreive the callee-saved registers
        for (int i=0; i<Register.callee_saved.size(); i++) {
            Register new_reg = new Register();
            callee_saved.add(new_reg);
			this.last_fresh = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, callee_saved.get(i), 
                Register.callee_saved.get(i), this.last_fresh));
            locals.add(new_reg);
        }
        
        // 1. Copy the return value into %rax
        this.ertlgraph.put(o.exit, new ERmbinop(
            Mbinop.Mmov, o.result, Register.rax, this.last_fresh));
        
        // *** Time for recursion ***
        visited_labels = new LinkedList<>();
        this.rtlgraph = o.body;
		RTL rtl = this.rtlgraph.graph.get(o.entry);
		this.RTL_label = o.entry;
		visited_labels.add(o.entry);
        if (rtl != null) rtl.accept(this);
        // *** End of recursion ***

        int n_args = o.formals.size();
        int k = n_args;
        if (n_args > 6) k = 6;
        
        // Cheat on last_fresh meaning: force control to be passed to o.entry
        this.last_fresh = o.entry;
        
        // 4. Copy the other parameters into pseudo-registers
		for (int i=6; i <= n_args-1; i++) {
			this.last_fresh = this.ertlgraph.add(new ERget_param(
                8*(n_args+1-i), o.formals.get(i), this.last_fresh));
		}
		
		// 3. Pass the min(n, 6) parameters into pseudo-register
		for (int i=k-1; i>=0; i--) {
			this.last_fresh = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, Register.parameters.get(i), 
                o.formals.get(i), this.last_fresh));
		}

        // 2. Save the callee-saved registers
        for (int i=0; i<Register.callee_saved.size(); i++) {
			this.last_fresh = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, Register.callee_saved.get(i), 
                callee_saved.get(i), this.last_fresh));
		}

        // 1. Alloc the activation table with alloc_frame
        this.last_fresh = this.ertlgraph.add(
            new ERalloc_frame(this.last_fresh));
        
        this.ertlfun = new ERTLfun(o.name, o.formals.size());
        this.ertlfun.body = this.ertlgraph;
        this.ertlfun.locals = locals;
		this.ertlfun.entry = this.last_fresh;
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
