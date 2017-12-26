package activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenowserver.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import adapter.AdapterRestaurantTableLog;
import database.DBHelper;
import shared.GlobalClass;
import shared.Models;
import shared.Models.RestaurantTableLog;
import shared.NetworkDetector;
import shared.WebServiceCall;

/**
 * Created by nviriyala on 31-08-2016.
 */
public class ActivityRestaurantTableLog extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{
    private DBHelper mydb;
    private String PageName = "ActivityRestaurantTableLog";
    private TextView tv_title;
    private ImageView img_back;
    private ImageView img_loading;
    private ImageView img_search;
    private ListView lv_tableloglst;
    private EditText et_search;
    public NetworkDetector nd;
    private int currentPage = 0;
    private boolean fetchingData = false;
    private String TableID, searchString;
    private AdapterRestaurantTableLog adapterSCE;
    public List<RestaurantTableLog> fetchedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_restauranttablelog);
            mydb = new DBHelper(this);
            nd = new NetworkDetector(this);
            TableID = mydb.getSystemParameter("TableID");
            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);

            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_search = (ImageView) findViewById(R.id.img_search);
            img_search.setOnClickListener(this);

            et_search = (EditText) findViewById(R.id.et_search);
            et_search.addTextChangedListener(new searchString());
            //et_search.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
            img_loading = (ImageView) findViewById(R.id.img_loading);
            Glide.with(this).load(R.drawable.loading).into(img_loading);

            lv_tableloglst = (ListView) findViewById(R.id.lv_tableloglst);
            lv_tableloglst.setOnItemClickListener(this);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            adapterSCE = new AdapterRestaurantTableLog(this);
            lv_tableloglst.setAdapter(adapterSCE);
            lv_tableloglst.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(view.getLastVisiblePosition() == totalItemCount-1 && !fetchingData && currentPage != -1){
                        //Toast.makeText(getActivity(), "Reached last", Toast.LENGTH_SHORT).show();
                        fetchingData = true;
                        fetchData();
                    }
                }
            });
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void fetchData(){
        try{
            if(nd.isInternetAvailable()) {
                searchString = et_search.getText().toString().toLowerCase().trim().replace(" ","%20");
                new getRestaurantTableLog().execute();
            }
            else
                Toast.makeText(ActivityRestaurantTableLog.this, getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){mydb.logAppError(PageName, "fetchList", "Exception", e.getMessage());}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try{
            LinearLayout ll_items = (LinearLayout) view.findViewById(R.id.ll_items);
            if(ll_items.getChildCount() == 0){
                View tableview = inflateItemsTable(position);
                ll_items.addView(tableview);
            }
            else if(ll_items.getChildAt(0).getVisibility() == View.VISIBLE){
                ll_items.getChildAt(0).setVisibility(View.GONE);
            }
            else
                ll_items.getChildAt(0).setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    public View inflateItemsTable(int position){
        TableLayout itemsTable = new TableLayout(this);
        LayoutInflater inflater = getLayoutInflater();
        try {
            String array = "["+fetchedList.get(position).getItems()+"]";
            JSONArray jsonArray = new JSONArray(array);
            for(int i =0; i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Models.RestaurantMenu item = mydb.getMenuItem(jsonObject.getInt("TID"));
                item.setQTY(jsonObject.getInt("Qty"));
                TableRow row = (TableRow)inflater.inflate(R.layout.row_restaurantmenusummary, itemsTable, false);

                //Item Name
                TextView tv_oid = (TextView) row.findViewById(R.id.tv_oid);
                tv_oid.setText(item.getTID()+"");

                //Item Type
                ImageView img_itemtype = (ImageView) row.findViewById(R.id.img_itemtype);
                if(item.getIT())
                    img_itemtype.setImageResource(R.drawable.ic_veg);
                else
                    img_itemtype.setImageResource(R.drawable.ic_nonveg);

                //Item Name
                TextView tv_itemname = (TextView) row.findViewById(R.id.tv_itemname);
                tv_itemname.setText(item.getIN());
                tv_itemname.setTextSize(12);

                //Item qty
                TextView tv_qty = (TextView) row.findViewById(R.id.tv_qty);
                tv_qty.setText(item.getQTY()+"");

                //Item Price
                TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
                tv_itemprice.setText(item.getIP()*item.getQTY()+"/-");
                itemsTable.addView(row);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "inflateItemsTable", "Exception", e.getMessage());}
        return itemsTable;
    }

    public class searchString implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            List<RestaurantTableLog> validEntries = new ArrayList<>();
            searchString = et_search.getText().toString().toLowerCase().trim().replace(" ","%20");
            currentPage = 0;
            for(RestaurantTableLog item:fetchedList){
                /*if(item.getName() != null && item.getName().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getVehicle() != null && item.getVehicle().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }*/
            }
            adapterSCE.reloadList(validEntries);
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
                case R.id.img_search:
                    searchString = et_search.getText().toString().toLowerCase().trim().replace(" ","%20");
                    if(searchString == null || searchString.equalsIgnoreCase(""))
                        et_search.setError("Enter text to search");
                    else {
                        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et_search.getWindowToken(),0);
                        new getRestaurantTableLog().execute();
                    }
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onResume() {
        super.onResume();
        tv_title.setText("Table - "+mydb.getSystemParameter("TableNo")+" log");
        img_back.setVisibility(View.VISIBLE);
        this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    public class getRestaurantTableLog extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            currentPage++;
            img_loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("GUID",TableID);
                jsonObject.put("SearchString",searchString);
                jsonObject.put("PageNumber",currentPage);
                jsonObject.put("PageCount",10);
                //String parameters = "?Guid="+TableID+"&SearchString="+searchString+"&PageNumber="+currentPage+"&PageCount=10";
                response = new WebServiceCall(ActivityRestaurantTableLog.this).Get("GetRestaurantTableLog", "?json="+jsonObject.toString());
            }
            catch (Exception e){mydb.logAppError(PageName, "getRestaurantTableLog--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                JSONArray jsonArray = new JSONArray(response);
                if(jsonArray.length() == 0)
                    currentPage = -1;
                else{
                    if(jsonArray.length() < 10)
                        currentPage = -1;
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<RestaurantTableLog>>(){}.getType();
                    List<RestaurantTableLog> entries = gson.fromJson(jsonArray.toString(), type);
                    if(searchString == null || searchString.equalsIgnoreCase("")) {
                        fetchedList.addAll(entries);
                        adapterSCE.appendList(entries);
                    }
                    else{
                        adapterSCE.reloadList(entries);
                    }
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getRestaurantTableLog--onPostExecute", "Exception", e.getMessage());}
            finally {img_loading.setVisibility(View.GONE);}
        }
    }
}
