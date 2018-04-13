package fb1sap;

import org.junit.Test;
import com.sap.conn.jco.*;


public class RFC_READ_TABLE_STPO_Test2
{   
    public JCoTable readTable() throws JCoException
    {
        /**
         * Shows how to process JCoTable (as importing)
         */
        
        JCoDestination dest = JCoDestinationManager.getDestination(I_Connection.ABAP_AS);
        JCoFunction fm = dest.getRepository().getFunction("RFC_READ_TABLE");
        
        // table we want to query is USR04
        // which is user authorization table in SAP
        fm.getImportParameterList().setValue("QUERY_TABLE", "STPO");
        
        // output data will be delimited by comma
        fm.getImportParameterList().setValue("DELIMITER", ",");
        
        // processing table parameters
        JCoTable options = fm.getTableParameterList().getTable("OPTIONS");
        // modification date >= 2012.01.01 and <= 2015.12.31
        options.appendRow();
        options.setValue("TEXT", "STLNR EQ '03643094' ");
//        options.appendRow();
//        options.setValue("TEXT", "AND MODDA LE '20151231' ");
        
        // We only care about fields of [user id] and [modification date]       
        String[] outputFields = new String[] {"STLNR"};
        JCoTable fields = fm.getTableParameterList().getTable("FIELDS");
        int count = outputFields.length;
        fields.appendRows(count);
        for (int i = 0; i < count; i++){
            fields.setRow(i);
            fields.setValue("FIELDNAME", outputFields[i]);          
        }
        
        fm.execute(dest);
        
        JCoTable data = fm.getTableParameterList().getTable("DATA");
        
        System.out.println(data.getNumRows());
        
        return data;
    }
    
    @Test
    public void printUsers() throws JCoException
    {
        JCoTable users = this.readTable();
        Print.printJCoTable(users);
    }
}