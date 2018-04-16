package fb1sap;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class ConnectionDAO implements I_Connection {
	//getConnection
	@Override
	public void getConnection() {
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.134.28.56");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "01");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "801");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "sfc");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "foxestar");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "ZH");
		connectProperties.getProperty(DestinationDataProvider.JCO_CODEPAGE,"8400");//糂礮
//		connectProperties.setProperty(DestinationDataProvider.JCO_EXPIRATION_TIME, "9999999999");
//		connectProperties.setProperty(DestinationDataProvider.JCO_EXPIRATION_PERIOD, "9999999999");
//		connectProperties.setProperty(DestinationDataProvider.JCO_MAX_GET_TIME, "9999999999");
//		connectProperties.setProperty("jco.session_timeout", "9999999999");
//		connectProperties.setProperty("jco.session_timeout.check_interval", "9999999999");

		// 需要將屬性配置保存屬性文件，該文件的文件名為 ABAP_AS_WITHOUT_POOL.jcoDestination，
		// JCoDestinationManager.getDestination()調用時會需要該連接配置文件，後綴名需要為jcoDestination
		createDataFile(ABAP_AS, "jcoDestination", connectProperties);
		System.out.println("ConnectionDAO implements I_Connection got Connection");
	}
	//把連線做成文件
	public static void createDataFile(String name, String suffix, Properties properties) {
		File cfg = new File(name + "." + suffix);
		//if (!cfg.exists()) {//如果文件存在就不再產生文件覆蓋
			try {
				FileOutputStream fos = new FileOutputStream(cfg, false);
				properties.store(fos, "for tests only !");
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		//}
	}
	//RFC_SYSTEM_INFO
	@Override
	public void accessSAPStructure() throws JCoException {
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);
		JCoFunction function = destination.getRepository().getFunction("RFC_SYSTEM_INFO");// 從對象倉庫中獲取RFM函數
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
	//ABAP_AS=connectWithoutPool
	public static void connectWithoutPool() throws JCoException {
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);// 只需指定文件名（不能帶擴展名jcoDestination名，會自動加上）
		System.out.println("**************Attributes**************:");
		// 調用destination屬性時就會發起連接，一直等待遠程響應
		System.out.println(destination.getAttributes());
	}
	
	// 訪問結構 (Structure)
	//............	

	public static void main(String[] args) throws JCoException {
		ConnectionDAO con = new ConnectionDAO();
		con.accessSAPStructure();
	}

}
