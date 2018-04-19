package fb1sap;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;

public class ZRFC_GSSD_READMSEG {
	public static List<List<String>> ListData = new ArrayList<List<String>>();
	public static String ErrorMsg = "";

	public static List<List<String>> getZRFC_GSSD_READMSEG(String plant, String partno, String mvt, String pono, String datefrom, String timefrom, String dateto, String timeto) throws JCoException, IOException {
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
		JCoFunction function = destination.getRepository().getFunction("ZRFC_GSSD_READMSEG");

		// 先檢查function
		System.out.println("先檢查function------------------------------------------");
		//CommonFunctions.checkFunction(function);

		// 傳入參數(字符串方式)
		JCoParameterList input = function.getImportParameterList();
		input.setValue("PLANT", plant);//工廠代碼
		input.setValue("PARTNO", partno);//料號
		input.setValue("MVT", mvt);//異動類型
		input.setValue("PONO", pono);//採購訂單號
		input.setValue("DATEFROM", datefrom);//異動起始日期
		input.setValue("TIMEFROM", timefrom);//異動起始時間
		input.setValue("DATETO", dateto);//異動截至日期
		input.setValue("TIMETO", timeto);//異動截至時間

		// 執行
		try {
			function.execute(destination);
		} catch (Exception e) {
			ErrorMsg = e.toString();
		}
		// PS:如果參數是一個結構，用參數名獲得一個對應類型的結構對(如果參數是table可查看)
		JCoTable table = function.getTableParameterList().getTable("OUT_TAB");

		// 先檢查table
		System.out.println("先檢查table----------------------------------------------");
		//CommonFunctions.checkReturnTable(table);

		// 印出回傳table(同時放入list)
		System.out.println("印出回傳table-------------------------------------------------------");
		ListData = CommonFunctions.printJCoTable(table);
//		ListData = CommonFunctions.printJCoTableAddlist(table);

		// 印出list
		// System.out.println("-------------------------------------------------------------------------");
		// System.out.println(ListData);
		return ListData;
		
//		// 待研究
//		// Move the table cursor to first row
//		table.firstRow();// 從首行開始重新遍歷 table.nextRow()：如果有下一行，下移一行並返回True
//		for (int i = 0; i < table.getNumRows(); i++, table.nextRow()) {
//			// 進一步獲取公司詳細信息(把回傳的參數再次塞入新參數)
//			function = destination.getRepository().getFunction("BAPI_COMPANYCODE_GETDETAIL");
//			if (function == null)
//				throw new RuntimeException("BAPI_COMPANYCODE_GETDETAIL not found in SAP.");
//			function.getImportParameterList().setValue("COMPANYCODEID", table.getString("COMP_CODE"));
//		}		 
	}

	public static void main(String[] args) throws JCoException, IOException {
		getZRFC_GSSD_READMSEG("AXVA", "", "", "", "20180303", "", "20180303", "");
		System.out.println(ListData.size());
		//寫入excel
		//CommonFunctions.writeExcel(ListData);
		//寫入txt
		// FileWriter writer = new FileWriter("output.txt");
		// for(List<String> str: ListData) {
		// writer.write(str.get(5));
		// }
		// writer.close();
	}
}
