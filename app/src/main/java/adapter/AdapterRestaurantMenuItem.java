package adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.ArrayList;
import java.util.List;

import database.DBHelper;
import shared.Models.RestaurantMenuItem ;

/**
 * Created by nviriyala on 16-08-2016.
 */
public class AdapterRestaurantMenuItem extends BaseAdapter {

    private Context context;
    private DBHelper mydb;
    String PageName = "AdapterRestaurantMenuItem";
    int prevDeviceID = 0;
    List<Boolean> headerlist = new ArrayList<>();
    List<RestaurantMenuItem > lstmenuitem = new ArrayList<>();

    public AdapterRestaurantMenuItem(Context context, List<RestaurantMenuItem> lstmenuitem){
        this.context = context;
        mydb = new DBHelper(context);
        this.lstmenuitem = lstmenuitem;
    }

    @Override
    public int getCount() {
        return lstmenuitem.size();
    }

    @Override
    public Object getItem(int position) {
        return lstmenuitem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = null;
        try {
            RestaurantMenuItem item = lstmenuitem.get(position);
            view = View.inflate(context, R.layout.item_restaurantmenu, null);

            if(headerlist.size() > position){
                if(headerlist.get(position)){
                    LinearLayout ll_itemgroup = (LinearLayout) view.findViewById(R.id.ll_itemgroup);
                    TextView tv_pname = (TextView) view.findViewById(R.id.tv_pname);
                    tv_pname.setText(item.getPerson());
                    ll_itemgroup.setVisibility(View.VISIBLE);
                }
            }
            else {
                if (item.getDeviceID() != prevDeviceID && position != 0) {
                    prevDeviceID = item.getDeviceID();
                    LinearLayout ll_itemgroup = (LinearLayout) view.findViewById(R.id.ll_itemgroup);
                    TextView tv_pname = (TextView) view.findViewById(R.id.tv_pname);
                    tv_pname.setText(item.getPerson());
                    ll_itemgroup.setVisibility(View.VISIBLE);
                    headerlist.add(true);
                } else {
                    headerlist.add(false);
                    prevDeviceID = item.getDeviceID();
                }
            }

            TextView tv_oid = (TextView) view.findViewById(R.id.tv_oid);
            TextView tv_itemname = (TextView) view.findViewById(R.id.tv_itemname);
            TextView tv_itemprice = (TextView) view.findViewById(R.id.tv_itemprice);
            TextView tv_masterid = (TextView) view.findViewById(R.id.tv_masterid);
            Button btn_delete = (Button) view.findViewById(R.id.btn_delete);

            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });

            tv_oid.setText(item.getOrderID()+"");
            tv_masterid.setText(item.getMasterID()+"");
            String itemdetails = "<b><small>"+item.getItemGroup()+"</small></b>";
            itemdetails = itemdetails+"<br/>"+item.getItemName();
            Spanned textDecoration = Html.fromHtml(itemdetails);
            tv_itemname.setText(textDecoration);

            itemdetails="";
            itemdetails = itemdetails+"<b>"+item.getItemPrice()+" /-</b>";
            itemdetails = itemdetails+"<br/>Qty: "+item.getQuantity();
            textDecoration = Html.fromHtml(itemdetails);
            tv_itemprice.setText(textDecoration);
        }
        catch (Exception e){mydb.logAppError(PageName, "getView", "Exception", e.getMessage());}
        return view;
    }
}
