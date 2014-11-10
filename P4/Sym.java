import java.util.List;
import java.util.ArrayList;

public class Sym {
    private String type;
    private List<String> structVars;
    
    public Sym(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public String toString() {
	return type;
    }

}