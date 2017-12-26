package activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.home.logmenowserver.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models;
import shared.WebServiceCall;

/**
 * Created by Home on 8/17/2016.
 */
public class ActivityRestaurantBill extends AppCompatActivity implements View.OnClickListener{

    private DBHelper mydb;
    private String PageName = "ActivityRestaurantBill";
    private TextView tv_title;
    private ImageView img_back;
    private ImageView img_home;
    private ImageView img_sync;
    private ImageView img_more;
    private ImageView img_loading;
    private LinearLayout ll_table;
    private CommonClasses cc;
    private String TableID;
    private List<Models.RestaurantMenuItem> itemlst;
    private List<Models.RestaurantMenuItem> itemsublst;
    private List<Models.RestaurantMenuItem> itemcompletelst;
    private int prevDeviceID = 0;
    private double totalbill = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_restaurantbill);
            mydb = new DBHelper(this);
            TableID = mydb.getSystemParameter("TableID");
            cc = new CommonClasses(this);
            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);
            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_home = (ImageView) findViewById(R.id.img_home);
            img_home.setOnClickListener(this);
            img_sync = (ImageView) findViewById(R.id.img_sync);
            img_sync.setOnClickListener(this);
            img_more = (ImageView) findViewById(R.id.img_more);
            img_more.setOnClickListener(this);
            img_loading = (ImageView) findViewById(R.id.img_loading);
            Glide.with(this).load(R.drawable.loading).into(img_loading);
            ll_table = (LinearLayout) findViewById(R.id.ll_table);
            itemsublst = new ArrayList<>();
            itemcompletelst = new ArrayList<>();
            new getMenuList().execute();
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void calculateTaxes(List<Models.RestaurantMenuItem> items, TableLayout tableLayout){
        try{
            if(items == null || items.size() == 0)
                return;
            totalbill = 0;
            JSONArray jsonArray = mydb.getRestaurantTaxes(Integer.parseInt(mydb.getSystemParameter("EntityID")));
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                TableRow tblrow = rowInfrater(jsonObject.getString("TaxType"), jsonObject.getDouble("Taxamt"), tableLayout, items);
                tableLayout.addView(tblrow);
            }
            TableRow tblrow = calculateTotal(tableLayout);
            tableLayout.addView(tblrow);
        }
        catch (Exception e){mydb.logAppError(PageName, "calculateTaxes", "Exception", e.getMessage());}
    }

    public TableRow calculateTotal(TableLayout tableLayout){
        TableRow returnview = null;
        try{
            LayoutInflater inflater = getLayoutInflater();
            TableRow row = (TableRow)inflater.inflate(R.layout.row_restaurantmenusummary, tableLayout, false);

            LinearLayout ll_col1 = (LinearLayout) row.findViewById(R.id.ll_col1);
            //TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            //layoutParams.setMargins(1, 1, 1, 10);
            ll_col1.setBackgroundColor(getResources().getColor(R.color.grey));
            //ll_col1.setLayoutParams(layoutParams);


            //layoutParams.setMargins(0, 1, 1, 10);
            LinearLayout ll_col2 = (LinearLayout) row.findViewById(R.id.ll_col2);
            ll_col2.setBackgroundColor(getResources().getColor(R.color.grey));
            //ll_col2.setLayoutParams(layoutParams);


            //Item Type
            ImageView img_itemtype = (ImageView) row.findViewById(R.id.img_itemtype);
            img_itemtype.setVisibility(View.GONE);

            //Item Name
            TextView tv_itemname = (TextView) row.findViewById(R.id.tv_itemname);
            tv_itemname.setText("Total");
            tv_itemname.setGravity(Gravity.RIGHT);
            tv_itemname.setTextColor(getResources().getColor(R.color.white));

            //Item qty
            TextView tv_qty = (TextView) row.findViewById(R.id.tv_qty);
            tv_qty.setVisibility(View.GONE);

            //Item Price
            TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
            tv_itemprice.setText(Math.round(totalbill)+"/-");
            tv_itemprice.setTextColor(getResources().getColor(R.color.white));
            returnview = row;
        }
        catch (Exception e){mydb.logAppError(PageName, "calculateTotal", "Exception", e.getMessage());}
        return returnview;
    }

    public TableRow rowInfrater(String tax, double taxpercentage, TableLayout tableLayout, List<Models.RestaurantMenuItem> items){
        TableRow returnview = null;
        try {
            double subtotal = 0;
            for (Models.RestaurantMenuItem sitem:items) {
                subtotal = subtotal+(sitem.getItemPrice()*sitem.getQuantity());
            }

            subtotal = subtotal*(taxpercentage/100);
            subtotal = Math.round(subtotal*100.0)/100.0;
            totalbill = totalbill + subtotal;

            LayoutInflater inflater = getLayoutInflater();
            TableRow row = (TableRow)inflater.inflate(R.layout.row_restaurantmenusummary, tableLayout, false);

            //Item Type
            ImageView img_itemtype = (ImageView) row.findViewById(R.id.img_itemtype);
            img_itemtype.setVisibility(View.GONE);

            //Item Name
            TextView tv_itemname = (TextView) row.findViewById(R.id.tv_itemname);
            tv_itemname.setText(tax);
            tv_itemname.setGravity(Gravity.RIGHT);

            //Item qty
            TextView tv_qty = (TextView) row.findViewById(R.id.tv_qty);
            tv_qty.setVisibility(View.GONE);


            //Item Price
            TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
            tv_itemprice.setText(subtotal+"/-");

            //trow.addView(row);
            returnview = row;
        }
        catch(Exception e){mydb.logAppError(PageName, "rowInfrater", "Exception", e.getMessage());}
        return returnview;
    }

    public void populateTotalBill(){
        try{
            TableLayout tableLayout = new TableLayout(this);
            //TableRow headerRow = spanRowName("Consolidate Bill");
            //tableLayout.addView(headerRow);
            for (Models.RestaurantMenuItem item : itemcompletelst) {
                TableRow tblrow = rowInfrater(item, tableLayout);
                tableLayout.addView(tblrow);//, new TableLayout.LayoutParams(AppBarLayout.LayoutParams.MATCH_PARENT, AppBarLayout.LayoutParams.MATCH_PARENT));
            }
            calculateTaxes(itemcompletelst, tableLayout);
            ll_table.addView(tableLayout, 1);

            //ll_table.addView(BillDivider("Individual Bills", R.drawable.ic_individual), 1);
        }
        catch (Exception e){mydb.logAppError(PageName, "populateTotalBill", "Exception", e.getMessage());}
    }

    public LinearLayout BillDivider(String text, int img_id){
        LinearLayout ll = new LinearLayout(this);
        try{
            LinearLayout.LayoutParams layoutll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            if(text.equalsIgnoreCase("Consolidated Bill"))
                layoutll.setMargins(1,0,1,20);
            else
                layoutll.setMargins(1,20,1,0);
            ll.setBackgroundColor(getResources().getColor(R.color.white));
            ll.setLayoutParams(layoutll);
            ImageView img = new ImageView(this);
            img.setImageResource(img_id);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(40, 40);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.setMargins(10, 10, 10, 10);
            img.setLayoutParams(layoutParams);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            TextView tv = new TextView(this);
            tv.setText(text);
            tv.setTextColor(getResources().getColor(R.color.colorPrimary));
            tv.setTextSize(18);
            tv.setLayoutParams(params);
            ll.addView(img);
            ll.addView(tv);
        }
        catch (Exception e){mydb.logAppError(PageName, "BillDivider", "Exception", e.getMessage());}
        return ll;
    }

    public void populateIndividualBill(){
        try{
            TableLayout tableLayout = new TableLayout(this);
            for (Models.RestaurantMenuItem item : itemlst) {
                if(prevDeviceID != item.getDeviceID()){
                    prevDeviceID = item.getDeviceID();
                    calculateTaxes(itemsublst, tableLayout);
                    TableRow tblrow = spanRowName(item.getPerson());
                    tableLayout.addView(tblrow);
                    itemsublst.clear();
                }
                prevDeviceID = item.getDeviceID();
                itemsublst.add(item);
                boolean itemExists = false;
                for(int i = 0; i < itemcompletelst.size(); i++){
                    if(itemcompletelst.get(i).getItemID() == item.getItemID()){
                        itemcompletelst.get(i).setQuantity(itemcompletelst.get(i).getQuantity()+1);
                        itemExists = true;
                    }
                }
                if(!itemExists)
                    itemcompletelst.add(item);

                TableRow tblrow = rowInfrater(item, tableLayout);
                tableLayout.addView(tblrow);//, new TableLayout.LayoutParams(AppBarLayout.LayoutParams.MATCH_PARENT, AppBarLayout.LayoutParams.MATCH_PARENT));
            }
            calculateTaxes(itemsublst, tableLayout);
            ll_table.addView(tableLayout);
        }
        catch (Exception e){mydb.logAppError(PageName, "populateTable", "Exception", e.getMessage());}
    }

    public TableRow spanRowName(String Name){
        TableRow returnview = null;
        try{
            TableRow row = new TableRow(this);
            TextView tv_name = new TextView(this);
            tv_name.setPadding(10,0,0,0);
            tv_name.setTextSize(18);
            tv_name.setTextColor(getResources().getColor(R.color.white));
            tv_name.setText(Name);
            row.addView(tv_name);
            returnview = row;
        }
        catch (Exception e){mydb.logAppError(PageName, "spanRowName", "Exception", e.getMessage());}
        return returnview;
    }

    public TableRow rowInfrater(Models.RestaurantMenuItem item, TableLayout tableLayout){
        TableRow returnview = null;
        try {
            LayoutInflater inflater = getLayoutInflater();
            TableRow row = (TableRow)inflater.inflate(R.layout.row_restaurantmenusummary, tableLayout, false);

            //Item Name
            TextView tv_oid = (TextView) row.findViewById(R.id.tv_oid);
            tv_oid.setText(item.getItemID()+"");

            //Item Type
            ImageView img_itemtype = (ImageView) row.findViewById(R.id.img_itemtype);
            if(item.getItemType() == 1)
                img_itemtype.setImageResource(R.drawable.ic_veg);
            else
                img_itemtype.setImageResource(R.drawable.ic_nonveg);

            //Item Name
            TextView tv_itemname = (TextView) row.findViewById(R.id.tv_itemname);
            tv_itemname.setText(item.getItemName());
            tv_itemname.setTextSize(12);

            //Item qty
            TextView tv_qty = (TextView) row.findViewById(R.id.tv_qty);
            tv_qty.setText(item.getQuantity()+"");

            //Item Price
            TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
            tv_itemprice.setText(item.getItemPrice()*item.getQuantity()+"/-");

            //trow.addView(row);
            returnview = row;
        }
        catch(Exception e){mydb.logAppError(PageName, "rowInfrater", "Exception", e.getMessage());}
        return returnview;
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
                response = new WebServiceCall(ActivityRestaurantBill.this).Get("GetPlacedOrderItems","?json={\"GUID\":\""+TableID+"\"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "getMenuList--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                Gson gson = new Gson();
                Type type = new TypeToken<List<Models.RestaurantMenuItem>>(){}.getType();
                itemlst = gson.fromJson(response, type);
                ll_table.addView(BillDivider("Consolidated Bill", R.drawable.ic_group));
                ll_table.addView(BillDivider("Individual Bills", R.drawable.ic_individual));
                populateIndividualBill();
                populateTotalBill();
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
                case R.id.img_home:
                    break;
                case R.id.img_sync:
                    totalbill = 0;
                    itemlst.clear();
                    itemsublst.clear();
                    itemcompletelst.clear();
                    ll_table.removeAllViews();
                    new getMenuList().execute();
                    break;
                case R.id.img_more:
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    @Override
    public void onResume() {
        super.onResume();
        this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        tv_title.setText("Table - "+mydb.getSystemParameter("TableNo")+": Total Bill");
        img_back.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onDestroy(){super.onDestroy();}
}
