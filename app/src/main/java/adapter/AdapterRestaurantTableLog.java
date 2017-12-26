package adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.ArrayList;
import java.util.List;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models.RestaurantTableLog;

/**
 * Created by nviriyala on 31-08-2016.
 */
public class AdapterRestaurantTableLog extends BaseAdapter {

    private Context context;
    private DBHelper mydb;
    private String PageName = "AdapterRestaurantTable";
    private List<RestaurantTableLog> tablelogList = new ArrayList<>();
    private CommonClasses cc;

    public AdapterRestaurantTableLog(Context context){
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
    }

    @Override
    public int getCount() {return tablelogList.size();}

    @Override
    public Object getItem(int position) {return tablelogList.get(position);}

    @Override
    public long getItemId(int position) {return position;}

    public void appendList(List<RestaurantTableLog> lst){
        tablelogList.addAll(lst);
        notifyDataSetChanged();
    }

    public void reloadList(List<RestaurantTableLog> lst){
        tablelogList = lst;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_restauranttablelog, null);
        try{
            RestaurantTableLog table = tablelogList.get(position);
            TextView tv_itemscount = (TextView) view.findViewById(R.id.tv_itemscount);
            TextView tv_itemsbill = (TextView) view.findViewById(R.id.tv_itemsbill);
            TextView tv_orderdate = (TextView) view.findViewById(R.id.tv_orderdate);
            TextView tv_ordertime = (TextView) view.findViewById(R.id.tv_ordertime);
            LinearLayout ll_items = (LinearLayout) view.findViewById(R.id.ll_items);

            tv_itemscount.setText("Total items: "+table.getTotalItems());
            tv_itemsbill.setText("Total bill: "+table.getTotalBill()+"/-");
            tv_orderdate.setText("Date: "+table.getPlacedTime().substring(0,10));
            tv_ordertime.setText("Time: "+cc.get12HourFormat(table.getPlacedTime()));
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }
}
