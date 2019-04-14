import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


public class mainProcess {


    private static boolean isdone(List<FutureTask<String>> list) throws ExecutionException, InterruptedException {//所有线程是否完成
        for(FutureTask<String> futureTask:list){
            if(futureTask.get()!="ok")return false;
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
            ExecutorService executorService =Executors.newFixedThreadPool(10);
            List<FutureTask<String>> futureTaskList = new ArrayList<FutureTask<String>>();//希望等到所有子线程结束，主线程才继续
            Long start = System.currentTimeMillis();
            for(int i =0;i<10;i++){
                futureTaskList.add(new FutureTask<String>(new simpleThread("地铁站","100000","json","Fz5R3KUIDbM1gerdsqYabt611pWhUkTj"
                        ,"sort_name:distance|sort_rule:1","2","0","BNpbsph8G5gkhzt4qqq8nP01Ad8qj1B0")));
                executorService.submit(futureTaskList.get(i));
            }
            executorService.shutdown();
//            while (true){
//                if(isdone(futureTaskList)){
//                    File fileout = new File("src/main/resources/resr.xls");
//                    OutputStream outputStream = new FileOutputStream(fileout);
//                    ((HSSFWorkbook) xssfWorkbook).write(outputStream);
//                    xssfWorkbook.close();
//                    outputStream.close();
//                    stream.close();
//                    return;
//                }
//
//            }
            if(isdone(futureTaskList)){
                Long end = System.currentTimeMillis();
                System.out.println((end-start)/sheet.getLastRowNum());
                System.out.println(sheet.getLastRowNum()/(end-start)*1000);
                File fileout = new File("src/main/resources/resr.xls");
                OutputStream outputStream = new FileOutputStream(fileout);
                ((HSSFWorkbook) xssfWorkbook).write(outputStream);
                xssfWorkbook.close();
                outputStream.close();
                stream.close();
                return;
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
}
