package mini_c;
import java.util.LinkedList;
import java.util.Set;

public class ToERTL implements RTLVisitor {
	
	private ERTLfile ertlfile;
	private ERTLgraph ertlgraph;
	private ERTLfun ertlfun;
	private RTLgraph rtlgraph;
	private LinkedList<Label> visited_labels;
	/* Invariant : when visiting a RTL element,
	 * RTL_label gives the label of the element in the original RTL graph
	 */
    private Label RTL_label;
	
	ERTLfile translate(RTLfile tree) {
		tree.accept(this);
		return this.ertlfile;
	}
	
	/* A method to recurse over the RTL graph
	 * Ensures the RTL_label invariant is maintained
	 * Recurse only if the given labels refers to an existing RTL in graph
	 */
	private void checkAndAccept(Label l) {
		
		RTL rtl = this.rtlgraph.graph.get(l);
		this.RTL_label = l;
		visited_labels.add(l);
        if (rtl != null) rtl.accept(this);
	}
	
	@Override
	public void visit(Rconst o) {
		
		Label myLabel = this.RTL_label;
		checkAndAccept(o.l);
		this.ertlgraph.put(myLabel, new ERconst(o.i, o.r, o.l));
	}

	@Override
	public void visit(Rload o) {
		
		Label myLabel = this.RTL_label;
		checkAndAccept(o.l);
		this.ertlgraph.put(myLabel, new ERload(o.r1, o.i, o.r2, o.l));
	}

	@Override
	public void visit(Rstore o) {
		
		Label myLabel = this.RTL_label;
		checkAndAccept(o.l);
		this.ertlgraph.put(myLabel, new ERstore(o.r1, o.r2, o.i, o.l));
	}

	@Override
	public void visit(Rmunop o) {
		
		Label myLabel = this.RTL_label;
		checkAndAccept(o.l);
		this.ertlgraph.put(myLabel, new ERmunop(o.m, o.r, o.l));
	}

	@Override
	public void visit(Rmbinop o) {
		
		Label myLabel = this.RTL_label;
		checkAndAccept(o.l);
		
		if (o.m.equals(Mbinop.Mdiv)) {
			Label auxl = this.ertlgraph.add(
				new ERmbinop(Mbinop.Mmov, Register.rax, o.r2, o.l));
			auxl = this.ertlgraph.add(
				new ERmbinop(Mbinop.Mdiv, o.r1, Register.rax, auxl));
			this.ertlgraph.put(myLabel,
				new ERmbinop(Mbinop.Mmov, o.r2, Register.rax, auxl));
		}
		else {			
			this.ertlgraph.put(myLabel, new ERmbinop(o.m, o.r1, o.r2, o.l));
		}
	}

	@Override
	public void visit(Rmubranch o) {
		
		Label myLabel = this.RTL_label;
		checkAndAccept(o.l1);
		checkAndAccept(o.l2);
		
        this.ertlgraph.put(myLabel,
			new ERmubranch(o.m, o.r, o.l1, o.l2));
	}

	@Override
	public void visit(Rmbbranch o) {
		
		Label myLabel = this.RTL_label;
		checkAndAccept(o.l1);
		checkAndAccept(o.l2);
        
		this.ertlgraph.put(myLabel,
			new ERmbbranch(o.m, o.r1, o.r2, o.l1, o.l2));
	}

	@Override
	public void visit(Rcall o) {
		
		Label myLabel = this.RTL_label;
		Label auxl = null;
		checkAndAccept(o.l);
		
		int n_args = o.rl.size();
		int k = n_args;
		
		// Optimize tail calls only for functions with less than 6 arguments
		if (this.rtlgraph.graph.get(o.l) != null || n_args > 6) {
			// Non-tail call
			// 5. If n > 6, pop 8×(n−6) bytes from the stack
			if (n_args > 6) {
				k = 6;
				Register r1 = new Register();
				auxl = this.ertlgraph.add(new ERmbinop(
	                Mbinop.Msub, r1, Register.rsp, o.l));
				auxl = this.ertlgraph.add(
	                new ERconst(8*(n_args-6), r1, auxl));
			} else {
				// If no 5. phase, transfer control to o.l
				auxl = o.l;
			}
			
			// 4. Copy %rax in r
			auxl = this.ertlgraph.add(new ERmbinop(
				Mbinop.Mmov, Register.rax, o.r, auxl));
			
			if (n_args > 0) {
				// 3. Call f(k)
				auxl = this.ertlgraph.add(
	                new ERcall(o.s, k, auxl));
			
				// 2. If n > 6, pass the other arguments on the stack
				for (int i=n_args-1; i>=6; i--) {
					auxl = this.ertlgraph.add(
	                    new ERpush_param(o.rl.get(i), auxl));
				}
				
				// 1. Pass the min(n, 6) arguments inside corresponding register
				for (int i=k-1; i>=1; i--) {
					auxl = this.ertlgraph.add(new ERmbinop(
	                    Mbinop.Mmov, o.rl.get(i), 
	                    Register.parameters.get(i), auxl));
				}
				this.ertlgraph.put(myLabel, new ERmbinop(
	                Mbinop.Mmov, o.rl.get(0), 
	                Register.parameters.get(0), auxl));
			} else {
				// No arguments, tranfert control directly to 3.
				this.ertlgraph.put(myLabel,
					new ERcall(o.s, k, auxl));
			}
		}
		else {
			// Here the next RTL instruction is null i.e. a return and the function has less than 6 parameters

			if (n_args > 0) {
				// Go to function entry point
				auxl = this.ertlgraph.add(new ERgoto(new Label(o.s)));

				// Pass exactly n arguments inside corresponding register (n <= 6)
				for (int i=k-1; i>=1; i--) {
					auxl = this.ertlgraph.add(new ERmbinop(
							Mbinop.Mmov, o.rl.get(i), 
							Register.parameters.get(i), auxl));
				}
				this.ertlgraph.put(myLabel, new ERmbinop(
						Mbinop.Mmov, o.rl.get(0), 
						Register.parameters.get(0), auxl));
			} else {
				// No arguments, directly go to function entry point
				this.ertlgraph.put(myLabel, new ERgoto(new Label(o.s)));
			}
		}
	}

	@Override
	public void visit(Rgoto o) {

		Label myLabel = this.RTL_label;
		if(!visited_labels.contains(o.l)) checkAndAccept(o.l);
		
		this.ertlgraph.put(myLabel, new ERgoto(o.l));
	}

	@Override
	// Note : Read code from bottom to top to follow control flow
	public void visit(RTLfun o) {

        Set<Register> locals = o.locals;
        LinkedList<Register> callee_saved = new LinkedList<>();

        // 4. Return
        Label auxl = this.ertlgraph.add(new ERreturn());
        
        // 3. Free the activation table
        auxl = this.ertlgraph.add(
            new ERdelete_frame(auxl));

        // 2. Retreive the callee-saved registers
        for (int i=0; i<Register.callee_saved.size(); i++) {
            Register new_reg = new Register();
            callee_saved.add(new_reg);
			auxl = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, callee_saved.get(i), 
                Register.callee_saved.get(i), auxl));
            locals.add(new_reg);
        }
        
        // 1. Copy the return value into %rax
        this.ertlgraph.put(o.exit, new ERmbinop(
            Mbinop.Mmov, o.result, Register.rax, auxl));
        
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
        
        // Redirect intermediate control to o.entry
        auxl = o.entry;
        
        // 4. Copy the other parameters into pseudo-registers
		for (int i=6; i <= n_args-1; i++) {
			auxl = this.ertlgraph.add(new ERget_param(
                8*(n_args+1-i), o.formals.get(i), auxl));
		}
		
		// 3. Pass the min(n, 6) parameters into pseudo-register
		for (int i=k-1; i>=0; i--) {
			auxl = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, Register.parameters.get(i), 
                o.formals.get(i), auxl));
		}

        // 2. Save the callee-saved registers
        for (int i=0; i<Register.callee_saved.size(); i++) {
			auxl = this.ertlgraph.add(new ERmbinop(
                Mbinop.Mmov, Register.callee_saved.get(i), 
                callee_saved.get(i), auxl));
		}

        // 1. Alloc the activation table with alloc_frame
        auxl = this.ertlgraph.add(
            new ERalloc_frame(auxl));
        
        this.ertlfun = new ERTLfun(o.name, o.formals.size());
        this.ertlfun.body = this.ertlgraph;
        this.ertlfun.locals = locals;
		this.ertlfun.entry = auxl;
	}

	@Override
	public void visit(RTLfile o) {
		this.ertlfile = new ERTLfile();
		for (RTLfun fun: o.funs) {
            this.ertlgraph = new ERTLgraph();
            fun.accept(this);
			this.ertlfile.funs.add(this.ertlfun);
		}
	}
}