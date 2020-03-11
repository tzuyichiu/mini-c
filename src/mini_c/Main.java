package mini_c;

import java.io.FileNotFoundException;

public class Main {

  static boolean parse_only = false;
  static boolean type_only = false;
  static boolean interp_rtl = false;
  static boolean interp_ertl = false;
  static boolean interp_ltl = false;
  static boolean debug = false;
  static String file = null;
  static String resultFile = null;

  static void usage() {
    System.err.println("mini-c [OPTIONS] file.c\n\nOPTION =\n"+
    											  "         --debug\n"+
    											  "         --interp-rtl\n"+
    											  "         --interp-ertl\n"+
    											  "         --interp-ltl\n"+
    											  "         -o name.s : specifies the output filename. Must finish with \".s\"\n"+
    											  "         --parse-only\n"+
    											  "         --type-only\n");
    System.exit(1);
  }

  public static void main(String[] args) throws Exception {
	  int count = 0;
	  for (String arg: args) {
		  if (arg.equals("--parse-only"))
			  parse_only= true;
		  else if (arg.equals("--type-only"))
			  type_only = true;
		  else if (arg.equals("--interp-rtl"))
			  interp_rtl = true;
		  else if (arg.equals("--interp-ertl"))
			  interp_ertl = true;
		  else if (arg.equals("--interp-ltl"))
			  interp_ltl = true;
		  else if (arg.equals("--debug"))
			  debug = true;
		  else if (arg.equals("-o")) {
			  try {    		  
				  resultFile = args[count + 1];
				  if (!resultFile.endsWith(".s")) usage();
			  }
			  catch (ArrayIndexOutOfBoundsException e) {
				  usage();
			  }
		  }
		  else {
			  if (file != null) usage();
			  if (!arg.endsWith(".c") && count == args.length-1) usage();
			  if (arg.endsWith(".c") && count == args.length-1) file = arg;
			  if (resultFile == null) resultFile = arg.substring(0, arg.length()-1) + "s";
		  }
		  count++;
	  }
    if (file == null) usage();
    try {    	
    	java.io.Reader reader = new java.io.FileReader(file);
    	Lexer lexer = new Lexer(reader);
    	MyParser parser = new MyParser(lexer);
    	Pfile f = (Pfile) parser.parse().value;
    	if (parse_only) System.exit(0);
    	Typing typer = new Typing();
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
    	if (interp_ltl) {
    		RTLfile rtl = (new ToRTL()).translate(tf);
    		ERTLfile ertl = (new ToERTL()).translate(rtl);
    		LTLfile ltl = (new ToLTL()).translate(ertl);
    		if (debug) {
    			rtl.print();
    			ertl.print();
    			ltl.print();
    		}
    		new LTLinterp(ltl);
    		System.exit(0);
    	}
    	else {
    		RTLfile rtl = (new ToRTL()).translate(tf);
    		ERTLfile ertl = (new ToERTL()).translate(rtl);
    		LTLfile ltl = (new ToLTL()).translate(ertl);
    		X86_64 asm = (new ToX86_64(resultFile)).translate(ltl);
    		if (debug) {
    			rtl.print();
    			ertl.print();
    			ltl.print();
    			asm.print();
    		}
    	}
    }
    catch (FileNotFoundException e) {
    	System.err.println("Unknown input file : "+file);
    	System.exit(2);
    }
  }
}
