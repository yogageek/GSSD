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

public class ZRFC_BOM_005 {
	public static List<List<String>> ListData = new ArrayList<List<String>>();
	public static String ErrorMsg = "";

	public static List<List<String>> getZRFC_BOM_005(String datefrom, String dateto, String i_capid, String i_mehrs, String i_mtnrv, String i_stlan, String i_werks) throws JCoException {
		// 取得連線
		JCoDestination destination = null;
		I_Connection dao = new ConnectionDAO();
		dao.getConnection();
		try {
			destination = JCoDestinationManager.getDestination(I_Connection.ABAP_AS);
		} catch (Exception e) {
			ErrorMsg = e.toString();
		}
		// 呼叫指定SAP RFC
		JCoFunction function = destination.getRepository().getFunction("ZRFC_BOM_005");
		
		// 先檢查function
		System.out.println("先檢查function------------------------------------------");
		// CommonFunctions.checkFunction(function);
		
		 // 傳入參數(字符串方式)
		JCoParameterList input = function.getImportParameterList();
		input.setValue("DATE_FROM", datefrom);
		input.setValue("DATE_TO", dateto);
		input.setValue("I_CAPID", i_capid);
		input.setValue("I_MEHRS", i_mehrs);
		input.setValue("I_MTNRV", i_mtnrv);
		input.setValue("I_STLAN", i_stlan);
		input.setValue("I_WERKS", i_werks);
		// 執行
		try {
			function.execute(destination);
		} catch (Exception e) {
			ErrorMsg = e.toString();
		}
		// PS:如果參數是一個結構，用參數名獲得一個對應類型的結構對(如果參數是table可查看)
		JCoTable table = function.getTableParameterList().getTable("T_BOMITEM");

		// 先檢查table
		System.out.println("先檢查table----------------------------------------------");
		// CommonFunctions.chec kReturnTable(table);

		// 印出回傳table(同時放入list)
		System.out.println("印出回傳table-------------------------------------------------------");
		ListData = CommonFunctions.printJCoTable(table);

		// 印出list
		// System.out.println("-------------------------------------------------------------------------");
		// System.out.println(ListData);
		return ListData;
	}	
	
	public static void main(String[] args) throws UnsupportedEncodingException, JCoException {
		getZRFC_BOM_005("20180220", "20180226", "PP01", "X", "3C407PA/A", "2", "AXVA");
		System.out.println(ListData);
	}
}
