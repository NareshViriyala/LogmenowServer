package activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenowserver.R;

import adapter.AdapterHospitalDoctorList;
import database.DBHelper;
import shared.WebServiceCall;

/**
 * Created by nviriyala on 04-08-2016.
 */
public class ActivityHospitalDoctor extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private DBHelper mydb;
    private String PageName = "ActivityHospitalDoctor";
    private TextView tv_title;
    private TextView tv_status;
    private TextView tv_selectall;
    private ImageView img_back;
    private ImageView img_home;
    private ImageView img_sync;
    private ImageView img_more;
    private ImageView img_loading;

    private ListView lv_doctorlst;

    private int EntityID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_hospitaldoctor);
            mydb = new DBHelper(this);
            EntityID = Integer.parseInt(mydb.getSystemParameter("EntityID"));

            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);


            tv_selectall = (TextView) findViewById(R.id.tv_selectall);
            tv_selectall.setOnClickListener(this);

            tv_status = (TextView) findViewById(R.id.tv_status);
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

            lv_doctorlst = (ListView) findViewById(R.id.lv_doctorlst);
            lv_doctorlst.setOnItemClickListener(this);

            if(mydb.getRecordCount("tbl_doctorlist") == 0)
                new getDoctorList().execute();
            else
                loadDoctors();
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void loadActivityAppointments(String guid){
        try{
            Intent intent = new Intent(this, ActivityHospitalAppointments.class);
            mydb.setSystemParameter("DocGuid", guid);
            startActivity(intent);
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }
        catch (Exception e){mydb.logAppError(PageName, "loadActivityAppointments", "Exception", e.getMessage());}
    }

    public void loadDoctors(){
        try{
            img_loading.setVisibility(View.GONE);
            tv_status.setText("Select Doctor");
            AdapterHospitalDoctorList hlAdapter = new AdapterHospitalDoctorList(this, EntityID);
            lv_doctorlst.setAdapter(hlAdapter);

            if(hlAdapter.getCount() == 1){
                tv_status.setText("Fetching appointments...");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadActivityAppointments("");
                    }
                }, 1000);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "loadDoctors", "Exception", e.getMessage());}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            TextView tv_guid = (TextView) view.findViewById(R.id.tv_guid);
            loadActivityAppointments(tv_guid.getText().toString());
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
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
                    new getDoctorList().execute();
                    break;
                case R.id.img_more:
                    break;
                case R.id.tv_selectall:
                    loadActivityAppointments("");
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
        tv_title.setText("Doctor");
        tv_status.setText("Select Doctor");
        img_back.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public class getDoctorList extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {img_sync.setImageResource(R.drawable.ic_syncing);}

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                response = new WebServiceCall(ActivityHospitalDoctor.this).Get("GetDoctorList", "?json={\"DeviceID\":\""+mydb.getDeviceID()+"\",\"HospitalID\":"+EntityID+"}");
                //response = new WebServiceCall(ActivityHospitalDoctor.this).asmxPost("DoctorServer", "GetDoctorList", "{'DeviceID':"+mydb.getDeviceID()+",'HospitalID':"+hospitalID+"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "getDoctorList--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                if(response.equalsIgnoreCase("[]")) {
                    Toast.makeText(ActivityHospitalDoctor.this, "No doctors registered", Toast.LENGTH_SHORT).show();
                }
                mydb.addDoctorList(response);

                loadDoctors();
            }
            catch (Exception e){mydb.logAppError(PageName, "getDoctorList--onPostExecute", "Exception", e.getMessage());}
            finally {img_sync.setImageResource(R.drawable.ic_sync);}
        }
    }
}
