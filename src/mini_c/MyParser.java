package mini_c;

import java_cup.runtime.*;

public class MyParser extends Parser {

	MyParser(Scanner scanner) {
		super(scanner);
	}

	public void report_error(String message, Object info) {
		// Override this method to be silent.
	}

	public void report_fatal_error(String message, Object info)
			throws Exception {
		// Override this method to be silent and throw an exception that
		// contains the error message.
		message = "Syntax error"; // discard message produced by CUP
		if (info instanceof Symbol) {
			Symbol symbol = (Symbol) info;
			message += String.format(" at line %d, column %d (%s)\n",
					symbol.left, symbol.right, showSymbol(symbol.sym));
		}
		throw new Exception(message);
	}

	String showSymbol(int token) {
		try {
			java.lang.reflect.Field[] classFields = sym.class.getFields();
			for (int i = 0; i < classFields.length; i++) {
				if (classFields[i].getInt(null) == token) {
					return classFields[i].getName();
				}
			}
		} catch (java.lang.IllegalAccessException e) {
		}
		throw new AssertionError(); // hopefully unreachable statement
	}

}
