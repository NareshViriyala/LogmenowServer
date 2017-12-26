package adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.ArrayList;
import java.util.List;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models.ParkingInfo;

/**
 * Created by nviriyala on 26-08-2016.
 */
public class AdapterParkingInfo  extends BaseAdapter {

    private Context context;
    private DBHelper mydb;
    String PageName = "AdapterParkingInfo";
    private List<ParkingInfo> EntryLst = new ArrayList<>();
    private int maxitems = 5;
    private CommonClasses cc;

    public AdapterParkingInfo(Context context){
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
    }

    public AdapterParkingInfo(Context context, List<ParkingInfo> EntryLst){
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
        this.EntryLst = EntryLst;
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

    public void clearList(){
        EntryLst.clear();
        notifyDataSetChanged();
    }

    public void addEntry(ParkingInfo newEntry){
        EntryLst.add(0, newEntry);
        while (EntryLst.size()>maxitems){
            EntryLst.remove(maxitems);
        }
        notifyDataSetChanged();
    }

    public void addEntry(List<ParkingInfo> newEntry){
        EntryLst.addAll(newEntry);
        notifyDataSetChanged();
    }

    public void refreshList(ParkingInfo update){
        for(int i = 0 ; i<EntryLst.size(); i++){
            if(EntryLst.get(i).getDBID() == update.getDBID()) {
                EntryLst.remove(i);
                EntryLst.add(i, update);
            }
        }
        notifyDataSetChanged();
    }

    public void removeItem(ParkingInfo remove){
        for(int i = 0 ; i<EntryLst.size(); i++){
            if(EntryLst.get(i).getDBID() == remove.getDBID()) {
                EntryLst.remove(i);
            }
        }
        notifyDataSetChanged();
    }

    public void reloadList(List<ParkingInfo> newEntry){
        this.EntryLst = newEntry;
        notifyDataSetChanged();
    }

    public class viewHolder{
        ImageView img_vehType;
        TextView tv_vehicleno;
        TextView tv_entry;
        TextView tv_exit;
        TextView tv_tariffAmt;
        ImageView img_wsStatus;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_parkinginfo, null);
        try{
            ParkingInfo parkingInfo = EntryLst.get(position);
            ImageView img_vehType = (ImageView) view.findViewById(R.id.img_vehType);
            TextView tv_vehicleno = (TextView) view.findViewById(R.id.tv_vehicleno);
            TextView tv_local = (TextView) view.findViewById(R.id.tv_local);
            TextView tv_server = (TextView) view.findViewById(R.id.tv_server);
            ImageView img_wsStatus = (ImageView) view.findViewById(R.id.img_wsStatus);
            TextView tv_tariffAmt = (TextView) view.findViewById(R.id.tv_tariffAmt);

            img_vehType.setImageResource(context.getResources().getIdentifier("wheeler"+parkingInfo.getVehicleType(), "drawable", context.getPackageName()));
            tv_vehicleno.setText(parkingInfo.getVehicleNo());
            String text = "";
            Spanned textDec;
            if(parkingInfo.getLocalEntryDate() != null) {
                text = "<u><b>Local Entry</b></u>";
                text = text+"<br/>Date: "+parkingInfo.getLocalEntryDate().substring(0,10);
                text = text+"<br/>Time: "+cc.get12HourFormat(parkingInfo.getLocalEntryDate());
            }
            if(parkingInfo.getLocalExitDate() != null){
                if(text.equalsIgnoreCase(""))
                    text = text + "<u><b>Local Exit</b></u>";
                else
                    text = text + "<br/><u><b>Local Exit</b></u>";
                text = text+"<br/>Date: "+parkingInfo.getLocalExitDate().substring(0,10);
                text = text+"<br/>Time: "+cc.get12HourFormat(parkingInfo.getLocalExitDate());
            }
            if(parkingInfo.getLocalParkTime() != null){
                if(text.equalsIgnoreCase(""))
                    text = text + "<u><b>Local Park Time</b></u>";
                else
                    text = text + "<br/><u><b>Local Park Time</b></u>";

                text = text+"<br/>"+parkingInfo.getLocalParkTime();
            }
            textDec = Html.fromHtml(text);
            tv_local.setText(textDec);

            text = "";
            if(parkingInfo.getServerEntryDate() != null) {
                text = "<u><b>Server Entry</b></u>";
                text = text + "<br/>Date: " + parkingInfo.getServerEntryDate().substring(0,10);
                text = text + "<br/>Time: " + cc.get12HourFormat(parkingInfo.getServerEntryDate());
            }
            if(parkingInfo.getServerExitDate() != null) {
                if(text.equalsIgnoreCase(""))
                    text = text + "<u><b>Server Exit</b></u>";
                else
                    text = text + "<br/><u><b>Server Exit</b></u>";
                text = text + "<br/>Date: " + parkingInfo.getServerExitDate().substring(0,10);
                text = text + "<br/>Time: " + cc.get12HourFormat(parkingInfo.getServerExitDate());
            }
            if(parkingInfo.getServerParkTime() != null){
                if(text.equalsIgnoreCase(""))
                    text = text + "<u><b>Server Park Time</b></u>";
                else
                    text = text + "<br/><u><b>Server Park Time</b></u>";

                text = text+"<br/>"+parkingInfo.getServerParkTime();
            }
            textDec = Html.fromHtml(text);
            tv_server.setText(textDec);

            tv_tariffAmt.setText(parkingInfo.getTariffAmt()+"/-");
            if(parkingInfo.getServerSync() == 0)
                img_wsStatus.setImageResource(R.drawable.icon_red_dot);
            else
                img_wsStatus.setImageResource(R.drawable.icon_green_dot);
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder = null;
        ParkingInfo parkingInfo = EntryLst.get(position);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_parkinginfo, null);
            holder = new viewHolder();
            holder.img_vehType = (ImageView) convertView.findViewById(R.id.img_vehType);
            holder.tv_vehicleno = (TextView) convertView.findViewById(R.id.tv_vehicleno);
            holder.tv_entry = (TextView) convertView.findViewById(R.id.tv_entry);
            holder.tv_exit = (TextView) convertView.findViewById(R.id.tv_exit);
            holder.img_wsStatus = (ImageView) convertView.findViewById(R.id.img_wsStatus);
            holder.tv_tariffAmt = (TextView) convertView.findViewById(R.id.tv_tariffAmt);
            convertView.setTag(holder);
        }
        else
            holder = (viewHolder)convertView.getTag();

        holder.img_vehType.setImageResource(context.getResources().getIdentifier("wheeler"+parkingInfo.getVehicleType(), "drawable", context.getPackageName()));
        holder.tv_vehicleno.setText(parkingInfo.getVehicleNo());
        String text = "";
        Spanned textDec;
        if(parkingInfo.getLocalEntryDate() != null) {
            text = "<u>Local Entry</u>";
            text = text+"<br/>Date: "+parkingInfo.getLocalEntryDate().substring(0,10);
            text = text+"<br/>Time: "+cc.get12HourFormat(parkingInfo.getLocalEntryDate());

            if(parkingInfo.getServerEntryDate() != null) {
                text = text + "<u>Server Entry</u>";
                text = text + "<br/>Date: " + parkingInfo.getServerEntryDate().substring(0,10);
                text = text + "<br/>Time: " + cc.get12HourFormat(parkingInfo.getServerEntryDate());
            }
            textDec = Html.fromHtml(text);
            holder.tv_entry.setText(textDec);
        }
        else
            holder.tv_entry.setVisibility(View.GONE);

        if(parkingInfo.getLocalExitDate() != null) {
            text = "<u>Local Exit</u>";
            text = text+"<br/>Date: "+parkingInfo.getLocalExitDate().substring(0,10);
            text = text+"<br/>Time: "+cc.get12HourFormat(parkingInfo.getLocalExitDate());

            if(parkingInfo.getServerExitDate() != null) {
                text = text + "<u>Server Exit</u>";
                text = text + "<br/>Date: " + parkingInfo.getServerExitDate().substring(0,10);
                text = text + "<br/>Time: " + cc.get12HourFormat(parkingInfo.getServerExitDate());
            }
            textDec = Html.fromHtml(text);
            holder.tv_exit.setText(textDec);
        }
        else
            holder.tv_exit.setVisibility(View.GONE);

        holder.tv_tariffAmt.setText(parkingInfo.getTariffAmt()+"/-");
        if(parkingInfo.getServerSync() == 0)
            holder.img_wsStatus.setImageResource(R.drawable.icon_red_dot);
        else
            holder.img_wsStatus.setImageResource(R.drawable.icon_green_dot);
        return convertView;
    }*/
}
