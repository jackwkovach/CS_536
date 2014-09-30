###
# This Makefile can be used to make a scanner for the CFlat language
# (Yylex.class) and to make a program that tests the scanner (P2.class).
#
# The default makes both the scanner and the test program.
#
# make clean removes all generated files.
#
# Note: P2.java will not compile unless Yylex.class exists.
#
###

# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = ~cs536-1/public/tools/deps:.

P2.class: P2.java Yylex.class sym.class
	$(JC) $(FLAGS) P2.java

Yylex.class: CFlat.jlex.java ErrMsg.class sym.class
	$(JC) $(FLAGS) CFlat.jlex.java

CFlat.jlex.java: CFlat.jlex sym.class
	java -cp $(CP) JLex.Main CFlat.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java
###
# testing - add more here to run your tester and compare its results
# to expected results
###
test:
	java -cp ~cs536-1/public/tools/deps:. P2 5
	diff -c allTokens.in allTokens.out
	diff -c IDTest.out IDTest_expect.out 
	diff -c IntegerLiteralTest.out IntegerLiteralTest_expect.out
	diff -c StringLiteralTest.out StringLiteralTest_expect.out

tokenTest:
	java -cp ~cs536-1/public/tools/deps:. P2 1
	diff -c allTokens.in allTokens.out

idTest:
	java -cp ~cs536-1/public/tools/deps:. P2 2
	diff -c IDTest.out IDTest_expect.out 

intTest:
	java -cp ~cs536-1/public/tools/deps:. P2 3
	diff -c IntegerLiteralTest.out IntegerLiteralTest_expect.out

strTest:
	java -cp ~cs536-1/public/tools/deps:. P2 4
	diff -c StringLiteralTest.out StringLiteralTest_expect.out

###
# clean up
###

clean:
	rm -f *~ *.class CFlat.jlex.java

cleantest:
	rm -f allTokens.out
