package gcm;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.widget.RemoteViews;

import com.example.home.logmenowserver.R;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import activity.ActivityHospital;
import activity.ActivityRestaurantTables;
import database.DBHelper;

/**
 * Created by nviriyala on 05-08-2016.
 */
public class GCMPushReceiverService extends GcmListenerService {

    private DBHelper mydb;
    private String PageName = "GCMPushReceiverService";
    public Vibrator v;
    private Context context;

    //This method will be called on every new message received
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        mydb = new DBHelper(this);
        context = this;
        sendNotification(message);
    }

    //This method is generating a notification and displaying the notification
    private void sendNotification(String message) {
        try {
            String NotificationMessage = "";
            v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            JSONObject jsonObject = new JSONObject(message);
            String ntType = jsonObject.getString("Status");
            switch (ntType){
                case "New Appointment":
                    mydb.addAppointmentList("["+jsonObject.toString()+"]");
                    NotificationMessage = ntType;
                    v.vibrate(200);
                    break;
                case "Appointment Cancelled":
                    mydb.setAppointmentStatus(jsonObject.getInt("ApptID"), "UserCancelled");
                    mydb.setAppointmentStatus(jsonObject.getInt("ApptID"), "InTime");
                    mydb.setAppointmentStatus(jsonObject.getInt("ApptID"), "OutTime");
                    NotificationMessage = ntType;
                    v.vibrate(200);
                    break;
                case "Call":
                    mydb.setTableInfo(jsonObject.getString("guid"), "Call", 0);
                    mydb.setTableInfo(jsonObject.getString("guid"), "TblStatus", 1);
                    NotificationMessage = "Call Request";
                    break;
                case "Order Placed":
                    mydb.setTableInfo(jsonObject.getString("guid"), "OrderPlaced", 0);
                    mydb.setTableInfo(jsonObject.getString("guid"), "TblStatus", 1);
                    NotificationMessage = "New Order";
                    break;
                default:
                    break;
            }

            if(context.getPackageName().equalsIgnoreCase(((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity.getPackageName())) {
                Intent intent = new Intent(getResources().getString(R.string.package_name));
                intent.putExtra("guid", jsonObject.getString("guid"));
                this.sendBroadcast(intent);
            }
            else {
                CustomNotification(jsonObject, NotificationMessage);
                /*Intent intent = new Intent(this, ActivityHospitalAppointments.class);
                mydb.setSystemParameter("DocGuid", jsonObject.getString("guid"));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                int requestCode = 0;
                PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(NotificationMessage)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, noBuilder.build()); //0 = ID of notification*/
            }
        }
        catch(Exception e){mydb.logAppError(PageName, "sendNotification", "Exception", e.getMessage());}
    }

    public void CustomNotification(JSONObject jsonObject, String NotificationMessage) {
        try {
            Intent intent = null;
            String ntType = jsonObject.getString("Status");
            switch (ntType){
                case "New Appointment":
                    intent = new Intent(this, ActivityHospital.class);
                    mydb.setSystemParameter("DocGuid", jsonObject.getString("guid"));
                    break;
                case "Appointment Cancelled":
                    intent = new Intent(this, ActivityHospital.class);
                    mydb.setSystemParameter("DocGuid", jsonObject.getString("guid"));
                    break;
                case "Call":
                    intent = new Intent(this, ActivityRestaurantTables.class);
                    break;
                case "Order Placed":
                    intent = new Intent(this, ActivityRestaurantTables.class);
                    break;
                default:
                    break;
            }
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_general);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .setContent(remoteViews);
            remoteViews.setImageViewResource(R.id.img_icon,R.drawable.ic_logo);
            remoteViews.setTextViewText(R.id.tv_notificationtext, Html.fromHtml(NotificationMessage));
            remoteViews.setTextColor(R.id.tv_notificationtext, getResources().getColor(R.color.colorPrimary));
            NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationmanager.notify(0, builder.build());
        }
        catch(Exception e){mydb.logAppError(PageName, "CustomNotification", "Exception", e.getMessage());}
    }
}
