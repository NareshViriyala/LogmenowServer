package adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.List;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models.RestaurantTable;

/**
 * Created by Home on 8/11/2016.
 */
public class AdapterRestaurantTable extends BaseAdapter {
    private Context context;
    private List<RestaurantTable> TableList;
    private DBHelper mydb;
    String PageName = "AdapterRestaurantTable";
    private CommonClasses cc;

    public AdapterRestaurantTable(Context context){
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
        this.TableList = mydb.getRestaurantTableList(Integer.parseInt(mydb.getSystemParameter("EntityID")));;
    }

    public void refreshList(){
        TableList = mydb.getRestaurantTableList(Integer.parseInt(mydb.getSystemParameter("EntityID")));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {return TableList.size();}

    @Override
    public Object getItem(int position) {return TableList.get(position);}

    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_restauranttable, null);
        try{
            RestaurantTable table = TableList.get(position);
            TextView tv_tableno = (TextView) view.findViewById(R.id.tv_tableno);
            TextView tv_guid = (TextView) view.findViewById(R.id.tv_guid);
            TextView tv_status = (TextView) view.findViewById(R.id.tv_status);
            TextView tv_tblstatus = (TextView) view.findViewById(R.id.tv_tblstatus);

            RelativeLayout rl_table = (RelativeLayout) view.findViewById(R.id.rl_table);
            RelativeLayout rl_filltable = (RelativeLayout) view.findViewById(R.id.rl_filltable);
            tv_tableno.setText(table.getTableNo());
            tv_guid.setText(table.getGuid());

            if(table.getTblStatus() == 0)
                tv_tblstatus.setVisibility(View.INVISIBLE);

            String status = "None";
            if(table.getOrderPlaced() == 0) {
                status = "OrderPlaced";
                rl_filltable.setBackgroundResource(R.drawable.background_tabletilesop);
            }
            if(table.getCall() == 0) {
                if(status.equalsIgnoreCase("None"))
                    status = "Call";
                else
                    status = "Both";
                rl_table.setBackgroundResource(R.drawable.background_tabletilescall);
                tv_tableno.setTextColor(context.getResources().getColor(R.color.white));
            }
            tv_status.setText(status);
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }
}

