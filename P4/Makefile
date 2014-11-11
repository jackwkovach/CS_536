###
# This Makefile can be used to make a parser for the CFlat language
# (parser.class) and to make a program (P3.class) that tests the parser and
# the unparse methods in ast.java.
#
# make clean removes all generated files.
#
###

JC = javac
CP = /u/c/s/cs536-1/public/tools/deps:.

P4.class: P4.java parser.class Yylex.class ASTnode.class
	$(JC) -g -cp $(CP) P4.java

parser.class: parser.java ASTnode.class Yylex.class ErrMsg.class
	$(JC) -cp $(CP) parser.java

parser.java: CFlat.cup
	java -cp $(CP) java_cup.Main < CFlat.cup

Yylex.class: CFlat.jlex.java sym.class ErrMsg.class
	$(JC) -cp $(CP) CFlat.jlex.java

ASTnode.class: ast.java
	$(JC) -g -cp $(CP) ast.java

CFlat.jlex.java: CFlat.jlex sym.class
	java -cp $(CP) JLex.Main CFlat.jlex

sym.class: sym.java
	$(JC) -g -cp $(CP) sym.java

sym.java: CFlat.cup
	java -cp $(CP) java_cup.Main < CFlat.cup

ErrMsg.class: ErrMsg.java
	$(JC) -cp $(CP) ErrMsg.java

test:
	java -cp $(CP) P4 test.cf test_result.out
error:
	java -cp $(CP) P4 nameErrors.cf nameErrors_result.out

###
# clean
###
clean:
	rm -f *~ *.class parser.java CFlat.jlex.java sym.java
