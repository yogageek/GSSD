package fb1sap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRecordMetaData;
import com.sap.conn.jco.JCoTable;

public class CommonFunctions {

	// 回傳此function會回傳的所有table名稱	
	public static List<String> getOutputJCoTableNames(JCoFunction function) {
		List<String> outputTableNames = new ArrayList<String>();
		JCoFieldIterator it = function.getTableParameterList().getFieldIterator();
		while (it.hasNextField()) {
			JCoField f = it.nextField();
			outputTableNames.add(f.getName());
			// logger.debug("f.getName() = " + f.getName());//must import
			// org.apache.log4j.Logger;
			// if (!StringUtils.startsWith(f.getName(), inputParamPrfix)) {
			// outputTableNames.add(f.getName());
			// }
		}
		return outputTableNames;
	}

	// 取得所有RFC方法
	public static List<String> getFunctions(String ABAP_AS) throws JCoException {
		// 取得連線
		JCoDestination destination = JCoDestinationManager.getDestination(ABAP_AS);
		// 呼叫SAP方法
		JCoFunction function = destination.getRepository().getFunction("RFC_FUNCTION_SEARCH");
		// 放入參數
		JCoParameterList importParams = function.getImportParameterList();
		importParams.setValue("FUNCNAME", "*");
		importParams.setValue("GROUPNAME", "*");
		if (function == null)
			throw new RuntimeException("RFC_FUNCTION_SEARCH not found in SAP.");
		try {
			// 執行
			function.execute(destination);
		} catch (AbapException e) {
			System.out.println(e.toString());
		}
		
		List<String> list = new ArrayList<String>();
		
		JCoTable funcDetailsTable = function.getTableParameterList().getTable("FUNCTIONS");		
		int totalNoFunc = funcDetailsTable.getNumRows();
		if (totalNoFunc > 0) {
			for (int i = 0; i < totalNoFunc; i++) {
				funcDetailsTable.setRow(i);// 將行指針指向特定的索引行
				list.add(funcDetailsTable.getString("FUNCNAME"));
				// System.out.println("Function Name: " +
				// funcDetailsTable.getString("FUNCNAME"));
				// System.out.println("GROUP Name: " +
				// funcDetailsTable.getString("GROUPNAME"));
			}
		}
		// //印出list中所有值
		// int i=0;
		// for (String s : list) {
		// System.out.println(i+" "+s);
		// i++;
		// }
		return list;
	}

	//印出所有資料+處理日期格式+addlist回傳
	// JCo中，與表參數相關的兩個接口是JCoTable和JCoRecordMetaDta, JCoTable就是RFM中table參數，而JCoRecordMetaDta是JCoTable或JCoStructure的元數據。為了方便顯示，可以考慮使用一個通用代碼進行輸出：  
	public static List<List<String>> printJCoTable(JCoTable tb) {
		List<List<String>> listData = new ArrayList<List<String>>();		
		// header 欄位名
		JCoRecordMetaData tableMeta = tb.getRecordMetaData();//JCoRecordMeataData is the meta data of either a structure or a tb. Each element describes a field of the structure or tb.
		for (int i = 0; i < tableMeta.getFieldCount(); i++) {
			System.out.print(String.format("%s\t\t", tableMeta.getName(i)));
		}
		System.out.println(); // new line		
		// line items 欄位資料
		for (int i = 0; i < tb.getNumRows(); i++) {			
			// Sets the row pointer to the specified position(beginning from zero)						
			tb.setRow(i);
			// Each line is of type JCoStructure
			List list = new ArrayList<>();			
			for (JCoField fld : tb) {
				System.out.print(fld.getTypeAsString()+":");//印出資料格式
				//判斷如果是日期格式就轉型(目前有些會沒轉到)				
				if (fld.getTypeAsString() == "DATE"||fld.getTypeAsString() == "TIME") {
					SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
					try {
						Date date = df.parse(fld.getValue().toString());
						String fd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
						list.add(fd);						
						System.out.print(String.format("%s\t", fd));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else {
					list.add(fld.getValue());					
					System.out.print(String.format("%s\t", fld.getValue()));
				}				
			}			
			listData.add(list);			
			System.out.println();			
		}		
		return listData;
	}
	
	//印出所資料+處理日期格式+addlist回傳+轉換編碼存入txt    PS:obpm報錯  
	public static List<List<String>> printJCoTableAddlist(JCoTable tb) throws IOException {
		FileWriter writer = new FileWriter("Original.txt");
		FileWriter writer1 = new FileWriter("ISO-8859-1_to_GB2312.txt");		
		FileWriter writer2 = new FileWriter("BIG5_to_GB2312.txt");			
		FileWriter writer3 = new FileWriter("BIG5_to_UTF-8.txt");
		FileWriter writer4= new FileWriter("ISO-8859-1_to_UTF-8.txt");
		
		List<List<String>> listData = new ArrayList<List<String>>();		
		// headerJCoRecordMeataData is the meta data of either a structure or a tb. Each element describes a field of the structure or tb.
		JCoRecordMetaData tableMeta = tb.getRecordMetaData();
		for (int i = 0; i < tableMeta.getFieldCount(); i++) {
			System.out.print(String.format("%s\t\t", tableMeta.getName(i)));
		}
		System.out.println(); // new line

		// line items
		for (int i = 3507; i < tb.getNumRows(); i++) { //從第3000筆開始取就好
			// Sets the row pointer to the specified position(beginning from zero)
			tb.setRow(i);
			// Each line is of type JCoStructure
			List list = new ArrayList<>();
			for (JCoField fld : tb) {				
				//判斷如果是日期格式就轉型(目前有些會沒轉到)
				if (fld.getTypeAsString() == "DATE") {
					SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
					try {
						Date date = df.parse(fld.getValue().toString());
						String fd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
						list.add(fd);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else {
//					String str=fld.getValue().toString();
//					str=new String(str.getBytes("ISO8859-1"),"GBK");
					list.add(fld.getValue());					
				}	
				
				String str=fld.getValue().toString();				
				String str1=new String(str.getBytes("ISO-8859-1"),"GB2312");
				String str2=new String(str.getBytes("BIG5"),"GB2312");
				String str3=new String(str.getBytes("BIG5"),"UTF-8");
				String str4=new String(str.getBytes("ISO-8859-1"),"UTF-8");				
				writer.write(str+System.getProperty("line.separator"));
				writer1.write(str1+System.getProperty("line.separator"));					
				writer2.write(str2+System.getProperty("line.separator"));
				writer3.write(str3+System.getProperty("line.separator"));
				writer4.write(str3+System.getProperty("line.separator"));
//				String encodedWithISO88591 = "代刚";
//				String decodedToUTF8 = new String(encodedWithISO88591.getBytes("ISO-8859-1"), "UTF-8");
//				System.out.println("decodedToUTF8"+decodedToUTF8);
//				str=new String(str.getBytes("UTF8"),"ISO8859-1");
//				System.out.print(str);
				System.out.print(String.format("%s\t", str));
			}			
			listData.add(list);
			System.out.println();			
		}		
		writer.close();
		writer1.close();
		writer2.close();
		writer3.close();
		writer4.close();
		return listData;
	}
	//addlist回傳
	public static List<List<String>> addList(JCoTable tb){
		List<List<String>> listData = new ArrayList<List<String>>();		
		for (int i = 0; i < tb.getNumRows(); i++) {			
			tb.setRow(i);			
			List list = new ArrayList<>();
			for (JCoField fld : tb) {				
				//判斷如果是日期格式就轉型(目前有些會沒轉到)
				if (fld.getTypeAsString() == "DATE") {
					SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
					try {
						Date date = df.parse(fld.getValue().toString());
						String fd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
						list.add(fd);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else {
					String str=fld.getValue().toString();					
					list.add(fld.getValue());
				}				
			}
			listData.add(list);					
		}		
		return listData;
	}
	
	// 是否為日期格式
	public static boolean isValidDate(String dateString) {
//	    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	    SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);  
	    try {
	        df.parse(dateString);
	        return true;
	    } catch (ParseException e) {
	        return false;
	    }
	}
	
	// 日期轉換
	public static String transDate(String dateString){		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
		Date d = null;
		try {
			d = sdf.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
		return formatDate;		
	}
	
	// 印出回傳的table結構
	public static void showTableStructure(JCoTable tb) {		
        int numRows = tb.getNumRows();
        System.out.println("Table rows = " + numRows);
        tb.firstRow();

        JCoMetaData tbMeta = tb.getMetaData();
        System.out.println("============================================================");
        System.out.println("Table name = " + tbMeta.getName());
        System.out.println("Field count = " + tbMeta.getFieldCount());
        System.out.println("FIELD NAME  : TYPE : DESC : VALUE");
        System.out.println("--------------------------------------");
        for (int i = 0; i < tbMeta.getFieldCount(); i++) {
            System.out.println(tbMeta.getName(i) + " : " + tbMeta.getDescription(i) + " : " + tbMeta.getTypeAsString(i) + "/" + tbMeta.getLength(i) + " : [" + tb.getString(i) + "]");

        }
    }
	
	public LinkedList getTableParameter(JCoField tb)
	{
	    LinkedList l = new LinkedList();
	    JCoTable t = tb.getTable();
	    for (int i = 0; i < t.getNumRows(); i++)
	    {
	        t.setRow(i);
	        JCoFieldIterator iter = t.getFieldIterator();
	        LinkedHashMap m = new LinkedHashMap();
	        while(iter.hasNextField())
	        {
	            JCoField f = iter.nextField();
	            m.put(f.getName(), t.getValue(f.getName()));
	        }
	        l.add(m);
	    }
	    System.out.println(l);
	    return l;
	}
		
	//檢查回傳的table  PS:obpm報錯
	public static void checkReturnTable(JCoTable tb) {
		//印出表
		System.out.println("print tb:"+tb);
		//是否為空
		System.out.println("tb.isEmpty()="+tb.isEmpty());		
		//筆數
		System.out.println("tb.getNumRows()="+tb.getNumRows());
	}
	
	//檢查function  PS:obpm報錯
	public static void checkFunction(JCoFunction fc) {
		//印出參數表
		System.out.print("印出參數表(fc.getImportParameterList())");
		System.out.println(fc.getImportParameterList());
		//印出會回傳哪些tables
		System.out.print("印出會回傳哪些tables()");
		System.out.println(getOutputJCoTableNames(fc));		
		
	}
	
	//將list寫入excel
	public static void writeExcel(List<List<String>> list) {
	    String outputFile = "C:/Users/Administrator/Desktop/writeExcel.xlsx";	    

	    try {
	        // 創建新的Excel 工作簿
	        XSSFWorkbook workbook = new XSSFWorkbook();
	        // 在Excel工作簿中建一工作表，其名為缺省值
	        XSSFSheet sheet = workbook.createSheet();
	        for (int i = 0; i < list.size(); i++) {
	            XSSFRow row = sheet.createRow((short) i);
	            List<String> listRow = list.get(i);
	            XSSFCell cell = row.createCell((short) 0);
	            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
	            cell.setCellValue(listRow.toString());
	        }
	        // 新建一輸出文件流
	        FileOutputStream fOut = new FileOutputStream(outputFile);
	        // 把相應的Excel 工作簿存盤
	        workbook.write(fOut);
	        fOut.flush();
	        // 操作結束，關閉文件
	        fOut.close();
	        System.out.println("文件生成完畢...");

	    } catch (FileNotFoundException e) {
	        System.out.println("文件沒找到 : " + e);
	    } catch (IOException e) {
	        System.out.println("已運行IO異常 : " + e);
	    }
	}
	
	public static void main(String[] args) throws ParseException {		
//		String date = "Thu JUL 27 18:05:49 CST 2015"; 
//		String a=transDate(date);
//		System.out.println(a);		
		}
}
