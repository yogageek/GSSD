package fb1sap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class TestSap {
	public static List<List<String>> listData;
	
	// 連接屬性配置文件名，名稱可以隨便取
	static String ABAP_AS = "ABAP_AS_WITHOUT_POOL";
	static {
		Properties connectProperties = new Properties();		
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.134.28.56");//正式
		//connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.134.28.112");//測試
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "01");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "801");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "SFC");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "foxestar");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");	
		connectProperties.getProperty(DestinationDataProvider.JCO_CODEPAGE, "1100");		
//		connectProperties.setProperty(DestinationDataProvider.JCO_EXPIRATION_TIME, "9999999999"); 
//		connectProperties.setProperty(DestinationDataProvider.JCO_EXPIRATION_PERIOD, "9999999999"); 
//		connectProperties.setProperty(DestinationDataProvider.JCO_MAX_GET_TIME, "9999999999"); 
//		connectProperties.setProperty("jco.session_timeout","9999999999");
//		connectProperties.setProperty("jco.session_timeout.check_interval","9999999999");
		
		// 需要將屬性配置保存屬性文件，該文件的文件名為 ABAP_AS_WITHOUT_POOL.jcoDestination，
		// JCoDestinationManager.getDestination()調用時會需要該連接配置文件，後綴名需要為jcoDestination
		createDataFile(ABAP_AS, "jcoDestination", connectProperties);		
	}
	// 基於上面設定的屬性生成連接配置文件
	static void createDataFile(String name, String suffix, Properties properties) {
		File cfg = new File(name + "." + suffix);
		if (!cfg.exists()) {
			try {
				FileOutputStream fos = new FileOutputStream(cfg, false);
				properties.store(fos, "for tests only !");
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	// 到當前類所在目錄中搜索 ABAP_AS_WITHOUT_POOL.jcoDestination屬性連接配置文件，並根據文件中的配置信息來創建連接
	public static void connectWithoutPool() throws JCoException {		
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);// 只需指定文件名（不能帶擴展名jcoDestination名，會自動加上）
		System.out.println("**************Attributes:");
		// 調用destination屬性時就會發起連接，一直等待遠程響應
		System.out.println(destination.getAttributes());
	}
	//訪問結構 (Structure)
	public static void accessSAPStructure() throws JCoException {
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);
		JCoFunction function = destination.getRepository().getFunction("RFC_SYSTEM_INFO");// 從對象倉庫中獲取 RFM函數
		if (function == null)
			throw new RuntimeException("RFC_SYSTEM_INFO not found in SAP.");

		try {
			function.execute(destination);
		} catch (AbapException e) {
			System.out.println(e.toString());
			return;
		}

		JCoStructure exportStructure = function.getExportParameterList().getStructure("RFCSI_EXPORT");
		System.out.println("System info for " + destination.getAttributes().getSystemID() + ":\n");
		for (int i = 0; i < exportStructure.getMetaData().getFieldCount(); i++) {
			System.out.println(exportStructure.getMetaData().getName(i) + ":\t" + exportStructure.getString(i));
		}
		System.out.println();
	 
		// JCo still supports the JCoFields, but direct access via getXX is more
		// efficient as field iterator 也可以使用下面的方式來遍歷
		System.out.println("The same using field iterator: \nSystem info for "
				+ destination.getAttributes().getSystemID() + ":\n");
		for (JCoField field : exportStructure) {
			System.out.println(field.getName() + ":\t" + field.getString());
		}
		System.out.println();

		// *********也可直接通過結構中的字段名或字段所在的索引位置來讀取某個字段的值
		System.out.println("RFCPROTO:\t" + exportStructure.getString(0));
		System.out.println("RFCPROTO:\t" + exportStructure.getString("RFCPROTO"));		
	}	
	//測試
	public static void workWithTable() throws JCoException {
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);
		JCoFunction function = destination.getRepository().getFunction("BAPI_COMPANYCODE_GETLIST");// 從對象倉庫中獲取RFM 函數：獲取公司列表
		if (function == null)
			throw new RuntimeException("BAPI_COMPANYCODE_GETLIST not found in SAP.");
		try {
			function.execute(destination);
		} catch (AbapException e) {
			System.out.println(e.toString());
			return;
		}
//		JCoStructure returnStructure = function.getExportParameterList().getStructure("return");
//		// 判斷讀取是否成功
//		if (!(returnStructure.getString("TYPE").equals("") || returnStructure.getString("TYPE").equals("S"))) {
//			throw new RuntimeException(returnStructure.getString("MESSAGE"));
//		}
		// 獲取Table參數：COMPANYCODE_LIST
		JCoTable table = function.getTableParameterList().getTable("COMPANYCODE_LIST");
		for (int i = 0; i < table.getNumRows(); i++) {// 遍歷Table
			table.setRow(i);// 將行指針指向特定的索引行
			System.out.println(table.getString("COMP_CODE") + '\t' + table.getString("COMP_NAME"));
		}		
		System.out.println("******************************************");
		// move the table cursor to first row
		table.firstRow();// 從首行開始重新遍歷 table.nextRow()：如果有下一行，下移一行並返回True
		for (int i = 0; i < table.getNumRows(); i++, table.nextRow()) {
			// 進一步獲取公司詳細信息
			function = destination.getRepository().getFunction("BAPI_COMPANYCODE_GETDETAIL");
			if (function == null)
				throw new RuntimeException("BAPI_COMPANYCODE_GETDETAIL not found in SAP.");
			function.getImportParameterList().setValue("COMPANYCODEID", table.getString("COMP_CODE"));
		}
	}
	
	//物料庫存異動紀錄 ZRFC_GSSD_READMSEG
	public static List<List<String>> getZRFC_GSSD_READMSEG(String plant,String partno,String mvt,String pono,String datefrom,String timefrom,String dateto,String timeto) throws JCoException, UnsupportedEncodingException {
		//取得連線
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);
		//呼叫SAP方法
		JCoFunction function = destination.getRepository().getFunction("ZRFC_GSSD_READMSEG");		
		//傳入參數(字符串方式)
		JCoParameterList input = function.getImportParameterList();		
		input.setValue("PLANT", plant);                
        input.setValue("PARTNO", partno);
        input.setValue("MVT", mvt);
        input.setValue("PONO", pono);
        input.setValue("DATEFROM", datefrom);
        input.setValue("TIMEFROM", timefrom);
        input.setValue("DATETO", dateto);
        input.setValue("TIMETO", timeto);        
        if (function == null)
			throw new RuntimeException("ZRFC_GSSD_READMSEG not found in SAP.");
		try {
			//執行
			function.execute(destination);			
		} catch (AbapException e) {
			System.out.println(e.toString());			
		}
		
//		// 待研究		
//		JCoStructure returnStructure = function.getExportParameterList().getStructure("return");
//		// 判斷讀取是否成功
//		if (!(returnStructure.getString("TYPE").equals("") || returnStructure.getString("TYPE").equals("S"))) {
//			throw new RuntimeException(returnStructure.getString("MESSAGE"));
//		}
		
//		// 獲取此function會回傳哪些Table
//		System.out.println(CommonFunctions.getOutputJCoTableNames(function));
		
		// 獲取Table參數：OUT_TAB
		JCoTable table = function.getTableParameterList().getTable("OUT_TAB");			
		//--LinkedList linkst = new LinkedList();
		List<List<String>> listData=new ArrayList<List<String>>();
		for (int i = 0; i < table.getNumRows(); i++) {
			table.setRow(i);			
			JCoFieldIterator iter = table.getFieldIterator();
			//--LinkedHashMap map = new LinkedHashMap();
			List<String> list=new ArrayList<>();
			// get fields of current record
			while (iter.hasNextField()) {
				JCoField field = iter.nextField();				
				//--map.put(field.getName(), table.getValue(field.getName()));
				//如果為日期就轉換
				if (CommonFunctions.isValidDate(table.getValue(field.getName()).toString())) {
					list.add(CommonFunctions.transDate(table.getValue(field.getName()).toString()));
				} else {
					list.add(table.getValue(field.getName()).toString());
					//String s=new String(s.getBytes("CP950"),"UTF8");
					String str=table.getValue(field.getName()).toString();
					str=new String(str.getBytes("ISO8859-1"),"GBK");
					System.out.println(str);
					
//					byte[] s=table.getValue(field.getName()).toString().getBytes();
//					byte[] a=new String(s, "CP950").getBytes("utf-8");
					//String utf8String = new String(table.getValue(field.getName()).toString().getBytes("ISO-8859-1"), "UTF8"); 
//					System.out.println(utf8String);
					//s=new String(s.getBytes("CP950"),"UTF8");
				}				
			}
			listData.add(list);
		}
		System.out.println(listData);		
		System.out.println("**************獲取Table結束****************");
		//印出看
		CommonFunctions.printJCoTable(table);
		CommonFunctions.showTableStructure(table);
		//待研究
//		// Move the table cursor to first row
//		table.firstRow();// 從首行開始重新遍歷 table.nextRow()：如果有下一行，下移一行並返回True
//		for (int i = 0; i < table.getNumRows(); i++, table.nextRow()) {
//			// 進一步獲取公司詳細信息(把回傳的參數再次塞入新參數)
//			function = destination.getRepository().getFunction("BAPI_COMPANYCODE_GETDETAIL");
//		if (function == null)
//			throw new RuntimeException("BAPI_COMPANYCODE_GETDETAIL not found in SAP.");
//		function.getImportParameterList().setValue("COMPANYCODEID", table.getString("COMP_CODE"));
//		}		
		return listData;
	}
	
	public static List<List<String>> getZRFC_BOM_005(String datefrom,String dateto,String i_capid,String i_mehrs,String i_mtnrv,String i_stlan,String i_werks) throws JCoException{
		//取得連線
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);
		//呼叫SAP方法
		JCoFunction function = destination.getRepository().getFunction("ZRFC_BOM_005");		
//		System.out.println(function.getImportParameterList());        
		//傳入參數(字符串方式)
		JCoParameterList input = function.getImportParameterList();
		//import parameter of type structure
//		JCoStructure struc=input.getStructure(4);
//		System.out.println(struc);
//		JCoTable t = function.getTableParameterList().getTable("T_BOMITEM");//ZTBOMITEM1//T_BOMITEM
//		System.out.println(t);
		
		input.setValue("DATE_FROM", datefrom);
		input.setValue("DATE_TO", dateto);		                
        input.setValue("I_CAPID", i_capid);
        input.setValue("I_MEHRS", i_mehrs);
        input.setValue("I_MTNRV", i_mtnrv);
        input.setValue("I_STLAN", i_stlan);
        input.setValue("I_WERKS", i_werks);       
        
		if (function == null)
			throw new RuntimeException("ZRFC_GSSD_READMSEG not found in SAP.");
		try {
			// 執行
			function.execute(destination);
		} catch (AbapException e) {
			System.out.println(e.toString());
		}
		
		
//		// 待研究		
//		JCoStructure returnStructure = function.getExportParameterList().getStructure("return");
//		// 判斷讀取是否成功
//		if (!(returnStructure.getString("TYPE").equals("") || returnStructure.getString("TYPE").equals("S"))) {
//			throw new RuntimeException(returnStructure.getString("MESSAGE"));
//		}
		
//		// 獲取此function會回傳哪些Table
//		System.out.println(CommonFunctions.getOutputJCoTableNames(function));
		
		// 獲取Table參數：OUT_TAB
		JCoTable table = function.getTableParameterList().getTable("T_BOMITEM");
		//showTableStructure(table);
		
		//--LinkedList linkst = new LinkedList();
		List<List<String>> listData=new ArrayList<List<String>>();
		for (int i = 0; i < table.getNumRows(); i++) {
			table.setRow(i);			
			JCoFieldIterator iter = table.getFieldIterator();
			//--LinkedHashMap map = new LinkedHashMap();
			List<String> list=new ArrayList<>();
			// get fields of current record
			while (iter.hasNextField()) {
				JCoField field = iter.nextField();				
				//--map.put(field.getName(), table.getValue(field.getName()));
				//如果為日期就轉換
				if (CommonFunctions.isValidDate(table.getValue(field.getName()).toString())) {
					list.add(CommonFunctions.transDate(table.getValue(field.getName()).toString()));
				} else {
					list.add(table.getValue(field.getName()).toString());
				}
			}
			listData.add(list);
		}
		System.out.println(listData);		
		System.out.println("**************獲取ZRFC_BOM_005Table結束****************");
		//印出看
		CommonFunctions.printJCoTable(table);
		//待研究
//		// Move the table cursor to first row
//		table.firstRow();// 從首行開始重新遍歷 table.nextRow()：如果有下一行，下移一行並返回True
//		for (int i = 0; i < table.getNumRows(); i++, table.nextRow()) {
//			// 進一步獲取公司詳細信息(把回傳的參數再次塞入新參數)
//			function = destination.getRepository().getFunction("BAPI_COMPANYCODE_GETDETAIL");
//		if (function == null)
//			throw new RuntimeException("BAPI_COMPANYCODE_GETDETAIL not found in SAP.");
//		function.getImportParameterList().setValue("COMPANYCODEID", table.getString("COMP_CODE"));
//		}
		
		return listData;
	}

	public static void main(String[] args) throws JCoException, UnsupportedEncodingException {
//		connectWithoutPool();
//		System.out.println("-----------------------");
		accessSAPStructure();
//		System.out.println("-----------------------");
//		workWithTable();
//		connectWithoutPool();
//		getZRFC_GSSD_READMSEG("AXVA","677-00995CPI","102","","","","","");//Trying to access row values in a table which does not have any rows yet 
		getZRFC_GSSD_READMSEG("AXVA","","311","","20180101","","20180130","");//java抓到2筆 SAP抓到18筆
//		CommonFunctions.getFunctions(ABAP_AS);
//		byte[] latin1 = "´ú¸Õ"; 
//		byte[] utf8 = new String(latin1, "ISO-8859-1").getBytes("UTF-8"); 
//		s=new String(s.getBytes("CP950"),"UTF8");

		
		getZRFC_BOM_005("20180201","20180226","PP01","X","3C407PA/A","2","AXVA");
		
	}
}
