package mini_c;

/** Opérations x86-64 utilisées pendant la sélection d'instructions */

/** opération x86-64 unaire */
abstract class Munop {}
class Maddi extends Munop {
	int n;
	Maddi(int n) { this.n = n;}
	public String toString() { return "add $" + n; } 
}
class Msetei extends Munop {
	int n;
	Msetei(int n) { this.n = n;}
	public String toString() { return "sete $" + n; } 
}
class Msetnei extends Munop {
	int n;
	Msetnei(int n) { this.n = n;}
	public String toString() { return "setne $" + n; } 
}

/** opération x86-64 binaire */
enum Mbinop {
  Mmov
, Madd
, Msub
, Mmul
, Mdiv
, Msete
, Msetne
, Msetl
, Msetle
, Msetg
, Msetge
}

/** opération x86-64 de branchement (unaire) */
abstract class Mubranch {} 
class Mjz extends Mubranch {
	public String toString() { return "jz"; } 	
}
class Mjnz extends Mubranch {
	public String toString() { return "jnz"; } 	
}
class Mjlei  extends Mubranch {
	int n;
	Mjlei(int n) { this.n = n;}
	public String toString() { return "jle $" + n; } 	
}
class Mjgi extends Mubranch {
	int n;
	Mjgi(int n) { this.n = n;}
	public String toString() { return "jg $" + n; } 	
}

/** opération x86-64 de branchement (binaire) */
enum Mbbranch {
  Mjl
, Mjle
}
