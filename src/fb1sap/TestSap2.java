package fb1sap;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;



import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRecordMetaData;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class TestSap2 {
	// 連接屬性配置文件名，名稱可以隨便取
	static String ABAP_AS = "ABAP_AS_WITHOUT_POOL";
	static {
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.134.28.112");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "801");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "SFC");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "foxestar");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");
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
	
	public static void connectWithoutPool() throws JCoException {
		// 到當前類所在目錄中搜索 ABAP_AS_WITHOUT_POOL.jcoDestination
		// 屬性連接配置文件，並根據文件中的配置信息來創建連接
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);// 只需指定文件名（不能帶擴展名jcoDestination名，會自動加上）
		System.out.println("**************Attributes:");
		// 調用destination屬性時就會發起連接，一直等待遠程響應
		System.out.println(destination.getAttributes());
	}
	
	public void getCompanyCodeDetail(String companycodeid) throws JCoException {
		// JCoDestination instance represents the backend SAP system
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);

		// JCoFunction instance is the FM in SAP we will use
		JCoRepository repository = destination.getRepository();
		JCoFunction fm = repository.getFunction("BAPI_COMPANYCODE_GETDETAIL");
		if (fm == null) {
			throw new RuntimeException("Function does not exists in SAP system.");
		}

		// set import parameter(s)
		fm.getImportParameterList().setValue("COMPANYCODEID", companycodeid);

		// call function
		fm.execute(destination);

		// get company code detail from exporting parameter 'COMPANYCODE_DETAIL'
		JCoStructure cocdDetail = fm.getExportParameterList().getStructure("COMPANYCODE_DETAIL");
		//outputParams = jCoFunction.getExportParameterList();  
		//通過 SAP 提供的結構（structure）\表格（table）和 字段 來獲取 數據，可在此封裝成對象返回。 
		this.printStructure(cocdDetail);
	} 
	//方法1
	private void printStructure(JCoStructure jcoStru) {
		for (JCoField field : jcoStru) {
			System.out.println(String.format("%s\\t%s", field.getName(), field.getString()));
		}
	}
	//方法2
	private void printStructure2(JCoStructure jcoStructure) {
		for (int i = 0; i < jcoStructure.getMetaData().getFieldCount(); i++) {
			System.out.println(
					String.format("%s\\t%s", jcoStructure.getMetaData().getName(i), jcoStructure.getString(i)));
		}
	}

	public static void main(String[] args) throws JCoException {
		
	}
}
