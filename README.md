###CS 536 Course Projects
* /Project: Main Function for each project
* /SelfDefinedClasses: Self-defined classes

*Tips:*
When running it, use: <br/>
`java -cp .:bin:**/*.class Projects.P<number>`<br/>

Or you can compile by yourself: <br/>
`javac -sourcepath src -d bin src/**/*.java` then run it.

####Project 1
* Write a SymTable and test it
* Test if example's output is **exactly** the same with mine

####Project 2
* Edit CFlat.jlex
* Add test cases
* Extend P2.java to test

####Project 3
* Use Java Cup to write a parser for the C Flat. 
* Files need to change:
  * CFlat.cup: write the parse specification
  * ast.java: writhing the unparse method for the AST nodes
  * P3.java
  * test.cf: the input file to test implementation

####Project 4
* Modify the Sym class from program 1 (by including some new fields and methods and/or by defining some subclasses).
* Modify the IdNode class in ast.java (by including a new Sym field and by modifying its unparse method).
* Write a new main program, P4.java (an extension of P3.java).
* Modify the ErrMsg class.
* Update the Makefile used for program 3 to include any new rules needed for program 4.
* Write two test inputs: nameErrors.cf and test.cf to test your new code.

####Project 5
* Some type check

####Project 6
* Code generate
* Handle structs