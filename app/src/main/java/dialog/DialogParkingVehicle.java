package dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import activity.ActivityParkingScanQR;
import database.DBHelper;
import shared.CommonClasses;
import shared.Models.ParkingInfo;

/**
 * Created by Home on 8/28/2016.
 */
public class DialogParkingVehicle extends Dialog implements View.OnClickListener{
    private DBHelper mydb;
    private ActivityParkingScanQR activityParkingScanQR;
    private String PageName = "DialogParkingVehicle";

    private Button btn_scnd;
    private Button btn_cntx;
    private ImageView img_scannedVehicle;
    private ImageView img_contextVehicle;

    private int ScannedVehicle;
    private int ContextVehicle;

    private ParkingInfo row;

    public DialogParkingVehicle(ActivityParkingScanQR activityParkingScanQR) {
        super(activityParkingScanQR);
        mydb = new DBHelper(activityParkingScanQR);
        this.activityParkingScanQR = activityParkingScanQR;
    }

    public void setVehicleTypes(ParkingInfo row){
        this.row = row;
        this.ScannedVehicle = row.getVehicleType();
        this.ContextVehicle = Integer.parseInt(mydb.getSystemParameter("ParkingVehicleType"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_parkingvehicle);
        this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        btn_scnd = (Button) findViewById(R.id.btn_scnd);
        btn_cntx = (Button) findViewById(R.id.btn_cntx);
        btn_scnd.setOnClickListener(this);
        btn_cntx.setOnClickListener(this);

        img_scannedVehicle = (ImageView)findViewById(R.id.img_scannedVehicle);
        img_scannedVehicle.setImageResource(activityParkingScanQR.getResources().getIdentifier("wheeler"+ScannedVehicle, "drawable", activityParkingScanQR.getPackageName()));
        img_contextVehicle = (ImageView) findViewById(R.id.img_contextVehicle);
        img_contextVehicle.setImageResource(activityParkingScanQR.getResources().getIdentifier("wheeler"+ContextVehicle, "drawable", activityParkingScanQR.getPackageName()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_scnd:
                activityParkingScanQR.saveEntry(row);
                dismiss();
                break;
            case R.id.btn_cntx:
                row.setVehicleType(ContextVehicle);
                activityParkingScanQR.saveEntry(row);
                dismiss();
                break;
            default:
                break;
        }
    }
}
