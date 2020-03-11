package mini_c;

import java.util.HashSet;

class ToX86_64 implements LTLVisitor {
    private LTLgraph cfg; // graphe en cours de traduction
    private X86_64 asm; // code en cours de construction
    private HashSet<Label> visited; // instructions déjà traduites
    
    ToX86_64(String file) {
        this.asm = new X86_64(file);
        this.visited = new HashSet<>();
    }
    
    private void lin(Label l) {
        if (this.visited.contains(l)) {
            this.asm.needLabel(l);
            this.asm.jmp(l.name);
        } else {
            this.visited.add(l);
            this.asm.label(l);
            this.cfg.graph.get(l).accept(this);
        }
    }

    X86_64 translate(LTLfile lf) {
		lf.accept(this);
		return this.asm;
	}

    @Override
    public void visit(Lload o) {
        this.asm.movq(o.i + "(" + o.r1 + ")", o.r2.name);
        this.lin(o.l);
    }

    @Override
    public void visit(Lstore o) {
        this.asm.movq(o.r1.name, o.i + "(" + o.r2 + ")");
        this.lin(o.l);
    }
    
    @Override
    public void visit(Lmubranch o) {
        if (!this.visited.contains(o.l1)) {
            if (o.m instanceof Mjz) {
                this.asm.addq("$0", o.r.toString());
                this.asm.jnz(o.l2.name);
            }
            if (o.m instanceof Mjnz) {
                this.asm.addq("$0", o.r.toString());
                this.asm.jz(o.l2.name);
            }
            if (o.m instanceof Mjlei) {            
                this.asm.cmpq(((Mjlei) o.m).n, o.r.toString()); 
                this.asm.jg(o.l2.name); 
            }
            if (o.m instanceof Mjgi) { 
                this.asm.cmpq(((Mjgi) o.m).n, o.r.toString()); 
                this.asm.jle(o.l2.name);
            }
            
            this.asm.needLabel(o.l2);
            this.lin(o.l1);
            this.lin(o.l2);
        }
        else {
            if (o.m instanceof Mjz) {
                this.asm.addq("$0", o.r.toString());
                this.asm.jz(o.l1.name);
            }
            if (o.m instanceof Mjnz) {
                this.asm.addq("$0", o.r.toString());
                this.asm.jnz(o.l1.name);
            }
            if (o.m instanceof Mjlei) {            
                this.asm.cmpq(((Mjlei) o.m).n, o.r.toString()); 
                this.asm.jle(o.l1.name);  
            }
            if (o.m instanceof Mjgi) { 
                this.asm.cmpq(((Mjgi) o.m).n, o.r.toString()); 
                this.asm.jg(o.l1.name); 
            }
            
            if (!this.visited.contains(o.l2)) {
                this.lin(o.l2);
                this.lin(o.l1);
            }
            else {
                this.asm.needLabel(o.l2);
                this.asm.jmp(o.l2.name);
            }
        }
    }
    
    @Override
    public void visit(Lmbbranch o) {
        this.asm.cmpq(o.r1.toString(), o.r2.toString());
        if (!this.visited.contains(o.l1)) {
            if (o.m == Mbbranch.Mjl) this.asm.jl(o.l2.name);    
            if (o.m == Mbbranch.Mjle) this.asm.jle(o.l2.name);
            
            this.lin(o.l1);
            this.lin(o.l2);
        }
        else {
            if (o.m == Mbbranch.Mjl) this.asm.jge(o.l1.name);    
            if (o.m == Mbbranch.Mjle) this.asm.jg(o.l1.name);
            
            if (!this.visited.contains(o.l2)) {
                this.lin(o.l2);
                this.lin(o.l1);
            }
            else {
                this.asm.needLabel(o.l2);
                this.asm.jmp(o.l2.name);
            }
        }
    }
    
    @Override
    public void visit(Lgoto o) {
        if (this.visited.contains(o.l)) {
            this.asm.needLabel(o.l);
            this.asm.jmp(o.l.name);
        }
        else {
            this.lin(o.l);
        }
    }
    
    @Override
    public void visit(Lreturn o) {
        this.asm.ret();
    }
    
    @Override
    public void visit(Lconst o) {
        this.asm.movq(o.i, o.o.toString());
        this.lin(o.l);
    }
    
    @Override
    public void visit(Lmunop o) {
        String s = o.o.toString();
        if (o.m instanceof Maddi)   
            this.asm.addq("$" + ((Maddi) o.m).n, s);
        if (o.m instanceof Msetei) {
            this.asm.cmpq(((Msetei) o.m).n, s); 
            this.asm.sete("%r15b"); 
            this.asm.movzbq("%r15b", s);
        }
        if (o.m instanceof Msetnei) {
            this.asm.cmpq(((Msetnei) o.m).n, s); 
            this.asm.setne("%r15b"); 
            this.asm.movzbq("%r15b", s);
        }
        this.lin(o.l);
    }
    
    @Override
    public void visit(Lmbinop o) {
        String s1 = o.o1.toString();
        String s2 = o.o2.toString();
        String st1 = Register.tmp1.name;

        switch (o.m) {
            case Mmov: this.asm.movq(s1, s2); break;
            case Madd: this.asm.addq(s1, s2); break;
            case Msub: this.asm.subq(s1, s2); break;
            
            // For imulq, the dest MUST be a register instead of memory addr
            case Mmul: {
                if (o.o2 instanceof Reg) {
                    this.asm.imulq(s1, s2);
                }
                else {
                    this.asm.movq(s2, st1);
                    this.asm.imulq(s1, st1);
                    this.asm.movq(st1, s2);
                }
                break;
            }

            case Mdiv: this.asm.cqto(); this.asm.idivq(s1); break;
            
            // st1 + "b" is the lower-byte of the temporary register %r15 (st1)
            case Msete: {
                this.asm.xorq(st1, st1);
                this.asm.cmpq(s1, s2); this.asm.sete(st1 + "b"); 
                this.asm.movq(st1, s2); break;
            }
            case Msetne: {
                this.asm.xorq(st1, st1);
                this.asm.cmpq(s1, s2); this.asm.setne(st1 + "b"); 
                this.asm.movq(st1, s2); break;
            }
            case Msetl: {
                this.asm.xorq(st1, st1);
                this.asm.cmpq(s1, s2); this.asm.setl(st1 + "b"); 
                this.asm.movq(st1, s2); break;
            }
            case Msetle: {
                this.asm.xorq(st1, st1);
                this.asm.cmpq(s1, s2); this.asm.setle(st1 + "b"); 
                this.asm.movq(st1, s2); break;
            }
            case Msetg: {
                this.asm.xorq(st1, st1);
                this.asm.cmpq(s1, s2); this.asm.setg(st1 + "b"); 
                this.asm.movq(st1, s2); break;
            }
            case Msetge: {
                this.asm.xorq(st1, st1);
                this.asm.cmpq(s1, s2); this.asm.setge(st1 + "b"); 
                this.asm.movq(st1, s2); break;
            }
            
        }
        this.lin(o.l);
    }
    
    @Override
    public void visit(Lpush o) {
        this.asm.pushq(o.o.toString());
        this.lin(o.l);
    }
    
    @Override
    public void visit(Lpop o) {
        this.asm.popq(o.r.name);
        this.lin(o.l);
    }
    
    @Override
    public void visit(Lcall o) {
        this.asm.call(o.s);
        this.lin(o.l);
    }
    
    @Override
    public void visit(LTLfun o) {
        this.cfg = o.body;
        this.asm.label(o.name);
        this.lin(o.entry);
    }
    
    @Override
    public void visit(LTLfile o) {
        this.asm.globl("main");
        for (LTLfun fun: o.funs) {
            fun.accept(this);
        }
        this.asm.printToFile();
    }
}
