package gcm;

import android.content.Context;
import android.os.AsyncTask;

import database.DBHelper;
import shared.WebServiceCall;

/**
 * Created by nviriyala on 29-08-2016.
 */
public class PushNotification extends AsyncTask {
    private DBHelper mydb;
    private Context context;
    private String PageName = "PushNotification";
    public PushNotification(Context context){
        this.context = context;
        mydb = new DBHelper(context);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try{
            new WebServiceCall(context).Get("PushNotification","");
        }
        catch (Exception e){mydb.logAppError(PageName, "doInBackground", "Exception", e.getMessage());}
        return null;
    }
}
