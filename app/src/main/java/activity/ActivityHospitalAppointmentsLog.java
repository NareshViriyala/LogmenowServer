package activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import adapter.AdapterHospitalAppointmentList;
import database.DBHelper;
import shared.GlobalClass;
import shared.Models;
import shared.NetworkDetector;
import shared.WebServiceCall;

/**
 * Created by nviriyala on 26-08-2016.
 */
public class ActivityHospitalAppointmentsLog extends AppCompatActivity implements View.OnClickListener, OnItemClickListener {
    private DBHelper mydb;
    private String PageName = "ActivityHospitalAppointmentsLog";
    private TextView tv_title;
    private ImageView img_back;
    private ImageView img_loading;
    private ImageView img_search;
    private ListView lv_entryloglst;
    private EditText et_search;
    public GlobalClass gc;
    public NetworkDetector nd;

    private int currentPage = 0;
    private boolean fetchingData = false;
    private String DocGuid, searchString;
    private Context context;

    private AdapterHospitalAppointmentList adapterSCE;
    private List<Models.AppointmentInfo> fetchedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_securitychecklog);
            mydb = new DBHelper(this);
            context = this;
            gc = (GlobalClass) this.getApplicationContext();
            nd = new NetworkDetector(this);
            DocGuid = mydb.getSystemParameter("DocGuid");
            if(DocGuid.equalsIgnoreCase(""))
                DocGuid = getIntent().getExtras().getString("DocGuid");
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
            lv_entryloglst.setOnItemClickListener(this);
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
                new getDoctorAppointmentsLog().execute();
            }
            else
                Toast.makeText(context, getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){mydb.logAppError(PageName, "fetchList", "Exception", e.getMessage());}
    }

    public void populateList(List<Models.AppointmentInfo> entries){
        try{
            if(adapterSCE == null) {
                adapterSCE = new AdapterHospitalAppointmentList(context, entries);
                lv_entryloglst.setAdapter(adapterSCE);
            }
            else{
                adapterSCE.appendApptList(entries);
            }
            fetchingData = false;
        }
        catch (Exception e){mydb.logAppError(PageName, "populateList", "Exception", e.getMessage());}
    }

    public class searchString implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            List<Models.AppointmentInfo> validEntries = new ArrayList<>();
            searchString = et_search.getText().toString().toLowerCase().trim().replace(" ","%20");
            currentPage = 0;
            for(Models.AppointmentInfo item:fetchedList){
                if(item.getPatientName().toLowerCase().contains(searchString))
                    validEntries.add(item);
            }
            adapterSCE.reloadLog(validEntries);
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
                    if(searchString == null || searchString.equalsIgnoreCase(""))
                        et_search.setError("Enter text to search");
                    else {
                        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et_search.getWindowToken(),0);
                        new getDoctorAppointmentsLog().execute(searchString);
                    }
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
            if(view instanceof ImageView){
                View parentView = (View) view.getParent().getParent();
                TextView tv_patientphone = (TextView) parentView.findViewById(R.id.tv_patientphone);
                String ph = tv_patientphone.getText().toString();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+ph));
                startActivity(callIntent);
                return;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onResume() {
        super.onResume();
        tv_title.setText("Appointments log");
        img_back.setVisibility(View.VISIBLE);
        this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    public class getDoctorAppointmentsLog extends AsyncTask<String, Integer, String> {

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
                jsonObject.put("GUID",DocGuid);
                jsonObject.put("SearchString",searchString);
                jsonObject.put("PageNumber",currentPage);
                jsonObject.put("PageCount",10);
                //String parameters = "{Guid="+DocGuid+"&SearchString="+searchString+"&PageNumber="+currentPage+"&PageCount=10}";
                response = new WebServiceCall(context).Get("GetDoctorAppointmentLog", "?json="+jsonObject.toString());
            }
            catch (Exception e){mydb.logAppError(PageName, "getDoctorAppointmentsLog--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                JSONArray jsonArray = new JSONArray(response);
                if(jsonArray.length() == 0) {
                    currentPage = -1;
                    Toast.makeText(context, "No results", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(jsonArray.length() < 10)
                        currentPage = -1;
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Models.AppointmentInfo>>(){}.getType();
                    List<Models.AppointmentInfo> entries = gson.fromJson(jsonArray.toString(), type);
                    if(searchString == null || searchString.equalsIgnoreCase("")) {
                        fetchedList.addAll(entries);
                        populateList(entries);
                    }
                    else{
                        adapterSCE.reloadLog(entries);
                    }
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getDoctorAppointmentsLog--onPostExecute", "Exception", e.getMessage());}
            finally {img_loading.setVisibility(View.GONE);}
        }
    }
}
