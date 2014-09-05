package Projects;

import selfDefinedClasses.EmptySymTableException;
import selfDefinedClasses.SymTable;

/**
 * This class tests if the symtable is functioning well
 * @author junhan
 *
 */
public class P1 {
	public static void main(String[] args){
		SymTable st = new SymTable();
		try{
			st.addScope();			st.print();
			st.removeScope();			st.print();
			st.removeScope();			st.print();
			st.removeScope();
			st.print();
		}catch(EmptySymTableException est){
			System.out.println("The SymTable is empty");

		}finally{
			System.out.print("finish");
		}
	}
}
