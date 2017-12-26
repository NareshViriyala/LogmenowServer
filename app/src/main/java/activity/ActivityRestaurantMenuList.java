package activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenowserver.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import adapter.AdapterRestaurantMenuItem;
import database.DBHelper;
import gcm.PushNotification;
import shared.CommonClasses;
import dialog.DialogMoreOptions;
import shared.Models.*;
import shared.WebServiceCall;

/**
 * Created by nviriyala on 16-08-2016.
 */
public class ActivityRestaurantMenuList extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private DBHelper mydb;
    private String PageName = "ActivityRestaurantMenuList";
    private TextView tv_title;
    private TextView tv_pname;
    private TextView tv_masterid;
    private Button btn_delete;
    private LinearLayout ll_itemgroup;
    private View vw_footer;

    private ImageView img_back;
    private ImageView img_call;
    private ImageView img_sync;
    private ImageView img_more;
    private ImageView img_loading;
    private CommonClasses cc;
    private String TableID;
    private ListView lv_items;

    private List<RestaurantMenuItem> itemlst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_restaurantmenulist);

            mydb = new DBHelper(this);
            cc = new CommonClasses(this);
            TableID = mydb.getSystemParameter("TableID");
            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);
            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_call = (ImageView) findViewById(R.id.img_call);
            img_call.setOnClickListener(this);
            img_more = (ImageView) findViewById(R.id.img_more);
            img_more.setOnClickListener(this);
            img_loading = (ImageView) findViewById(R.id.img_loading);
            Glide.with(this).load(R.drawable.loading).into(img_loading);
            Glide.with(this).load(R.drawable.calling).into(img_call);
            tv_pname = (TextView) findViewById(R.id.tv_pname);
            tv_masterid = (TextView) findViewById(R.id.tv_masterid);
            btn_delete = (Button) findViewById(R.id.btn_delete);
            btn_delete.setOnClickListener(this);
            ll_itemgroup = (LinearLayout) findViewById(R.id.ll_itemgroup);
            vw_footer = findViewById(R.id.vw_footer);
            inflateFooter();
            lv_items = (ListView) findViewById(R.id.lv_items);
            lv_items.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(itemlst != null && itemlst.size() > firstVisibleItem) {
                        tv_pname.setText(itemlst.get(firstVisibleItem).getPerson());
                        tv_masterid.setText(itemlst.get(firstVisibleItem).getMasterID()+"");
                    }
                    else
                        tv_pname.setText("");
                }
            });
            lv_items.setOnItemClickListener(this);

            if(!mydb.needTableTrip(Integer.parseInt(mydb.getSystemParameter("EntityID"))))
                img_call.setVisibility(View.GONE);



        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void populateMenuList(){
        try{
            AdapterRestaurantMenuItem ami = new AdapterRestaurantMenuItem(this, itemlst);
            lv_items.setAdapter(ami);
        }
        catch (Exception e){mydb.logAppError(PageName, "populateMenuList", "Exception", e.getMessage());}
    }

    public class getMenuList extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            img_sync.setImageResource(R.drawable.ic_syncing);
            img_loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                response = new WebServiceCall(ActivityRestaurantMenuList.this).Get("GetPlacedOrderItems","?json={\"GUID\":\""+TableID+"\"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "getMenuList--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                Gson gson = new Gson();
                Type type = new TypeToken<List<RestaurantMenuItem>>(){}.getType();
                itemlst = gson.fromJson(response, type);
                if(itemlst.size() == 0) {
                    ll_itemgroup.setVisibility(View.GONE);
                    Toast.makeText(ActivityRestaurantMenuList.this, "No Order", Toast.LENGTH_SHORT).show();
                    lv_items.setVisibility(View.GONE);
                    mydb.setTableInfo(TableID, "TblStatus", 0);
                }
                else {
                    ll_itemgroup.setVisibility(View.VISIBLE);
                    lv_items.setVisibility(View.VISIBLE);
                    populateMenuList();
                }
                new PushNotification(ActivityRestaurantMenuList.this).execute();
            }
            catch (Exception e){mydb.logAppError(PageName, "getMenuList--onPostExecute", "Exception", e.getMessage());}
            finally {
                img_sync.setImageResource(R.drawable.ic_sync);
                img_loading.setVisibility(View.GONE);}
        }
    }

    @Override
    public void onClick(View v) {
        try{
            switch (v.getId()){
                case R.id.img_back:
                    onBackPressed();
                    break;
                case R.id.tv_title:
                    onBackPressed();
                    break;
                case R.id.img_call:
                    break;
                case R.id.ll_search:
                    Intent Rintent = new Intent(this, ActivityRestaurantTableLog.class);
                    startActivity(Rintent);
                    break;
                case R.id.ll_update:
                    itemlst.clear();
                    new getMenuList().execute();
                    break;
                case R.id.img_more:
                    new DialogMoreOptions(this, "ActivityRestaurantMenuList", TableID, mydb.getSystemParameter("EntityID")).show();
                    break;
                case R.id.btn_delete:
                    new AlertDialog.Builder(this)
                            .setMessage("Are you sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new deleteOrder().execute(tv_masterid.getText().toString(), "OrderDeleted");
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();

                    break;
                case R.id.ll_clsbill:
                    new AlertDialog.Builder(this)
                            .setMessage("Are you sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mydb.setTableInfo(TableID, "TblStatus", 0);
                                    mydb.setTableInfo(TableID, "Call", 1);
                                    mydb.setTableInfo(TableID, "OrderPlaced", 1);
                                    mydb.deleteRestaurantOrder(0, TableID);
                                    new deleteOrder().execute("0","OrderCompleted");
                                    //Intent intent = new Intent(ActivityRestaurantMenuList.this, ActivityRestaurantTables.class);
                                    //startActivity(intent);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                    break;
                case R.id.ll_plcorder:
                    Intent Aintent = new Intent(this, ActivityRestaurantAddMenu.class);
                    startActivity(Aintent);
                    break;
                case R.id.ll_ttlbill:
                    Intent intent = new Intent(this, ActivityRestaurantBill.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public class deleteOrder extends AsyncTask<String, Integer, String>{
        @Override
        protected void onPreExecute() {img_sync.setImageResource(R.drawable.ic_syncing);}

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("MasterID", params[0]);
                jsonObject.put("GUID", TableID);
                jsonObject.put("DeviceID", "0");
                jsonObject.put("Status", params[1]);
                response = new WebServiceCall(ActivityRestaurantMenuList.this).Get("DeleteRestaurantOrder","?json="+jsonObject.toString());
            }
            catch (Exception e){mydb.logAppError(PageName, "deleteOrder--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                new getMenuList().execute();
                new PushNotification(ActivityRestaurantMenuList.this).execute();
            }
            catch (Exception e){mydb.logAppError(PageName, "getMenuList--onPostExecute", "Exception", e.getMessage());}
            finally {img_sync.setImageResource(R.drawable.ic_sync);}
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if(view instanceof LinearLayout) {
                return;
            }
            View parentView = (View) view.getParent();
            final TextView tv_masterid = (TextView) parentView.findViewById(R.id.tv_masterid);

            new AlertDialog.Builder(this)
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new deleteOrder().execute(tv_masterid.getText().toString(), "DeleteOrder");
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    @Override
    public void onResume() {
        super.onResume();
        //this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        tv_title.setText("Table - "+mydb.getSystemParameter("TableNo"));
        img_back.setVisibility(View.VISIBLE);
        new getMenuList().execute();
        //this.registerReceiver(mMessageReceiver, new IntentFilter(getResources().getString(R.string.package_name)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //this.unregisterReceiver(mMessageReceiver);
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onDestroy(){super.onDestroy();}

    public void inflateFooter(){
        try{
            LinearLayout ll_ttlbill = (LinearLayout) vw_footer.findViewById(R.id.ll_ttlbill);
            LinearLayout ll_plcorder = (LinearLayout) vw_footer.findViewById(R.id.ll_plcorder);
            LinearLayout ll_clsbill = (LinearLayout) vw_footer.findViewById(R.id.ll_clsbill);
            LinearLayout ll_update = (LinearLayout) vw_footer.findViewById(R.id.ll_update);
            LinearLayout ll_search = (LinearLayout) vw_footer.findViewById(R.id.ll_search);
            img_sync = (ImageView) vw_footer.findViewById(R.id.img_sync);

            ll_ttlbill.setOnClickListener(this);
            ll_plcorder.setOnClickListener(this);
            ll_clsbill.setOnClickListener(this);
            ll_update.setOnClickListener(this);
            ll_search.setOnClickListener(this);
        }
        catch (Exception e){mydb.logAppError(PageName, "inflateFooter", "Exception", e.getMessage());}
    }
}
