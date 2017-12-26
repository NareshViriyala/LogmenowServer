package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.home.logmenowserver.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import activity.ActivityParkingScanQR;
import database.DBHelper;
import shared.CommonClasses;
import shared.Models.ParkingInfo;

/**
 * Created by Home on 8/27/2016.
 */
public class DialogParkingUsage extends Dialog implements View.OnClickListener{

    private DBHelper mydb;
    private ActivityParkingScanQR activityParkingScanQR;
    private String PageName = "DialogParkingUsage";

    private Button btn_overrideusg;
    private Button btn_cancelusg;
    private ImageView img_usgVehType;
    private TextView tv_usgVehNo;
    private TextView tv_usagetext;

    private CommonClasses cc;
    private ParkingInfo row;

    public DialogParkingUsage(ActivityParkingScanQR activityParkingScanQR) {
        super(activityParkingScanQR);
        mydb = new DBHelper(activityParkingScanQR);
        this.activityParkingScanQR = activityParkingScanQR;

        cc = new CommonClasses(activityParkingScanQR);
    }

    public void pushRow(ParkingInfo row){
        this.row = row;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_parkingusage);
        this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        btn_overrideusg = (Button) findViewById(R.id.btn_overrideusg);
        btn_cancelusg = (Button) findViewById(R.id.btn_cancelusg);
        btn_overrideusg.setOnClickListener(this);
        btn_cancelusg.setOnClickListener(this);

        tv_usgVehNo = (TextView)findViewById(R.id.tv_usgVehNo);
        tv_usgVehNo.setText(row.getVehicleNo());
        img_usgVehType = (ImageView) findViewById(R.id.img_usgVehType);
        img_usgVehType.setImageResource(activityParkingScanQR.getResources().getIdentifier("wheeler"+row.getVehicleType(), "drawable", activityParkingScanQR.getPackageName()));

        tv_usagetext = (TextView)findViewById(R.id.tv_usagetext);
        if(row.getUsage() == 1){
            tv_usagetext.setText("An entry already exists for this vehicle at \nDate: "+row.getServerEntryDate().substring(0,10)+"\nTime: "+cc.get12HourFormat(row.getServerEntryDate()));
        }else{
            tv_usagetext.setText("No entry exists for this Vehicle");
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_overrideusg:
                Gson gson = new Gson();
                Type ParkingInfotype = new TypeToken<ParkingInfo>(){}.getType();
                String lst = "["+gson.toJson(row, ParkingInfotype).replace(" ", "%20")+"]";
                String input = "<entry><ServerID>0</ServerID><EntityID>"+row.getEntityID()+"</EntityID>";
                input = input + "<ServerDeviceID>"+mydb.getDeviceID()+"</ServerDeviceID>";
                input = input + "<LocalDBID>"+row.getDBID()+"</LocalDBID>";
                input = input + "<LocalDBTime>"+row.getLocalTime()+"</LocalDBTime>";
                input = input + "<DeviceID>"+row.getDeviceID()+"</DeviceID>";
                input = input + "<VehicleNo>"+row.getVehicleNo()+"</VehicleNo>";
                input = input + "<VehicleType>"+row.getVehicleType()+"</VehicleType>";
                input = input + "<TCTID>"+row.getTariffType()+"</TCTID>";
                input = input + "<AddType>"+row.getUsage()+"</AddType>";
                input = input + "<OverRide>"+row.getOverRide()+"</OverRide>";
                input = input + "</entry>";
                activityParkingScanQR.new pushToserver().execute(input);
                dismiss();
                break;
            case R.id.btn_cancelusg:
                mydb.deleteParkingRow(row.getDBID());
                activityParkingScanQR.adapterSCE.removeItem(row);
                activityParkingScanQR.setFooterTextCounters();
                dismiss();
                break;
            default:
                break;
        }
    }
}
