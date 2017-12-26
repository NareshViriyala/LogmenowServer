package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.home.logmenowserver.R;

import activity.ActivityParkingScanQR;
import database.DBHelper;

/**
 * Created by Home on 8/27/2016.
 */
public class DialogParkingContext extends Dialog implements View.OnClickListener{

    private Context context;
    private DBHelper mydb;
    private ActivityParkingScanQR activityParkingScanQR;
    private String PageName = "DialogParkingContext";

    public Button btn_cntxSet;
    public Button btn_cntxCancel;
    public ImageView img_enter;
    public ImageView img_exit;

    public ImageView img_wheeler2;
    public ImageView img_wheeler3;
    public ImageView img_wheeler4;
    public ImageView img_wheeler0;

    public ImageView img_normaltariff;
    public ImageView img_weekendtariff;
    public ImageView img_peaktimetariff;
    public ImageView img_holidaytariff;

    public DialogParkingContext(ActivityParkingScanQR activityParkingScanQR) {
        super(activityParkingScanQR);
        this.context = activityParkingScanQR;
        mydb = new DBHelper(context);
        this.activityParkingScanQR = activityParkingScanQR;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_parkingcontext);
        this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        btn_cntxSet = (Button) findViewById(R.id.btn_cntxSet);
        btn_cntxCancel = (Button) findViewById(R.id.btn_cntxCancel);
        btn_cntxCancel.setOnClickListener(this);
        btn_cntxSet.setOnClickListener(this);

        img_enter = (ImageView) findViewById(R.id.img_enter);
        img_exit = (ImageView) findViewById(R.id.img_exit);
        img_enter.setOnClickListener(this);
        img_exit.setOnClickListener(this);
        switch (mydb.getSystemParameter("ParkingUsage")){
            case "1":
                img_enter.setBackgroundResource(R.drawable.border_selected);
                break;
            case "2":
                img_exit.setBackgroundResource(R.drawable.border_selected);
                break;
        }

        img_wheeler2 = (ImageView) findViewById(R.id.img_wheeler2);
        img_wheeler3 = (ImageView) findViewById(R.id.img_wheeler3);
        img_wheeler4 = (ImageView) findViewById(R.id.img_wheeler4);
        img_wheeler0 = (ImageView) findViewById(R.id.img_wheeler0);
        img_wheeler2.setOnClickListener(this);
        img_wheeler3.setOnClickListener(this);
        img_wheeler4.setOnClickListener(this);
        img_wheeler0.setOnClickListener(this);
        switch (mydb.getSystemParameter("ParkingVehicleType")){
            case "0":
                img_wheeler0.setBackgroundResource(R.drawable.border_selected);
                break;
            case "2":
                img_wheeler2.setBackgroundResource(R.drawable.border_selected);
                break;
            case "3":
                img_wheeler3.setBackgroundResource(R.drawable.border_selected);
                break;
            case "4":
                img_wheeler4.setBackgroundResource(R.drawable.border_selected);
                break;
        }


        img_normaltariff = (ImageView) findViewById(R.id.img_normaltariff);
        img_weekendtariff = (ImageView) findViewById(R.id.img_weekendtariff);
        img_peaktimetariff = (ImageView) findViewById(R.id.img_peaktimetariff);
        img_holidaytariff = (ImageView) findViewById(R.id.img_holidaytariff);
        img_normaltariff.setOnClickListener(this);
        img_weekendtariff.setOnClickListener(this);
        img_peaktimetariff.setOnClickListener(this);
        img_holidaytariff.setOnClickListener(this);
        switch (mydb.getSystemParameter("ParkingTariffType")){
            case "1":
                img_holidaytariff.setBackgroundResource(R.drawable.border_selected);
                break;
            case "2":
                img_normaltariff.setBackgroundResource(R.drawable.border_selected);
                break;
            case "3":
                img_peaktimetariff.setBackgroundResource(R.drawable.border_selected);
                break;
            case "4":
                img_weekendtariff.setBackgroundResource(R.drawable.border_selected);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cntxSet:
                if(mydb.getSystemParameter("ParkingUsage").equalsIgnoreCase("") || mydb.getSystemParameter("ParkingVehicleType").equalsIgnoreCase("") || mydb.getSystemParameter("ParkingTariffType").equalsIgnoreCase(""))
                    Toast.makeText(activityParkingScanQR, "Select all options", Toast.LENGTH_SHORT).show();
                else {
                    dismiss();
                    activityParkingScanQR.setParkingContext();
                }
                break;
            case R.id.btn_cntxCancel:
                dismiss();
                break;
            case R.id.img_enter:
                selectUsage("1");
                break;
            case R.id.img_exit:
                selectUsage("2");
                break;
            case R.id.img_wheeler2:
                selectVehicle("2");
                break;
            case R.id.img_wheeler3:
                selectVehicle("3");
                break;
            case R.id.img_wheeler4:
                selectVehicle("4");
                break;
            case R.id.img_wheeler0:
                selectVehicle("0");
                break;
            case R.id.img_normaltariff:
                selectTariff("2");
                break;
            case R.id.img_weekendtariff:
                selectTariff("4");
                break;
            case R.id.img_peaktimetariff:
                selectTariff("3");
                break;
            case R.id.img_holidaytariff:
                selectTariff("1");
                break;
            default:
                break;
        }
    }

    public void selectUsage(String usage){
        try{
            if(usage.equalsIgnoreCase("1")){
                img_enter.setBackgroundResource(R.drawable.border_selected);
                img_exit.setBackgroundResource(R.drawable.border_unselected);
                mydb.setSystemParameter("ParkingUsage","1");
            }
            else{
                img_exit.setBackgroundResource(R.drawable.border_selected);
                img_enter.setBackgroundResource(R.drawable.border_unselected);
                mydb.setSystemParameter("ParkingUsage","2");
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "selectUsage", "Exception", e.getMessage());
            mydb.setSystemParameter("ParkingUsage","");
        }
    }

    public void selectVehicle(String vehicleType){
        try{
            if(vehicleType.equalsIgnoreCase("2")){
                img_wheeler2.setBackgroundResource(R.drawable.border_selected);
                img_wheeler3.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler4.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler0.setBackgroundResource(R.drawable.border_unselected);
                mydb.setSystemParameter("ParkingVehicleType","2");
            }
            if(vehicleType.equalsIgnoreCase("3")){
                img_wheeler2.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler3.setBackgroundResource(R.drawable.border_selected);
                img_wheeler4.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler0.setBackgroundResource(R.drawable.border_unselected);
                mydb.setSystemParameter("ParkingVehicleType","3");
            }
            if(vehicleType.equalsIgnoreCase("4")){
                img_wheeler2.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler3.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler4.setBackgroundResource(R.drawable.border_selected);
                img_wheeler0.setBackgroundResource(R.drawable.border_unselected);
                mydb.setSystemParameter("ParkingVehicleType","4");
            }
            if(vehicleType.equalsIgnoreCase("0")){
                img_wheeler2.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler3.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler4.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler0.setBackgroundResource(R.drawable.border_selected);
                mydb.setSystemParameter("ParkingVehicleType","0");
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "selectVehicle", "Exception", e.getMessage());
            mydb.setSystemParameter("ParkingVehicleType","");
        }
    }

    public void selectTariff(String tariff){
        try{
            if(tariff.equalsIgnoreCase("2")){
                img_normaltariff.setBackgroundResource(R.drawable.border_selected);
                img_weekendtariff.setBackgroundResource(R.drawable.border_unselected);
                img_peaktimetariff.setBackgroundResource(R.drawable.border_unselected);
                img_holidaytariff.setBackgroundResource(R.drawable.border_unselected);
                mydb.setSystemParameter("ParkingTariffType","2");
            }
            if(tariff.equalsIgnoreCase("4")){
                img_normaltariff.setBackgroundResource(R.drawable.border_unselected);
                img_weekendtariff.setBackgroundResource(R.drawable.border_selected);
                img_peaktimetariff.setBackgroundResource(R.drawable.border_unselected);
                img_holidaytariff.setBackgroundResource(R.drawable.border_unselected);
                mydb.setSystemParameter("ParkingTariffType","4");
            }
            if(tariff.equalsIgnoreCase("3")){
                img_normaltariff.setBackgroundResource(R.drawable.border_unselected);
                img_weekendtariff.setBackgroundResource(R.drawable.border_unselected);
                img_peaktimetariff.setBackgroundResource(R.drawable.border_selected);
                img_holidaytariff.setBackgroundResource(R.drawable.border_unselected);
                mydb.setSystemParameter("ParkingTariffType","3");
            }
            if(tariff.equalsIgnoreCase("1")){
                img_normaltariff.setBackgroundResource(R.drawable.border_unselected);
                img_weekendtariff.setBackgroundResource(R.drawable.border_unselected);
                img_peaktimetariff.setBackgroundResource(R.drawable.border_unselected);
                img_holidaytariff.setBackgroundResource(R.drawable.border_selected);
                mydb.setSystemParameter("ParkingTariffType","1");
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "selectTariff", "Exception", e.getMessage());
            mydb.setSystemParameter("ParkingTariffType","");
        }
    }
}
