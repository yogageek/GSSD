package fb1sap;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

public class RFC_export_file_sample {
	static String ABAP_AS_POOLED = "ABAP_AS_WITH_POOL";

	static {
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.0.0");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "10");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "100");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "LIUWL");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "ADMIN123");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "ZH");
		// JCO_PEAK_LIMIT - Maximum number of idle connections kept open by the destination.
		connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3");

		// JCO_POOL_CAPACITY - Maximum number of active connections that
		// can be created for a destination simultaneously
		connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10");
		createDataFile(ABAP_AS_POOLED, "jcoDestination", connectProperties);
	}

	static void createDataFile(String name, String suffix, Properties properties) {
		File cfg = new File(name + "." + suffix);
		if (!cfg.exists()) {
			try {
				FileOutputStream fos = new FileOutputStream(cfg, false);
				properties.store(fos, "for tests only !");
				fos.close();
			} catch (Exception e) {
				throw new RuntimeException("Unable to create the destination file " + cfg.getName(), e);
			}
		}
	}

	public static void accessTable() throws JCoException {
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS_POOLED);
		JCoFunction function = destination.getRepository().getFunction("ZRFC_TEST_JCO_SELECT");
		function.getImportParameterList().setValue("CARRID", "LH");
		if (function == null)
			throw new RuntimeException("ZRFC_TEST_JCO_SELECT not found in SAP.");

		try {
			function.execute(destination);
		} catch (AbapException e) {
			System.out.println(e.toString());
			return;
		}

		JCoTable exportTable = function.getTableParameterList().getTable("DATA");
		System.out.println("table row:" + exportTable.getNumRows());
		exportTable.firstRow();
		for (int i = 0; i < exportTable.getMetaData().getFieldCount(); i++) {
			System.out.print(exportTable.getMetaData().getName(i) + " ");
		}
		System.out.println();

		for (int j = 0; j < exportTable.getNumRows(); j++) {
			for (int k = 0; k < exportTable.getMetaData().getFieldCount(); k++) {
				System.out.print(exportTable.getString(k) + "   ");
				// system.out.print(exportTable.getString("PRICE"); 輸出指定字段的值
			}
			exportTable.nextRow();
			System.out.println();
		}

	}

	public static void main(String[] args) throws JCoException {
		accessTable();
	}
}
