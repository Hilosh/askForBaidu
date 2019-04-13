import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class mainProcess {



    public static void main(String[] args){
        try {
            File file = new File("src/main/resources/resr.xls");
            //获取输入流
            InputStream stream = new FileInputStream(file);
            Workbook xssfWorkbook = new HSSFWorkbook(stream);
            Sheet Sheet = xssfWorkbook.getSheetAt(0);
            simpleThread.setSheet(Sheet);
            ExecutorService executorService =Executors.newFixedThreadPool(5);
            for(int i =0;i<5;i++){
                executorService.execute(new simpleThread("demo"+i,"地铁站","100000","json","Fz5R3KUIDbM1gerdsqYabt611pWhUkTj"
                        ,"sort_name:distance|sort_rule:1","2","0","BNpbsph8G5gkhzt4qqq8nP01Ad8qj1B0"));
            }
            executorService.shutdown();
            OutputStream outputStream = new FileOutputStream(file);
            ((HSSFWorkbook) xssfWorkbook).write();
            xssfWorkbook.close();
            outputStream.close();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
