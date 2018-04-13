package fb1sap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Writefile {

	public static void main(String[] args) {
		File writename = new File("d:/a.txt"); // 相對路徑，如果沒有則要建立一個新的output。txt文件
		try {
			writename.createNewFile();
			// 創建新文件
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write("testing"); // \r\n即為換行
			out.flush(); // 把緩存區內容壓入文件
			out.close(); // 最後記得關閉文件
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	}

}
