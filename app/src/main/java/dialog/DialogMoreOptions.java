package dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.home.logmenowserver.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import activity.ActivityRestaurantBill;
import activity.ActivityRestaurantTables;
import database.DBHelper;
import dialog.DialogLoading;
import shared.Models;
import shared.WebServiceCall;

/**
 * Created by Home on 8/16/2016.
 */
public class DialogMoreOptions extends Dialog {

    private Context context;
    private DBHelper mydb;
    private String fromActivity;
    private String guid;
    private DialogLoading dl;
    private String PageName = "DialogMoreOptions";
    private int EntityID;

    public DialogMoreOptions(Context context, String fromActivity, String guid, String EntityID) {
        super(context);
        this.context = context;
        mydb = new DBHelper(context);
        this.fromActivity = fromActivity;
        this.guid = guid;
        dl = new DialogLoading(context);
        this.EntityID = Integer.parseInt(EntityID);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_restaurantmoreitems);
        LinearLayout ll_clsorder = (LinearLayout) findViewById(R.id.ll_clsorder);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP;
        wmlp.y = 70;   //y position
        getWindow().setAttributes(wmlp);
        final String fromact = fromActivity;


        ll_clsorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromact.equalsIgnoreCase("ActivityRestaurantMenuList")) {
                    new AlertDialog.Builder(context)
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mydb.setTableInfo(guid, "TblStatus", 0);
                                new deleteOrder().execute();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                }
                else
                    Toast.makeText(context, "Not valid in this screen", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });


        LinearLayout ll_placeorder = (LinearLayout) findViewById(R.id.ll_placeorder);
        ll_placeorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        LinearLayout ll_totalbill = (LinearLayout) findViewById(R.id.ll_totalbill);
        ll_totalbill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromact.equalsIgnoreCase("ActivityRestaurantMenuList")){
                    Intent intent = new Intent(context, ActivityRestaurantBill.class);
                    context.startActivity(intent);
                    dismiss();
                }
                else
                    Toast.makeText(context, "Not valid in this screen", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });


        LinearLayout ll_updatetax = (LinearLayout) findViewById(R.id.ll_updatetax);
        ll_updatetax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new updateTaxes().execute();
                dismiss();
            }
        });


        LinearLayout ll_updatemenu = (LinearLayout) findViewById(R.id.ll_updatemenu);
        ll_updatemenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new updateRestaurantMenu().execute();
                dismiss();
            }
        });
    }

    public class updateTaxes extends AsyncTask<Object, Integer, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dl.show();
        }

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
            dl.dismiss();
        }
    }

    public class updateRestaurantMenu extends AsyncTask<Object, Integer, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dl.show();
        }

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
            dl.dismiss();
        }
    }

    public class deleteOrder extends AsyncTask<String, Integer, String>{
        @Override
        protected void onPreExecute() {dl.show();}

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("MasterID",0);
                jsonObject.put("GUID",guid);
                jsonObject.put("DeviceID",0);
                jsonObject.put("Status","OrderCompleted");
                response = new WebServiceCall(context).Get("DeleteRestaurantOrder","?json="+jsonObject.toString());
            }
            catch (Exception e){mydb.logAppError(PageName, "deleteOrder--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            dl.dismiss();
            Intent intent = new Intent(context, ActivityRestaurantTables.class);
            context.startActivity(intent);
        }
    }
}
