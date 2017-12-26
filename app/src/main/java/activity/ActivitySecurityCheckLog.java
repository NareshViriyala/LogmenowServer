package activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenowserver.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Entity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import adapter.AdapterSecurityCheckEntry;
import database.DBHelper;
import shared.GlobalClass;
import shared.Models;
import shared.NetworkDetector;
import shared.WebServiceCall;

/**
 * Created by nviriyala on 25-08-2016.
 */
public class ActivitySecurityCheckLog extends AppCompatActivity implements View.OnClickListener{
    private DBHelper mydb;
    private String PageName = "ActivitySecurityCheckLog";
    private TextView tv_title;
    private ImageView img_back;
    private ImageView img_loading;
    private ImageView img_search;
    private ListView lv_entryloglst;
    private EditText et_search;
    public GlobalClass gc;
    public NetworkDetector nd;
    private int EntityID;
    private int currentPage = 0;
    private boolean fetchingData = false;
    private String DeviceID, searchString;
    private AdapterSecurityCheckEntry adapterSCE;
    private List<Models.SecurityCheckEntry> fetchedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_securitychecklog);
            mydb = new DBHelper(this);
            gc = (GlobalClass) this.getApplicationContext();
            nd = new NetworkDetector(this);
            DeviceID = mydb.getDeviceID();
            EntityID = Integer.parseInt(mydb.getSystemParameter("EntityID"));
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

            lv_entryloglst = (ListView) findViewById(R.id.lv_entryloglst);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            //fetchData();

            lv_entryloglst.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                new getSecurityCheckLog().execute();
            }
            else
                Toast.makeText(ActivitySecurityCheckLog.this, getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){mydb.logAppError(PageName, "fetchList", "Exception", e.getMessage());}
    }

    public void populateList(List<Models.SecurityCheckEntry> entries){
        try{
            if(adapterSCE == null) {
                adapterSCE = new AdapterSecurityCheckEntry(ActivitySecurityCheckLog.this, entries);
                lv_entryloglst.setAdapter(adapterSCE);
            }
            else{
                adapterSCE.addEntry(entries);
            }
            fetchingData = false;
        }
        catch (Exception e){mydb.logAppError(PageName, "populateList", "Exception", e.getMessage());}
    }

    public class searchString implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            List<Models.SecurityCheckEntry> validEntries = new ArrayList<>();
            searchString = et_search.getText().toString().toLowerCase().trim().replace(" ","%20");
            currentPage = 0;
            for(Models.SecurityCheckEntry item:fetchedList){
                if(item.getName() != null && item.getName().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getVehicle() != null && item.getVehicle().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getComingFrom() != null && item.getComingFrom().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getVisitingCompany() != null && item.getVisitingCompany().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getEmail() != null && item.getEmail().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getPhone() != null && item.getPhone().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getGender() != null && item.getGender().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getPov() != null && item.getPov().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getContactPerson() != null && item.getContactPerson().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getBlock() != null && item.getBlock().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }

                if(item.getFlat() != null && item.getFlat().toLowerCase().contains(searchString)) {
                    validEntries.add(item);
                    continue;
                }
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
                        new getSecurityCheckLog().execute();
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
        tv_title.setText("Entry log");
        img_back.setVisibility(View.VISIBLE);
        this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    public class getSecurityCheckLog extends AsyncTask<String, Integer, String> {

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
                jsonObject.put("EntityID",EntityID);
                jsonObject.put("DeviceID",DeviceID);
                jsonObject.put("SearchString",searchString);
                jsonObject.put("PageNumber",currentPage);
                jsonObject.put("PageCount",10);
                //String parameters = "?EntityID="+EntityID+"&DeviceID="+DeviceID+"&SearchString="+searchString+"&PageNumber="+currentPage+"&PageCount=10";
                response = new WebServiceCall(ActivitySecurityCheckLog.this).Get("GetSecurityCheckEntryLog", "?json="+jsonObject.toString());
            }
            catch (Exception e){mydb.logAppError(PageName, "getSecurityCheckLog--doInBackground", "Exception", e.getMessage());}
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
                    Type type = new TypeToken<List<Models.SecurityCheckEntry>>(){}.getType();
                    List<Models.SecurityCheckEntry> entries = gson.fromJson(jsonArray.toString(), type);
                    for(int i = 0; i < entries.size(); i++){
                        entries.get(i).setsynced(true);
                    }
                    if(searchString == null || searchString.equalsIgnoreCase("")) {
                        fetchedList.addAll(entries);
                        populateList(entries);
                    }
                    else{
                        adapterSCE.reloadList(entries);
                    }
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getSecurityCheckLog--onPostExecute", "Exception", e.getMessage());}
            finally {img_loading.setVisibility(View.GONE);}
        }
    }
}
