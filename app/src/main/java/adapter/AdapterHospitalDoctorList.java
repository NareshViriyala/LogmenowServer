package adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.List;

import database.DBHelper;
import shared.Models.*;

/**
 * Created by nviriyala on 04-08-2016.
 */
public class AdapterHospitalDoctorList extends BaseAdapter {
    private Context context;
    private List<DoctorInfo> DList;
    private DBHelper mydb;
    String PageName = "AdapterHospitalDoctorList";

    public AdapterHospitalDoctorList(Context context, int EntityID){
        this.context = context;
        mydb = new DBHelper(context);
        DList = mydb.getDoctorList(EntityID);
    }

    @Override
    public int getCount() {
        return DList.size();
    }

    @Override
    public Object getItem(int position) {
        return DList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_hospitaldoctor, null);
        try{
            DoctorInfo doctorInfo = DList.get(position);

            TextView tv_docName = (TextView) view.findViewById(R.id.tv_docName);
            TextView tv_docdeg = (TextView) view.findViewById(R.id.tv_docdeg);
            TextView tv_details = (TextView) view.findViewById(R.id.tv_details);
            TextView tv_email = (TextView) view.findViewById(R.id.tv_email);
            TextView tv_contact = (TextView) view.findViewById(R.id.tv_contact);
            TextView tv_guid = (TextView) view.findViewById(R.id.tv_guid);

            tv_guid.setText(doctorInfo.getGuid());
            tv_docName.setText(doctorInfo.getName());

            String formatinfo = doctorInfo.getDegree();
            if(formatinfo.equalsIgnoreCase(""))
                tv_docdeg.setVisibility(View.GONE);
            else
                tv_docdeg.setText(formatinfo);

            formatinfo = "";
            if(!doctorInfo.getSpecialty().equalsIgnoreCase("null"))
                formatinfo = formatinfo + "<u><b>Specialty</b></u>: " + doctorInfo.getSpecialty();

            if(doctorInfo.getConsultationFee() != 0)
                formatinfo = formatinfo + "<br/><u><b>Consultation Fee</b></u>: " + doctorInfo.getConsultationFee();

            if(doctorInfo.getAvgConsultationTime() != 0)
                formatinfo = formatinfo + "<br/><u><b>Consultation time</b></u>: " + doctorInfo.getAvgConsultationTime();

            Spanned textDecoration = Html.fromHtml(formatinfo);
            tv_details.setText(textDecoration);

            formatinfo = "";
            if(!doctorInfo.getMobile().equalsIgnoreCase("null"))
                formatinfo = formatinfo + "<br/><u><b>Mobile</b></u>: " + doctorInfo.getMobile();

            if(!doctorInfo.getWork().equalsIgnoreCase("null"))
                formatinfo = formatinfo + "<br/><u><b>Work</b></u>: " + doctorInfo.getWork();

            textDecoration = Html.fromHtml(formatinfo);
            if(formatinfo.equalsIgnoreCase(""))
                tv_contact.setVisibility(View.GONE);
            else
                tv_contact.setText(textDecoration);

            formatinfo = "";
            if(!doctorInfo.getEmail().equalsIgnoreCase("null"))
                formatinfo = formatinfo + "<u><b>Email</b></u>: " + doctorInfo.getEmail();
            textDecoration = Html.fromHtml(formatinfo);
            if(formatinfo.equalsIgnoreCase(""))
                tv_email.setVisibility(View.GONE);
            else
                tv_email.setText(textDecoration);

        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }
}