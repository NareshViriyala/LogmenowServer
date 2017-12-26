package adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.List;

import database.DBHelper;
import shared.Models;

/**
 * Created by nviriyala on 12-08-2016.
 */
public class AdapterEntityList extends BaseAdapter {
    private Context context;
    private List<Models.EntityInfo> HList;
    private DBHelper mydb;
    String PageName = "AdapterEntityList";

    public AdapterEntityList(Context context, String tablename){
        this.context = context;
        mydb = new DBHelper(context);
        HList = mydb.getEntityList(tablename);
    }

    @Override
    public int getCount() {
        return HList.size();
    }

    @Override
    public Object getItem(int position) {
        return HList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(context, R.layout.item_entity, null);
        try{
            Models.EntityInfo hinfo = HList.get(position);

            TextView tv_id = (TextView) view.findViewById(R.id.tv_id);
            TextView tv_hospName = (TextView) view.findViewById(R.id.tv_entityName);
            TextView tv_contact = (TextView) view.findViewById(R.id.tv_contact);
            TextView tv_address = (TextView) view.findViewById(R.id.tv_address);
            TextView tv_email = (TextView) view.findViewById(R.id.tv_email);
            TextView tv_website = (TextView) view.findViewById(R.id.tv_website);

            tv_id.setText(String.valueOf(hinfo.getEEID()));
            tv_hospName.setText(hinfo.getEname());

            String formatinfo = "";
            if(!hinfo.getAdd1().equalsIgnoreCase(""))
                formatinfo = formatinfo + hinfo.getAdd1();

            if(!hinfo.getAdd2().equalsIgnoreCase(""))
                formatinfo = formatinfo + "\n" + hinfo.getAdd2();

            if(!hinfo.getCity().equalsIgnoreCase(""))
                formatinfo = formatinfo + "\n" + hinfo.getCity();

            if(!hinfo.getState().equalsIgnoreCase(""))
                formatinfo = formatinfo + "\n" + hinfo.getState();

            if(!hinfo.getCountry().equalsIgnoreCase(""))
                formatinfo = formatinfo + "\n" + hinfo.getCountry();

            if(!hinfo.getZip().equalsIgnoreCase(""))
                formatinfo = formatinfo + "\n" + hinfo.getZip();

            tv_address.setText(formatinfo);

            formatinfo = "";
            if(!hinfo.getContactNo1().equalsIgnoreCase(""))
                formatinfo = formatinfo + hinfo.getContactNo1();

            if(!hinfo.getContactNo2().equalsIgnoreCase(""))
                formatinfo = formatinfo + "\n" + hinfo.getContactNo2();

            if(formatinfo.equalsIgnoreCase(""))
                tv_contact.setVisibility(View.GONE);
            else
                tv_contact.setText(formatinfo);

            formatinfo = hinfo.getEmailID();
            if(formatinfo.equalsIgnoreCase(""))
                tv_email.setVisibility(View.GONE);
            else
                tv_email.setText(formatinfo);

            formatinfo = hinfo.getWebSite();
            if(formatinfo.equalsIgnoreCase(""))
                tv_website.setVisibility(View.GONE);
            else
                tv_website.setText(formatinfo);
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }
}
