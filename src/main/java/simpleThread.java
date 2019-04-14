

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class simpleThread implements Callable<String> {
    private static Sheet sheet;
    private static AtomicInteger row_number = new AtomicInteger(1);//共享的xls表和行号,支持原子操作i++
    private final static int LAT_number = 17;//纬度所在列
    private final static int LNG_number = 16;//经度所在列
    private final static int DIS_number = 19;//地铁距离所在列
    private int innerRow = 0;
    private final static String path = "http://api.map.baidu.com";
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

    simpleThread(String query,String radius,String output,String ak,String filter,String scope,String page_num,String sk){
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
    private String MD5(String query,String loaction,String radius,String output,String ak,String filter,String scope
            ,String page_num,String sk) throws UnsupportedEncodingException {
        //map赋值
        paramsMap.put("query",query);
        paramsMap.put("location",loaction);
        paramsMap.put("radius",radius);
        paramsMap.put("output",output);
        paramsMap.put("ak",ak);
        paramsMap.put("filter",filter);
        paramsMap.put("scope",scope);
        paramsMap.put("page_size","1");
        paramsMap.put("page_num",page_num);

        //编码拼接
//        StringBuffer queryString = new StringBuffer();
//        for(Map.Entry<String,String> pair:paramsMap.entrySet()){
//            queryString.append(pair.getKey()+"=");
//            queryString.append(URLEncoder.encode((String)pair.getValue(),"UTF-8")+"&");
//        }
//        if(queryString.length()>1)queryString.deleteCharAt(queryString.length()-1);
//        String wholeString = new String("/place/v2/search?"+queryString.toString()+sk);
//        wholeString = URLEncoder.encode(wholeString,"UTF-8");
//        String sn = MD5_sn(wholeString);
        StringBuffer resString = new StringBuffer();
        for(Map.Entry<String,String> pair:paramsMap.entrySet()){
            resString.append(pair.getKey()+"=");
            resString.append(pair.getValue()+"&");
        }
        if(resString.length()>1)resString.deleteCharAt(resString.length()-1);
        return "/place/v2/search?"+resString;

    }

    private String MD5_sn(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
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

    public String call() throws Exception {
        while((innerRow = row_number.getAndIncrement())<=sheet.getLastRowNum()){//共享量转为私有量，以支持之后的操作引用
            Row row = sheet.getRow(innerRow);
            Double LAT = row.getCell(LAT_number).getNumericCellValue();
            Double LNG = row.getCell(LNG_number).getNumericCellValue();
            location = LAT.toString()+","+LNG.toString();
            if(location!=null){
                try {
                    String url_path=path+MD5(query,location,radius,output,ak,filter,scope,page_num,sk);
                    URL url = new URL(url_path);
                    //System.out.println(url_path);
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
                        if("ok".equals(jsonObject.get("message"))){//返回值为ok才继续解析
                            if("0".equals(jsonObject.get("total"))){
                                //未搜索到
                                row.createCell(DIS_number).setCellValue("0");
                            }
                            else{
                                /*
                                继续往下解析json,形如
                                {"total":79,"message":"ok","results":[{"area":"西湖区","uid":"279bf9ff217d927ba5da3943",
                                "address":"地铁2号线","province":"浙江省","city":"杭州市","detail_info":{"distance":12984,
                                "children":[],"tag":"地铁站"},"name":"文新","location":{"lng":120.104622,"lat":30.295617},
                                "detail":1}]
                                 */
                                JSONArray jsonArray = new JSONArray(jsonObject.get("results").toString());
                                int distance = (Integer) new JSONObject(new JSONObject(jsonArray.get(0).toString())
                                        .get("detail_info").toString()).get("distance");
                                Cell cell = row.createCell(DIS_number);
                                cell.setCellValue(distance);
                                System.out.println(innerRow+":"+distance);
                            }
                        }
                        else row.createCell(DIS_number).setCellValue("NAN");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
        return "ok";
    }
}
