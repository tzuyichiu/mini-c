package mini_c;

import java.util.HashMap;
import java.util.LinkedList;

/* When we create an Expr give its type!!!!! */

public class Typing implements Pvisitor {

	// le résultat du typage sera mis dans cette variable
	private File file;
    private Typ typ = new Ttypenull();  // Modified by Tzu-yi on 30 jan
    private Typ return_typ; // for statements
	private Stmt stmt;
    private Expr expr;
    private boolean is_lvalue;
	private LinkedList<HashMap<String, Pdeclvar>> vars;
    private LinkedList<Decl_fun> l_decl_fun = new LinkedList<>();
    private HashMap<String, Decl_fun> funs = new HashMap<>();
    private HashMap<String, Structure> structs = new HashMap<>();
    
    private int isAssigned(String id, Loc loc) {
        for(int i = this.vars.size()-1; i >= 0; i --) {
        	if(this.vars.get(i).containsKey(id)) {	
        		return i;
        	}
        }       	
        throw new Error(loc.toString() + ": unknown variable: " + id);
    }
    private int isAssigned(String id) {
        for(int i = this.vars.size()-1; i >= 0; i --) {
        	if(this.vars.get(i).containsKey(id)) {	
        		return i;
        	}
        }       	
        throw new Error("Unknown struct: " + id);
    }
    
    // et renvoyé par cette fonction
	File getFile() {
		if (this.file == null)
			throw new Error("typing not yet done!");
		return this.file;
	}
	
	@Override
	public void visit(Pfile n) {
		for (Pdecl d : n.l) {
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

        this.typ = new Tstructp(new Structure(n.id));
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
		int index = isAssigned(n.id, n.loc);
		for(int i = 0; i < this.vars.size(); i++) {
			System.out.println(this.vars.get(i));
		}
        
        vars.get(index).get(n.id).typ.accept(this);

        this.is_lvalue = true;
        this.expr = new Eaccess_local(n.id);
        this.expr.typ = this.typ;
	}

	@Override
	public void visit(Punop n) {
        
        n.e1.accept(this);
        if (n.op == Unop.Uneg && !this.expr.typ.equals(new Tint())) {
            throw new Error(n.e1.loc.toString() + ": should be int");
        }
        this.expr = new Eunop(n.op, this.expr);
        this.expr.typ = new Tint();
	}

	@Override
	public void visit(Passign n) {

        this.is_lvalue = false;
        n.e1.accept(this);
        Expr e1 = this.expr;

        if (!this.is_lvalue) {
            throw new Error(n.e1.loc.toString() + 
                ": left member not a valid expression");
        }

        n.e2.accept(this);
        Expr e2 = this.expr;
        if (!e1.typ.equals(e2.typ)) {
            throw new Error(n.e1.loc.toString() + ": different types (" +
                e1.typ.toString() + ", " + e2.typ.toString() + ")");
        }

        this.expr = new Eassign_local(((Pident) n.e1).id, e2);
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
                throw new Error(n.e1.loc.toString() + ": different types (" +
                    e1.typ.toString() + ", " + e2.typ.toString() + ")");
            }
        }
        else if (n.op == Binop.Badd || n.op == Binop.Bsub ||
                 n.op == Binop.Bmul || n.op == Binop.Bdiv)
        {
            if (!(e1.typ instanceof Tint)) {
                throw new Error(n.e1.loc.toString() + ": should be int");
            }
            if (!(e2.typ instanceof Tint)) {
                throw new Error(n.e2.loc.toString() + ": should be int");
            }
        }
        
        this.expr = new Ebinop(n.op, e1, e2);
        this.expr.typ = new Tint();
	}

	@Override
	public void visit(Parrow n) {
		
        n.e.accept(this);
        Expr e = this.expr;
        
        if ((!e.typ.equals(new Tstructp(new Structure("")))) || 
                !(e.typ instanceof Tstructp)) {
            throw new Error(n.e.loc.toString() + ": should be struct*");
        }
        Structure s = ((Tstructp) e.typ).s;
        if (!s.fields.containsKey(n.f)) {
            throw new Error(n.e.loc.toString() + ": " + s.toString() + 
                " doesn't contain the field " + n.f);
        }
        Field f = s.fields.get(n.f);
        
        this.expr = new Eaccess_field(e, f);
        this.expr.typ = f.field_typ;
        this.is_lvalue = true;
	}

	@Override
	public void visit(Pcall n) {
        
        if (!this.funs.containsKey(n.f)) {
            throw new Error(n.loc.toString() + ": function " + 
                n.f + " not declared");
        }
        Decl_fun df = this.funs.get(n.f);
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
            String st1 = e.typ.toString();
            String st2 = df.fun_formals.get(i).t.toString();
            
            if (!st2.equals(st1)) {
                throw new Error(n.loc.toString() + ": type error for argument " 
                    + i + ": " + st1 + " given, " + st2 + " expected");
            }
        }
        this.expr = new Ecall(n.f, l_exprs);
        this.typ = df.fun_typ;
	}

	@Override
	public void visit(Psizeof n) {
		
		int index = isAssigned(n.id);
		
        Pdeclvar pd = vars.get(index).get(n.id);
        pd.typ.accept(this);
        
        if ((!this.typ.equals(new Tstructp(new Structure("")))) || 
                !(this.typ instanceof Tstructp)) {
            throw new Error(n.loc.toString() + ": should be struct*");
        }

        this.expr = new Esizeof(((Tstructp) this.typ).s);
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
        		throw new Error(dv.loc.toString() + ": redefinition of variable: " + dv.id);
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
//        System.out.println("Before pop");
//        for(int i = 0; i < this.vars.size(); i++) {
//			
//			System.out.println(this.vars.get(i));
//			System.out.println("");
//		}
        this.vars.removeLast();
//        System.out.println("After pop");
//        for(int i = 0; i < this.vars.size(); i++) {
//			System.out.println(this.vars.get(i));
//			System.out.println("");
//		}
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
        for (Pdeclvar dv: n.fl) {
            if (s.fields.containsKey(dv.id)) {
                throw new Error("redefinition of field " + dv.id + 
                                " inside struct " + n.s);
            }
            dv.typ.accept(this);
            s.fields.put(dv.id, new Field(dv.id, this.typ));
        }
        this.structs.put(n.s, s);
	}

	@Override
	public void visit(Pfun n) {
        
        n.ty.accept(this);
		this.return_typ = this.typ;
		String fun_name = n.s;
        LinkedList<Decl_var> fun_formals = new LinkedList<>();
        
        if (this.funs.containsKey(fun_name)) {
            throw new Error("redefinition of function: " + fun_name);
        }
        
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
		Stmt fun_body = this.stmt;
        Decl_fun d_fun = new Decl_fun(this.return_typ, fun_name, 
                                        fun_formals, fun_body);
        this.l_decl_fun.add(d_fun);
        this.funs.put(fun_name, d_fun);
	}
}