import java.util.List;
import java.util.HashMap;

public class Sym {
    private String type;
    private HashMap<String, Sym> structVars;
    private List<String> formalListVars;
    private boolean isStruct;
    private boolean isFunc;
    
    public Sym(String type) {
        this.type = type;
	structVars = null;
	isStruct = false;
	isFunc = false;
    }
    
    public void setStruct(boolean b){
	isStruct = b;
    }
    
    public boolean isStruct(){
	return isStruct;
    }

    public void setFunc(boolean b){
	isFunc = b;
    }

    public boolean isFunc(){
	return isFunc;
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

    public void setFormalList(List<String> fl){
	formalListVars = fl;
    }

    public List<String> getFormalListVars(){
	return formalListVars;
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

