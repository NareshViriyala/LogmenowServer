package shared;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import database.DBHelper;

/**
 * Created by Home on 8/6/2016.
 */
public class WebServiceCall {

    private DBHelper mydb;
    private String PageName = "WebServiceCall";
    //private String apiURL = "http://caremetricsdemo.ihealthtechnologies.com/api/";
    private String apiURL = "http://www.logmenow.com/db_calls/";


    public WebServiceCall(Context context){mydb = new DBHelper(context);}

    public String Post(String controllerName, String jsonPostString){
        String returnStr = "";
        try{
            URL url = new URL(apiURL+controllerName);
            HttpURLConnection httpurlconnection = (HttpURLConnection)url.openConnection();
            httpurlconnection.setRequestMethod("POST");
            httpurlconnection.setReadTimeout(10000);
            //httpurlconnection.setConnectTimeout(10000);
            httpurlconnection.setDoInput(true);


            httpurlconnection.setDoOutput(true);
            OutputStream ops = httpurlconnection.getOutputStream();
            ops.write(("="+jsonPostString).getBytes());
            ops.flush();
            ops.close();

            if(httpurlconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream inStream = httpurlconnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp = "";
                while ((temp = bReader.readLine()) != null) {
                    returnStr += temp;
                }
                bReader.close();
            }
            httpurlconnection.disconnect();
            returnStr = returnStr.replace("\\\"", "\"");
            returnStr = returnStr.substring(1, returnStr.length()-1);
        }
        catch(Exception e){mydb.logAppError(PageName, "Post", "Exception", e.getMessage());}
        return returnStr;
    }

    public String Get(String rawControllerName, String querystring){
        String returnStr = "";
        String controllerName = rawControllerName+".php";
        try{
            querystring = querystring.replace(" ","%20");
            URL url = new URL(apiURL+controllerName+querystring);
            HttpURLConnection  httpurlconnection = (HttpURLConnection)url.openConnection();
            httpurlconnection.setRequestMethod("GET");
            httpurlconnection.setReadTimeout(10000);
            httpurlconnection.setDoInput(true);

            if(httpurlconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream inStream = httpurlconnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp = "";
                while ((temp = bReader.readLine()) != null) {
                    returnStr += temp;
                }
                bReader.close();
            }
            /*else{
                int i = httpurlconnection.getResponseCode();
                String MethodName = httpurlconnection.getResponseMessage();
                InputStream inStream = httpurlconnection.getErrorStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp = "";
                while ((temp = bReader.readLine()) != null) {
                    returnStr += temp;
                }
                bReader.close();
            }*/
            httpurlconnection.disconnect();
            //returnStr = returnStr.replace("\\\"", "\"");
            //returnStr = returnStr.substring(1, returnStr.length()-1);
        }
        catch(Exception e){mydb.logAppError(PageName, "Get", "Exception", e.getMessage());}
        return returnStr;
    }

    public Bitmap GetImage(String controllerName, String querystring){
        Bitmap bitmap = null;
        try{
            URL url = new URL(apiURL+controllerName+querystring);
            HttpURLConnection  httpurlconnection = (HttpURLConnection)url.openConnection();
            httpurlconnection.setRequestMethod("GET");
            httpurlconnection.setReadTimeout(10000);
            httpurlconnection.setDoInput(true);

            if(httpurlconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream inStream = httpurlconnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inStream);
                inStream.close();
            }
            httpurlconnection.disconnect();
        }
        catch(Exception e){mydb.logAppError(PageName, "GetImage", "Exception", e.getMessage());}
        return bitmap;
    }
}
