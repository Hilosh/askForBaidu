

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class simpleThread extends Thread {
    private static Sheet sheet;
    private static volatile int row_number = 1;//共享的xls表和行号
    private final static int LAT_number = 17;//纬度所在列
    private final static int LNG_number = 16;//经度所在列

    private static String path = "http://api.map.baidu.com/place/v2/search?";
    //访问参数
    String query;
    String location;
    String radius;
    String output;
    String ak;
    String filter;
    String scope;
    String page_num;
    String sk;

    private Map<String,String> paramsMap = new LinkedHashMap<String, String>();

    simpleThread(String threadname,String query,String radius,String output,String ak,String filter,String scope,String page_num,String sk){
        super(threadname);
        this.query = query;
        this.radius = radius;
        this.output = output;
        this.ak = ak;
        this.filter = filter;
        this.scope = scope;
        this.page_num = page_num;
        this.sk = sk;
    }
    /*
    初始化sheet
     */
    public static void setSheet(Sheet st){
        sheet = st;
    }


    @Override
    public void run(){
        while(row_number<=sheet.getLastRowNum()){
            location = getStatics();
            if(location!=null){
                try {
                    URL url = new URL(path+MD5(query,location,radius,output,ak,filter,scope,page_num,sk));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    if(connection.getResponseCode()==200){//访问成功
                        InputStream inputStream=connection.getInputStream();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while((len=inputStream.read(buffer))!=-1){
                            byteArrayOutputStream.write(buffer,0,len);
                        }
                        String jsonString = byteArrayOutputStream.toString();
                        byteArrayOutputStream.close();
                        inputStream.close();

                        JSONObject jsonObject = new JSONObject(jsonString);
                        System.out.println(jsonObject.toString());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    读取经纬度方法
     */
    synchronized private String getStatics(){
        if(sheet!=null && row_number<=sheet.getLastRowNum()){
            Row row = sheet.getRow(row_number++);
            Double LAT = row.getCell(LAT_number).getNumericCellValue();
            Double LNG = row.getCell(LNG_number).getNumericCellValue();
            return LAT.toString()+","+LNG.toString();
        }
        return null;
    }

    /**
     * url编码sn验证
     * @param query
     * @param loaction
     * @param radius
     * @param output
     * @param ak
     * @param filter
     * @param scope
     * @param page_num
     * @param sk
     * @return
     * @throws UnsupportedEncodingException
     */
    private String MD5(String query,String loaction,String radius,String output,String ak,String filter,String scope,String page_num,String sk) throws UnsupportedEncodingException {
        //map赋值
        paramsMap.put("query",query);
        paramsMap.put("location",loaction);
        paramsMap.put("radius",radius);
        paramsMap.put("output",output);
        paramsMap.put("ak",ak);
        paramsMap.put("filter",filter);
        paramsMap.put("scope",scope);
        paramsMap.put("page_num",page_num);

        //编码拼接
        StringBuffer queryString = new StringBuffer();
        for(Map.Entry<String,String> pair:paramsMap.entrySet()){
            queryString.append(pair.getKey()+"=");
            queryString.append(URLEncoder.encode((String)pair.getValue(),"UTF-8")+"&");
        }
        if(queryString.length()>0){
            queryString.deleteCharAt(queryString.length()-1);
        }
        String wholeString = new String(queryString+sk);
        wholeString = URLEncoder.encode(wholeString,"UTF-8");

        //来自stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(wholeString.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

}
