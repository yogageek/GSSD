package fb1sap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;

public class ZRFC_WEBCTBS_CFA_GET_PO_01 {
	
	public static List<List<String>> ListData=new ArrayList<List<String>>();
	public static String SapReturnedTable;
	public static String ErrorMsg="";
	
	//如果RFC查詢回傳多個table就讓用戶自己指定table
	public static void setSapReturnTable(String value) {
		if (value == "header") {
			SapReturnedTable = "ZPMM";
		}
		if (value == "detail") {
			SapReturnedTable = "ZPMN";
		}
	}	
	
	public static List<List<String>> getZRFC_WEBCTBS_CFA_GET_PO_01(String inputtb,String aedatf, String aedatt, String ebelnf, String ebelnt, String werks) throws JCoException, UnsupportedEncodingException {		
		setSapReturnTable(inputtb);
		// 取得連線
		JCoDestination destination = null;
		I_Connection dao = new ConnectionDAO();
		dao.getConnection();
		try {					
			destination = JCoDestinationManager.getDestination(I_Connection.ABAP_AS);
		} catch (Exception e) {			
			ErrorMsg=e.toString();
		}	
		// 呼叫指定SAP RFC
		JCoFunction function = destination.getRepository().getFunction("ZRFC_WEBCTBS_CFA_GET_PO_01");
		
		//先檢查function		
		System.out.println("-------------------------------------------------------------------------");
		//CommonFunctions.checkFunction(function);		
		
		// 傳入參數(字符串方式) 
		/**在下面的示例代碼中，僅僅設置了一個最簡單的類型的參數。事實上，setValue方法有許多重載形式，允許設置各種複雜類型的參數，比如structure類型和table類型的參數*/
		JCoParameterList input = function.getImportParameterList();		
		input.setValue("AEDATF", aedatf);
		input.setValue("AEDATT", aedatt);
		input.setValue("EBELNF", ebelnf);
		input.setValue("EBELNT", ebelnt);
		input.setValue("WERKS", werks);	
		
		// set up table parameter  是設置table中的參數or新增一行?
//		JCoTable tDateRange = function.getTableParameterList().getTable("ZPMN");
//		tDateRange.appendRow();
//		tDateRange.setRow(0);  
//		tDateRange.setValue("PMN04", "677-09961BPI");		

		//執行
		try {
			function.execute(destination);
		} catch (Exception e) {			
			ErrorMsg=e.toString();
		}	
		
		//PS:如果參數是一個結構，用參數名獲得一個對應類型的結構對
		JCoTable table = function.getTableParameterList().getTable(SapReturnedTable);
		
		//先檢查table
		System.out.println("-------------------------------------------------------------------------");
		//CommonFunctions.checkReturnTable(table);
		
		//印出回傳table(同時放入list)
		System.out.println("-------------------------------------------------------------------------");
		//ListData=CommonFunctions.printJCoTableAddlist(table);
		ListData=CommonFunctions.printJCoTable(table);
		
		//印出list
		//System.out.println("-------------------------------------------------------------------------");
//		System.out.println(listData);
		return ListData;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, JCoException {
//		setSapReturnTable("header");
		getZRFC_WEBCTBS_CFA_GET_PO_01("header","20180327", "20180328", "", "", "AXVA");
		System.out.println(ListData);
	}
}
