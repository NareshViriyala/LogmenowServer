package activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.home.logmenowserver.R;

import adapter.AdapterRestaurantTable;
import database.DBHelper;
import shared.BackgroundTasks;
import shared.CommonClasses;
import dialog.DialogMoreOptions;
import shared.WebServiceCall;

/**
 * Created by Home on 8/11/2016.
 */
public class ActivityRestaurantTables extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private DBHelper mydb;
    private String PageName = "ActivityRestaurantTables";
    private TextView tv_title;
    private ImageView img_back;
    private ImageView img_call;
    private ImageView img_sync;
    private ImageView img_more;
    private ImageView img_loading;
    private GridView gv_tables;
    private CommonClasses cc;
    private String RestaurantID;
    private AdapterRestaurantTable tables;
    private String DeviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_restauranttables);
            mydb = new DBHelper(this);
            cc = new CommonClasses(this);
            RestaurantID = mydb.getSystemParameter("EntityID");
            DeviceID = mydb.getDeviceID();
            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);
            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_call = (ImageView) findViewById(R.id.img_call);
            img_call.setOnClickListener(this);
            img_sync = (ImageView) findViewById(R.id.img_sync);
            img_sync.setOnClickListener(this);
            img_more = (ImageView) findViewById(R.id.img_more);
            img_more.setOnClickListener(this);
            img_loading = (ImageView) findViewById(R.id.img_loading);
            Glide.with(this).load(R.drawable.loading).into(img_loading);
            Glide.with(this).load(R.drawable.calling).into(img_call);

            if(!mydb.needTableTrip(Integer.parseInt(mydb.getSystemParameter("EntityID"))))
                img_call.setVisibility(View.GONE);

            gv_tables = (GridView) findViewById(R.id.gv_tables);
            gv_tables.setOnItemClickListener(this);

            if(mydb.getRecordCount("tbl_RestaurantMenu", Integer.parseInt(RestaurantID)) == 0)
                new BackgroundTasks(this, null).updateMenuList(Integer.parseInt(RestaurantID));

            if(mydb.getRecordCount("tbl_restauranttaxes", Integer.parseInt(RestaurantID)) == 0)
                new BackgroundTasks(this, null).updateTaxes(Integer.parseInt(RestaurantID));

            if(mydb.getRecordCount("tbl_restauranttable", Integer.parseInt(RestaurantID)) == 0)
                new getTableList().execute();
            else
                populateTableList();
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void populateTableList(){
        try{
            img_loading.setVisibility(View.GONE);
            tables = new AdapterRestaurantTable(this);
            gv_tables.setAdapter(tables);
        }
        catch (Exception e){mydb.logAppError(PageName, "populateTableList", "Exception", e.getMessage());}
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            img_call.setVisibility(View.VISIBLE);
            tables.refreshList();
        }
    };

    public class getTableList extends AsyncTask<String, Integer, String>{
        @Override
        protected void onPreExecute() {
            img_sync.setImageResource(R.drawable.ic_syncing);
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                response = new WebServiceCall(ActivityRestaurantTables.this).Get("GetRestaurantTables","?json={\"DeviceID\":\""+DeviceID+"\",\"EntityID\":"+RestaurantID+"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "getTableList--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                mydb.addRestaruantTableList(response, Integer.parseInt(RestaurantID));
                populateTableList();
                img_sync.setImageResource(R.drawable.ic_sync);
            }
            catch (Exception e){mydb.logAppError(PageName, "getTableList--onPostExecute", "Exception", e.getMessage());}
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
                case R.id.img_sync:
                    new getTableList().execute();
                    break;
                case R.id.img_more:
                    new DialogMoreOptions(this, "ActivityRestaurantTables", "", RestaurantID).show();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            TextView tv_guid = (TextView) view.findViewById(R.id.tv_guid);
            TextView tv_status = (TextView) view.findViewById(R.id.tv_status);
            TextView tv_tableno = (TextView) view.findViewById(R.id.tv_tableno);
            switch (tv_status.getText().toString()){
                case "None":
                    mydb.setSystemParameter("TableID", tv_guid.getText().toString());
                    mydb.setSystemParameter("TableNo", tv_tableno.getText().toString());
                    loadActivityMenuList();
                    break;
                case "Call":
                    mydb.setTableInfo(tv_guid.getText().toString(), "Call", 1);
                    new AckTableCall().execute(tv_guid.getText().toString());
                    tables.refreshList();
                    break;
                case "OrderPlaced":
                    mydb.setTableInfo(tv_guid.getText().toString(), "OrderPlaced", 1);
                    mydb.setSystemParameter("TableID", tv_guid.getText().toString());
                    mydb.setSystemParameter("TableNo", tv_tableno.getText().toString());
                    loadActivityMenuList();
                    break;
                case "Both":
                    mydb.setTableInfo(tv_guid.getText().toString(), "Call", 1);
                    mydb.setTableInfo(tv_guid.getText().toString(), "OrderPlaced", 1);
                    mydb.setSystemParameter("TableID", tv_guid.getText().toString());
                    mydb.setSystemParameter("TableNo", tv_tableno.getText().toString());
                    loadActivityMenuList();
                    break;
                default:
                    break;
            }
            if(!mydb.needTableTrip(Integer.parseInt(mydb.getSystemParameter("EntityID"))))
                img_call.setVisibility(View.GONE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    public class AckTableCall extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String[] params) {
            try{
                new WebServiceCall(ActivityRestaurantTables.this).Get("CallWaiterAck","?json={\"GUID\":\""+params[0]+"\"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "AckTableCall", "Exception", e.getMessage());}
            return null;
        }
    }

    public void loadActivityMenuList(){
        try{
            Intent intent = new Intent(this, ActivityRestaurantMenuList.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }
        catch (Exception e){mydb.logAppError(PageName, "loadActivityMenuList", "Exception", e.getMessage());}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onResume() {
        super.onResume();
        tv_title.setText("Tables");
        img_back.setVisibility(View.VISIBLE);
        if(tables != null && tables.getCount() > 0)
            tables.refreshList();
        if(mydb.needTableTrip(Integer.parseInt(mydb.getSystemParameter("EntityID"))))
            img_call.setVisibility(View.VISIBLE);
        this.registerReceiver(mMessageReceiver, new IntentFilter(getResources().getString(R.string.package_name)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mMessageReceiver);
        //this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onDestroy(){super.onDestroy();}
}
