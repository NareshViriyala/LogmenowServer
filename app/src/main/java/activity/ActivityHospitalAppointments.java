package activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenowserver.R;

import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.AdapterHospitalAppointmentList;
import database.DBHelper;
import gcm.PushNotification;
import shared.CommonClasses;
import shared.WebServiceCall;

/**
 * Created by Home on 8/4/2016.
 */
public class ActivityHospitalAppointments extends AppCompatActivity implements OnClickListener, OnItemClickListener{

    private DBHelper mydb;
    private String PageName = "ActivityHospitalAppointments";
    private TextView tv_title;
    private TextView tv_clear;
    private ImageView img_back;
    private ImageView img_home;
    private ImageView img_sync;
    private ImageView img_more;
    private ImageView img_loading;

    private View vw_footer;
    //private ImageView img_sync;

    private LinearLayout lv_docnames;
    private AdapterHospitalAppointmentList hlAdapter;

    private ListView lv_apptlst;
    private String DocGuid;
    private CommonClasses cc;
    private int EntityID;

    //When horizontal scroll view contains multiple doctors and then the user clicks on sync icon
    //the list of doctors is added again the the horizontal scroll view
    //below tempGuid is needed to fix that scenario
    private String tempGuid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_hospitalappointments);
            mydb = new DBHelper(this);
            cc = new CommonClasses(this);
            DocGuid = mydb.getSystemParameter("DocGuid");
            EntityID = Integer.parseInt(mydb.getSystemParameter("EntityID"));
            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);

            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_home = (ImageView) findViewById(R.id.img_home);
            img_home.setOnClickListener(this);
            //img_sync = (ImageView) findViewById(R.id.img_sync);
            //img_sync.setOnClickListener(this);
            img_more = (ImageView) findViewById(R.id.img_more);
            img_more.setOnClickListener(this);
            img_loading = (ImageView) findViewById(R.id.img_loading);
            Glide.with(this).load(R.drawable.loading).into(img_loading);

            vw_footer = findViewById(R.id.vw_footer);
            inflateFooter();

            lv_apptlst = (ListView) findViewById(R.id.lv_apptlst);
            lv_apptlst.setOnItemClickListener(this);

            lv_docnames = (LinearLayout) findViewById(R.id.lv_docnames);

            if(mydb.getRecordCount("tbl_appointmentlist", DocGuid) == 0)
                new getAppointments().execute();
            else
                loadAppointments(DocGuid);

        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void loadAppointments(String guid){
        try{
            img_loading.setVisibility(View.GONE);
            if(!tempGuid.equalsIgnoreCase("")){
                DocGuid = tempGuid;
                guid = tempGuid;
            }
            if(guid.equalsIgnoreCase(""))
                DocGuid = getDocGuid();
            else {
                tv_title.setText(mydb.getDoctorName(DocGuid));
                if(tempGuid.equalsIgnoreCase(""))
                    lv_docnames.setVisibility(View.GONE);
                else
                    tempGuid = "";
                hlAdapter = new AdapterHospitalAppointmentList(this, DocGuid);
                lv_apptlst.setAdapter(hlAdapter);
            }
            tv_clear.setText("Clear: "+mydb.getRecordCount("tbl_appointmentlist", DocGuid));
        }
        catch (Exception e){mydb.logAppError(PageName, "loadDoctors", "Exception", e.getMessage());}
    }

    public String getDocGuid(){
        String firstDocGuid = "";
        try{
            List<String> guidList = mydb.getGuidList(EntityID);
            tv_title.setText(mydb.getDoctorName(guidList.get(0)));
            firstDocGuid = guidList.get(0);
            if(guidList.size() == 1){
                lv_docnames.setVisibility(View.GONE);
            }
            else{
                lv_docnames.setVisibility(View.VISIBLE);
                LayoutInflater inflater = getLayoutInflater();
                for(String guid:guidList) {
                    LinearLayout ll_doctab = (LinearLayout) inflater.inflate(R.layout.item_hospitaldocapptlist, null);
                    /*if(guidList.size() == 2){
                        Resources resources = getResources();
                        Configuration config = resources.getConfiguration();
                        DisplayMetrics dm = resources.getDisplayMetrics();
                        double screenWidthInPixels = (double)config.screenWidthDp * dm.density;
                        double screenHeightInPixels = (double)config.screenHeightDp * dm.density;
                    }*/
                    ll_doctab.setOnClickListener(this);
                    TextView tv_docName = (TextView) ll_doctab.findViewById(R.id.tv_docName);
                    TextView tv_guid = (TextView) ll_doctab.findViewById(R.id.tv_guid);
                    tv_docName.setText(mydb.getDoctorName(guid));
                    tv_guid.setText(guid);
                    lv_docnames.addView(ll_doctab);
                }
                docTabClick(guidList.get(0));
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "setFirstDocGuid", "Exception", e.getMessage());}
        return firstDocGuid;
    }

    public void docTabClick(String guid){
        try{
            tv_clear.setText("Clear: "+mydb.getRecordCount("tbl_appointmentlist", guid));
            if(lv_docnames.getVisibility() == View.GONE){
                hlAdapter.reloadApptList(guid);
                return;
            }

            for(int i=0; i < lv_docnames.getChildCount(); i++){
                LinearLayout ll_doctab = (LinearLayout) lv_docnames.getChildAt(i);
                TextView tv_guid = (TextView) ll_doctab.findViewById(R.id.tv_guid);
                if(tv_guid.getText().toString().equalsIgnoreCase(guid)) {
                    ImageView img_notify = (ImageView) ll_doctab.findViewById(R.id.img_notify);
                    img_notify.setVisibility(View.INVISIBLE);
                    ll_doctab.setBackgroundResource(R.drawable.border_doctabselected);
                    tv_title.setText(mydb.getDoctorName(guid));
                    if(hlAdapter == null) {
                        hlAdapter = new AdapterHospitalAppointmentList(this, guid);
                        lv_apptlst.setAdapter(hlAdapter);
                    }
                    else
                        hlAdapter.reloadApptList(guid);
                }
                else
                    ll_doctab.setBackgroundResource(R.drawable.border_doctabunselected);
            }

            //situation where Appointments Screen is open and the doctor has no appointments
            //a push notification arrives with the new appointment
            //below code is the populate the listview with the new appointment
            if(lv_docnames.getChildCount() == 0){
                hlAdapter = new AdapterHospitalAppointmentList(this, DocGuid);
                lv_apptlst.setAdapter(hlAdapter);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "docTabClick", "Exception", e.getMessage());}
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
                case R.id.ll_appointment:
                    if(DocGuid.equalsIgnoreCase("")){
                        Toast.makeText(ActivityHospitalAppointments.this, "Select Doctor", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        return;
                    }
                    showTakeApptPopup();
                    break;
                case R.id.ll_clear:
                    mydb.deleteCompletedAppointments(DocGuid);
                    tv_clear.setText("Clear: "+mydb.getRecordCount("tbl_appointmentlist", DocGuid));
                    break;
                case R.id.ll_sync:
                    if(lv_docnames.getVisibility() == View.VISIBLE) {
                        tempGuid = DocGuid;
                        DocGuid = "";
                    }
                    new getAppointments().execute();
                    break;
                case R.id.ll_search:
                    Intent intent = new Intent(ActivityHospitalAppointments.this, ActivityHospitalAppointmentsLog.class);
                    intent.putExtra("DocGuid", DocGuid);
                    startActivity(intent);
                    break;
                case R.id.ll_doctab:
                    TextView tv_guid = (TextView)  v.findViewById(R.id.tv_guid);
                    DocGuid = tv_guid.getText().toString();
                    docTabClick(DocGuid);
                    //tv_clear.setText("Clear: "+mydb.getRecordCount("tbl_appointmentlist", DocGuid));
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
        img_back.setVisibility(View.VISIBLE);
        this.registerReceiver(mMessageReceiver, new IntentFilter(getResources().getString(R.string.package_name)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mMessageReceiver);
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
            String newguid = intent.getStringExtra("guid");
            if(DocGuid.equalsIgnoreCase(newguid))
                docTabClick(newguid);
            else if(DocGuid.equalsIgnoreCase("")){
                /*DocGuid = newguid;
                docTabClick(DocGuid);*/
                loadAppointments("");
            }
            else{
                if(lv_docnames.getVisibility() == View.VISIBLE){
                    for(int i=0; i < lv_docnames.getChildCount(); i++){
                        LinearLayout ll_doctab = (LinearLayout) lv_docnames.getChildAt(i);
                        TextView tv_guid = (TextView) ll_doctab.findViewById(R.id.tv_guid);
                        if(tv_guid.getText().toString().equalsIgnoreCase(newguid)) {
                            ImageView img_notify = (ImageView) ll_doctab.findViewById(R.id.img_notify);
                            img_notify.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {

            if(view instanceof LinearLayout) {
                TextView tv_timer = (TextView) view.findViewById(R.id.tv_timer);
                if(tv_timer.getText().toString().equalsIgnoreCase("New")) {
                    TextView tv_apptid = (TextView) view.findViewById(R.id.tv_apptid);
                    String ApptID = tv_apptid.getText().toString().replace("Appt ID: ", "");
                    mydb.setAppointmentStatus(Integer.parseInt(ApptID), "JustIn");
                    tv_timer.setVisibility(View.INVISIBLE);
                }
                return;
            }

            if(view instanceof ImageView){
                View parentView = (View) view.getParent().getParent();
                TextView tv_patientphone = (TextView) parentView.findViewById(R.id.tv_patientphone);
                String ph = tv_patientphone.getText().toString();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+ph));
                startActivity(callIntent);
                return;
            }

            View parentView = (View) view.getParent().getParent();
            TextView tv_apptid = (TextView) parentView.findViewById(R.id.tv_apptid);
            TextView tv = (TextView)view;
            String ApptID = tv_apptid.getText().toString().replace("Appt ID: ","");
            String tv_text = tv.getText().toString();

            switch (view.getId()){
                case R.id.tv_call:
                    switch (tv_text){
                        case "Call":
                            tv.setText("Calling...");
                            new setAppointmentStatus().execute(ApptID, "In", "1", mydb.getDeviceID());
                            break;
                        case "Calling...":
                            break;
                        case "Close":
                            tv.setText("Closing...");
                            new setAppointmentStatus().execute(ApptID, "Out", "1", mydb.getDeviceID());
                            break;
                        case "Closing...":
                            break;
                        case "Closed":
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.tv_cancel:
                    switch (tv_text){
                        case "Cancel":
                            tv.setText("Cancelling...");
                            new setAppointmentStatus().execute(ApptID, "DocCancelled", "1", mydb.getDeviceID());
                            break;
                        case "Cancelling...":
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    public class getAppointments extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            img_sync.setImageResource(R.drawable.ic_syncing);
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("DeviceID", mydb.getDeviceID());
                jsonObject.put("HospitalID", mydb.getSystemParameter("EntityID"));
                jsonObject.put("Guid", "0");
                //String queryParams = "DeviceID="+mydb.getDeviceID()+"&HospitalID="+mydb.getSystemParameter("EntityID");
                //String queryParams = "{'DeviceID':"+mydb.getDeviceID()+",'HospitalID':"+mydb.getSystemParameter("HospitalID")+",'Guid':"+null+"}";
                /*if(!DocGuid.equalsIgnoreCase(""))
                    queryParams = queryParams + "&Guid="+DocGuid;*/
                response = new WebServiceCall(ActivityHospitalAppointments.this).Get("GetAppointmentList","?json="+jsonObject.toString());
                //response = new WebServiceCall(ActivityHospitalAppointments.this).asmxPost("DoctorServer","GetAppointmentList",queryParams);
            }
            catch (Exception e){mydb.logAppError(PageName, "getAppointments--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                if(response.equalsIgnoreCase("[]")) {
                    Toast.makeText(ActivityHospitalAppointments.this, "No appointments", Toast.LENGTH_SHORT).show();
                    img_loading.setVisibility(View.GONE);
                }
                else {
                    mydb.addAppointmentList(response);
                    loadAppointments(DocGuid);
                }
                img_sync.setImageResource(R.drawable.ic_sync);
            }
            catch (Exception e){mydb.logAppError(PageName, "getAppointments--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class setAppointmentStatus extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                //String QueryParams = "?ApptID="+params[0]+"&ct="+params[1]+"&cf="+params[2]+"&deviceid="+params[3];
                JSONObject jobj = new JSONObject();
                jobj.put("ApptID",params[0]);
                jobj.put("CallType",params[1]);
                jobj.put("CallFrom",params[2]);
                jobj.put("DeviceID",params[3]);

                response = new WebServiceCall(ActivityHospitalAppointments.this).Get("SetDoctorAppointment","?json="+jobj.toString());
                //response = new WebServiceCall(ActivityHospitalAppointments.this).asmxPost("DoctorCommon","UpdateAppointmentStatus", "{'input':'"+jobj.toString()+"'}");
                if(response.equalsIgnoreCase("Success"))
                    response = "{\"ApptID\":"+params[0]+", \"Status\":Success, \"CallType\":"+params[1]+"}";
                else
                    response = "{\"ApptID\":"+params[0]+", \"Status\":Failed, \"CallType\":"+params[1]+"}";
            }
            catch (Exception e){mydb.logAppError(PageName, "setAppointmentStatus--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                JSONObject json = new JSONObject(response);
                int ApptID = json.getInt("ApptID");
                if(json.getString("Status").equalsIgnoreCase("Success")){

                    switch (json.getString("CallType")) {
                        case "In":
                            mydb.setAppointmentStatus(ApptID, "InTime");
                            break;
                        case "Out":
                            mydb.setAppointmentStatus(ApptID, "OutTime");
                            break;
                        case "DocCancelled":
                            mydb.setAppointmentStatus(ApptID, "InTime");
                            mydb.setAppointmentStatus(ApptID, "OutTime");
                            mydb.setAppointmentStatus(ApptID, "UserCancelled");
                            break;
                        default:
                            break;
                    }
                    docTabClick(DocGuid);
                    new PushNotification(ActivityHospitalAppointments.this).execute();
                }
                else{

                }
            }
            catch (Exception e){mydb.logAppError(PageName, "setAppointmentStatus--onPostExecute", "Exception", e.getMessage());}
        }
    }

    private void showTakeApptPopup(){
        try{
            LayoutInflater layoutInflater = getLayoutInflater();
            final View popupView = layoutInflater.inflate(R.layout.dialog_hospitalpatientdetails, null);
            final PopupWindow popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
            Button btn_ok = (Button) popupView.findViewById(R.id.btn_ok);
            Button btn_cancel = (Button) popupView.findViewById(R.id.btn_cancel);

            final EditText et_patientname = (EditText) popupView.findViewById(R.id.et_patientname);
            final EditText et_ageyears = (EditText) popupView.findViewById(R.id.et_ageyears);
            final EditText et_agemonths = (EditText) popupView.findViewById(R.id.et_agemonths);
            final RadioGroup rg_gender = (RadioGroup) popupView.findViewById(R.id.rg_gender);
            final EditText et_patientphone = (EditText) popupView.findViewById(R.id.et_patientphone);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String status = validatePatientDetails(et_patientname.getText().toString(), "Name");
                    if(status != null) {
                        Toast.makeText(ActivityHospitalAppointments.this, status, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    status = validatePatientDetails(et_ageyears.getText().toString(), "AgeYear");
                    if(status != null) {
                        Toast.makeText(ActivityHospitalAppointments.this, status, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    status = validatePatientDetails(et_agemonths.getText().toString(), "AgeMonth");
                    if(status != null) {
                        Toast.makeText(ActivityHospitalAppointments.this, status, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(et_ageyears.getText().toString().equalsIgnoreCase("") && et_agemonths.getText().toString().equalsIgnoreCase("")){
                        Toast.makeText(ActivityHospitalAppointments.this, "Please enter age", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    status = validatePatientDetails(et_patientphone.getText().toString(), "Phone");
                    if(status != null) {
                        //Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                        et_patientphone.setError(status);
                        return;
                    }

                    int gender = rg_gender.getCheckedRadioButtonId();
                    if(gender == -1){
                        Toast.makeText(ActivityHospitalAppointments.this, "Select Gender", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RadioButton rb = (RadioButton) rg_gender.findViewById(gender);
                    String gen = rb.getText().toString();
                    gender = gen.equalsIgnoreCase("Male")?1:0;
                    String[] push = {DocGuid, et_patientname.getText().toString(), et_ageyears.getText().toString(), et_agemonths.getText().toString(), String.valueOf(gender), et_patientphone.getText().toString()};
                    new takeAppointment().execute(push);
                    popupWindow.dismiss();
                }
            });
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
            popupWindow.setAnimationStyle(R.style.DialogAnimation);
            popupWindow.showAtLocation(popupView, Gravity.TOP, 0, 100);
        }
        catch (Exception e){mydb.logAppError(PageName, "showTakeApptPopup", "Exception", e.getMessage());}
    }

    public String validatePatientDetails(String text, String type){
        String retValue = null;
        try{
            switch (type){
                case "Name":
                    if(text.equalsIgnoreCase("") || text == null)
                        return "Empty Name";
                    for (char c: text.toCharArray()) {
                        if(Character.isDigit(c))
                            return "Name has numeric values";
                        else{
                            Pattern pattern = Pattern.compile("^[A-Za-z\\s]$");
                            Matcher matcher = pattern.matcher(c+"");
                            if(!matcher.matches())
                                return "Name has special characters";
                        }
                    }
                    break;
                case "AgeYear":
                    if(text.equalsIgnoreCase(""))
                        return null;

                    if(!cc.isInteger(text))
                        return "Age invalid value";
                    int ageyear = Integer.parseInt(text);
                    if(ageyear < 0 || ageyear > 125)
                        return "years should be between 0 and 125";
                    break;
                case "AgeMonth":
                    if(text.equalsIgnoreCase(""))
                        return null;
                    if(!cc.isInteger(text))
                        return "Age invalid value";
                    int agemonth = Integer.parseInt(text);
                    if(agemonth < 0 || agemonth > 12)
                        return "months should be between 0 and 12";
                    break;
                case "Phone":
                    for (char c: text.toCharArray()) {
                        if(!Character.isDigit(c))
                            return "Phone number contains non numeric value(s)";
                    }
                    if(text.length() < 10 || text.length() > 12)
                        return "Not a valid phone number";
                    if(text.length() == 10 && text.charAt(0) == '0')
                        return "Not a valid phone number";
                    if(text.length() == 11 && text.charAt(0) != '0')
                        return "Not a valid phone number";
                    if(text.length() == 12 && !(text.substring(0, 2).equalsIgnoreCase("91")))
                        return "Not a valid phone number";
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "validatePatientDetails", "Exception", e.getMessage());}
        return retValue;
    }

    public class takeAppointment extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            try {

                JSONObject jobj = new JSONObject();
                jobj.put("DocGuid",params[0]);
                jobj.put("PatientName",params[1]);
                jobj.put("AgeYear",(params[2].equalsIgnoreCase(""))?"0":params[2]);
                jobj.put("AgeMonth",(params[3].equalsIgnoreCase(""))?"0":params[3]);
                jobj.put("Gender",params[4]);
                jobj.put("PatientPhone",params[5]);
                jobj.put("PatientDeviceID",mydb.getDeviceID());
                jobj.put("CurrApptID","0");

                String DGuid = params[0];
                String pn = params[1].replace(" ", "%20");
                String ay = params[2];
                if(ay.equalsIgnoreCase(""))
                    ay = "0";
                String am = params[3];
                if(am.equalsIgnoreCase(""))
                    am = "0";
                String gen = params[4];
                String patientphone = params[5];
                String pdid = mydb.getDeviceID();

                String ApptID = "0";

                String ParamString = "?DGuid="+DGuid+"&pn="+pn+"&ay="+ay+"&am="+am+"&gen="+gen+"&pdid="+pdid+"&currapt="+ApptID;
                //response = new WebServiceCall(ActivityHospitalAppointments.this).Get("DoctorAppointment", ParamString);

                response = new WebServiceCall(ActivityHospitalAppointments.this).Get("AddDoctorAppointment", "?json="+jobj.toString());
                //response = new WebServiceCall(ActivityHospitalAppointments.this).asmxPost("DoctorCommon","AddDoctorAppointment", ParamString);
            }
            catch (Exception e){mydb.logAppError(PageName, "takeAppointment--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new PushNotification(ActivityHospitalAppointments.this).execute();
        }
    }

    public void inflateFooter(){
        try{
            LinearLayout ll_search = (LinearLayout) vw_footer.findViewById(R.id.ll_search);
            LinearLayout ll_appointment = (LinearLayout) vw_footer.findViewById(R.id.ll_appointment);
            LinearLayout ll_sync = (LinearLayout) vw_footer.findViewById(R.id.ll_sync);
            LinearLayout ll_clear = (LinearLayout) vw_footer.findViewById(R.id.ll_clear);

            img_sync = (ImageView) vw_footer.findViewById(R.id.img_sync);
            tv_clear = (TextView) vw_footer.findViewById(R.id.tv_clear);
            ll_search.setOnClickListener(this);
            ll_appointment.setOnClickListener(this);
            ll_clear.setOnClickListener(this);
            ll_sync.setOnClickListener(this);

            //tv_clear.setText("Clear: "+mydb.getRecordCount("tbl_appointmentlist"));
        }
        catch (Exception e){mydb.logAppError(PageName, "inflateFooter", "Exception", e.getMessage());}
    }
}
