package mini_c;

import java.util.HashMap;
import java.util.LinkedList;

/* When we create an Expr give its type!!!!! */

public class Typing implements Pvisitor {

	// le résultat du typage sera mis dans cette variable
	private File file;
    private Typ typ = new Ttypenull();
    private Typ return_typ; // for statements
	private Stmt stmt;
    private Expr expr;
	private LinkedList<HashMap<String, Pdeclvar>> vars;
    private LinkedList<Decl_fun> l_decl_fun = new LinkedList<>();
    private HashMap<String, Decl_fun> funs = new HashMap<>();
    private HashMap<String, Decl_fun> fun_prototypes = new HashMap<>();
    private HashMap<String, Structure> structs = new HashMap<>();
    
	File getFile() {
		if (this.file == null)
			throw new Error("typing not yet done!");
		
		return this.file;
	}
	
	@Override
	public void visit(Pfile n) {
		// Implement prototype functions: sbrk and putchar
        
        // sbrk
        LinkedList<Decl_var> lsbrk = new LinkedList<>();
		lsbrk.add(new Decl_var(new Tint(), ""));
		this.fun_prototypes.put("sbrk", new Decl_fun(
            new Tvoidstar(), "sbrk", lsbrk, null));
		LinkedList<Decl_var> lputchar = new LinkedList<>();
		lputchar.add(new Decl_var(new Tint(), ""));
        
        // putchar
        this.fun_prototypes.put("putchar", 
            new Decl_fun(new Tint(), "putchar", lputchar, null));
		for (Pdecl d: n.l) {
			d.accept(this);
		}
        
        this.file = new File(this.l_decl_fun);
	}

	@Override
	public void visit(PTint n) {

        this.typ = new Tint();
	}

	@Override
	public void visit(PTstruct n) {

        this.typ = new Tstructp(this.structs.get(n.id));
	}

	@Override
	public void visit(Pint n) {

        this.expr = new Econst(n.n);
        if (n.n != 0) {
            this.expr.typ = new Tint();
        }
        else {
            this.expr.typ = new Ttypenull();
        }
	}

	@Override
	public void visit(Pident n) {
        int index = -1;
        for (int i = this.vars.size()-1; i >= 0; i--) {
        	if (this.vars.get(i).containsKey(n.id)) {	
                index = i;
                break;
        	}
        }       	
        if (index < 0)
            throw new Error(n.loc.toString() + ": unknown variable: " + n.id);
        
        vars.get(index).get(n.id).typ.accept(this);

        this.expr = new Eaccess_local(n.id);
        this.expr.typ = this.typ;
	}

	@Override
	public void visit(Punop n) {
        
        n.e1.accept(this);
        if (n.op == Unop.Uneg && !this.expr.typ.equals(new Tint())) {
            throw new Error(n.e1.loc.toString() + ": should be int" +
                this.expr.typ.toString() + " given");
        }
        this.expr = new Eunop(n.op, this.expr);
        this.expr.typ = new Tint();
	}

	@Override
	public void visit(Passign n) {

        n.e1.accept(this);
        Expr e1 = this.expr;
        n.e2.accept(this);
        Expr e2 = this.expr;

        if (!e1.typ.equals(e2.typ)) {
            throw new Error(n.loc.toString() + ": different types (" +
                e1.typ.toString() + ", " + e2.typ.toString() + ")");
        }
        if (e1.typ instanceof Tstructp) {
        	if(e2.typ instanceof Ttypenull) {
        		((Tstructp) e1.typ).s.is_null = true;
        	}
        	else {
        		((Tstructp) e1.typ).s.is_null = false;
        	}
        }

        if (e1 instanceof Eaccess_local) {
            Eaccess_local e = (Eaccess_local) e1;
            this.expr = new Eassign_local(e.i, e2);
        }
        if (e1 instanceof Eaccess_field) {
            Eaccess_field e = (Eaccess_field) e1;
            this.expr = new Eassign_field(e.e, e.f, e2);
        }
        this.expr.typ = e1.typ;
	}

	@Override
	public void visit(Pbinop n) {
        
        n.e1.accept(this);
        Expr e1 = this.expr;
        n.e2.accept(this);
        Expr e2 = this.expr;
        
        if (n.op == Binop.Beq || n.op == Binop.Bneq || n.op == Binop.Blt || 
            n.op == Binop.Ble || n.op == Binop.Bgt || n.op == Binop.Bge)
        {    
            if (!e1.typ.equals(e2.typ)) {
                throw new Error(n.loc.toString() + ": different types (" +
                    e1.typ.toString() + ", " + e2.typ.toString() + ")");
            }
        }
        else if (n.op == Binop.Badd || n.op == Binop.Bsub ||
                 n.op == Binop.Bmul || n.op == Binop.Bdiv)
        {
        	//TODO Tidy up the "e1 = " and "e2 = "
            if (!(e1.typ instanceof Tint || e1.typ instanceof Ttypenull)) {
                throw new Error("e1 = " + n.loc.toString() + ": should be int. " +
                    e1.typ.toString() + " given");
            }
            if (!(e2.typ instanceof Tint || e2.typ instanceof Ttypenull)) {
                throw new Error("e2 = " +n.loc.toString() + ": should be int. " + 
                    e2.typ.toString() + " given");
            }
        }
        if(n.op == Binop.Bdiv && e2.typ instanceof Ttypenull) {
        	throw new Error(n.loc.toString() + ": Division by zero.");
        }
        
        this.expr = new Ebinop(n.op, e1, e2);
        this.expr.typ = new Tint();
	}

	@Override
	public void visit(Parrow n) {
		
        n.e.accept(this);
        Expr e = this.expr;
        
        if (!e.typ.equals(new Tvoidstar())) {
            throw new Error(n.loc.toString() + ": should be struct *; " + 
                e.typ.toString() + " given");
        }
        Structure s = ((Tstructp) e.typ).s;
        if (!s.fields.containsKey(n.f)) {
            throw new Error(n.loc.toString() + ": " + s.toString() + 
                " doesn't contain field " + n.f);
        }
        if(s.is_null) {
        	throw new Error(n.loc.toString() + ": " + s.toString() + 
                    " null pointer exception");
        }
        Field f = s.fields.get(n.f);
        
        this.expr = new Eaccess_field(e, f);
        this.expr.typ = f.typ;
	}

	@Override
	public void visit(Pcall n) {
		
        if (!this.fun_prototypes.containsKey(n.f)) {
            throw new Error(n.loc.toString() + ": function " + 
                n.f + " not declared");
        }
        Decl_fun df = this.fun_prototypes.get(n.f);
        if (n.l.size() != df.fun_formals.size()) {
            throw new Error(n.loc.toString() + 
                ": wrong number of arguments: " + n.l.size() + " given, " +
                df.fun_formals.size() + " expected");
        }
        
        LinkedList<Expr> l_exprs = new LinkedList<>();
        for (int i = 0; i < n.l.size(); i++) {
            n.l.get(i).accept(this);
            Expr e = this.expr;
            l_exprs.add(e);
            Typ t1 = e.typ;
            Typ t2 = df.fun_formals.get(i).t;
            
            if (!t1.equals(t2)) {
                throw new Error(n.loc.toString() + 
                    ": argument " + (i+1) + ": " + 
                    t1.toString() + " given, " + t2.toString() + " expected");
            }
        }
        this.expr = new Ecall(n.f, l_exprs);
        this.expr.typ = df.fun_typ;
        this.typ = df.fun_typ;
	}

	@Override
	public void visit(Psizeof n) {
		
        if (!this.structs.containsKey(n.id)) {
            throw new Error(n.loc.toString() + ": " + n.id + " not defined");
        }
    
        this.expr = new Esizeof(this.structs.get(n.id));
        this.expr.typ = new Tint();
	}

	@Override
	public void visit(Pskip n) {
        
        this.stmt = new Sskip();
	}

	@Override
	public void visit(Peval n) {
		
        n.e.accept(this);
        this.stmt = new Sexpr(this.expr);
	}

	@Override
	public void visit(Pif n) {
        
        n.e.accept(this);
        Expr e = this.expr;
        n.s1.accept(this);
        Stmt s1 = this.stmt;
        n.s2.accept(this);
        Stmt s2 = this.stmt;
        this.stmt = new Sif(e, s1, s2);
	}

	@Override
	public void visit(Pwhile n) {
        
        n.e.accept(this);
        Expr e = this.expr;
        n.s1.accept(this);
        Stmt s1 = this.stmt;
        this.stmt = new Swhile(e, s1);
	}

	@Override
	public void visit(Pbloc n) {
        
        LinkedList<Decl_var> d_vars = new LinkedList<>();
        LinkedList<Stmt> l_stmt = new LinkedList<>();
        
        this.vars.addLast(new HashMap<String, Pdeclvar>());
        
        for (Pdeclvar dv: n.vl) {
            
        	if (this.vars.get(this.vars.size()-1).containsKey(dv.id)) {
                throw new Error(dv.loc.toString() + 
                                ": redefinition of variable: " + dv.id);
        	}
        	
        	dv.typ.accept(this);
	        this.vars.get(this.vars.size()-1).put(dv.id, dv);
	        d_vars.add(new Decl_var(this.typ, dv.id));
		}
		for (Pstmt s: n.sl) {
            s.accept(this);
            l_stmt.add(this.stmt);
        }
        this.stmt = new Sblock(d_vars, l_stmt);

        this.vars.removeLast();
	}

	@Override
	public void visit(Preturn n) {
        n.e.accept(this);
        
        this.stmt = new Sreturn(this.expr);

        if (!this.return_typ.equals(this.expr.typ)) {
            throw new Error(n.loc.toString() + ": wrong return type: " +
                this.expr.typ.toString() + " given, " + 
                this.return_typ.toString() + " expected");
        }
	}

	@Override
	public void visit(Pstruct n) {
    
        if (this.structs.containsKey(n.s)) {
            throw new Error("redefinition of struct: " + n.s);
        }
        
        Structure s = new Structure(n.s);
        this.structs.put(n.s, s);
        int offset = 0;
        for (Pdeclvar dv: n.fl) {
            if (s.fields.containsKey(dv.id)) {
                throw new Error("redefinition of field " + dv.id + 
                                " inside struct " + n.s);
            }
            dv.typ.accept(this);
            s.fields.put(dv.id, new Field(dv.id, this.typ, offset));
            offset += 8;
        }
        s.size = offset;
	}

	@Override
	public void visit(Pfun n) {
        
        n.ty.accept(this);
		this.return_typ = this.typ;
        String fun_name = n.s;
        
        if (this.fun_prototypes.containsKey(fun_name)) {
            throw new Error("redefinition of function: " + fun_name);
        }
        
        LinkedList<Decl_var> fun_formals = new LinkedList<>();
        Decl_fun d_fun = new Decl_fun(this.return_typ, fun_name, 
        		fun_formals, null);
        fun_prototypes.put(fun_name, d_fun);
        
        this.l_decl_fun.add(d_fun);
        
        this.vars = new LinkedList<>();
        this.vars.addLast(new HashMap<String, Pdeclvar>());
     
        for (Pdeclvar dv: n.pl) {
            
        	if (this.vars.get(this.vars.size()-1).containsKey(dv.id)) {
                throw new Error("redefinition of variable " + dv.id + 
                        " inside function " + fun_name);
        	}
        	
        	dv.typ.accept(this);
        	fun_formals.add(new Decl_var(this.typ, dv.id));
        	this.vars.get(this.vars.size()-1).put(dv.id, dv);
        }
                
        n.b.accept(this);
		d_fun.fun_body = this.stmt;
		this.funs.put(fun_name, d_fun);
	}
}
