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
    /** for tail-call optimization (limited to recursive functions) */
    private Label exitLabel; // exit label of rtlGraph (to identify tail calls)
    private LinkedList<Label> gotoLabels; // labels in ertlGraph corresponding to goto
	
	ToERTL() {
        this.ertlFile = new ERTLfile();
	}
	
	ERTLfile translate(RTLfile tree) {
		tree.accept(this);
		return this.ertlFile;
	}
	
	
    /** checkAndAccept: a method to recurse over the RTL graph
	 *      - Ensures this.rtlLabel invariant is maintained
	 *      - Recurses only if not exiting
	 */
	private void checkAndAccept(Label l) {
		RTL rtl = this.rtlGraph.graph.get(l);
		this.rtlLabel = l;
		visitedLabels.add(l);
        if (rtl != null) rtl.accept(this);
    }
    
	@Override
	public void visit(Rconst o) {
		Label myLabel = this.rtlLabel;
		checkAndAccept(o.l);
		this.ertlGraph.put(myLabel, new ERconst(o.i, o.r, o.l));
	}

	@Override
	public void visit(Rload o) {
		Label myLabel = this.rtlLabel;
		checkAndAccept(o.l);
		this.ertlGraph.put(myLabel, new ERload(o.r1, o.i, o.r2, o.l));
	}

	@Override
	public void visit(Rstore o) {
		Label myLabel = this.rtlLabel;
		checkAndAccept(o.l);
		this.ertlGraph.put(myLabel, new ERstore(o.r1, o.r2, o.i, o.l));
	}

	@Override
	public void visit(Rmunop o) {
		Label myLabel = this.rtlLabel;
		checkAndAccept(o.l);
		this.ertlGraph.put(myLabel, new ERmunop(o.m, o.r, o.l));
	}

	@Override
	public void visit(Rmbinop o) {
		Label myLabel = this.rtlLabel;
		checkAndAccept(o.l);
		
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
		checkAndAccept(o.l1);
        checkAndAccept(o.l2);
        this.ertlGraph.put(myLabel,
			new ERmubranch(o.m, o.r, o.l1, o.l2));
	}

	@Override
	public void visit(Rmbbranch o) {
		
		Label myLabel = this.rtlLabel;
		checkAndAccept(o.l1);
		checkAndAccept(o.l2);
		this.ertlGraph.put(myLabel,
			new ERmbbranch(o.m, o.r1, o.r2, o.l1, o.l2));
	}

	@Override
	public void visit(Rcall o) {

        boolean isRecTailCall = 
            (this.exitLabel == o.l && o.s.equals(this.ertlFun.name));
        
        Label myLabel = this.rtlLabel;
		checkAndAccept(o.l);
		
		int n_args = o.rl.size();
        int k = n_args; 
        if (k > 6) k = 6; // k = min(n_args, 6)
        
        // Cheat on lastFresh meaning: force control to be passed to o.l
        this.lastFresh = o.l;

        if (!isRecTailCall) {
            /** 5. If n > 6, pop 8×(n−6) bytes from the stack */
            if (n_args > 6) {
                Register r1 = new Register();
                this.lastFresh = this.ertlGraph.add(new ERmbinop(
                    Mbinop.Msub, r1, Register.rsp, this.lastFresh));
                this.lastFresh = this.ertlGraph.add(
                    new ERconst(8*(n_args-6), r1, this.lastFresh));
            }
            
            /** 4. Copy %rax in r */
            this.lastFresh = this.ertlGraph.add(new ERmbinop(
                Mbinop.Mmov, Register.rax, o.r, this.lastFresh));
        }

        if (n_args > 0) {
            /** 
             * 3.
             *  - Call f(k) if not tail call
             *  - Goto entry point otherwise
             */
            if (!isRecTailCall) {
                this.lastFresh = this.ertlGraph.add(
                    new ERcall(o.s, k, this.lastFresh));
            }
            else {
                Label gotoLabel = new Label();
                this.gotoLabels.add(gotoLabel);
                this.lastFresh = gotoLabel;
            }

            /** 2. If n > 6, pass the other arguments on the stack */
			for (int i=n_args-1; i>=6; i--) {
				this.lastFresh = this.ertlGraph.add(
                    new ERpush_param(o.rl.get(i), this.lastFresh));
			}
			
			/** 1. Pass the min(n, 6) arguments inside corresponding register */
			for (int i=k-1; i>=1; i--) {
				this.lastFresh = this.ertlGraph.add(new ERmbinop(
                    Mbinop.Mmov, o.rl.get(i), 
                    Register.parameters.get(i), this.lastFresh));
			}
			this.ertlGraph.put(myLabel, new ERmbinop(
                Mbinop.Mmov, o.rl.get(0), 
                Register.parameters.get(0), this.lastFresh));
        } 
        else /** No arguments: transfer control to 3. */ {
            if (!isRecTailCall) {
			    this.ertlGraph.put(myLabel,
                    new ERcall(o.s, k, this.lastFresh));
            } else {
                this.gotoLabels.add(myLabel);
            }
		}
	}

	@Override
	public void visit(Rgoto o) {
		Label myLabel = this.rtlLabel;
		if (!visitedLabels.contains(o.l)) checkAndAccept(o.l);
		this.ertlGraph.put(myLabel, new ERgoto(o.l));
	}

	@Override
	public void visit(RTLfun o) {

        this.ertlFun = new ERTLfun(o.name, o.formals.size());
        this.exitLabel = o.exit;
        
        Set<Register> locals = o.locals;
        LinkedList<Register> callee_saved = new LinkedList<>();

        /** 9. Return */
        this.lastFresh = this.ertlGraph.add(new ERreturn());
        
        /** 8. Free the activation table */
        this.lastFresh = this.ertlGraph.add(
            new ERdelete_frame(this.lastFresh));

        /** 7. Retreive the callee-saved registers */
        for (int i=0; i<Register.callee_saved.size(); i++) {
            Register new_reg = new Register();
            callee_saved.add(new_reg);
			this.lastFresh = this.ertlGraph.add(new ERmbinop(
                Mbinop.Mmov, callee_saved.get(i), 
                Register.callee_saved.get(i), this.lastFresh));
            locals.add(new_reg);
        }
        
        /** 6. Copy the return value into %rax */
        this.ertlGraph.put(o.exit, new ERmbinop(
            Mbinop.Mmov, o.result, Register.rax, this.lastFresh));
        
        /** 5. Time for recursion */
        visitedLabels = new LinkedList<>();
        this.rtlGraph = o.body;
		RTL rtl = this.rtlGraph.graph.get(o.entry);
		this.rtlLabel = o.entry;
		visitedLabels.add(o.entry);
        if (rtl != null) rtl.accept(this);
        /** End of recursion */

        int n_args = o.formals.size();
        int k = n_args;
        if (n_args > 6) k = 6;
        
        // Cheat on lastFresh meaning: force control to be passed to o.entry
        this.lastFresh = o.entry;
        
        /** 4. Copy the other parameters into pseudo-registers */
		for (int i=6; i <= n_args-1; i++) {
			this.lastFresh = this.ertlGraph.add(new ERget_param(
                8*(n_args+1-i), o.formals.get(i), this.lastFresh));
		}
		
		/** 3. Pass the min(n, 6) parameters into pseudo-register */
		for (int i=k-1; i>=0; i--) {
			this.lastFresh = this.ertlGraph.add(new ERmbinop(
                Mbinop.Mmov, Register.parameters.get(i), 
                o.formals.get(i), this.lastFresh));
		}

        /** For tail-call optimization */
        for (Label gotoLabel: this.gotoLabels) {
            this.ertlGraph.graph.put(gotoLabel, new ERgoto(this.lastFresh));
        }

        /** 2. Save the callee-saved registers */
        for (int i=0; i<Register.callee_saved.size(); i++) {
			this.lastFresh = this.ertlGraph.add(new ERmbinop(
                Mbinop.Mmov, Register.callee_saved.get(i), 
                callee_saved.get(i), this.lastFresh));
		}

        /** 1. Alloc the activation table with alloc_frame */
        this.lastFresh = this.ertlGraph.add(
            new ERalloc_frame(this.lastFresh));
        
        this.ertlFun.body = this.ertlGraph;
        this.ertlFun.locals = locals;
        this.ertlFun.entry = this.lastFresh;
	}

	@Override
	public void visit(RTLfile o) {
		for (RTLfun fun: o.funs) {
            this.ertlGraph = new ERTLgraph();
            this.gotoLabels = new LinkedList<>();
            fun.accept(this);
			this.ertlFile.funs.add(this.ertlFun);
		}
	}
}