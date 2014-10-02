import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol

/**
 * This program is to be used to test the Scanner.
 * This version is set up to test all tokens, but more code is needed to test 
 * other aspects of the scanner (e.g., input that causes errors, character 
 * numbers, values associated with tokens)
 */
public class P2 {
    public static void main(String[] args) throws IOException {
                                           // exception may be thrown by yylex
        // test all tokens
	int opNum = Integer.parseInt(args[0]);
	// see if every good token can be recognized
	switch(opNum){
	case 1:
	    testAllTokens("allTokens.in", "allTokens.out");
	    CharNum.num = 1;
	    break;

	case 2:
	    testID("IDTest.in", "IDTest.out");
	    CharNum.num = 1;
	    break;

	case 3:
	    testIntegerLiteral("IntegerLiteralTest.in", "IntegerLiteralTest.out");
	    CharNum.num = 1;
	    break;

	case 4:
	    testStringLiteral("StringLiteralTest.in", "StringLiteralTest.out");
	    CharNum.num = 1;
	    break;

	default:
	    // test all
	    testAllTokens("allTokens.in", "allTokens.out");
	    CharNum.num = 1;

	    testID("IDTest.in", "IDTest.out");
	    CharNum.num = 1;
	
	    testIntegerLiteral("IntegerLiteralTest.in", "IntegerLiteralTest.out");
	    CharNum.num = 1;

	    testStringLiteral("StringLiteralTest.in", "StringLiteralTest.out");
	    CharNum.num = 1;

	}
        // ADD CALLS TO OTHER TEST METHODS HERE
    }

    private static void testID(String fin, String fout) throws IOException {
        // open input and output files
	System.out.println("============================================");
	System.out.println("+             Test ID Cases                +");
	System.out.println("============================================");
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {

	    inFile = new FileReader(fin);
            outFile = new PrintWriter(new FileWriter(fout));
        } catch (FileNotFoundException ex) {
            System.err.println(fin +" not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println(fout + " cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
	    if(token.sym == sym.ID){
		String idVal = ((IdTokenVal)token.value).idVal;
		showTokenInfo("Identifier", idVal, token);
		outFile.println(idVal);
	    }
            token = scanner.next_token();
	}
        outFile.close();
    }

    private static void testIntegerLiteral(String fin, String fout) throws IOException {
        // open input and output files
	System.out.println("============================================");
	System.out.println("+        Test Integer Literal Cases        +");
	System.out.println("============================================");

        FileReader inFile = null;
        PrintWriter outFile = null;
        try {

	    inFile = new FileReader(fin);
            outFile = new PrintWriter(new FileWriter(fout));
        } catch (FileNotFoundException ex) {
            System.err.println(fin + " not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println(fout + " cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
	    if(token.sym == sym.INTLITERAL){
		int intVal = ((IntLitTokenVal)token.value).intVal;
		showTokenInfo("Integer Literal", Integer.toString(intVal),token);
                outFile.println(intVal);
	    }
            token = scanner.next_token();
	}
        outFile.close();
    }

    private static void testStringLiteral(String fin, String fout) throws IOException {
        // open input and output files
	System.out.println("============================================");
	System.out.println("+        Test String Literal Cases         +");
	System.out.println("============================================");

        FileReader inFile = null;
        PrintWriter outFile = null;
        try {

	    inFile = new FileReader(fin);
            outFile = new PrintWriter(new FileWriter(fout));
        } catch (FileNotFoundException ex) {
            System.err.println(fin + " not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println(fout + " cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {

	    if(token.sym == sym.STRINGLITERAL) {
		String strVal = ((StrLitTokenVal)token.value).strVal;
		showTokenInfo("String Literal", strVal, token);
                outFile.println(strVal);
	    }
            token = scanner.next_token();
	}
        outFile.close();
    }

    /**
     * testAllTokens
     *
     * Open and read from file allTokens.txt
     * For each token read, write the corresponding string to allTokens.out
     * If the input file contains all tokens, one per line, we can verify
     * correctness of the scanner by comparing the input and output files
     * (e.g., using a 'diff' command).
     */
    private static void testAllTokens(String fin, String fout) throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {

	    inFile = new FileReader(fin);
            outFile = new PrintWriter(new FileWriter(fout));
        } catch (FileNotFoundException ex) {
            System.err.println("File allTokens.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("allTokens.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {

	    String idVal;
            int intVal;
	    String strVal;

	    switch (token.sym) {
            case sym.BOOL:
		showTokenInfo("Reversed Word", "bool", token);
                outFile.println("bool"); 
                break;

	    case sym.INT:
		showTokenInfo("Reversed Word", "int", token);
                outFile.println("int");
                break;

            case sym.VOID:
		showTokenInfo("Reversed Word", "void", token);
                outFile.println("void");
                break;

            case sym.TRUE:
		showTokenInfo("Reversed Word", "true", token);
                outFile.println("true"); 
                break;

            case sym.FALSE:
		showTokenInfo("Reversed Word", "false", token);
                outFile.println("false"); 
                break;

            case sym.STRUCT:
		showTokenInfo("Reversed Word", "struct", token);
                outFile.println("struct"); 
                break;

            case sym.CIN:
		showTokenInfo("Reversed Word", "cin", token);
                outFile.println("cin"); 
                break;

            case sym.COUT:
		showTokenInfo("Reversed Word", "cout", token);
                outFile.println("cout");
                break;				

            case sym.IF:
		showTokenInfo("Reversed Word", "if", token);
                outFile.println("if");
                break;

            case sym.ELSE:
		showTokenInfo("Reversed Word", "else", token);
                outFile.println("else");
                break;
            case sym.WHILE:
		showTokenInfo("Reversed Word", "while", token);
                outFile.println("while");
                break;

            case sym.RETURN:
		showTokenInfo("Reversed Word", "return", token);
                outFile.println("return");
                break;

            case sym.ID:
		idVal = ((IdTokenVal)token.value).idVal;
		showTokenInfo("Identifier", idVal, token);
		outFile.println(idVal);
                break;
            case sym.INTLITERAL:  
		intVal = ((IntLitTokenVal)token.value).intVal;
		showTokenInfo("Integer Literal", Integer.toString(intVal),token);
                outFile.println(intVal);
                break;
            case sym.STRINGLITERAL: 
		strVal = ((StrLitTokenVal)token.value).strVal;
		showTokenInfo("String Literal", strVal, token);
                outFile.println(strVal);
                break;    

            case sym.LCURLY:
		showTokenInfo("Operand", "{", token);
                outFile.println("{");
                break;
            case sym.RCURLY:
		showTokenInfo("Operand", "}", token);
                outFile.println("}");
                break;
            case sym.LPAREN:
		showTokenInfo("Operand", "(", token);
                outFile.println("(");
                break;
            case sym.RPAREN:
		showTokenInfo("Operand", ")", token);
                outFile.println(")");
                break;
            case sym.SEMICOLON:
		showTokenInfo("Operand", ";", token);
                outFile.println(";");
                break;
            case sym.COMMA:
		showTokenInfo("Operand", ",", token);
                outFile.println(",");
                break;
            case sym.DOT:
		showTokenInfo("Operand", ".", token);
                outFile.println(".");
                break;
            case sym.WRITE:
		showTokenInfo("Operand", "<<", token);
                outFile.println("<<");
                break;
            case sym.READ:
		showTokenInfo("Operand", ">>", token);
                outFile.println(">>");
                break;				
            case sym.PLUSPLUS:
		showTokenInfo("Operand", "++", token);
                outFile.println("++");
                break;
            case sym.MINUSMINUS:
		showTokenInfo("Operand", "--", token);
                outFile.println("--");
                break;	
            case sym.PLUS:
		showTokenInfo("Operand", "+", token);
                outFile.println("+");
                break;
            case sym.MINUS:
		showTokenInfo("Operand", "-", token);
                outFile.println("-");
                break;
            case sym.TIMES:
		showTokenInfo("Operand", "*", token);
                outFile.println("*");
                break;
            case sym.DIVIDE:
		showTokenInfo("Operand", "/", token);
                outFile.println("/");
                break;
            case sym.NOT:
		showTokenInfo("Operand", "!", token);
                outFile.println("!");
                break;
            case sym.AND:
		showTokenInfo("Operand", "&&", token);
                outFile.println("&&");
                break;
            case sym.OR:
		showTokenInfo("Operand", "||", token);
                outFile.println("||");
                break;
            case sym.EQUALS:
		showTokenInfo("Operand", "==", token);
                outFile.println("==");
                break;
            case sym.NOTEQUALS:
		showTokenInfo("Operand", "!=", token);
                outFile.println("!=");
                break;
            case sym.LESS:
		showTokenInfo("Operand", "<", token);
                outFile.println("<");
                break;
            case sym.GREATER:
		showTokenInfo("Operand", ">", token);
                outFile.println(">");
                break;
            case sym.LESSEQ:
		showTokenInfo("Operand", "<=", token);
                outFile.println("<=");
                break;
            case sym.GREATEREQ:
		showTokenInfo("Operand", ">=", token);
                outFile.println(">=");
                break;
	    case sym.ASSIGN:
		showTokenInfo("Operand", "=", token);
                outFile.println("=");
                break;
	    default:
		outFile.println("UNKNOWN TOKEN");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }

    /**
       Helper
     */

    private static void showTokenInfo(String type, String val, Symbol token){
	    int tokenLine = ((TokenVal)token.value).linenum;
	    int tokenCharnum = ((TokenVal)token.value).charnum;

	    System.out.print(tokenLine +":"+tokenCharnum + " ***" + type.toUpperCase()+"***");
	    System.out.println("  content: "+ val);
    }


}
