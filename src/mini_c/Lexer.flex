package mini_c;

import java_cup.runtime.*;
import static mini_c.sym.*;

%%

%class Lexer
%unicode
%cup
%cupdebug
%line
%column
%yylexthrow Exception

/* The symbols produced by the lexical analyzer not just integers, but objects
   of type java_cup.runtime.Symbol. To create such an object, one invokes the
   function symbol(), defined below, and supplies an integer constant, which
   identifies a terminal symbol; if necessary, one also supplies a semantic
   value, of an appropriate type -- this must match the type declared for this
   terminal symbol in Parser.cup. */

/* See https://www2.in.tum.de/repos/cup/develop/src/java_cup/runtime/ */

/* Technical note: CUP seems to assume that the two integer parameters
   passed to the Symbol constructor are character counts for the left
   and right positions. Instead, we choose to provide line and column
   information. Accordingly, we will replace CUP's error reporting
   routine with our own. */

%{

    private Symbol symbol(int id)
    {
	return new Symbol(id, yyline, yycolumn);
    }

    private Symbol symbol(int id, Object value)
    {
	return new Symbol(id, yyline, yycolumn, value);
    }

	static int decodeHexa(String s) {
		return Integer.decode("0x" + s.substring(3, 5));
	}

%}
LineTerminator     = \r | \n | \r\n
InputCharacter     = [^\r\n]
WhiteSpace         = [ \t\f\r\n]

Comment            = "//" {InputCharacter}* {LineTerminator}

Identifier         = [:jletter:] [:jletterdigit:]*

Integer            = [:digit:]+
char               = [^\\'\"]
octal              = [01234567]
hexa               = [:digit:] | [abcdefABCDEF]
 
%state COMMENT

%%

/* A specification of which regular expressions to recognize and what
   symbols to produce. */

<YYINITIAL> {

    "="
    { return symbol(EQUAL); }

    ","
    { return symbol(COMMA); }

    ";"
    { return symbol(SEMICOLON); }

    "("
    { return symbol(LP); }

    ")"
    { return symbol(RP); }

    "{"
    { return symbol(LBRA); }

    "}"
    { return symbol(RBRA); }

    "+"
    { return symbol(PLUS); }

    "->"
    { return symbol(ARROW); }
 
    "-"
    { return symbol(MINUS); }

    "*"
    { return symbol(TIMES); }

    "/"
    { return symbol(DIV); }

    "<"
    { return symbol(CMP, Binop.Blt); }

    "<="
    { return symbol(CMP, Binop.Ble); }

    ">"
    { return symbol(CMP, Binop.Bgt); }

    ">="
    { return symbol(CMP, Binop.Bge); }

    "=="
    { return symbol(EQ); }

    "!="
    { return symbol(NEQ); }
 
    "!"
    { return symbol(NOT); }

    "||"
    { return symbol(BARBAR); }

    "&&"
    { return symbol(AMPAMP); }

    "int"
    { return symbol(INT); }

    "struct"
    { return symbol(STRUCT); }

    "if"
    { return symbol(IF); }

    "else"
    { return symbol(ELSE); }

    "while"
    { return symbol(WHILE); }

    "return"
    { return symbol(RETURN); }

    "sizeof"
    { return symbol(SIZEOF); }

    {Identifier}
    { return symbol(IDENT, yytext().intern()); }
    // The call to intern() allows identifiers to be compared using == .
    
    "0" {octal}+
    { return symbol(INTEGER, Integer.parseInt(yytext(), 8)); }

    "0x" {hexa}+
    { return symbol(INTEGER, Integer.decode(yytext())); }
 
    {Integer}
    { return symbol(INTEGER, Integer.parseInt(yytext())); }

    "'" {char} "'"
    { int c = yytext().charAt(1); return symbol(INTEGER, c); }

    "'\\n'"
    { return symbol(INTEGER, 10); }

    "'\\t'"
    { return symbol(INTEGER, 9); }

     "'\\''"
    { return symbol(INTEGER, 39); }

    "'\\\"'"
    { return symbol(INTEGER, 34); }

	"'\\x" {hexa} {hexa} "'"
	{ return symbol(INTEGER, decodeHexa(yytext())); }
	
    {Comment}
    { /* ignore */ }
    
    "/*"
    { yybegin(COMMENT); }

    {WhiteSpace}
    { /* ignore */ }

    .
    { throw new Exception (String.format(
        "Line %d, column %d: illegal character: '%s'\n", yyline, yycolumn, yytext()
      ));
    }

}

<COMMENT> {
      "*/"         { yybegin(YYINITIAL); } 
      {WhiteSpace} { /* ignore */ }
      .            { /* ignore */ }
      <<EOF>>    { throw new Exception(String.format(
        "Line %d, column %d: unclosed comment\n", yyline, yycolumn)); }
}
  

