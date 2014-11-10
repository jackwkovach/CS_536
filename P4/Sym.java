import java.util.List;
import java.util.HashMap;

public class Sym {
    private String type;
    private HashMap<String, Sym> structVars;
    private boolean isStruct;

    
    public Sym(String type) {
        this.type = type;
	structVars = null;
	isStruct = false;
    }
    
    public void setStruct(boolean b){
	isStruct = b;
    }
    
    public boolean isStruct(){
	return isStruct;
    }

    public String getType() {
        return type;
    }
    
    public void setStructMap(HashMap<String, Sym> h){
	structVars = h;
    }

    public HashMap<String,Sym> getStructVars(){
	return structVars;
    }

    public void printStructVars(){
        System.out.print("\nStruct Vars\n");
	System.out.print(structVars.toString());
        System.out.println();
    }

    public String toString() {
	return type;
    }

}

