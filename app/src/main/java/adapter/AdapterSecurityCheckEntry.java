package adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.List;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models.*;

/**
 * Created by nviriyala on 24-08-2016.
 */
public class AdapterSecurityCheckEntry extends BaseAdapter {

    private Context context;
    private List<SecurityCheckEntry> EntryLst;
    private DBHelper mydb;
    String PageName = "AdapterSecurityCheckEntry";
    private int maxitems = 5;
    private CommonClasses cc;

    public AdapterSecurityCheckEntry(Context context, List<SecurityCheckEntry> EntryLst){
        this.context = context;
        mydb = new DBHelper(context);
        this.EntryLst = EntryLst;
        cc = new CommonClasses(context);
    }

    public void addEntry(SecurityCheckEntry newEntry){
        EntryLst.add(0, newEntry);
        while (EntryLst.size()>maxitems){
            EntryLst.remove(maxitems);
        }
        notifyDataSetChanged();
    }

    public void addEntry(List<SecurityCheckEntry> newEntries){
        EntryLst.addAll(newEntries);
        notifyDataSetChanged();
    }

    public void refreshList(int DBID, boolean update){
        for(int i = 0 ; i<EntryLst.size(); i++){
            if(EntryLst.get(i).getDBID() == DBID)
                EntryLst.get(i).setsynced(update);
        }
        notifyDataSetChanged();
    }

    public void reloadList(List<SecurityCheckEntry> newEntries){
        this.EntryLst = newEntries;
        notifyDataSetChanged();
    }

    public void clearList(){
        EntryLst.clear();
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return EntryLst.size();
    }

    @Override
    public Object getItem(int position) {
        return EntryLst.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        LinearLayout ll_entry;
        TextView tv_deviceid;
        TextView tv_name;
        TextView tv_age;
        TextView tv_dob;
        TextView tv_vehicle;
        TextView tv_comingfrom;
        TextView tv_visitingcom;
        TextView tv_homeadd;
        TextView tv_phone;
        TextView tv_email;
        TextView tv_gender;
        TextView tv_pov;
        TextView tv_visitingper;
        TextView tv_ofcadd;
        TextView tv_entertime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_securitycheckentry, null);
        try{
            SecurityCheckEntry entry = EntryLst.get(position);
            Spanned textDec;
            String text;

            LinearLayout ll_entry = (LinearLayout) view.findViewById(R.id.ll_entry);
            TextView tv_deviceid = (TextView) view.findViewById(R.id.tv_deviceid);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            TextView tv_age = (TextView) view.findViewById(R.id.tv_age);
            TextView tv_dob = (TextView) view.findViewById(R.id.tv_dob);
            TextView tv_vehicle = (TextView) view.findViewById(R.id.tv_vehicle);
            TextView tv_comingfrom = (TextView) view.findViewById(R.id.tv_comingfrom);
            TextView tv_visitingcom = (TextView) view.findViewById(R.id.tv_visitingcom);
            TextView tv_homeadd = (TextView) view.findViewById(R.id.tv_homeadd);
            TextView tv_phone = (TextView) view.findViewById(R.id.tv_phone);
            TextView tv_email = (TextView) view.findViewById(R.id.tv_email);
            TextView tv_gender = (TextView) view.findViewById(R.id.tv_gender);
            TextView tv_pov = (TextView) view.findViewById(R.id.tv_pov);
            TextView tv_visitingper = (TextView) view.findViewById(R.id.tv_visitingper);
            TextView tv_ofcadd = (TextView) view.findViewById(R.id.tv_ofcadd);
            TextView tv_block = (TextView) view.findViewById(R.id.tv_block);
            TextView tv_flat = (TextView) view.findViewById(R.id.tv_flat);
            TextView tv_entertime = (TextView) view.findViewById(R.id.tv_entertime);
            ImageView img_sync = (ImageView) view.findViewById(R.id.img_sync);

            tv_deviceid.setText(entry.getDeviceID());
            tv_name.setText(entry.getName());
            tv_entertime.setText(cc.get12HourFormat(entry.getEnterTime().replace('T', ' ').substring(0, 19)));


            if(entry.getAge() == null)
                tv_age.setVisibility(View.GONE);
            else {
                text = "<small><b>Age: </b><small>"+entry.getAge()+" years";
                textDec = Html.fromHtml(text);
                tv_age.setText(textDec);
            }

            if(entry.getDOB() == null)
                tv_dob.setVisibility(View.GONE);
            else {
                text = "<small><b>DOB: </b><small>"+entry.getDOB();
                textDec = Html.fromHtml(text);
                tv_dob.setText(textDec);
            }

            if(entry.getVehicle() == null)
                tv_vehicle.setVisibility(View.GONE);
            else {
                text = "<small><b>Vehicle: </b><small>"+entry.getVehicle();
                textDec = Html.fromHtml(text);
                tv_vehicle.setText(textDec);
            }

            if(entry.getComingFrom() == null)
                tv_comingfrom.setVisibility(View.GONE);
            else {
                text = "<small><b>Coming From: </b><small>"+entry.getComingFrom();
                textDec = Html.fromHtml(text);
                tv_comingfrom.setText(textDec);
            }

            if(entry.getVisitingCompany() == null)
                tv_visitingcom.setVisibility(View.GONE);
            else {
                text = "<small><b>Visiting Company: </b><small>"+entry.getVisitingCompany();
                textDec = Html.fromHtml(text);
                tv_visitingcom.setText(textDec);
            }

            if(entry.getHomeAddress() == null)
                tv_homeadd.setVisibility(View.GONE);
            else {
                text = "<small><b>Address(Home): </b><small><br/>"+entry.getHomeAddress().replace("\n", "<br/>");
                textDec = Html.fromHtml(text);
                tv_homeadd.setText(textDec);
            }

            if(entry.getOfficeAddress() == null)
                tv_ofcadd.setVisibility(View.GONE);
            else {
                text = "<small><b>Address(Office): </b><small><br/>"+entry.getOfficeAddress().replace("\n", "<br/>");
                textDec = Html.fromHtml(text);
                tv_ofcadd.setText(textDec);
            }

            if(entry.getPhone() == null)
                tv_phone.setVisibility(View.GONE);
            else {
                text = "<small><b>M: </b><small>"+entry.getPhone();
                textDec = Html.fromHtml(text);
                tv_phone.setText(textDec);
            }

            if(entry.getEmail() == null)
                tv_email.setVisibility(View.GONE);
            else {
                text = "<small><b>E: </b><small><a>"+entry.getEmail()+"</a>";
                textDec = Html.fromHtml(text);
                tv_email.setText(textDec);
            }

            if(entry.getGender() == null)
                tv_gender.setVisibility(View.GONE);
            else {
                text = "<small><b>Sex: </b><small>"+(entry.getGender().equalsIgnoreCase("1")?"Male":"Female");
                textDec = Html.fromHtml(text);
                tv_gender.setText(textDec);
            }

            if(entry.getPov() == null)
                tv_pov.setVisibility(View.GONE);
            else {
                text = "<small><b>Purpose: </b><small>"+entry.getPov();
                textDec = Html.fromHtml(text);
                tv_pov.setText(textDec);
            }

            if(entry.getContactPerson() == null)
                tv_visitingper.setVisibility(View.GONE);
            else {
                text = "<small><b>Contact Person: </b><small>"+entry.getContactPerson();
                textDec = Html.fromHtml(text);
                tv_visitingper.setText(textDec);
            }

            if(entry.getBlock() == null)
                tv_block.setVisibility(View.GONE);
            else {
                text = "<small><b>Block: </b><small>"+entry.getBlock();
                textDec = Html.fromHtml(text);
                tv_block.setText(textDec);
            }

            if(entry.getFlat() == null)
                tv_flat.setVisibility(View.GONE);
            else {
                text = "<small><b>Flat: </b><small>"+entry.getFlat();
                textDec = Html.fromHtml(text);
                tv_flat.setText(textDec);
            }

            if(entry.getsynced())
                img_sync.setImageResource(R.drawable.icon_green_dot);
            else
                img_sync.setImageResource(R.drawable.icon_red_dot);
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        try{
            if(convertView == null){
                convertView = View.inflate(context, R.layout.item_securitycheckentry, null);
                viewHolder = new ViewHolder();
                viewHolder.ll_entry = (LinearLayout) convertView.findViewById(R.id.ll_entry);
                viewHolder.tv_deviceid = (TextView) convertView.findViewById(R.id.tv_deviceid);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_age = (TextView) convertView.findViewById(R.id.tv_age);
                viewHolder.tv_dob = (TextView) convertView.findViewById(R.id.tv_dob);
                viewHolder.tv_vehicle = (TextView) convertView.findViewById(R.id.tv_vehicle);
                viewHolder.tv_comingfrom = (TextView) convertView.findViewById(R.id.tv_comingfrom);
                viewHolder.tv_visitingcom = (TextView) convertView.findViewById(R.id.tv_visitingcom);
                viewHolder.tv_homeadd = (TextView) convertView.findViewById(R.id.tv_homeadd);
                viewHolder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);
                viewHolder.tv_email = (TextView) convertView.findViewById(R.id.tv_email);
                viewHolder.tv_gender = (TextView) convertView.findViewById(R.id.tv_gender);
                viewHolder.tv_pov = (TextView) convertView.findViewById(R.id.tv_pov);
                viewHolder.tv_visitingper = (TextView) convertView.findViewById(R.id.tv_visitingper);
                viewHolder.tv_ofcadd = (TextView) convertView.findViewById(R.id.tv_ofcadd);
                viewHolder.tv_entertime = (TextView) convertView.findViewById(R.id.tv_entertime);
                convertView.setTag(viewHolder);

            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }


            SecurityCheckEntry entry = EntryLst.get(position);
            Spanned textDec;
            String text;
            viewHolder.tv_deviceid.setText(entry.getDeviceID());
            viewHolder.tv_name.setText(entry.getName());
            viewHolder.tv_entertime.setText(cc.get12HourFormat(entry.getEnterTime().replace('T', ' ').substring(0, 19)));
            viewHolder.tv_entertime.setText(entry.getEnterTime().replace('T', ' ').substring(0, 19));


            if(entry.getAge() == null)
                viewHolder.tv_age.setVisibility(View.GONE);
            else {
                text = "<small><b>Age: </b><small>"+entry.getAge()+" years";
                textDec = Html.fromHtml(text);
                viewHolder.tv_age.setText(textDec);
            }

            if(entry.getDOB() == null)
                viewHolder.tv_dob.setVisibility(View.GONE);
            else {
                text = "<small><b>DOB: </b><small>"+entry.getDOB();
                textDec = Html.fromHtml(text);
                viewHolder.tv_dob.setText(textDec);
            }

            if(entry.getVehicle() == null)
                viewHolder.tv_vehicle.setVisibility(View.GONE);
            else {
                text = "<small><b>Vehicle: </b><small>"+entry.getVehicle();
                textDec = Html.fromHtml(text);
                viewHolder.tv_vehicle.setText(textDec);
            }

            if(entry.getComingFrom() == null)
                viewHolder.tv_comingfrom.setVisibility(View.GONE);
            else {
                text = "<small><b>Coming From: </b><small>"+entry.getComingFrom();
                textDec = Html.fromHtml(text);
                viewHolder.tv_comingfrom.setText(textDec);
            }

            if(entry.getVisitingCompany() == null)
                viewHolder.tv_visitingcom.setVisibility(View.GONE);
            else {
                text = "<small><b>Visiting Company: </b><small>"+entry.getVisitingCompany();
                textDec = Html.fromHtml(text);
                viewHolder.tv_visitingcom.setText(textDec);
            }

            if(entry.getHomeAddress() == null)
                viewHolder.tv_homeadd.setVisibility(View.GONE);
            else {
                text = "<small><b>Address(Home): </b><small>"+entry.getHomeAddress();
                textDec = Html.fromHtml(text);
                viewHolder.tv_homeadd.setText(textDec);
            }

            if(entry.getOfficeAddress() == null)
                viewHolder.tv_ofcadd.setVisibility(View.GONE);
            else {
                text = "<small><b>Office(Home): </b><small>"+entry.getOfficeAddress();
                textDec = Html.fromHtml(text);
                viewHolder.tv_ofcadd.setText(textDec);
            }

            if(entry.getPhone() == null)
                viewHolder.tv_phone.setVisibility(View.GONE);
            else {
                text = "<small><b>M: </b><small>"+entry.getPhone();
                textDec = Html.fromHtml(text);
                viewHolder.tv_phone.setText(textDec);
            }

            if(entry.getEmail() == null)
                viewHolder.tv_email.setVisibility(View.GONE);
            else {
                text = "<small><b>E: </b><small><a>"+entry.getEmail()+"</a>";
                textDec = Html.fromHtml(text);
                viewHolder.tv_email.setText(textDec);
            }

            if(entry.getGender() == null)
                viewHolder.tv_gender.setVisibility(View.GONE);
            else {
                text = "<small><b>Sex: </b><small>"+entry.getGender();
                textDec = Html.fromHtml(text);
                viewHolder.tv_gender.setText(textDec);
            }

            if(entry.getPov() == null)
                viewHolder.tv_pov.setVisibility(View.GONE);
            else {
                text = "<small><b>Purpose: </b><small>"+entry.getPov();
                textDec = Html.fromHtml(text);
                viewHolder.tv_pov.setText(textDec);
            }

            if(entry.getContactPerson() == null)
                viewHolder.tv_visitingper.setVisibility(View.GONE);
            else {
                text = "<small><b>Contact Person: </b><small>"+entry.getContactPerson();
                textDec = Html.fromHtml(text);
                viewHolder.tv_visitingper.setText(textDec);
            }

            if(entry.getsynced())
                viewHolder.ll_entry.setBackgroundColor(context.getResources().getColor(R.color.lightgreen));
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return convertView;
    }*/
}
