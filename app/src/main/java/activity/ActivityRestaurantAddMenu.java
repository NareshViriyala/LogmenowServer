package activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.logmenowserver.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import adapter.AdapterRestaurantMenu;
import database.DBHelper;
import gcm.PushNotification;
import shared.CommonClasses;
import shared.Models;
import shared.WebServiceCall;
import android.app.ActionBar.LayoutParams;

/**
 * Created by Home on 8/20/2016.
 */
public class ActivityRestaurantAddMenu extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private DBHelper mydb;
    private String PageName = "ActivityRestaurantAddMenu";
    private TextView tv_title;

    private ImageView img_back;
    private CommonClasses cc;
    private String TableID;

    private TextView tv_done;
    private int RestaurantID;
    private ListView lv_items;
    private List<Models.RestaurantMenu> itemlst;
    private AdapterRestaurantMenu adapterRestaurantMenu;

    private LinearLayout ll_selections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_restaurantaddmenu);

            mydb = new DBHelper(this);
            cc = new CommonClasses(this);
            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);

            tv_done = (TextView) findViewById(R.id.tv_done);
            tv_done.setOnClickListener(this);
            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);

            lv_items = (ListView) findViewById(R.id.lv_items);
            lv_items.setOnItemClickListener(this);

            ll_selections = (LinearLayout) findViewById(R.id.ll_selections);

            RestaurantID = Integer.parseInt(mydb.getSystemParameter("EntityID"));
            TableID = mydb.getSystemParameter("TableID");
            inflateMenuItems();
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void inflateMenuItems(){
        try{
            itemlst = mydb.getMenuItems(RestaurantID);
            JSONArray jsonArray = mydb.getRestaurantOrder(TableID);
            for(int i =0; i<jsonArray.length(); i++){
                for(int j = 0; j<itemlst.size(); j++){
                    if(jsonArray.getJSONObject(i).getInt("TID") == itemlst.get(j).getTID()) {
                        itemlst.get(j).setQTY(jsonArray.getJSONObject(i).getInt("Qty"));
                        itemlst.get(j).setSelected(true);
                    }
                }
            }
            updateSelection();
            adapterRestaurantMenu = new AdapterRestaurantMenu(this, itemlst);
            lv_items.setAdapter(adapterRestaurantMenu);
        }
        catch (Exception e){mydb.logAppError(PageName, "inflateMenuItems", "Exception", e.getMessage());}
    }

    public void updateSelection(){
        try{
            ll_selections.removeAllViews();
            int count = 0;
            //tv_total.setText(String.valueOf(mydb.getMenuItemCount("SelectedItems")));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            params.setMargins(10,10,0,10);
            for(int i = 0; i<itemlst.size(); i++){
                if(itemlst.get(i).getSelected()) {
                    count++;
                    TextView tv = new TextView(this);
                    tv.setBackgroundResource(R.drawable.view_click);
                    tv.setText(itemlst.get(i).getOID() + "");
                    tv.setOnClickListener(this);
                    if (itemlst.get(i).getOID() < 10)
                        tv.setPadding(32, 15, 32, 15);
                    else if (itemlst.get(i).getOID() < 100)
                        tv.setPadding(25, 15, 25, 15);
                    else
                        tv.setPadding(15, 15, 15, 15);
                    tv.setLayoutParams(params);
                    ll_selections.addView(tv);
                }
            }

            if(count > 0){
                TextView tv = new TextView(this);
                tv.setBackgroundResource(R.drawable.view_click);
                tv.setPadding(15, 15, 15, 15);
                tv.setLayoutParams(params);
                tv.setText("Total: "+count);
                ll_selections.addView(tv, 0);
            }
        }
        catch(Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try{
            //int id = v.getId();
            switch (v.getId()){
                case R.id.img_back:
                    onBackPressed();
                    break;
                case R.id.tv_title:
                    onBackPressed();
                    break;
                case R.id.tv_done:
                    new placeOrder().execute();
                    break;
                case -1:
                    int position = Integer.parseInt(((TextView)v).getText().toString());
                    lv_items.smoothScrollToPositionFromTop(position-1, 0, 0);
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
            TextView tv_tid;
            String Qty = "0";
            if(view instanceof LinearLayout)
                tv_tid = (TextView) view.findViewById(R.id.tv_tid);
            else {
                tv_tid = (TextView) ((LinearLayout) view.getParent()).findViewById(R.id.tv_tid);
                if(R.id.tv_qtyplus == view.getId())
                    Qty = "1";
                if(R.id.tv_qtyminus == view.getId())
                    Qty = "-1";
            }

            //TextView tv_tid = (TextView) view.findViewById(R.id.tv_tid);
            for(int i = 0; i<itemlst.size();i++){
                if(itemlst.get(i).getTID() == Integer.parseInt(tv_tid.getText().toString())){
                    switch (Qty) {
                        case "0": //case when linearlayout is clicked
                            if (itemlst.get(i).getSelected()) {
                                itemlst.get(i).setSelected(false);
                                itemlst.get(i).setQTY(1);
                                mydb.deleteRestaurantOrder(itemlst.get(i).getTID(), TableID);
                            } else {
                                itemlst.get(i).setSelected(true);
                                mydb.addRestaurantOrder(itemlst.get(i).getTID(), TableID, 1, itemlst.get(i).getOID());
                            }
                            break;
                        case "1": //case when QtyPlus is clicked
                            int cntp = itemlst.get(i).getQTY();
                            itemlst.get(i).setSelected(true);
                            itemlst.get(i).setQTY(cntp+1);
                            mydb.addRestaurantOrder(itemlst.get(i).getTID(), TableID, itemlst.get(i).getQTY(), itemlst.get(i).getOID());
                            break;
                        case "-1": //case when QtyMinus is clicked
                            int cnt = itemlst.get(i).getQTY();
                            itemlst.get(i).setSelected(true);
                            itemlst.get(i).setQTY(cnt==1?1:cnt-1);
                            mydb.addRestaurantOrder(itemlst.get(i).getTID(), TableID, itemlst.get(i).getQTY(), itemlst.get(i).getOID());
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
            updateSelection();
            adapterRestaurantMenu.refreshList(itemlst);
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    public class placeOrder extends AsyncTask<Object,Integer,String>{

        @Override
        protected String doInBackground(Object[] params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            try {
                String DeviceID =  mydb.getDeviceID();
                JSONArray itemList = new JSONArray();
                String oitems = "<Order>";
                for(Models.RestaurantMenu item:itemlst){
                    if(item.getSelected()){
                        oitems = oitems+"<TID>"+item.getTID()+"</TID>";
                        oitems = oitems+"<QTY>"+item.getQTY()+"</QTY>";
                    }
                }
                oitems = oitems+"</Order>";
                if(oitems.equalsIgnoreCase("<Order></Order>"))
                    response = "0";
                else {
                    response = new WebServiceCall(ActivityRestaurantAddMenu.this).Get("AddRestaurantOrder", "?json={\"DeviceID\":" + DeviceID + ",\"SubjectID\":\"" + TableID + "\",\"XMLOrder\":\"" + oitems + "\",\"DeviceType\":2,\"PersonName\":\"Waiter\"}");
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "placeOrder--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                JSONObject jsonObject = new JSONArray(response).getJSONObject(0);
                if(cc.isInteger(jsonObject.getString("MasterID"))){
                    onBackPressed();
		            new PushNotification(ActivityRestaurantAddMenu.this).execute();
                }
                else{
                    Toast.makeText(ActivityRestaurantAddMenu.this, "Error placing Order", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "placeOrder--onPostExecute", "Exception", e.getMessage());}
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        tv_title.setText("Add Items: Table-"+mydb.getSystemParameter("TableNo"));
        img_back.setVisibility(View.VISIBLE);
        this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onDestroy(){super.onDestroy();}
}
