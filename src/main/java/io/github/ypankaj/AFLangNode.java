package io.github.ypankaj;

import java.util.Arrays;

public class AFLangNode {
	public String label;
	public String entityName;
	public String[] uoi;
	
	public AFLangNode(String label, String entityName, String[] uoi) {
		this.label = label;
		this.entityName = entityName;
		this.uoi = uoi;
	}
	
	@Override public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        
        AFLangNode objNode = (AFLangNode) obj;
        
        if (Helper.logicalOperators.contains(objNode.label)) {
        	return false;
        }
 
        // check if all properties are equal or not
        return this.label.equals(objNode.label)
            && this.entityName.equals(objNode.entityName)
            && Arrays.equals(this.uoi, objNode.uoi);
    }
}
