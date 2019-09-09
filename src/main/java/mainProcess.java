import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class mainProcess {

    private static boolean isdone(List<FutureTask<String>> list) throws ExecutionException, InterruptedException {//所有线程是否完成
        for(FutureTask<String> futureTask:list){
            if(!futureTask.get().equals("ok"))return false;
        }
        return true;
    }

    public static void main(String[] args){
        try {
            File file = new File("src/main/resources/res.xls");
            //获取输入流
            InputStream stream = new FileInputStream(file);
            Workbook xssfWorkbook = new HSSFWorkbook(stream);
            Sheet sheet = xssfWorkbook.getSheetAt(0);
            Row row = sheet.getRow(0);
            simpleThread.setSheet(sheet);
            ExecutorService executorService =Executors.newFixedThreadPool(4);
            List<FutureTask<String>> futureTaskList = new ArrayList<>();//希望等到所有子线程结束，主线程才继续
            Long start = System.currentTimeMillis();
            for(int i =0;i<3;i++){
                futureTaskList.add(new FutureTask<>(new simpleThread("地铁站","100000","json","--------"
                        ,"sort_name:distance|sort_rule:1","2","0","*****")));
                executorService.submit(futureTaskList.get(i));
            }
            //不再提交线程，等待线程结束则关闭线程池
            executorService.shutdown();
            if(isdone(futureTaskList)){
                Long end = System.currentTimeMillis();
                System.out.println("平均每条耗时："+(end-start)/sheet.getLastRowNum()+"ms");
                System.out.println("总耗时："+(end-start)+"ms");
                File fileout = new File("src/main/resources/resr.xls");
                OutputStream outputStream = new FileOutputStream(fileout);
                ((HSSFWorkbook) xssfWorkbook).write(outputStream);
                xssfWorkbook.close();
                outputStream.close();
                stream.close();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }
}
