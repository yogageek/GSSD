package fb1sap;

import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoRecordMetaData;
import com.sap.conn.jco.JCoTable;

public class Print
{
    public static void printJCoTable(JCoTable jcoTable)
    {
        // header
        
        // JCoRecordMeataData is the meta data of either a structure or a table.
        // Each element describes a field of the structure or table.        
        JCoRecordMetaData tableMeta = jcoTable.getRecordMetaData();     
        for(int i = 0; i < tableMeta.getFieldCount(); i++){
            System.out.print(String.format("%s\t", tableMeta.getName(i)));                  
        }
        System.out.println(); // new line
        
        // line items
        
        for(int i = 0; i < jcoTable.getNumRows(); i++){
            // Sets the row pointer to the specified position(beginning from zero)
            jcoTable.setRow(i);
            
            // Each line is of type JCoStructure
            for(JCoField fld : jcoTable){
                System.out.print(String.format("%s\t", fld.getValue()));
            }
            System.out.println();
        }
    }
}

