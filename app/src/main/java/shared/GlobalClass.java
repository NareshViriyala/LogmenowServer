package shared;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nviriyala on 04-07-2016.
 */
public class GlobalClass extends Application{

    private boolean isAppRunning = true;
    private boolean ProceedOffline;
    private String ToActivity;
    private String FromActivity;
    private String guid;

    public boolean getIsAppRunning() {return this.isAppRunning;}
    public void setIsAppRunning(boolean isAppRunning){this.isAppRunning = isAppRunning;}

    public boolean getProceedOffline() {
        return this.ProceedOffline;
    }
    public void setProceedOffline(boolean ProceedOffline) {
        this.ProceedOffline = ProceedOffline;
    }

    public String getToActivity(){return this.ToActivity;}
    public void setToActivity(String ToActivity){this.ToActivity = ToActivity;}

    public String getFromActivity(){return this.FromActivity;}
    public void setFromActivity(String FromActivity){this.FromActivity = FromActivity;}

    public String getguid(){return this.guid;}
    public void setguid(String guid){this.guid = guid;}
}
