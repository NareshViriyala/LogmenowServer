package adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.home.logmenowserver.R;

import java.util.ArrayList;
import java.util.List;

import database.DBHelper;
import shared.Models;

/**
 * Created by Home on 8/20/2016.
 */
public class AdapterRestaurantMenu extends BaseAdapter {

    private Context context;
    private DBHelper mydb;
    String PageName = "AdapterRestaurantMenu";
    List<Models.RestaurantMenu> lstmenuitem = new ArrayList<>();


    public AdapterRestaurantMenu(Context context, List<Models.RestaurantMenu> lstmenuitem){
        this.context = context;
        mydb = new DBHelper(context);
        this.lstmenuitem = lstmenuitem;
    }

    public void refreshList(List<Models.RestaurantMenu> lstitem){
        //lstmenuitem.clear();
        lstmenuitem = lstitem;
        notifyDataSetChanged();
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
        View view = View.inflate(context, R.layout.item_restaurantaddmenu, null);
        try {
            Models.RestaurantMenu item = lstmenuitem.get(position);

            LinearLayout ll_item = (LinearLayout) view.findViewById(R.id.ll_item);
            ImageView img_itemtype = (ImageView) view.findViewById(R.id.img_itemtype);
            TextView tv_tid = (TextView) view.findViewById(R.id.tv_tid);
            TextView tv_oid = (TextView) view.findViewById(R.id.tv_oid);
            TextView tv_itemname = (TextView) view.findViewById(R.id.tv_itemname);
            TextView tv_itemprice = (TextView) view.findViewById(R.id.tv_itemprice);
            TextView tv_qtyplus = (TextView) view.findViewById(R.id.tv_qtyplus);
            TextView tv_qty = (TextView) view.findViewById(R.id.tv_qty);
            TextView tv_qtyminus = (TextView) view.findViewById(R.id.tv_qtyminus);

            tv_qtyplus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });

            tv_qtyminus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });
            tv_tid.setText(item.getTID()+"");

            if(item.getIT())
                img_itemtype.setImageResource(R.drawable.ic_veg);
            else
                img_itemtype.setImageResource(R.drawable.ic_nonveg);
            tv_oid.setText(item.getOID()+"");
            tv_itemname.setText(item.getIN());
            tv_itemprice.setText((item.getIP()*item.getQTY())+"/-");
            tv_qty.setText(item.getQTY()+"");

            if(item.getSelected()) {
                ll_item.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                tv_itemname.setTextColor(context.getResources().getColor(R.color.white));
                tv_itemprice.setTextColor(context.getResources().getColor(R.color.white));
                tv_qty.setTextColor(context.getResources().getColor(R.color.white));
                tv_oid.setTextColor(context.getResources().getColor(R.color.white));

                tv_qtyplus.setBackgroundColor(context.getResources().getColor(R.color.white));
                tv_qtyminus.setBackgroundColor(context.getResources().getColor(R.color.white));
                tv_qtyplus.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                tv_qtyminus.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            }
            else {
                ll_item.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "getView", "Exception", e.getMessage());}
        return view;
    }
}
