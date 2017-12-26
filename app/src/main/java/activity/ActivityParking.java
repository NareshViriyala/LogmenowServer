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

import adapter.AdapterEntityList;
import database.DBHelper;
import shared.GlobalClass;
import shared.WebServiceCall;

/**
 * Created by nviriyala on 26-08-2016.
 */
public class ActivityParking extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private DBHelper mydb;
    private String PageName = "ActivityParking";
    private TextView tv_title;
    private TextView tv_status;
    private ImageView img_back;
    private ImageView img_home;
    private ImageView img_sync;
    private ImageView img_more;
    private ImageView img_loading;
    private ListView lv_parkinglst;
    public GlobalClass gc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_parking);
            mydb = new DBHelper(this);
            gc = (GlobalClass) this.getApplicationContext();

            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);

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

            lv_parkinglst = (ListView) findViewById(R.id.lv_parkinglst);
            lv_parkinglst.setOnItemClickListener(this);

            if(mydb.getRecordCount("tbl_parkinglist") == 0)
                new getParkingList().execute();
            else
                loadParking();
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void loadActivityParking(){
        try{
            Intent intent = new Intent(this, ActivityParkingScanQR.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }
        catch (Exception e){mydb.logAppError(PageName, "loadActivity", "Exception", e.getMessage());}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            TextView tv_id = (TextView) view.findViewById(R.id.tv_id);
            mydb.setSystemParameter("EntityID", tv_id.getText().toString());
            loadActivityParking();
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    public void loadParking(){
        try{
            img_loading.setVisibility(View.GONE);
            tv_status.setText("Select Entity");
            AdapterEntityList hlAdapter = new AdapterEntityList(this, "tbl_parkinglist");
            lv_parkinglst.setAdapter(hlAdapter);

            if(hlAdapter.getCount() == 1){
                tv_status.setText("Fetching information...");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lv_parkinglst.performItemClick(lv_parkinglst.getChildAt(0), 0, 0);
                    }
                }, 1000);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "loadHospitals", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try{
            switch (v.getId()){
                case R.id.img_back:
                    break;
                case R.id.tv_title:
                    break;
                case R.id.img_home:
                    break;
                case R.id.img_sync:
                    new getParkingList().execute();
                    break;
                case R.id.img_more:
                    Toast.makeText(ActivityParking.this, mydb.getDeviceID(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public class getParkingList extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {img_sync.setImageResource(R.drawable.ic_syncing);}

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{
                //response = new WebServiceCall(ActivityParking.this).Get("GetEntityList", "?DeviceID="+mydb.getDeviceID()+"&EntityType=Parking");
                response = new WebServiceCall(ActivityParking.this).Get("GetServerDeviceEntityList", "?json={\"DeviceID\":"+mydb.getDeviceID()+",\"EntityType\":\"Parking\"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "getParkingList--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                mydb.addEntityList(response, "tbl_parkinglist");
                loadParking();
            }
            catch (Exception e){mydb.logAppError(PageName, "getParkingList--onPostExecute", "Exception", e.getMessage());}
            finally {img_sync.setImageResource(R.drawable.ic_sync);}
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onResume() {
        super.onResume();
        tv_title.setText("Parking");
        if(lv_parkinglst.getCount() > 0)
            tv_status.setText("Select Entity");
        img_back.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
