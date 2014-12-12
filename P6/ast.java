import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Mini program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);
    public void codeGen(PrintWriter p){}

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }

    /**
     * Helper in ASTnode
     * usage: echo(msg)
     */
    protected void echo(String msg){
	System.out.println("*ECHO*: " + msg);
    }
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);

	// after return, the symTable is filled
	// check if have a main entry
	SemSym s = symTab.lookupGlobal("main");
	if(s != null && s instanceof FnSym){
	    // it's fine
	}else{
	    ErrMsg.fatal(0,0,"No main function");
	}

    }
    
    public boolean typeCheck(){
    	// TODO: You'll have to change this
	return myDeclList.typeCheck();
    }

    public void codeGen(PrintWriter p){
	myDeclList.codeGen(p);
	System.out.println("\n***DONE***\n***Assembly code generated successfully in test_out.s***");

	if(p!= null)
	    p.close();
    }
    
    public void unparse(PrintWriter p, int indent) {
	echo("unparse called");
        myDeclList.unparse(p, indent);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
	// declListSize = 0;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the 
     * decls in the list.
     */    
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
		// declListSize += 4;
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }    

    public boolean typeCheck(){
    	// TODO: You'll have to change this
	boolean result = true;
	for(DeclNode node : myDecls){
	    if(node instanceof FnDeclNode){
		if(((FnDeclNode)node).typeCheck() == false){
		    result = false;
		}
	    }else{
		continue;
	    }
	}
	return result;
    }

    public int markOffset(int start){
	for (DeclNode node : myDecls) {
	    start = node.markOffset(start);
        }

	return start;
    }

    public void codeGen(PrintWriter p){
	for(DeclNode node : myDecls){
	    node.codeGen(p);
	}
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
    // private int declListSize;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
	int formalOffset = 0;
        for (FormalDeclNode node : myFormals) {
            SemSym sym = node.nameAnalysis(symTab);
            if (sym != null) {
		// mark offset of each variables in formalList for codeGen
		sym.size = 4;
		sym.offset = formalOffset; // mark variable's offset
		// fs.formalSpace = formalOffset + 4; // set parameter's space
		formalOffset -= 4;

                typeList.add(sym.getType());
            }
        }
        return typeList;
    }    
    
    /**
     * Return the number of formals in this list.
     */
    public int length() {
        return myFormals.size();
    }
    
    public List<FormalDeclNode> getFormalList(){
	return myFormals;
    }

    public void codeGen(PrintWriter p){
	int offset = 8;
	for(FormalDeclNode n : myFormals){
	    offset += 4;
	}
	Codegen.p = p;
	Codegen.generate("addu", "$fp", "$sp", Integer.toString(offset));
 
   }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab, int formalOffset) {
        myDeclList.nameAnalysis(symTab);

	/**
	 * offset start from -<paramete size + 8>
	 */
	int offset = formalOffset - 8;
	offset = myDeclList.markOffset(offset);

        myStmtList.nameAnalysis(symTab);
	offset = myStmtList.markOffset(offset);

	// echo("offset -->" + offset + "formalOffset -->" + formalOffset);

	/**
	 * Thus the local space for local parameters is the -offset - <parameter size + 8>
	 */
	this.localSpace = formalOffset - offset - 8;
    }    

    public boolean typeCheck(TypeNode rTypeNode){
	return myStmtList.typeCheck(rTypeNode);
    }

    public void codeGen(PrintWriter p, String exitLab){
	// set space for local variables
	// List dl = myDeclList.getDeclList();
	Codegen.p = p;
	if(this.localSpace > 0) // only add this command when there are variables declared
	    Codegen.generate("subu", "$sp", "$sp", this.localSpace);
	// each stmtnode handles itself
	myStmtList.codeGen(p, exitLab);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
    private int localSpace;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }    
    
    public boolean typeCheck(TypeNode rTypeNode){
	boolean result = true;
	for(StmtNode node : myStmts){
	    if(node.typeCheck(rTypeNode) == false)
		result = false;
	}
	return result;
    }

    public int markOffset(int start){
	for (StmtNode node : myStmts) {
            start = node.markOffset(start);
        }
	return start;
    }

    public void codeGen(PrintWriter p, String exitLab){
	for(StmtNode sn : myStmts){
	    if(sn instanceof ReturnStmtNode){
		((ReturnStmtNode)sn).codeGen(p, exitLab);
	    }else{
		sn.codeGen(p);
	    }
	}
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }
    
    public List<ExpNode> getCallExpList(){
	return myExps;
    }

    public void codeGen(PrintWriter p){
	for(ExpNode en : myExps)
	    en.codeGen(p);
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a sym
     */
    abstract public SemSym nameAnalysis(SymTable symTab);

    public int markOffset(int start){return start;}
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /**
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     */
    public SemSym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public SemSym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        SemSym sym = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (myType instanceof StructNode) {
            structId = ((StructNode)myType).idNode();
            sym = globalTab.lookupGlobal(structId.name());
            
            // if the name for the struct type is not found, 
            // or is not a struct type
            if (sym == null || !(sym instanceof StructDefSym)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(), 
                             "Invalid name of struct type");
                badDecl = true;
            }
            else {
		structId.link(sym);
            }
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (myType instanceof StructNode) {
                    sym = new StructSym(structId);
		    sym.size = structId.sym().size;
                }
                else {
                    sym = new SemSym(myType.type());
		    sym.size = 4;
                }

		// this section mark each variable as global or local
		if(symTab.whichScopeLevel() == 1){
		    sym.isGlobal = true;
		}else{
		    // this section add an offset to each variable for generating code
		    // no need for global variables as they are referred directly
		    // sym.offset = 8 + 4 * symTab.variableInScope();
		}

		// if(sym.isGlobal)
		//     echo("varDecl: " + myId.name() + " -> Global");
		// else
		//     echo("varDecl: " + myId.name() + " -> Local");
		// echo("varDecl: " + myId.name() + " offset -> " + sym.offset);

                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    

    public int markOffset(int start){
	SemSym s = myId.sym();
	int size = 0;
	if(s instanceof StructSym){
	    SemSym tempSym = ((StructSym)s).getStructType().sym();
	    // echo("struct ----> " + myId.name() + " -offset: " + start);
	    // this is the offset of the start of a struct declaration
	    size = tempSym.size;
	    s.offset = start;  

	    // traverse every id inside the struct and mark them with an offset
	    SymTable structSymTab = ((StructDefSym)tempSym).getSymTable();
	    // echo("fields contents: ");

	    //mark each field
	    HashMap<String, SemSym> fields = structSymTab.getField();
	    int innerOffset = 0;
	    Iterator it = fields.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry pairs = (Map.Entry)it.next();
		SemSym fieldSym = (SemSym)pairs.getValue();

		fieldSym.structOffset = innerOffset;
		// fieldSym.offset = start + innerOffset;
		// echo(pairs.getKey()+ " offset: "+ fieldSym.offset + " -innerOffset: " + fieldSym.structOffset);

		innerOffset -= fieldSym.size;
	    }
	    // accumlate size and mark each offset

	}else{ // not struct, maybe int or bool
	    s.offset = start; 
	    size = 4;
	}

	return start - size;
}

    public void codeGen(PrintWriter p){
	SemSym s = myId.sym();
	if(s.isGlobal){
	    p.println("\t.data");
	    p.println("\t\t.align 2");
	    // mySym.offset = 4;
	    // p.println("\t_" + myStrVal + ":\t" + ".space " + mySym.offset);
	    p.println("\t_" + myId.name() + ":\t" + ".space " + "4");
	}
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        // p.print(myId.name());
	myId.unparse(p,0);
        p.println(";");
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
	// localVarSize = 0;
	// formalVarSize = 0;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FnSym sym = null;

        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                         "Multiply declared identifier");
        }
        
        else { // add function name to local symbol table
            try {
                sym = new FnSym(myType.type(), myFormalsList.length());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }
	
	// parameters size
	sym.formalSpace = typeList.size()*4;
	// echo("after fomralAnalysis, parameter space: " + 
	//      Integer.toString(sym.formalSpace));

	// process the function body and mark localSpace needed
        myBody.nameAnalysis(symTab, sym.formalSpace*(-1)); 

        try {
            symTab.removeScope();  // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }
        
        return null;
    }    
    
    public boolean typeCheck(){
	TypeNode rTypeNode = myType;
	// check fnBody
	return myBody.typeCheck(rTypeNode);
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t# FUNCTION ENTRY");

	if(myId.name().equals("main")){
	    p.print("\t.text\n" + "\t.globl main\n" + "main:");
	    // p.println("\t\t# METHOD ENTRY\n" + "\t__start:");
	    p.println("\t\t# METHOD ENTRY");
	}else{
	    p.print("\t.text\n" + "_"+myId.name()+":");
	    p.println("\t\t# METHOD ENTRY");
	}

	Codegen.p = p;
	Codegen.genPush("$ra");
	Codegen.genPush("$fp");
	// assembly for formalList
	myFormalsList.codeGen(p);

	// assembly for fnBody
	String exitLab = "_"+myId.name()+"_Exit";
	myBody.codeGen(p, exitLab); // handle declList Only, others let stmtNode itself handle

	p.println("\t\t# FUNCTION EXIT");
	// exit arguments depends on formalList, need offsets from formalslist and myBody
	// p.println("_"+myId.name()+"_Exit:");
	Codegen.genLabel(exitLab);
	int raOffset = ((FnSym)myId.sym()).formalSpace * (-1);
	int fpOffset = raOffset - 4;
	// space for parameters
	Codegen.generateIndexed("lw", "$ra", "$fp", raOffset, "get ra");
	Codegen.generateWithComment("move", "save control link", "$t0", "$fp");
	// space for local variables
	Codegen.generateIndexed("lw", "$fp", "$fp", fpOffset, "restore FP");
	Codegen.generateWithComment("move", "restore SP", "$sp", "$t0");

	if(myId.name().equals("main")){
	    Codegen.generateWithComment("li", "load exit code for syscall","$v0", "10");
	    Codegen.generate("syscall");
	}else{
	    Codegen.generateWithComment("jr","retrun","$ra");
	}

    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        SemSym sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                sym = new SemSym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void codeGen(PrintWriter p){

    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        // p.print(myId.name());
	myId.unparse(p,0);

    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this struct
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }

        SymTable structSymTab = new SymTable();
        
        // process the fields of the struct
	// mark the offset of each fields inside the struct
        myDeclList.nameAnalysis(structSymTab, symTab);
	// get the size

        if (!badDecl) {
            try {   // add entry to symbol table
                StructDefSym sym = new StructDefSym(structSymTab);
		sym.size = 0 - myDeclList.markOffset(0);
		// echo("declared struct with size: " + sym.size);

                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return null;
    }    
    
    public void codeGen(PrintWriter p){

    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("};\n");

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }
    
    /**
     * type
     */
    public Type type() {
        return new StructType(myId);
    }
    
    public void codeGen(PrintWriter p){

    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }
    
    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);
    public boolean typeCheck(TypeNode r){ return false;}
    public int markOffset(int start){return start;}
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }
    
    public boolean typeCheck(TypeNode r){
	Type t = myAssign.typeCheck();
	if( t instanceof ErrorType)
	    return false;
	else
	    return true;
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#ASSIGN");
	myAssign.codeGen(p);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public boolean typeCheck(TypeNode r){
	Type t = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	if(t instanceof ErrorType){
	    return false;
	}

	if(!(t instanceof IntType)){
	    i.typeCheckError("Arithmetic operator applied to non-numeric operand");
	    return false;
	}else{
	    return true;
	}
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#POST-INCREMENT");
	myExp.codeGen(p);
	Codegen.p = p;
	Codegen.genPop("$t0");
	Codegen.generate("addi", "$t0", "$t0", "1");
	Codegen.genPush("$t0");

	Codegen.generateIndexed("lw", "$t0", "$sp", 4, "peek");
	if(myExp instanceof IdNode){
	    SemSym s = ((IdNode)myExp).sym();
	    if(s.isGlobal){
		Codegen.generate("sw", "$t0", "_"+((IdNode)myExp).name());
	    }else{
		Codegen.generateIndexed("sw", "$t0", "$fp", (s.offset));
	    }
	}else if(myExp instanceof DotAccessExpNode){
	    Codegen.generateIndexed("sw", "$t0", "$fp", ((DotAccessExpNode)myExp).unrollDot());
	}

    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public boolean typeCheck(TypeNode r){
	Type t = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	if(t instanceof ErrorType)
	    return false;
	
	if(!(t instanceof IntType)){
	    i.typeCheckError("Arithmetic operator applied to non-numeric operand");
	    return false;
	}else{
	    return true;
	}
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#POST-DECRESEMENT");
	// myExp.codeGen(p);
	myExp.codeGen(p);
	Codegen.p = p;
	Codegen.genPop("$t0");
	Codegen.generate("addi", "$t0", "$t0", "-1");
	Codegen.genPush("$t0");

	Codegen.generateIndexed("lw", "$t0", "$sp", 4, "peek");
	if(myExp instanceof IdNode){
	    SemSym s = ((IdNode)myExp).sym();
	    if(s.isGlobal){
		Codegen.generate("sw", "$t0", "_"+((IdNode)myExp).name());
	    }else{
		Codegen.generateIndexed("sw", "$t0", "$fp", s.offset);
	    }
	}else if(myExp instanceof DotAccessExpNode){
	    Codegen.generateIndexed("sw", "$t0", "$fp", ((DotAccessExpNode)myExp).unrollDot());
	}
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }    

    public boolean typeCheck(TypeNode r){
	Type t = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	if(t instanceof ErrorType){
	    return false;
	}
	if(t instanceof FnType){
	    i.typeCheckError("Attempt to read a function");
	    return false;
	}
	if(t instanceof StructDefType){
	    i.typeCheckError("Attempt to read a struct name");
	    return false;
	}
	
	if(t instanceof StructType){// id or other legal type
	    i.typeCheckError("Attempt to read a struct variable");
	    return false;
	}
	return true;
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#READ");
	Codegen.p = p;
	Codegen.generate("li", "$v0", "5");
	Codegen.generate("syscall");

	Codegen.genPush("$v0");
	Codegen.generateIndexed("lw", "$t0", "$sp", 4, "peek");
	if(myExp instanceof IdNode){
	    SemSym s = ((IdNode)myExp).sym();
	    if(s.isGlobal){
		Codegen.generate("sw", "$t0", "_"+((IdNode)myExp).name());
	    }else{
		Codegen.generateIndexed("sw", "$t0", "$fp", s.offset);
	    }
	}else if(myExp instanceof DotAccessExpNode){
	    Codegen.generateIndexed("sw", "$t0", "$fp", ((DotAccessExpNode)myExp).unrollDot());
	}	
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public boolean typeCheck(TypeNode r){
	Type t = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	// echo("writing: " + t.toString());
	writeType = t;

	if(t instanceof ErrorType){
	    return false;
	}
	if(t instanceof FnType){
	    i.typeCheckError("Attempt to read a function");
	    return false;
	}
	if(t instanceof StructDefType){
	    i.typeCheckError("Attempt to read a struct name");
	    return false;
	}
	
	if(t instanceof StructType){// id or other legal type
	    i.typeCheckError("Attempt to read a struct variable");
	    return false;
	}
	
	if(t instanceof VoidType){
	    i.typeCheckError("Attempt to write void");
	    return false;
	}
	return true;
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#WRITE");
	myExp.codeGen(p);
	Codegen.p = p;
	Codegen.genPop("$a0");
	if(writeType instanceof IntType){
	    Codegen.generate("li", "$v0", "1");
	}else if(writeType instanceof StringType){
	    Codegen.generate("li", "$v0", "4");
	}else if(writeType instanceof BoolType){
	    Codegen.generate("li", "$v0", "1");
	}else{
	    ErrMsg.fatal(0,0,"unkonwn error in writestmt while generating code" + writeType);
	    System.exit(-1);
	}
	Codegen.generate("syscall");

    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp;
    private Type writeType;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }

    public boolean typeCheck(TypeNode r){
	boolean result = true;
	Type condT = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	if(!(condT instanceof BoolType)){
	    i.typeCheckError("Non-bool expression used as a if condition");
	    result = false;
	}

	return result && myStmtList.typeCheck(r);
    }
    
    public int markOffset(int start){
	start = myDeclList.markOffset(start);
	start = myStmtList.markOffset(start);
	return start;
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#IF COND");
	String trueLab = Codegen.nextLabel();
	myExp.codeGen(p); // evaluate myexp
	// pop out 
	// p.println("\t\t#IF STMT");
	Codegen.p = p;
	Codegen.genPop("$t0");
	Codegen.generate("beq", "$t0", "0", trueLab);
	// myDeclList.codeGen(p);
	myStmtList.codeGen(p, trueLab);

	Codegen.genLabel(trueLab, "if(alone) is ended");	
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public boolean typeCheck(TypeNode r){
	boolean result = true;
	Type condT = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	if(!(condT instanceof BoolType)){
	   i.typeCheckError("Non-bool expression used as a if condition");
	    result = false;
	}
	result = result && myThenStmtList.typeCheck(r);
	return result && myElseStmtList.typeCheck(r);
    }

    public int markOffset(int start){
	start = myThenDeclList.markOffset(start);
	start = myThenStmtList.markOffset(start);
	start = myElseDeclList.markOffset(start);
	start = myElseStmtList.markOffset(start);
	return start;
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t# IF-ELSE COND");
	String trueLab = Codegen.nextLabel();
	String doneLab = Codegen.nextLabel();
	myExp.codeGen(p);

	Codegen.p = p;
	Codegen.genPop("$t0");
	Codegen.generate("beq", "$t0", "0", trueLab);
	// myThenDeclList.codeGen(p);
	myThenStmtList.codeGen(p, trueLab);
	Codegen.generate("b",doneLab);
	Codegen.genLabel(trueLab);
	// myElseDeclList.codeGen(p);
	myElseStmtList.codeGen(p, trueLab);
	Codegen.genLabel(doneLab);

    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public boolean typeCheck(TypeNode r){
	boolean result = true;
	Type condT = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	if(!(condT instanceof BoolType)){
	    i.typeCheckError("Non-bool expression used as a while condition");
	    result = false;
	}
	return result && myStmtList.typeCheck(r);
    }

    public int markOffset(int start){
	start = myDeclList.markOffset(start);
	start = myStmtList.markOffset(start);
	return start;
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#WHILE COND");
	String trueLab = Codegen.nextLabel();
	String doneLab = Codegen.nextLabel();
	Codegen.p = p;
	Codegen.genLabel(doneLab);
	myExp.codeGen(p);
	// get the final result and evaluate it
	Codegen.genPop("$t0");
	Codegen.generate("beq", "$t0", "0", trueLab);
	// myDeclList.codeGen(p);
	myStmtList.codeGen(p, trueLab);
	Codegen.generate("b", doneLab);
	Codegen.genLabel(trueLab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }

    public boolean typeCheck(TypeNode r){
	Type t = myCall.typeCheck();
	IdNode i = myCall.getExpFirstIdNode();
	if(t instanceof ErrorType){
	    return false;
	}else{
	    return true;
	}
    }

    public void codeGen(PrintWriter p){
	myCall.codeGen(p);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    public boolean typeCheck(TypeNode rTypeNode){
	Type rType = rTypeNode.type();
	if(myExp == null){
	    if(!(rType instanceof VoidType)){
		ErrMsg.fatal(0,0,"Missing return value");
		return false;
	    }
	    return true;
	}else{
	    Type t = myExp.typeCheck();
	    IdNode i = myExp.getExpFirstIdNode();
	    if(rType instanceof VoidType){
		i.typeCheckError("Return with a value in a void function");
		return false;
	    }else{
		if(t instanceof ErrorType){
		    return false;
		}else{
		    if(t.toString().equals(rType.toString()))
			return true;
		    else{
			i.typeCheckError("Bad return value");
			return false;
		    }
		}
	    }
	}
    }

    public void codeGen(PrintWriter p, String exitLab){
	p.println("\t\t#RETURN");
	if(myExp != null){
	    myExp.codeGen(p);
	    Codegen.p = p;
	    Codegen.genPop("$v0");
	}

	Codegen.generate("b", exitLab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) { }
    abstract public Type typeCheck();
    abstract public IdNode getExpFirstIdNode();
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public IdNode getExpFirstIdNode(){
	IdNode i = new IdNode(myLineNum, myCharNum, "int");
	SemSym s = new SemSym(new IntType());
	i.link(s);
	return i;
    }

    public Type typeCheck(){
	return new IntType();
    }

    public void codeGen(PrintWriter p){
	Codegen.p = p;
	Codegen.generate("li","$t0",Integer.toString(myIntVal));
	Codegen.genPush("$t0");

    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public IdNode getExpFirstIdNode(){
	IdNode i = new IdNode(myLineNum, myCharNum, "string");
	SemSym s = new SemSym(new StringType());
	i.link(s);
	return i;
    }

    public Type typeCheck(){
	return new StringType();
    }

    public String stringContent(){
	return myStrVal;
    }

    public void codeGen(PrintWriter p){
	Codegen.p = p;
	String stringLab = Codegen.nextLabel();
	p.println("\t.data");
	p.println(stringLab + ":\t.asciiz  " + myStrVal );
	p.println("\t.text");
	
	Codegen.generate("la","$t0",stringLab);
	Codegen.genPush("$t0");

    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public IdNode getExpFirstIdNode(){
	IdNode i = new IdNode(myLineNum, myCharNum, "true");
	SemSym s = new SemSym(new BoolType());
	i.link(s);
	return i;
    }

    public Type typeCheck(){
	return new BoolType();
    }

    public void codeGen(PrintWriter p){
	Codegen.generate("li","$t0","1");
	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public IdNode getExpFirstIdNode(){
	IdNode i = new IdNode(myLineNum, myCharNum, "false");
	SemSym s = new SemSym(new BoolType());
	i.link(s);
	return i;
    }

    public Type typeCheck(){
	return new BoolType();
    }

    public void codeGen(PrintWriter p){
	Codegen.generate("li","$t0","0");
	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;

	this.offset = 0;
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(SemSym sym) {
        mySym = sym;
    }
    
    /**
     * Return the name of this ID.
     */
    public String name() {
        return myStrVal;
    }
    
    /**
     * Return the symbol associated with this ID.
     */
    public SemSym sym() {
        return mySym;
    }
    
    /**
     * Return the line number for this ID.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this ID.
     */
    public int charNum() {
        return myCharNum;
    }    
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     */
    public void nameAnalysis(SymTable symTab) {
        SemSym sym = symTab.lookupGlobal(myStrVal);
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }
    
    public IdNode getExpFirstIdNode(){
	return this;
    }

    public void typeCheckError(String msg){
	ErrMsg.fatal(myLineNum, myCharNum, msg);
    }

    public Type typeCheck(){
	return mySym.getType();
    }

    public void genJumpAndLink(){

    }

    public void codeGen(PrintWriter p){
	Codegen.p = p;
	if(mySym.isGlobal){
	// global: use _name
	    Codegen.generateWithComment("lw", "load global var", "$t0", "_"+ myStrVal) ;
	}else{
	// local: use -offset($fp)
	    Codegen.generateIndexed("lw", "$t0", "$fp", mySym.offset, "load local var");
	}
	    Codegen.genPush("$t0");
    }

    public void genAddr(){

    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("(" + mySym + ")");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private SemSym mySym;

    public int offset; // for code gen
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;    
        myId = id;
        mySym = null;

	this.dotLeftOffset = 0;
	this.dotRightOffset = 0;
    }

    /**
     * Return the symbol associated with this dot-access node.
     */
    public SemSym sym() {
        return mySym;
    }    
    
    /**
     * Return the line number for this dot-access node. 
     * The line number is the one corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return myId.lineNum();
    }
    
    /**
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return myId.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a struct type, set the sym for this node so that
     *   a dot-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymTab = null; // to lookup RHS of dot-access
        SemSym sym = null;

        myLoc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode)myLoc;
            sym = id.sym();
            // check ID has been declared to be of a struct type

            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof StructSym) { 
                // get symbol table for struct type
                SemSym tempSym = ((StructSym)sym).getStructType().sym();
                structSymTab = ((StructDefSym)tempSym).getSymTable();
		// this.dotLeftOffset = sym.offset;
		// echo("accessing: " + id.name() + " offset: " + this.dotLeftOffset);

		id.link(sym); 
            } 
            else {  // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Dot-access of non-struct type");
                badAccess = true;
            }
        }
        
        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLoc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode)myLoc;

            if (loc.badAccess) {  // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            }
            else { //  no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) {  // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Dot-access of non-struct type");
                    badAccess = true;
                }
                else {  // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym)sym).getSymTable();

			// this.dotLeftOffset = loc.dotRightOffset;

                    }
                    else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {
            sym = structSymTab.lookupGlobal(myId.name()); // lookup
            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                             "Invalid struct field name");
                badAccess = true;
            }
            
            else {
		// this.dotRightOffset = this.dotLeftOffset + sym.structOffset;

		// echo("accessing: " + myId.name() + " -offset " + this.dotRightOffset);
                myId.link(sym);  // link the symbol
		// myId.offset = sym.offset;
                // if RHS is itself as struct type, link the symbol for its struct 
                // type to this dot-access node (to allow chained dot-access)

                if (sym instanceof StructSym) {
                    mySym = ((StructSym)sym).getStructType().sym();
                }
            }
        }

    }    

    public IdNode getExpFirstIdNode(){
	return myId;
	// if(myLoc instanceof DotAccessExpNode)
	//     return ((DotAccessExpNode)myLoc).getExpFirstIdNode();
	// else // my loC is also an IdNode
	//     return (IdNode)myLoc;
    }
    
    public Type typeCheck(){
	return myId.sym().getType();
    }

    public int unrollDot(){
	if(myLoc instanceof IdNode){
	    return ((IdNode)myLoc).sym().offset + ((IdNode)myId).sym().structOffset;
	}else if(myLoc instanceof DotAccessExpNode){
	    DotAccessExpNode DA = (DotAccessExpNode)myLoc;
	    // return DA.unrollDot() + DA.getExpFirstIdNode().sym().structOffset;
	    return DA.unrollDot() + ((IdNode)myId).sym().structOffset;
	}else{
	    // error
	    echo("error in unrolling dot");
	    return -1;
	}
    }


    public void codeGen(PrintWriter p){
	// echo("codegen in dot: " + myId.name() + ": " +myId.offset);
	// need access again..
	this.dotRightOffset = unrollDot();

	Codegen.p = p;
	Codegen.generateIndexed("lw", "$t0", "$fp", this.dotRightOffset, "load struct field: " + myId.name());
	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        myLoc.unparse(p, 0);
        p.print(".");
	myId.sym().offset = dotRightOffset;
        myId.unparse(p, 0);
    }

    // 2 kids
    private ExpNode myLoc;    
    private IdNode myId;
    private SemSym mySym;          // link to Sym for struct type
    private boolean badAccess;  // to prevent multiple, cascading errors

    public int dotLeftOffset;
    public int dotRightOffset;
    
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }

    public IdNode getExpFirstIdNode(){
	return myLhs.getExpFirstIdNode();
    }
    
    public Type typeCheck(){
	Type lType = myLhs.typeCheck();
	Type rType = myExp.typeCheck();
	// function, struct name, struct variable check
	IdNode i = myLhs.getExpFirstIdNode();
	// echo("Check Assign: lType: " + lType + " rType: " + rType);

	if(lType instanceof FnType && rType instanceof FnType){
	    i.typeCheckError("Function assignment");
	    return new ErrorType();
	}
	
	if(lType instanceof StructDefType && rType instanceof StructDefType){
	    i.typeCheckError("Struct name assignment");
	    return new ErrorType();
	}
	
	if(lType instanceof StructType && rType instanceof StructType){
	    i.typeCheckError("Struct variable assignment");
	    return new ErrorType();
	}
	
	if(lType instanceof ErrorType || rType instanceof ErrorType){
	    return new ErrorType();
	}else{
	    String lTypeName = lType.toString();
	    String rTypeName = rType.toString();
	    if(lTypeName.equals(rTypeName))
		return lType;
	    else{
		i.typeCheckError("Type mismatch");
		return new ErrorType();
	    }
	}

    }

    public void codeGen(PrintWriter p){
	myExp.codeGen(p);
	Codegen.p = p;
	Codegen.generateIndexed("lw", "$t0", "$sp", 4, "peek");

	if(myLhs instanceof IdNode){
	    // assign to IdNode
	    SemSym s = ((IdNode)myLhs).sym();
	    if(s.isGlobal){
		Codegen.generate("sw", "$t0", "_"+((IdNode)myLhs).name());
	    }else{
		Codegen.generateIndexed("sw", "$t0", "$fp", ((IdNode)myLhs).sym().offset);
	    }

	}else if(myLhs instanceof DotAccessExpNode){
	    // assign to struct access
	    Codegen.generateIndexed("sw", "$t0", "$fp", ((DotAccessExpNode)myLhs).unrollDot());
	    
	}else{
	    ErrMsg.fatal(0,0,"Unexpected error in codeGen of IDNode");
	}
	// pop
	Codegen.genPop("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }    

    public IdNode getExpFirstIdNode(){
	return myId;
    }
    
    public Type typeCheck(){
	// search symTable to get FnSym
	// compare myExplist see if the calling arguments number and type are correct
	Type fnType = myId.sym().getType();
	boolean result = true;
	if(!(fnType instanceof FnType)){
	    myId.typeCheckError("Attempt to call a non-function");
	    result = false;
	}else{ // it is a function call
	    FnSym fs = (FnSym)myId.sym();
	    List<Type> declFormal = fs.getParamTypes();
	    List<ExpNode> callFormal = myExpList.getCallExpList();
	    if(declFormal.size() != callFormal.size()){
		myId.typeCheckError("Function call with wrong number of args");
		result = false;
	    }else{ // check each type 
		int s = declFormal.size();
		for(int i = 0; i < s; i++){
		    Type callType = callFormal.get(i).typeCheck();
		    Type declType = declFormal.get(i);

		    String callTypeName = callType.toString();
		    String declTypeName = declType.toString();
		    if(!(callTypeName.equals(declTypeName))){
			IdNode fId = callFormal.get(i).getExpFirstIdNode();
			fId.typeCheckError("Type of actual does not match type of formal");
			result = false;
		    }
		}
	    }
	}

	if(fnType instanceof FnType)
	    return ((FnSym)(myId.sym())).getReturnType();
	else
	    return new ErrorType();
	// if(result == true){
	//     return ((FnSym)(myId.sym())).getReturnType();
	// }else{
	//     return new ErrorType();
	// }

    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#CALL");
	myExpList.codeGen(p);
	// then jump
	Codegen.p = p;
	Codegen.generate("jal","_"+myId.name());
	// push the result for other use
	Codegen.genPush("$v0");
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public IdNode getExpFirstIdNode(){
	return myExp.getExpFirstIdNode();
    }
    
    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }

    public IdNode getExpFirstIdNode(){
	return myExp1.getExpFirstIdNode();
    }    

    protected Type checkArithmetic(ExpNode lExp, ExpNode rExp){
	Type lType = lExp.typeCheck();
	Type rType = rExp.typeCheck();
	// echo("checking arithmetic..");
	boolean result = true;

	if(!(lType instanceof ErrorType)){
	    if(!(lType instanceof IntType)){
		IdNode i = lExp.getExpFirstIdNode();
		i.typeCheckError("Arithmetic operator applied to non-numeric operand");
		result = false;
	    }
	}else{
	    result = false;
	}

	if(!(rType instanceof ErrorType)){
	    if(!(rType instanceof IntType)){
		IdNode i = rExp.getExpFirstIdNode();
		i.typeCheckError("Arithmetic operator applied to non-numeric operand");
		result = false;
	    }
	}else{
	    result = false;
	}
	if(result == true)
	    return new IntType();
	else
	    return new ErrorType();
    }
    
    protected Type checkLogical(ExpNode lExp, ExpNode rExp){
	Type lType = lExp.typeCheck();
	Type rType = rExp.typeCheck();
	boolean result = true;

	if(!(lType instanceof ErrorType)){
	    if(!(lType instanceof BoolType)){
		IdNode i = lExp.getExpFirstIdNode();
		i.typeCheckError("Logical operator applied to non-bool operand");
		result = false;
	    }
	}else{
	    result = false;
	}

	if(!(rType instanceof ErrorType)){
	    if(!(rType instanceof BoolType)){
		IdNode i = rExp.getExpFirstIdNode();
		i.typeCheckError("Logical operator applied to non-bool operand");
		result = false;
	    }
	}else{
	    result = false;
	}

	if(result == true)
	    return new BoolType();
	else
	    return new ErrorType();


    }

    protected Type checkRelation(ExpNode lExp, ExpNode rExp){
	Type lType = lExp.typeCheck();
	Type rType = rExp.typeCheck();
	boolean result = true;
	if(!(lType instanceof ErrorType)){
	    if(!(lType instanceof IntType)){
		IdNode i = lExp.getExpFirstIdNode();
		i.typeCheckError("Relational operator applied to non-numeric operand");
		result = false;
	    }
	}else{
	    result = false;
	}

	if(!(rType instanceof ErrorType)){
	    if(!(rType instanceof IntType)){
		IdNode i = rExp.getExpFirstIdNode();
		i.typeCheckError("Relational operator applied to non-numeric operand");
		result = false;
	    }
	}else{
	    result = false;
	}
	if(result == true)
	    return new BoolType();
	else
	    return new ErrorType();

    }

    protected Type checkEquality(ExpNode lExp, ExpNode rExp){
	Type lType = lExp.typeCheck();
	Type rType = rExp.typeCheck();
	IdNode i = lExp.getExpFirstIdNode();

	if(lType instanceof VoidType && rType instanceof VoidType){
	    i.typeCheckError("Equality operator applied to void functions");
	    return new ErrorType();
	}

	if(lType instanceof StructDefType && rType instanceof StructDefType){
	    i.typeCheckError("Equality operator applied to struct names");
	    return new ErrorType();
	}

	if(lType instanceof StructType && rType instanceof StructType){
	    i.typeCheckError("Equality operator applied to struct variables");
	    return new ErrorType();
	}

	if(lType instanceof FnType && rType instanceof FnType){
	    i.typeCheckError("Equality operator applied to functions");
	    return new ErrorType();
	}

	if(lType instanceof ErrorType || rType instanceof ErrorType){
	    return new ErrorType();
	}else{
	    String lTypeName = lType.toString();
	    String rTypeName = rType.toString();
	    if(lTypeName.equals(rTypeName))
		return new BoolType();
	    else{
		i.typeCheckError("Type mismatch");
		return new ErrorType();
	    }
	}
    }

    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public Type typeCheck(){
	Type t = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	if(t instanceof ErrorType){
	    return new ErrorType();
	}else if(!(t instanceof IntType)){
	    i.typeCheckError("Arithmetic operator applied to non-numeric operand");
	    return new ErrorType();
	}else{
	    return t;
	}
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public Type typeCheck(){
	Type t = myExp.typeCheck();
	IdNode i = myExp.getExpFirstIdNode();
	if(t instanceof ErrorType){
	    return new ErrorType();
	}else if(!(t instanceof BoolType)){
	    i.typeCheckError("Logical operator applied to non-bool operand");
	    return new ErrorType();
	}else{
	    return t;
	}
    }

    public void codeGen(PrintWriter p){
	myExp.codeGen(p);

	Codegen.p = p;
	Codegen.genPop("$t0");

	Codegen.generate("seq", "$t0", "$t0", "0");

	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkArithmetic(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	myExp2.codeGen(p);
	// pop, add and push
	Codegen.p = p;
	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("add", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkArithmetic(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	myExp2.codeGen(p);
	// pop, minus and push
	Codegen.p = p;
	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("sub", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");

    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkArithmetic(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	myExp2.codeGen(p);
	// pop, multiply and push
	Codegen.p = p;
	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("mulo", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkArithmetic(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	myExp2.codeGen(p);
	// pop, divide and push
	Codegen.p = p;
	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("div", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkLogical(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	// pop, if it is 0, no need to do further
	Codegen.p = p;
	Codegen.genPop("$t0");
	String trueLab = Codegen.nextLabel();
	String doneLab = Codegen.nextLabel();
	Codegen.generate("beq", "$t0", "0", trueLab);
	
	myExp2.codeGen(p);
	Codegen.generate("b", doneLab);

	Codegen.genLabel(trueLab, "&& left is false, push false");
	Codegen.genPush("$t0");
	Codegen.genLabel(doneLab, "&& is done");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkLogical(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	// pop, if it is 1, no need to do further
	Codegen.p = p;
	Codegen.genPop("$t0");
	String trueLab = Codegen.nextLabel();
	String doneLab = Codegen.nextLabel();
	Codegen.generate("beq", "$t0", "1", trueLab);
	
	myExp2.codeGen(p);
	Codegen.generate("b", doneLab);

	Codegen.genLabel(trueLab, "|| left is true, push true");
	Codegen.genPush("$t0");
	Codegen.genLabel(doneLab, "|| is done");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkEquality(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#EQUALITY");
	Codegen.p = p;
	// string comparison
	if(myExp1 instanceof StringLitNode && myExp2 instanceof StringLitNode){
	    String se1 = ((StringLitNode)myExp1).stringContent();
	    String se2 = ((StringLitNode)myExp2).stringContent();
	    if(se1.equals(se2)){
		// load t1 t2 with equal value
		Codegen.generate("li","$t0",0);
		Codegen.genPush("$t0");
		Codegen.generate("li","$t1",0);
		Codegen.genPush("$t1");
	    }else{
		Codegen.generate("li","$t0",1);
		Codegen.genPush("$t0");
		Codegen.generate("li","$t1",0);
		Codegen.genPush("$t1");
		// load t1 t2 with different value
	    }

	}else{
	    myExp1.codeGen(p);
	    myExp2.codeGen(p);
	}
	// pop, compare and push

	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("seq", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");

    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkEquality(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	p.println("\t\t#NOT-EQUALITY");
	Codegen.p = p;
	if(myExp1 instanceof StringLitNode && myExp2 instanceof StringLitNode){
	    String se1 = ((StringLitNode)myExp1).stringContent();
	    String se2 = ((StringLitNode)myExp2).stringContent();
	    if(se1.equals(se2)){
		// load t1 t2 with equal value
		Codegen.generate("li","$t0",0);
		Codegen.genPush("$t0");
		Codegen.generate("li","$t1",0);
		Codegen.genPush("$t1");
	    }else{
		Codegen.generate("li","$t0",1);
		Codegen.genPush("$t0");
		Codegen.generate("li","$t1",0);
		Codegen.genPush("$t1");
		// load t1 t2 with different value
	    }

	}else{

	    myExp1.codeGen(p);
	    myExp2.codeGen(p);
	}
	    // pop, compare and push

	    Codegen.genPop("$t1");
	    Codegen.genPop("$t0");
	    Codegen.generate("sne", "$t0", "$t0", "$t1");

	    Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkRelation(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	myExp2.codeGen(p);

	// pop, compare and push
	Codegen.p = p;
	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("slt", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkRelation(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	myExp2.codeGen(p);

	// pop, compare and push
	Codegen.p = p;
	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("sgt", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkRelation(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	myExp2.codeGen(p);

	// pop, compare and push
	Codegen.p = p;
	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("sle", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public Type typeCheck(){
	return checkRelation(myExp1, myExp2);
    }

    public void codeGen(PrintWriter p){
	myExp1.codeGen(p);
	myExp2.codeGen(p);

	// pop, compare and push
	Codegen.p = p;
	Codegen.genPop("$t1");
	Codegen.genPop("$t0");
	Codegen.generate("sge", "$t0", "$t0", "$t1");

	Codegen.genPush("$t0");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
