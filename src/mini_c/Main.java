package mini_c;

public class Main {

  static boolean parse_only = false;
  static boolean type_only = false;
  static boolean interp_rtl = false;
  static boolean interp_ertl = false;
  static boolean debug = false;
  static String file = null;

  static void usage() {
    System.err.println("mini-c [OPTIONS] file.c\n\nOPTION =\n"+
    											  "         --debug\n"+
    											  "         --interp-rtl\n"+
    											  "         --interp-ertl\n"+
    											  "         --parse-only\n"+
    											  "         --type-only\n");
    System.exit(1);
  }

  public static void main(String[] args) throws Exception {
    for (String arg: args)
      if (arg.equals("--parse-only"))
        parse_only= true;
      else if (arg.equals("--type-only"))
        type_only = true;
      else if (arg.equals("--interp-rtl"))
        interp_rtl = true;
      else if (arg.equals("--interp-ertl"))
    	interp_ertl = true;
      else if (arg.equals("--debug"))
        debug = true;
      else {
        if (file != null) usage();
        if (!arg.endsWith(".c")) usage();
        file = arg;
      }
    if (file == null) usage ();

    java.io.Reader reader = new java.io.FileReader(file);
    Lexer lexer = new Lexer(reader);
    MyParser parser = new MyParser(lexer);
    Pfile f = (Pfile) parser.parse().value;
    if (parse_only) System.exit(0);
    Typing typer = new Typing();
    try {
        typer.visit(f);
        File tf = typer.getFile();
        if (type_only) System.exit(0);
        if (interp_rtl) {
        	RTLfile rtl = (new ToRTL()).translate(tf);
        	if (debug) {
        		rtl.print();
        	}
        	new RTLinterp(rtl);
        	System.exit(0);
        }
        if (interp_ertl) {
        	RTLfile rtl = (new ToRTL()).translate(tf);
        	ERTLfile ertl = (new ToERTL()).translate(rtl);
        	if (debug) {
        		rtl.print();
        		ertl.print();
        	}
        	new ERTLinterp(ertl);
        	System.exit(0);
        }
    } catch (Error e) {
        System.err.println(e);
    }
  }

}
