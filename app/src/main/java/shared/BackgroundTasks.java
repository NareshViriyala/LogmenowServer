package shared;

import android.content.Context;
import android.os.AsyncTask;

import database.DBHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nviriyala on 11-07-2016.
 */
public class BackgroundTasks {

    private DBHelper mydb;
    private String PageName = "BackgroundTasks";
    private Context context;
    private NetworkDetector nd;
    private CommonClasses cc;
    private int EntityID;

    public BackgroundTasks(Context context, String MethodName){
        try {
            this.context = context;
            mydb = new DBHelper(context);
            cc = new CommonClasses(context);
            nd = new NetworkDetector(context);
            if (!nd.isInternetAvailable())
                return;

            if(MethodName == null)
                MethodName = "";
            if(getServerMapID()){
                pushErrorLog();
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "BackgroundTasks", "Exception", e.getMessage());}
    }

    public boolean getServerMapID(){
        boolean retVal = false;
        try{
            JSONObject jobj = mydb.getDeviceInfo();

            String lastServerUpdateStr = mydb.getSystemParameter("DeviceInfo");
            String lastLocalUpdateStr = jobj.getString("ModTime");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date lastServerUpdate = simpleDateFormat.parse(lastServerUpdateStr);
            Date lastLocalUpdate = simpleDateFormat.parse(lastLocalUpdateStr);
            //boolean id = isInteger(mydb.getDeviceID());
            //int com = lastLocalUpdate.compareTo(lastServerUpdate);
            //int com1 = lastServerUpdate.compareTo(lastLocalUpdate);
            if(lastLocalUpdate.compareTo(lastServerUpdate) <= 0 && cc.isInteger(mydb.getDeviceID()))
                return true;

            JSONObject jo = new JSONObject();
            jo.put("DeviceID",jobj.getString("DeviceID"));
            jo.put("Uri",jobj.getString("DeviceToken"));
            jo.put("DeviceType",jobj.getString("DeviceType"));
            jo.put("DeviceVersion",jobj.getString("DeviceVersion"));
            jo.put("AppVersion",jobj.getString("AppVersion"));
            String response = new WebServiceCall(context).Get("AddServerDevice", "?json="+jo.toString());
            //String response = new WebServiceCall(context).asmxPost("AllServer","AddServerDevice","{'input':'"+jo.toString()+"'}");
            JSONObject jsonObject = new JSONArray(response).getJSONObject(0);
            int ServerMAPID = jsonObject.getInt("ServerMAPID");
            mydb.setDeviceInfo("ServerMapID",ServerMAPID+"");
            mydb.setSystemParameter("DeviceInfo", new Models.TimeStamp().getCurrentTimeStamp());
            retVal = true;
        }
        catch (JSONException e){mydb.logAppError(PageName, "getServerMapID", "JSONException", e.getMessage());}
        catch (Exception e){mydb.logAppError(PageName, "getServerMapID", "Exception", e.getMessage());}
        return retVal;
    }

    public void pushErrorLog(){
        try{
            List<Models.ErrorLog> rows = mydb.getErrorLog(10);
            String DeviceID = mydb.getDeviceID();
            if(rows.size() > 0) {
                String input = "<row>";
                for(Models.ErrorLog row : rows){
                    input = input+"<DeviceID>"+DeviceID+"</DeviceID>";
                    input = input+"<PageName>"+row.getPageName()+"</PageName>";
                    input = input+"<MethodName>"+row.getMethodName()+"</MethodName>";
                    input = input+"<ExceptionType>"+row.getExceptionType()+"</ExceptionType>";
                    input = input+"<ExceptionText>"+row.getExceptionText()+"</ExceptionText>";
                    input = input+"<OcrTime>"+row.getOcrTime()+"</OcrTime>";
                }
                input = input+"</row>";
                String response = new WebServiceCall(context).Get("AddServerDeviceErrorLog", "?json="+input);
                //String response = new WebServiceCall(context).asmxPost("AllServer","AddServerErrorLog", "{'input':'"+strObj+"'}");
                //if(response.equalsIgnoreCase("Success"))
                mydb.deleteErrorLog(rows.size());
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "pushErrorLog", "Exception", e.getMessage());}
    }

    public void updateTaxes(int EntityID){
        try{
            this.EntityID = EntityID;
            new updateTaxes().execute();
        }
        catch (Exception e){mydb.logAppError(PageName, "updateTaxes", "Exception", e.getMessage());}
    }

    public class updateTaxes extends AsyncTask<Object, Integer, String>{

        @Override
        protected String doInBackground(Object[] params) {
            String response = "";
            try {
                if(response.equalsIgnoreCase("")) {
                    response = new WebServiceCall(context).Get("GetRestaurantTaxes", "?id=" + EntityID);
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "updateTaxes--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            mydb.addRestaurantTaxes(EntityID, response);
        }
    }


    public void updateMenuList(int EntityID){
        try{
            this.EntityID = EntityID;
            new updateRestaurantMenu().execute();
        }
        catch (Exception e){mydb.logAppError(PageName, "updateMenuList", "Exception", e.getMessage());}
    }

    public class updateRestaurantMenu extends AsyncTask<Object, Integer, String> {
        @Override
        protected String doInBackground(Object[] params) {
            String response = "";
            try {
                if(response.equalsIgnoreCase("")) {
                    response = new WebServiceCall(context).Get("GetRestaurantMenu", "?input="+EntityID+"&PageNumber=1&PageCount=1000");
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "updateRestaurantMenu--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                JSONArray jsonArray = new JSONArray(response);
                List<Models.RestaurantMenu> fetchedList = new ArrayList<>();
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jo = jsonArray.getJSONObject(i);
                    jo.put("Selected", false);
                    jo.put("QTY", 1);
                    Gson gson = new Gson();
                    Type type = new TypeToken<Models.RestaurantMenu>(){}.getType();
                    Models.RestaurantMenu rmi = gson.fromJson(jo.toString(), type);
                    fetchedList.add(rmi);
                }
                mydb.addMenuItems(EntityID, fetchedList);
            }
            catch (JSONException e){mydb.logAppError(PageName, "updateRestaurantMenu--onPostExecute", "Exception", e.getMessage());}
            catch(Exception e){mydb.logAppError(PageName, "updateRestaurantMenu--onPostExecute", "Exception", e.getMessage());}
        }
    }
}
