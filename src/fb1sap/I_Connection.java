package fb1sap;

import com.sap.conn.jco.JCoException;

public interface I_Connection {
	
	public static String ABAP_AS = "ABAP_AS_WITHOUT_POOL";

	public void getConnection()throws JCoException;	
	
	public void accessSAPStructure() throws JCoException;
	
}
