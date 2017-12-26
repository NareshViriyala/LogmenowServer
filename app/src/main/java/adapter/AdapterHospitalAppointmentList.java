package adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.List;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models.*;

/**
 * Created by Home on 8/5/2016.
 */
public class AdapterHospitalAppointmentList extends BaseAdapter {
    private Context context;
    private List<AppointmentInfo> ApptList;
    private DBHelper mydb;
    String PageName = "AdapterHospitalAppointmentList";
    private CommonClasses cc;

    public AdapterHospitalAppointmentList(Context context, String Guid){
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
        ApptList = mydb.getAppointmentList(Guid);
    }

    public AdapterHospitalAppointmentList(Context context, List<AppointmentInfo> ApptList){
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
        this.ApptList = ApptList;
    }

    public void appendApptList(List<AppointmentInfo> additionalList){
        ApptList.addAll(additionalList);
        this.notifyDataSetChanged();
    }

    public void reloadApptList(String Guid){
        ApptList = mydb.getAppointmentList(Guid);
        this.notifyDataSetChanged();
    }

    public void reloadLog(List<AppointmentInfo> resultSet){
        this.ApptList = resultSet;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {return ApptList.size();}

    @Override
    public Object getItem(int position) {return ApptList.get(position);}

    @Override
    public long getItemId(int position) {return position;}

    public class viewHolder{
        TextView tv_apptid;
        TextView tv_PName;
        TextView tv_age;
        TextView tv_gender;
        TextView tv_qtime;
        TextView tv_intime;
        TextView tv_outtime;
        TextView tv_call;
        TextView tv_cancel;
        TextView tv_timer;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_hospitalappointment, null);
        try{
            AppointmentInfo Appt = ApptList.get(position);

            TextView tv_apptid = (TextView) view.findViewById(R.id.tv_apptid);
            TextView tv_PName = (TextView) view.findViewById(R.id.tv_PName);
            TextView tv_age = (TextView) view.findViewById(R.id.tv_age);
            TextView tv_gender = (TextView) view.findViewById(R.id.tv_gender);
            TextView tv_qtime = (TextView) view.findViewById(R.id.tv_qtime);
            TextView tv_intime = (TextView) view.findViewById(R.id.tv_intime);
            TextView tv_outtime = (TextView) view.findViewById(R.id.tv_outtime);
            TextView tv_call = (TextView) view.findViewById(R.id.tv_call);
            TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
            TextView tv_timer = (TextView) view.findViewById(R.id.tv_timer);
            ImageView img_callpatient = (ImageView) view.findViewById(R.id.img_callpatient);
            TextView tv_patientphone = (TextView) view.findViewById(R.id.tv_patientphone);

            tv_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });

            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });

            img_callpatient.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });

            String formatinfo = "<b>Appt ID</b>: "+Appt.getApptID();
            Spanned textDecoration = Html.fromHtml(formatinfo);
            tv_apptid.setText(textDecoration);
            tv_PName.setText(Appt.getPatientName());
            tv_patientphone.setText(Appt.getPatientPhone());

            formatinfo = "<b>Age</b>:";
            if(Appt.getAgeYear() != 0)
                formatinfo = formatinfo+" "+Appt.getAgeYear()+" Years";

            if(Appt.getAgeMonth() != 0)
                formatinfo = formatinfo+" "+Appt.getAgeMonth()+" Months";
            textDecoration = Html.fromHtml(formatinfo);
            tv_age.setText(textDecoration);

            formatinfo = "<b>Gender</b>:";
            if(Appt.getGender() == 1)
                formatinfo = formatinfo+" Male";
            else
                formatinfo = formatinfo+" Female";

            textDecoration = Html.fromHtml(formatinfo);
            tv_gender.setText(textDecoration);

            formatinfo = "<b>Q Time</b>: "+cc.get12HourFormat(Appt.getApptTime());
            textDecoration = Html.fromHtml(formatinfo);
            tv_qtime.setText(textDecoration);

            if(Appt.getInTime() == null)
                tv_intime.setVisibility(View.INVISIBLE);
            else{
                formatinfo = "<b>In</b>: "+cc.get12HourFormat(Appt.getInTime());
                textDecoration = Html.fromHtml(formatinfo);
                tv_intime.setText(textDecoration);
                tv_call.setText("Close");
                Appt.setJustIn(0);
                tv_timer.setVisibility(View.VISIBLE);
                tv_timer.setText(cc.getTimerValue(Appt.getInTime(), new TimeStamp().getCurrentTimeStamp()));
                view.setBackgroundResource(R.drawable.background_patientin);
            }

            if(Appt.getOutTime() == null)
                tv_outtime.setVisibility(View.INVISIBLE);
            else{
                formatinfo = "<b>Out</b>: "+cc.get12HourFormat(Appt.getOutTime());
                textDecoration = Html.fromHtml(formatinfo);
                tv_outtime.setText(textDecoration);
                tv_call.setText("Closed");
                tv_call.setBackgroundResource(R.drawable.background_patientout);
                tv_cancel.setBackgroundResource(R.drawable.background_patientout);
                Appt.setJustIn(0);
                tv_timer.setVisibility(View.VISIBLE);
                tv_timer.setText(cc.getTimerValue(Appt.getInTime(), Appt.getOutTime()));
                view.setBackgroundResource(R.drawable.background_patientout);
            }

            if(Appt.getJustIn() == 1 && mydb.getNewApptStatus(Appt.getApptID()) == 1) {
                tv_timer.setVisibility(View.VISIBLE);
                tv_timer.setText("New");
            }

            if(Appt.getUserCancelled() == 1) {
                tv_timer.setVisibility(View.VISIBLE);
                tv_timer.setText("Cancelled");
            }

            /*if(Appt.getInTime() == null && Appt.getOutTime() == null) {
                tv_call.setBackgroundResource(R.drawable.border_popup);
                tv_cancel.setBackgroundResource(R.drawable.border_popup);
                view.setBackgroundColor(context.getResources().getColor(R.color.white));
            }*/

        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }
}
