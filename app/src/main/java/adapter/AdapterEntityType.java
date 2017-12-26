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
 * Created by Home on 8/23/2016.
 */
public class AdapterEntityType extends BaseAdapter {

    private Context context;
    private List<String> Etype;
    private DBHelper mydb;
    String PageName = "AdapterEntityType";

    public AdapterEntityType(Context context, List<String> Etype){
        this.context = context;
        mydb = new DBHelper(context);
        this.Etype = Etype;
    }

    @Override
    public int getCount() {
        return Etype.size();
    }

    @Override
    public Object getItem(int position) {
        return Etype.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_entitytype, null);
        try{
            String enttype = Etype.get(position);
            TextView tv_entitytype = (TextView) view.findViewById(R.id.tv_entitytype);
            tv_entitytype.setText(enttype);
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }
}
