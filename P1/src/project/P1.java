///////////////////////////////////////////////////////////////////////////////
// Title:            CS536 Project 1
// Files:            P1.java
// Semester:         CS536 Fall 2014
//
// Author:           Junhan Zhu
// Email:            jzhu84@wisc.edu
// CS Login:         junhan
// Lecturer's Name:  Andrew J. Davidson
//

package project;
/**
 * <p>This class tests if the SymTable is functioning well.<p>
 * It tests:<p>
 * - Sym.getType() and Sym.toString()<p>
 * - SymTable Constructor<p>
 * - SymTable.addScope() <p>
 * - SymTable.removeScope() and its exceptions<p>
 * - SymTable.addDecl() and its exceptions<p>
 * - SymTable.lookupLocal() and SymTable.lookupGlobal() in an empty SymTable
 * - SymTable.lookupLocal() and SymTable.lookupGlobal() in an non-empty SymTable
 * @author Junhan
 *
 */
public class P1 {
	public static void main(String[] args){
		boolean isFail = false;
		// Test Sym
		Sym[] a = new Sym[5];
		
		a[0] = new Sym("int");
		a[1] = new Sym("float");
		a[2] = new Sym("double");
		a[3] = new Sym("char");
		a[4] = new Sym("string");
		
		System.out.println(" +++++----- Test Sym Class -----+++++ ");
		System.out.println("Create 5 Syms...");
		for(int i = 0; i < 5; i++){
			System.out.println("Sym Object" + i +": GetType: " + a[i].getType() + ", toString: " + a[i].toString());
		}
		System.out.println();
				
		// Test SymTable
		// Test Constructor of SymTable		
		System.out.println(" +++++----- Test SymTable constructor -----+++++ ");
		System.out.println("After constructing..");
		SymTable st = new SymTable();
		st.print();
		if(st.size() != 1){
			System.out.println("wrong number of hashMap when initilized, failed\n");
			isFail = true;
		}else{
			System.out.println("construct with an empty hashMap, passed\n");
		}
		
		// Test add some scope
		System.out.println(" +++++----- Test addScope()... (scope number = 3) -----+++++");
		System.out.print("Before:");st.print();
		for(int i = 0; i < 3; i++)
			st.addScope();
		
		if(st.size() != 4){
			System.out.println("Test: addScope() failed, wrong number\n");
			isFail = true;
		}else{
			System.out.print("After:");
			st.print();
			System.out.println("addScope() adds 3 scopes, passed\n");
		}

		// Test delete socpes and get emptySymTableExeception
		System.out.println(" +++++----- Test removeScopes and throw exception -----+++++ ");
		System.out.print("Before:");st.print();
		try{
			for(int i = 0; i < 5; i++)
				st.removeScope();
			// this line should not be executed
			isFail = true;
			System.out.println("Test: throw emptySymTableExeception, no exception throws, failed");
		}catch(EmptySymTableException est){
			System.out.print("After remove 5 scopes:");st.print();
			System.out.println("throw emptySymTableExeception, passed");

		}
		
		// Test emptySymTableException when add declaration to an empty SymTable
		System.out.println(" +++++----- Test addDecl() when SymTable is empty -----+++++ ");
		st.print();
		try{
			st.addDecl("V1", a[0]);
			isFail = true;
			System.out.println("add when SymTable is empty, no exception throws, failed");
		}catch(EmptySymTableException e){
			System.out.println("add when SymTable is empty, EmptySymTableException throwed, passd\n");
		} catch (DuplicateSymException e) {
			// TODO Auto-generated catch block
			System.out.println("add when SymTable is empty, wrong exception throws, failed");
		}
		
		//Test NullPointException when add declaration(Sym) with a name of null point
		System.out.println(" +++++----- Test addDecl() when name is null -----+++++ ");
		try{
			st.addDecl(null, a[0]);
			isFail = true;
			System.out.println("add String of null, no exception throws, failed");
		}catch(EmptySymTableException e){
			System.out.println("add String of null, wrong exception throws, failed\n");
		} catch (DuplicateSymException e) {
			// TODO Auto-generated catch block
			System.out.println("add String of null, wrong exception throws, failed\n");
		}catch(NullPointerException e){
			System.out.println("add String of null, NullpointException throws, passed\n");
		}
		
		// Test lookup in an empty SymTable
		System.out.print(" +++++----- Test lookup in an empty SymTable -----+++++");
		st.print();
		if(st.lookupGlobal("V1") == null)
			System.out.println("lookupGlobal(V1) in empty SymTable is null, passed");
		else
			System.out.println("lookupGlobal in empty SymTable, failed");

		if(st.lookupLocal("V1") == null)
			System.out.println("lookupLocal(V1) in empty SymTable is null, passed\n");
		else
			System.out.println("Test: lookupLocal in empty SymTable, failed");
				
		// Test DuplicationSymException
		System.out.print(" +++++----- Test duplication detect -----+++++ ");
		st.addScope();
		try{
			st.addDecl("V1", a[0]);
			st.print();
			
			st.addDecl("V1", a[1]);
			isFail = true;
			System.out.println("detect duplication. no exception throws, failed");
		}catch(EmptySymTableException e){
			isFail = true;
			System.out.println("wrong exception throws, failed");
		} catch (DuplicateSymException e) {
			System.out.println("detect duplication when adding another V1, Passed\n");
		}
	
		// Test lookup in a non-empty SymTable
		System.out.println(" +++++----- Test lookup in a non-empty SymTable -----+++++ ");
		try{
			st.addDecl("V2", a[1]);
			st.addDecl("V3", a[2]);
			st.addScope();
			st.addDecl("V1",a[3]);
			st.addDecl("V4", a[4]);
			System.out.println("Initial SymTable as follows:");
			st.print();
			
			Sym result1 = st.lookupGlobal("V1");
			Sym result2 = st.lookupGlobal("Wired");
			if(result1 != null && result2 == null){
				if(result1.getType().equals("char")){
					System.out.println("lookupGlobal(\"V1\") in non-empty SymTable, "+"result: " + result1.toString()+", passed");
					System.out.println("lookupGlobal(\"Wired\") in non-empty SymTable, "+"result: null, passed");
				}else{
					System.out.println("lookupGlobal in non-empty SymTable, unmatch, failed");
					isFail = true;
				}
			}else{
				System.out.println("lookupGlobal in non-empty SymTable, wrong result, failed");
				isFail = true;
			}
			
			result1 = st.lookupLocal("V4");
			result2 = st.lookupLocal("V3");
			if(result1 != null && result2 == null){
				if(result1.getType().equals("string")){
					System.out.println("lookupLocal(\"V4\") in non-empty SymTable, "+"result: " + result1.toString()+", passed");
					System.out.println("lookupLocal(\"V3\") in non-empty SymTable, "+"result: null, passed");
				}else{
					System.out.println("lookupLocal in non-empty SymTable, unmatch, failed");
					isFail = true;
				}
			}else{
				System.out.println("lookupLocal in non-empty SymTable, wrong result, failed");
				isFail = true;
			
			}
			
		}catch(EmptySymTableException e){
			System.out.println("lookLocal, no emptySymTableExceptions should be throwed, failed");
		} catch (DuplicateSymException e) {
			System.out.println("lookLocal, no DuplicatieExceptions should be throwed, failed");
		}
		
		if(isFail)
			System.out.print("\nOverview: Failed\n");
		else
			System.out.print("\nOverview: Passed\n");
	
	}
}
