package project;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class SymTable {
	private List<HashMap<String, Sym>> symTableList;
	/**
	 * Constructor. Initialize a list contains an empty symbol table
	 * 
	 */
	public SymTable(){
		HashMap<String, Sym> st = new HashMap<String, Sym>();
		symTableList= new LinkedList<HashMap<String, Sym>>();
		symTableList.add(st);
		
	}
	/**
	 * Add the given name and sym to the first HashMap in the list.
	 * @param name The name of the symbol
	 * @param sym  The symbol information
	 * @throws DuplicateSymException If the symtable has already contained the given name as a key
	 * @throws EmptySymTableException If the symtable is empty
	 */
	public void addDecl(String name, Sym sym) throws DuplicateSymException, EmptySymTableException{
		if(name == null || sym == null){
			throw new NullPointerException();
			
		}else if(symTableList.isEmpty()){
			throw new EmptySymTableException();
			
		}
		else{
			//check in the first symtable
			HashMap<String, Sym> st = symTableList.get(0);
			if(st.containsKey(name))
				throw new DuplicateSymException();
			else{
				st.put(name, sym);
				symTableList.set(0, st);
			}
		}
	}
	
	/**
	 * Add a new, empty HashMap to the front of the list.
	 */
	public void addScope(){
		HashMap<String, Sym> st = new HashMap<String, Sym>();
		symTableList.add(0, st);
	}
	
	/**
	 * @param name the search name of the symbol
	 * @return Associated Sym if the first HashMap in the list contains name as a key; otherwise, return null.
	 */
	public Sym lookupLocal(String name){
		if(symTableList.isEmpty() || name == null){
			return null;
		}else{
			return symTableList.get(0).get(name);

		}
	}
	/**
	 * 
	 * @param name the search name of the symbol
	 * @return Sym if symtable contains it, otherwise null.
	 */
	public Sym lookupGlobal(String name){
		if(symTableList.isEmpty() || name == null){
			return null;
		}else{
			// check all the hashmaps in the list
			Iterator<HashMap<String, Sym>> itr = symTableList.iterator();
			while(itr.hasNext()){
				HashMap<String, Sym> st = itr.next();
				if(st.containsKey(name))
					return st.get(name); // once find it, return the sym
			}
			// not found, return null
			return null;
		}
	}
	/**
	 * Remove the HashMap from the front of the list.
	 * @throws EmptySymTableException If this symtable's list is empty
	 */
	public void removeScope() throws EmptySymTableException{
		if(symTableList.isEmpty())
			throw new EmptySymTableException();
		else
			symTableList.remove(0);
	}
	/**
	 * This method is for debugging. First, print "\nSym Table\n". 
	 * Then, for each HashMap M in the list, print M.toString() followed by a newline. 
	 * Finally, print one more newline. All output should go to System.out.
	 */
	public void print(){
		System.out.print("\nSym Table\n");
		Iterator<HashMap<String, Sym>> itr = symTableList.iterator();
		while(itr.hasNext()){
			HashMap<String, Sym> M = itr.next();
			System.out.println(M.toString());
		}
		System.out.println();
	}
	
	/**
	 * Get the size of the symbolttable
	 * @return how many scopes in symTable
	 */
	public int size(){
		return symTableList.size();
	}
	
}
