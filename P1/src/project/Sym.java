package project;
/**
 * The Sym class is a data structure used for Symbol Table.
 * @author Junhan
 *
 */
public class Sym {
	private String symType;
	/**
	 * This is the constructor; it should initialize the Sym to have the given type.
	 * @param type
	 */
	public Sym(String type){
		this.symType = type;
	}
	/**
	 * Return this Sym's type.
	 * @return String The data type of this symbol
	 */
	public String getType(){
		return this.symType;
	}
	/**
	 * Return this Sym's type.
	 * @return String This is the override method to return the data type of this symbol
	 */
	public String toString(){
		return this.symType;
	}
}
