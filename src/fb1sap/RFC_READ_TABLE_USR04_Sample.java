package fb1sap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;


public class RFC_READ_TABLE_USR04_Sample {

	static String DST = "DST";

    static {
        Properties connectProperties = new Properties();
        
        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.134.28.112");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "801");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "SFC");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "foxestar");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");
        createDestinationDataFile(DST, connectProperties);
    }


    static void createDestinationDataFile(String destinationName, Properties connectProperties) {
        File destCfg = new File(destinationName + ".jcoDestination");
        try {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectProperties.store(fos, "for tests only !");
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create the destination files", e);
        }
    }
    

    public static void TEST() throws JCoException {
        JCoDestination destination;
        JCoRepository sapRepository;

        destination = JCoDestinationManager.getDestination(DST);
        JCoDestinationManager.getDestination(DST);
        System.out.println("Attributes:");
        System.out.println(destination.getAttributes());
        System.out.println();
        
        try {
            JCoContext.begin(destination);
            sapRepository = destination.getRepository();
            
            if (sapRepository == null) {
                System.out.println("Couldn't get repository!");
                System.exit(0);
            } 
            
            JCoFunctionTemplate template2 = sapRepository.getFunctionTemplate("RFC_READ_TABLE");
            System.out.println("Getting template");
            JCoFunction function2 = template2.getFunction();
            function2.getImportParameterList().setValue("QUERY_TABLE", "USR04");
            function2.getImportParameterList().setValue("DELIMITER", ",");
            function2.getImportParameterList().setValue("ROWSKIPS", Integer.valueOf(0));//Integer的valueOf（）就是把參數給的值，轉化為Integer類型。
            function2.getImportParameterList().setValue("ROWCOUNT", Integer.valueOf(0));
            
            System.out.println("Setting OPTIONS");
            Date date = new Date(1410152400000L);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(date);
            
            String dt = dateString.substring(0, 8);
            String tm = dateString.substring(8);
            System.out.println("dt > " + dt + ", tm > " + tm);

            JCoTable returnOptions = function2.getTableParameterList().getTable("OPTIONS");
            returnOptions.appendRow();
            //returnOptions.setValue("TEXT", "MODDA GE '20140908' AND MODTI GT '000000'");            
            returnOptions.setValue("TEXT", "MODDA GE '"+dt+"' AND MODTI GT '"+tm+"'");
            //returnOptions.appendRow();
            //returnOptions.setValue("TEXT", "AND TYPE = 'DN'");//加了這行會錯

            System.out.println("Setting FIELDS");//導出指定fields
            JCoTable returnFields = function2.getTableParameterList().getTable("FIELDS");
            returnFields.appendRow();
            returnFields.setValue("FIELDNAME", "BNAME");
            
            function2.execute(destination);
            
            JCoTable jcoTablef = function2.getTableParameterList().getTable("FIELDS");
            JCoTable jcoTabled = function2.getTableParameterList().getTable("DATA");

            int icodeOffSet = 0;
            int icodeLength = 0;

            int numRows = jcoTabled.getNumRows();
            System.out.println("numRows > " + numRows);
            
            if (numRows > 0) {
                //for (int iRow = 0; iRow  " + sValue)
                }
            //mycode
            String header = "SN";
            for (int i = 0; i < jcoTablef.getNumRows(); i++) {
            	jcoTablef.setRow(i);
                header += "," + jcoTablef.getString("FIELDNAME");
                System.out.println(jcoTablef.getString("FIELDNAME"));
            }
            
            final FileWriter outFile = new FileWriter("out.csv");
            outFile.write(header + "\n");
            final JCoTable rows = function2.getTableParameterList().getTable("DATA");

            for (int i = 0; i < rows.getNumRows(); i++) {
                rows.setRow(i);
                outFile.write(i + "," + rows.getString("WA") + "\n");
                outFile.flush();
            }
            outFile.close();
            
            
            }
        catch (Exception e) {
            //System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            JCoContext.end(destination);
        }
    }


    public static void main(String[] args) {
        try {
            TEST();
        } catch (JCoException jce) {
            System.out.println("Exception > " + jce);
//        	jce.printStackTrace();
        }
    }
    
}