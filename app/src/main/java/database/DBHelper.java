package database;

/**
 * Created by Home on 6/1/2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import shared.Models;
import shared.Models.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ServerApp.db";
    public Context context;
    private String PageName = "DBHelper";

    @Override
    public void onCreate(SQLiteDatabase db){
        //create base info table
        /*db.execSQL("DROP TABLE IF EXISTS tbl_errorLog");
        db.execSQL("DROP TABLE IF EXISTS tbl_serverDeviceInfo");
        db.execSQL("DROP TABLE IF EXISTS tbl_DeviceUsage");*/
        String ts = new TimeStamp().getCurrentTimeStamp();

        db.execSQL("create table tbl_deviceinfo (id integer" +
                ", DeviceID text" +
                ", DeviceType text" +
                ", DeviceVersion text" +
                ", AppVersion text" +
                ", DeviceToken text" +
                ", ServerMapID int" +
                ", ModTime text)");
        String DeviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String DeviceType = "Android";
        String DeviceVersion = Build.VERSION.RELEASE;
        String AppVersion = "1.0.0.0";
        String DeviceToken = "";
        int ServerMapID = 0;
        String ModTime = ts;
        db.execSQL("Insert into tbl_deviceinfo values(1, '"+DeviceID+"','"+DeviceType+"','"+DeviceVersion+"','"+AppVersion+"','"+DeviceToken+"', "+ServerMapID+", '"+ModTime+"')");

        db.execSQL("create table tbl_errorLog (id integer primary key AUTOINCREMENT" +
                ", PageName text" +
                ", MethodName text" +
                ", ExceptionType text" +
                ", ExceptionText text" +
                ", OcrTime DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("create table tbl_SystemParameters(ParamName text primary key, ParamValue text)");
        db.execSQL("Insert into tbl_SystemParameters values('ProceedOffline', 'False')");
        db.execSQL("Insert into tbl_SystemParameters values('DeviceInfo', '"+ts+"')");

        db.execSQL("create table tbl_hospitallist (EEID integer primary key" +
                ", Ename text, Add1 text, Add2 text, City text, State text, Country text, Zip text" +
                ", ContactNo1 text, ContactNo2 text, EmailID text, WebSite text, Lat double, Long double, PD int)");

        db.execSQL("create table tbl_doctorlist (Guid text primary key" +
                ", Name text, Degree text, Specialty text, ConsultationFee int, AvgConsultationTime int, Mobile text, Work text" +
                ", Email text, remarks text, EntityID int)");

        db.execSQL("create table tbl_appointmentlist (ApptID integer primary key" +
                ", PatientName text, AgeYear int, AgeMonth int, Gender int, ApptTime text, Guid text" +
                ", InTime text, OutTime text, JustIn int, UserCancelled int, PatientPhone  text)");

        db.execSQL("create table tbl_parkinglist (EEID integer primary key" +
                ", Ename text, Add1 text, Add2 text, City text, State text, Country text, Zip text" +
                ", ContactNo1 text, ContactNo2 text, EmailID text, WebSite text, Lat double, Long double, PD int)");

        db.execSQL("create table tbl_VehicleEntry(id integer primary key AUTOINCREMENT" +
                ", ServerID int" +
                ", EntityID int" +
                ", DeviceID text" +
                ", VehicleNo text" +
                ", VehicleType int" +
                ", Usage int" +
                ", TariffType int" +
                ", TariffAmt int" +
                ", LocalEntryDate DATETIME" +
                ", LocalExitDate DATETIME" +
                ", ServerEntryDate DATETIME" +
                ", ServerExitDate DATETIME" +
                ", ServerSync INTEGER DEFAULT 0)");

        db.execSQL("create table tbl_restaurantlist (EEID integer primary key" +
                ", Ename text, Add1 text, Add2 text, City text, State text, Country text, Zip text" +
                ", ContactNo1 text, ContactNo2 text, EmailID text, WebSite text, Lat double, Long double, PD int)");

        db.execSQL("create table tbl_restauranttable (Guid text primary key, EntityID int, TableNo text, Call int, OrderPlaced int, TblStatus int)");

        db.execSQL("create table tbl_restauranttaxes (EntityID int, OID int, TaxName text, Taxamt double)");

        db.execSQL("create table tbl_RestaurantMenu(TID int primary key, OID int not null unique, IT int, IG text, IName text, IDesc text" +
                ", IP int, SI int, CR int, Usr int, RT int, QTY int, Selected int, EntityID int)");

        db.execSQL("create table tbl_restaurantplacedorder(TID int primary key, Guid text, Qty int, OID int, UNIQUE(TID, Guid) ON CONFLICT REPLACE)");

        db.execSQL("create table tbl_entitylist (EEID integer primary key" +
                ", Ename text, Add1 text, Add2 text, City text, State text, Country text, Zip text" +
                ", ContactNo1 text, ContactNo2 text, EmailID text, WebSite text, Lat double, Long double, PD int)");

        db.execSQL("create table tbl_securitycheckentry (id integer primary key AUTOINCREMENT" +
                ", EntityID int, DeviceID text, DeviceIDInt int, Name text, Email text, Phone text, DOB text, Age text, Sex text" +
                ", Vehicle text, VehicleType text, ComingFrom text, Purpose text, VisitingCompany text, ContactPerson text" +
                ", HomeAdd text, OfcAdd text, Block text, Flat text, isDeleted int, synced int, LogTime DATETIME DEFAULT CURRENT_TIMESTAMP, ServerID int)");
    }

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    public boolean logAppError(String PageName, String MethodName, String ExceptionType, String ExceptionText){
        if(ExceptionText == null)
            return true;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("PageName", PageName);
        contentValues.put("MethodName", MethodName);
        contentValues.put("ExceptionType", ExceptionType);
        contentValues.put("ExceptionText", ExceptionText);
        db.insert("tbl_errorLog", null, contentValues);
        try {
            Toast.makeText(context, "logAppError :: " + ExceptionText, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){}
        finally {
            db.close();
        }
        return true;
    }



    //Security Check Tables
    public long addSecurityCheckEntry(SecurityCheckEntry entry){
        SQLiteDatabase db = this.getWritableDatabase();
        long id = 0;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put("EntityID", entry.getEntityID());
            contentValues.put("DeviceID", entry.getDeviceID());
            contentValues.put("Name", entry.getName());
            contentValues.put("Email", entry.getEmail());
            contentValues.put("Phone", entry.getPhone());
            contentValues.put("DOB", entry.getDOB());
            contentValues.put("Age", entry.getAge());
            contentValues.put("Sex", entry.getGender());
            contentValues.put("Vehicle", entry.getVehicle());
            contentValues.put("VehicleType", entry.getVehicleType());
            contentValues.put("ComingFrom", entry.getComingFrom());
            contentValues.put("Purpose", entry.getPov());
            contentValues.put("VisitingCompany", entry.getVisitingCompany());
            contentValues.put("ContactPerson", entry.getContactPerson());
            contentValues.put("HomeAdd", entry.getHomeAddress());
            contentValues.put("OfcAdd", entry.getOfficeAddress());
            contentValues.put("Block", entry.getBlock());
            contentValues.put("Flat", entry.getFlat());
            contentValues.put("isDeleted", entry.getisDeleted());
            contentValues.put("synced", entry.getsynced());
            id = db.insert("tbl_securitycheckentry", null, contentValues);
        }
        catch (Exception e){logAppError(PageName, "getEntityList", "Exception", e.getMessage());}
        finally {db.close();}
        return id;
    }

    public void updateSecurityCheck(JSONObject jsonObject){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            String Query = "update tbl_securitycheckentry set ServerID = "+jsonObject.getInt("ID");
            Query = Query+", DeviceIDInt = "+jsonObject.getInt("DeviceID");
            Query = Query+", synced = 1";
            Query = Query+", LogTime='"+jsonObject.getString("SystemTime").replace('T',' ').substring(0, 19)+"'";
            Query = Query+" where id="+jsonObject.getInt("DBID");
            db.execSQL(Query);
        }
        catch (Exception e){logAppError(PageName, "updateSecurityCheck", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public void clearSecurityCheckTable(int EntityID, boolean forcedelete){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            String Query = "delete from tbl_securitycheckentry where EntityID = "+EntityID;
            if(!forcedelete)
                Query = Query + " and synced=1";

            db.execSQL(Query);
        }
        catch (Exception e){logAppError(PageName, "clearSecurityCheckTable", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public long getSecurityCheckEntryCount(int EntityID, String type){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select count(*) from tbl_securitycheckentry where EntityID = "+EntityID;
        switch (type){
            case "All":
                break;
            case "Unsynced":
                Query = Query + " and synced = 0";
                break;
            case "Synced":
                Query = Query + " and synced = 1";
                break;
        }
        SQLiteStatement s = db.compileStatement(Query);
        long cnt =  s.simpleQueryForLong();
        s.close();
        db.close();
        return cnt;
    }

    public List<SecurityCheckEntry> getSecurityCheckEntryRows(int EntityID){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "Select * from tbl_securitycheckentry where EntityID = "+EntityID+" and synced = 0";
        Cursor res =  db.rawQuery(Query, null );
        List<SecurityCheckEntry> rows = new ArrayList<>();
        try {
            if (res.moveToFirst()) {
                do {
                    SecurityCheckEntry row = new SecurityCheckEntry();
                    row.setDeviceID(res.getString(res.getColumnIndex("DeviceID")));
                    row.setDBID(res.getInt(res.getColumnIndex("id")));
                    String value = res.getString(res.getColumnIndex("Name"));
                    if(value != null)
                        row.setName(value);

                    value = res.getString(res.getColumnIndex("Email"));
                    if(value != null)
                        row.setEmail(value);

                    value = res.getString(res.getColumnIndex("Phone"));
                    if(value != null)
                        row.setPhone(value);

                    value = res.getString(res.getColumnIndex("DOB"));
                    if(value != null)
                        row.setDOB(value);

                    value = res.getString(res.getColumnIndex("Age"));
                    if(value != null)
                        row.setAge(value);

                    value = res.getString(res.getColumnIndex("Sex"));
                    if(value != null)
                        row.setGender(value);

                    value = res.getString(res.getColumnIndex("Vehicle"));
                    if(value != null)
                        row.setVehicle(value);

                    value = res.getString(res.getColumnIndex("VehicleType"));
                    if(value != null)
                        row.setVehicleType(value);

                    value = res.getString(res.getColumnIndex("ComingFrom"));
                    if(value != null)
                        row.setComingFrom(value);

                    value = res.getString(res.getColumnIndex("Purpose"));
                    if(value != null)
                        row.setPov(value);

                    value = res.getString(res.getColumnIndex("VisitingCompany"));
                    if(value != null)
                        row.setVisitingCompany(value);

                    value = res.getString(res.getColumnIndex("ContactPerson"));
                    if(value != null)
                        row.setContactPerson(value);

                    value = res.getString(res.getColumnIndex("HomeAdd"));
                    if(value != null)
                        row.setHomeAddress(value);

                    value = res.getString(res.getColumnIndex("OfcAdd"));
                    if(value != null)
                        row.setOfficeAddress(value);

                    value = res.getString(res.getColumnIndex("Block"));
                    if(value != null)
                        row.setBlock(value);

                    value = res.getString(res.getColumnIndex("Flat"));
                    if(value != null)
                        row.setFlat(value);

                    value = String.valueOf(res.getInt(res.getColumnIndex("isDeleted")));
                    if(value != null)
                        row.setsynced(!value.equalsIgnoreCase("0"));

                    rows.add(row);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "getEntityList", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return rows;
    }



    //Parking Tables
    public void clearParkingTable(int EntityID){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.delete("tbl_VehicleEntry", "EntityID = "+EntityID+" and ServerSync=1", null);
        }
        catch (Exception e){logAppError(PageName, "clearParkingTable", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public long getParkingEntryCount(int EntityID, String type){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select count(*) from tbl_VehicleEntry where EntityID = "+EntityID;
        switch (type){
            case "All":
                break;
            case "Unsynced":
                Query = Query + " and (ServerSync is null or ServerSync = 0)";
                break;
            case "Synced":
                Query = Query + " and ServerSync = 1";
                break;
        }
        SQLiteStatement s = db.compileStatement(Query);
        long cnt =  s.simpleQueryForLong();
        s.close();
        db.close();
        return cnt;
    }

    public long addParkingEntry(ParkingInfo entry){
        SQLiteDatabase db = this.getWritableDatabase();
        long id = 0;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put("EntityID", entry.getEntityID());
            contentValues.put("DeviceID", entry.getDeviceID());
            contentValues.put("VehicleNo", entry.getVehicleNo());
            contentValues.put("VehicleType", entry.getVehicleType());
            contentValues.put("Usage", entry.getUsage());
            contentValues.put("TariffType", entry.getTariffType());
            contentValues.put("LocalEntryDate", entry.getLocalEntryDate());
            contentValues.put("LocalExitDate", entry.getLocalExitDate());
            id = db.insert("tbl_VehicleEntry", null, contentValues);
        }
        catch (Exception e){logAppError(PageName, "addParkingEntry", "Exception", e.getMessage());}
        finally {db.close();}
        return id;
    }

    public List<ParkingInfo> getParkingEntryRows(int EntityID){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "Select * from tbl_VehicleEntry where EntityID = "+EntityID+" and ServerSync = 0";
        Cursor res =  db.rawQuery(Query, null );
        List<ParkingInfo> rows = new ArrayList<>();
        try {
            if (res.moveToFirst()) {
                do {
                    ParkingInfo row = new ParkingInfo();
                    row.setDBID(res.getInt(res.getColumnIndex("id")));
                    row.setEntityID(res.getInt(res.getColumnIndex("EntityID")));
                    row.setDeviceID(res.getString(res.getColumnIndex("DeviceID")));
                    row.setVehicleNo(res.getString(res.getColumnIndex("VehicleNo")));
                    row.setVehicleType(res.getInt(res.getColumnIndex("VehicleType")));
                    row.setUsage(res.getInt(res.getColumnIndex("Usage")));
                    row.setTariffType(res.getInt(res.getColumnIndex("TariffType")));
                    row.setLocalEntryDate(res.getString(res.getColumnIndex("LocalEntryDate")));
                    row.setLocalExitDate(res.getString(res.getColumnIndex("LocalExitDate")));
                    if(row.getUsage() == 1)
                        row.setLocalTime(res.getString(res.getColumnIndex("LocalEntryDate")));
                    else
                        row.setLocalTime(res.getString(res.getColumnIndex("LocalExitDate")));
                    rows.add(row);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "getParkingEntryRows", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return rows;
    }

    public ParkingInfo getParkingEntryRow(int DBID){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "Select * from tbl_VehicleEntry where id = "+DBID;
        Cursor res =  db.rawQuery(Query, null );
        ParkingInfo row = new ParkingInfo();
        try {
            if (res.moveToFirst()) {
                do {
                    row.setDBID(res.getInt(res.getColumnIndex("id")));
                    row.setEntityID(res.getInt(res.getColumnIndex("EntityID")));
                    row.setDeviceID(res.getString(res.getColumnIndex("DeviceID")));
                    row.setVehicleNo(res.getString(res.getColumnIndex("VehicleNo")));
                    row.setVehicleType(res.getInt(res.getColumnIndex("VehicleType")));
                    row.setUsage(res.getInt(res.getColumnIndex("Usage")));
                    row.setTariffType(res.getInt(res.getColumnIndex("TariffType")));
                    row.setLocalEntryDate(res.getString(res.getColumnIndex("LocalEntryDate")));
                    row.setLocalExitDate(res.getString(res.getColumnIndex("LocalExitDate")));
                    if(row.getUsage() == 1)
                        row.setLocalTime(res.getString(res.getColumnIndex("LocalEntryDate")));
                    else
                        row.setLocalTime(res.getString(res.getColumnIndex("LocalExitDate")));
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "getParkingEntryRows", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return row;
    }

    public void updateParkingRows(JSONObject jsonObject){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            String Query = "update tbl_VehicleEntry set ServerID = "+jsonObject.getInt("ID");
            Query = Query+", ServerSync = 1";
            if(!jsonObject.getString("LocalEnterDate").equalsIgnoreCase(""))
                Query = Query+", LocalEntryDate='"+jsonObject.getString("LocalEnterDate").replace('T',' ').substring(0, 19)+"'";
            if(!jsonObject.getString("LocalExitDate").equalsIgnoreCase(""))
                Query = Query+", LocalExitDate='"+jsonObject.getString("LocalExitDate").replace('T',' ').substring(0, 19)+"'";
            if(!jsonObject.getString("IEntryDate").equalsIgnoreCase(""))
                Query = Query+", ServerEntryDate='"+jsonObject.getString("IEntryDate").replace('T',' ').substring(0, 19)+"'";
            if(!jsonObject.getString("IExitDate").equalsIgnoreCase(""))
                Query = Query+", ServerExitDate='"+jsonObject.getString("IExitDate").replace('T',' ').substring(0, 19)+"'";
            Query = Query+" where id="+jsonObject.getInt("DBID");
            db.execSQL(Query);
        }
        catch (Exception e){logAppError(PageName, "updateSecurityCheck", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public void deleteParkingRow(long DBID){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.delete("tbl_VehicleEntry", "id="+DBID, null);
        }
        catch (Exception e){logAppError(PageName, "updateSecurityCheck", "Exception", e.getMessage());}
        finally {db.close();}
    }



    //Restaurant Tables
    public void addRestaruantTableList(String inputstring, int EntityID){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            JSONArray jsonArray = new JSONArray(inputstring);
            db.delete("tbl_restauranttable", "EntityID = "+EntityID, null);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("Guid", jsonObject.getString("SubjectID"));
                contentValues.put("EntityID", jsonObject.getInt("EntityID"));
                contentValues.put("TableNo", jsonObject.getString("SubjectInfo"));
                contentValues.put("Call", jsonObject.getString("CallWaiter"));
                contentValues.put("OrderPlaced", jsonObject.getString("AckBit"));
                contentValues.put("TblStatus", jsonObject.getString("TblStatus"));
                db.insert("tbl_restauranttable", null, contentValues);
            }
        }
        catch (Exception e){logAppError(PageName, "addRestaruantTableList", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public void addRestaurantTaxes(int EntityID, String inputstring){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            JSONArray jsonArray = new JSONArray(inputstring);
            db.delete("tbl_restauranttaxes", "EntityID="+EntityID, null);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("EntityID", EntityID);
                contentValues.put("OID", jsonObject.getInt("OID"));
                contentValues.put("TaxName", jsonObject.getString("TaxName"));
                contentValues.put("Taxamt", jsonObject.getDouble("TaxPercentage"));
                db.insert("tbl_restauranttaxes", null, contentValues);
            }
        }
        catch (Exception e){logAppError(PageName, "addRestaurantTaxes", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public void addRestaurantOrder(int TID, String Guid, int Qty, int OID){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put("TID", TID);
            contentValues.put("Guid", Guid);
            contentValues.put("Qty", Qty);
            contentValues.put("OID", OID);
            //db.insert("tbl_restaurantplacedorder", null, contentValues); //this is valid
            //check the table creation above.

            long insertId = db.insert("tbl_restaurantplacedorder", null,contentValues);
            if(insertId < 0){
                db.execSQL("update tbl_restaurantplacedorder set Qty = "+Qty+" where TID = "+TID+" and Guid = '"+Guid+"'");
            }
        }
        catch (Exception e){logAppError(PageName, "addRestaurantTaxes", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public void deleteRestaurantOrder(int TID, String Guid){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            String Query = "delete from tbl_restaurantplacedorder where Guid = '"+Guid+"'";
            if(TID != 0)
                Query = Query + " and TID = "+TID;
            db.execSQL(Query);
        }
        catch (Exception e){logAppError(PageName, "addRestaurantTaxes", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public JSONArray getRestaurantOrder(String Guid){
        SQLiteDatabase db = this.getReadableDatabase();
        JSONArray jsonArray = new JSONArray();
        Cursor res =  db.rawQuery( "select * from tbl_restaurantplacedorder where Guid = '"+Guid+"'", null );
        try {
            if (res.moveToFirst()) {
                do {
                    int TID = res.getInt(res.getColumnIndex("TID"));
                    int Qty = res.getInt(res.getColumnIndex("Qty"));
                    int OID = res.getInt(res.getColumnIndex("OID"));
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("TID", TID);
                    jsonObject.put("Qty", Qty);
                    jsonObject.put("OID", OID);
                    jsonArray.put(jsonObject);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "getRestaurantOrder", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return jsonArray;
    }

    public JSONArray getRestaurantTaxes(int EntityID){
        SQLiteDatabase db = this.getReadableDatabase();
        JSONArray jsonArray = new JSONArray();
        Cursor res =  db.rawQuery( "select * from tbl_restauranttaxes where EntityID = "+EntityID+" order by OID", null );
        try {
            if (res.moveToFirst()) {
                do {
                    String TaxType = res.getString(res.getColumnIndex("TaxName"));
                    double Taxamt = res.getDouble(res.getColumnIndex("Taxamt"));
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("TaxType", TaxType);
                    jsonObject.put("Taxamt", Taxamt);
                    jsonArray.put(jsonObject);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "getRestaurantTaxes", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return jsonArray;
    }

    public void addMenuItems(int EntityID, List<Models.RestaurantMenu> items) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tbl_RestaurantMenu", "EntityID="+EntityID, null);
        for (Models.RestaurantMenu item: items) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("EntityID", EntityID);
            contentValues.put("TID", item.getTID());
            contentValues.put("OID", item.getOID());
            contentValues.put("IT", item.getIT()?1:0);
            contentValues.put("IG", item.getIG());
            contentValues.put("IName", item.getIN());
            contentValues.put("IDesc", item.getID());
            contentValues.put("IP", item.getIP());
            contentValues.put("SI", item.getSI());
            contentValues.put("CR", item.getCR()?1:0);
            contentValues.put("Usr", item.getUsr());
            contentValues.put("RT", item.getRT());
            contentValues.put("QTY", item.getQTY());
            contentValues.put("Selected", item.getSelected()?1:0);
            db.insertWithOnConflict("tbl_RestaurantMenu","TID",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.close();
    }

    public List<RestaurantMenu> getMenuItems(int EntityID){
        List<RestaurantMenu> items = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("Select * from tbl_RestaurantMenu where EntityID = "+EntityID, null );
        try {
            if (res.moveToFirst()) {
                do {
                    int TID = res.getInt(res.getColumnIndex("TID"));
                    int OID = res.getInt(res.getColumnIndex("OID"));
                    int ITint = res.getInt(res.getColumnIndex("IT"));
                    boolean IT = (ITint == 1); //this is same as (ITint == 1) ? true : false

                    String IG = res.getString(res.getColumnIndex("IG"));
                    String IN = res.getString(res.getColumnIndex("IName"));
                    String ID = res.getString(res.getColumnIndex("IDesc"));

                    int IP = res.getInt(res.getColumnIndex("IP"));
                    int SI = res.getInt(res.getColumnIndex("SI"));
                    int CRint = res.getInt(res.getColumnIndex("CR"));
                    boolean CR = (CRint == 1); //this is same as (CRint == 1) ? true : false

                    int Usr = res.getInt(res.getColumnIndex("Usr"));
                    int RT = res.getInt(res.getColumnIndex("RT"));
                    int QTY = res.getInt(res.getColumnIndex("QTY"));
                    int Sint = res.getInt(res.getColumnIndex("Selected"));
                    boolean Selected = (Sint == 1); //this is same as (Sint == 1) ? true : false

                    RestaurantMenu item = new Models.RestaurantMenu(TID, OID, IT, IG, IN, IP, ID, SI, CR, Usr, RT, QTY, Selected);
                    items.add(item);

                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getMenuItems", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return items;
    }

    public RestaurantMenu getMenuItem(int TID){
        RestaurantMenu item = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("Select * from tbl_RestaurantMenu where TID = "+TID, null );
        try {
            if (res.moveToFirst()) {
                do {
                    int OID = res.getInt(res.getColumnIndex("OID"));
                    int ITint = res.getInt(res.getColumnIndex("IT"));
                    boolean IT = (ITint == 1); //this is same as (ITint == 1) ? true : false

                    String IG = res.getString(res.getColumnIndex("IG"));
                    String IN = res.getString(res.getColumnIndex("IName"));
                    String ID = res.getString(res.getColumnIndex("IDesc"));

                    int IP = res.getInt(res.getColumnIndex("IP"));
                    int SI = res.getInt(res.getColumnIndex("SI"));
                    int CRint = res.getInt(res.getColumnIndex("CR"));
                    boolean CR = (CRint == 1); //this is same as (CRint == 1) ? true : false

                    int Usr = res.getInt(res.getColumnIndex("Usr"));
                    int RT = res.getInt(res.getColumnIndex("RT"));
                    int QTY = res.getInt(res.getColumnIndex("QTY"));
                    int Sint = res.getInt(res.getColumnIndex("Selected"));
                    boolean Selected = (Sint == 1); //this is same as (Sint == 1) ? true : false

                    item = new Models.RestaurantMenu(TID, OID, IT, IG, IN, IP, ID, SI, CR, Usr, RT, QTY, Selected);

                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getMenuItem", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return item;
    }

    public List<RestaurantTable> getRestaurantTableList(int EntityID){
        List<RestaurantTable> retList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from tbl_restauranttable where EntityID = "+EntityID, null );
        try {
            if (res.moveToFirst()) {
                do {
                    String Guid = res.getString(res.getColumnIndex("Guid"));
                    String TableNo = res.getString(res.getColumnIndex("TableNo"));
                    int Call = res.getInt(res.getColumnIndex("Call"));
                    int OrderPlaced = res.getInt(res.getColumnIndex("OrderPlaced"));
                    int TblStatus = res.getInt(res.getColumnIndex("TblStatus"));
                    RestaurantTable row = new RestaurantTable(Guid,EntityID,TableNo,Call,OrderPlaced,TblStatus);
                    retList.add(row);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "getRestaurantTableList", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return retList;
    }

    public boolean setTableInfo(String guid, String column, int value){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("update tbl_restauranttable set "+column+" = "+value+" where Guid = '"+guid+"'");
        }
        catch (Exception e){logAppError("DBHelper", "setTableInfo", "Exception", e.getMessage());}
        finally {db.close();}
        return true;
    }

    public boolean needTableTrip(int EntityID){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select count(*) from tbl_restauranttable where EntityID = "+EntityID+" and Call = 0 or OrderPlaced = 0";
        SQLiteStatement s = db.compileStatement(Query);
        long cnt =  s.simpleQueryForLong();
        s.close();
        db.close();
        return cnt>0?true:false;
    }

    public long getRecordCount(String tablename, int EntityID){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select count(*) from "+tablename+" where EntityID = "+EntityID;
        //String Query = "select count(*) from "+tablename;
        SQLiteStatement s = db.compileStatement(Query);
        long cnt =  s.simpleQueryForLong();
        s.close();
        db.close();
        return cnt;
    }



    //Hospital Tables
    public void addDoctorList(String inputstring){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            JSONArray jsonArray = new JSONArray(inputstring);
            db.delete("tbl_doctorlist", null, null);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("Guid", jsonObject.getString("guid"));
                contentValues.put("Name", jsonObject.getString("Dname"));
                contentValues.put("Degree", jsonObject.getString("Deg"));
                contentValues.put("Specialty", jsonObject.getString("Spec"));
                contentValues.put("ConsultationFee", jsonObject.getInt("Fee"));
                contentValues.put("AvgConsultationTime", jsonObject.getInt("Act"));
                contentValues.put("Mobile", jsonObject.getString("mobile"));
                contentValues.put("Work", jsonObject.getString("land"));
                contentValues.put("Email", jsonObject.getString("email"));
                contentValues.put("remarks", jsonObject.getString("remarks"));
                contentValues.put("EntityID", jsonObject.getString("EntityID"));
                db.insert("tbl_doctorlist", null, contentValues);
            }
        }
        catch (Exception e){logAppError(PageName, "addHospitalList", "Exception", e.getMessage());}
        finally {db.close();}
    }

    /*public void clearHospitalHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        //db.delete("tbl_hospitallist", null, null);
        //db.delete("tbl_doctorlist", null, null);
        db.delete("tbl_appointmentlist", null, null);
        db.close();
    }*/

    public void deleteCompletedAppointments(String Guid){
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "delete from tbl_appointmentlist where Guid = '"+Guid+"' and OutTime != '' or OutTime is not null";
        db.execSQL(Query);
    }

    public long getRecordCount(String tablename, String guid){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select count(*) from "+tablename+" where Guid = '"+guid+"' or '"+guid+"' = ''";
        SQLiteStatement s = db.compileStatement(Query);
        long cnt =  s.simpleQueryForLong();
        s.close();
        db.close();
        return cnt;
    }

    public List<AppointmentInfo> getAppointmentList(String guid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        List<AppointmentInfo> rows = new ArrayList<>();
        try {
            res =  db.rawQuery( "select * from tbl_appointmentlist where Guid = '"+guid+"' and InTime IS NOT NULL and OutTime IS NULL", null );
            if (res.moveToFirst()) {
                do {
                    int ApptID = res.getInt(res.getColumnIndex("ApptID"));
                    String PatientName = res.getString(res.getColumnIndex("PatientName"));
                    int AgeYear = res.getInt(res.getColumnIndex("AgeYear"));
                    int AgeMonth = res.getInt(res.getColumnIndex("AgeMonth"));
                    int Gender = res.getInt(res.getColumnIndex("Gender"));
                    String ApptTime = res.getString(res.getColumnIndex("ApptTime"));
                    String Guid = res.getString(res.getColumnIndex("Guid"));
                    String InTime = res.getString(res.getColumnIndex("InTime"));
                    String OutTime = res.getString(res.getColumnIndex("OutTime"));
                    int JustIn = res.getInt(res.getColumnIndex("JustIn"));
                    int UserCancelled = res.getInt(res.getColumnIndex("UserCancelled"));
                    String PatientPhone = res.getString(res.getColumnIndex("PatientPhone"));
                    AppointmentInfo hi = new AppointmentInfo(ApptID,PatientName,AgeYear,AgeMonth,Gender,ApptTime,Guid,InTime,OutTime,JustIn, UserCancelled, PatientPhone);
                    rows.add(hi);
                } while (res.moveToNext());
            }

            res =  db.rawQuery( "select * from tbl_appointmentlist where Guid = '"+guid+"' and InTime IS NULL and OutTime IS NULL", null );
            if (res.moveToFirst()) {
                do {
                    int ApptID = res.getInt(res.getColumnIndex("ApptID"));
                    String PatientName = res.getString(res.getColumnIndex("PatientName"));
                    int AgeYear = res.getInt(res.getColumnIndex("AgeYear"));
                    int AgeMonth = res.getInt(res.getColumnIndex("AgeMonth"));
                    int Gender = res.getInt(res.getColumnIndex("Gender"));
                    String ApptTime = res.getString(res.getColumnIndex("ApptTime"));
                    String Guid = res.getString(res.getColumnIndex("Guid"));
                    String InTime = res.getString(res.getColumnIndex("InTime"));
                    String OutTime = res.getString(res.getColumnIndex("OutTime"));
                    int JustIn = res.getInt(res.getColumnIndex("JustIn"));
                    int UserCancelled = res.getInt(res.getColumnIndex("UserCancelled"));
                    String PatientPhone = res.getString(res.getColumnIndex("PatientPhone"));
                    AppointmentInfo hi = new AppointmentInfo(ApptID,PatientName,AgeYear,AgeMonth,Gender,ApptTime,Guid,InTime,OutTime,JustIn, UserCancelled, PatientPhone);
                    rows.add(hi);
                } while (res.moveToNext());
            }

            res =  db.rawQuery( "select * from tbl_appointmentlist where Guid = '"+guid+"' and InTime IS NOT NULL and OutTime IS NOT NULL", null );
            if (res.moveToFirst()) {
                do {
                    int ApptID = res.getInt(res.getColumnIndex("ApptID"));
                    String PatientName = res.getString(res.getColumnIndex("PatientName"));
                    int AgeYear = res.getInt(res.getColumnIndex("AgeYear"));
                    int AgeMonth = res.getInt(res.getColumnIndex("AgeMonth"));
                    int Gender = res.getInt(res.getColumnIndex("Gender"));
                    String ApptTime = res.getString(res.getColumnIndex("ApptTime"));
                    String Guid = res.getString(res.getColumnIndex("Guid"));
                    String InTime = res.getString(res.getColumnIndex("InTime"));
                    String OutTime = res.getString(res.getColumnIndex("OutTime"));
                    int JustIn = res.getInt(res.getColumnIndex("JustIn"));
                    int UserCancelled = res.getInt(res.getColumnIndex("UserCancelled"));
                    String PatientPhone = res.getString(res.getColumnIndex("PatientPhone"));
                    AppointmentInfo hi = new AppointmentInfo(ApptID,PatientName,AgeYear,AgeMonth,Gender,ApptTime,Guid,InTime,OutTime,JustIn, UserCancelled, PatientPhone);
                    rows.add(hi);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "addHospitalList", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return rows;
    }

    public void setAppointmentStatus(int ApptID, String colname){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            String ts = new TimeStamp().getCurrentTimeStamp();
            String Query = "update tbl_appointmentlist set ";
            switch (colname){
                case "InTime":
                    Query = Query + "InTime = '"+ts+"'";
                    break;
                case "OutTime":
                    Query = Query + "OutTime = '"+ts+"'";
                    break;
                case "JustIn":
                    Query = Query + "JustIn = 0";
                    break;
                case "UserCancelled":
                    Query = Query + "UserCancelled = 1";
                    break;
                default:
                    break;
            }
            Query = Query + " where ApptID="+ApptID;
            db.execSQL(Query);
        }
        catch (Exception e){logAppError(PageName, "setAppointmentStatus", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public int getNewApptStatus(int ApptID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select JustIn from tbl_appointmentlist where ApptID="+ApptID, null );
        int apptStatus = 0;
        try {
            if (res.moveToFirst()) {
                do {
                    apptStatus = res.getInt(res.getColumnIndex("JustIn"));
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "addHospitalList", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return apptStatus;
    }

    public void addAppointmentList(String inputstring){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            JSONArray jsonArray = new JSONArray(inputstring);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("ApptID", jsonObject.getString("ApptID"));
                contentValues.put("PatientName", jsonObject.getString("pn"));
                contentValues.put("AgeYear", jsonObject.getInt("ay"));
                contentValues.put("AgeMonth", jsonObject.getInt("am"));
                contentValues.put("Gender", jsonObject.getInt("gen"));
                contentValues.put("ApptTime", jsonObject.getString("ApptTime").replace('T',' ').substring(0,19));
                contentValues.put("Guid", jsonObject.getString("guid"));
                contentValues.put("PatientPhone", jsonObject.getString("PatientPhone"));


                if(!jsonObject.getString("InTime").equalsIgnoreCase("null"))
                    contentValues.put("InTime", jsonObject.getString("InTime").replace('T',' ').substring(0,19));
                if(!jsonObject.getString("OutTime").equalsIgnoreCase("null"))
                    contentValues.put("OutTime", jsonObject.getString("OutTime").replace('T',' ').substring(0,19));

                db.insertWithOnConflict("tbl_appointmentlist","ApptID",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
        catch (Exception e){logAppError(PageName, "addAppointmentList", "Exception", e.getMessage());}
        finally {db.close();}
    }

    /*public void addAppointmentList(JSONObject jsonObject){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put("ApptID", jsonObject.getString("ApptID"));
            contentValues.put("PatientName", jsonObject.getString("pn"));
            contentValues.put("AgeYear", jsonObject.getInt("ay"));
            contentValues.put("AgeMonth", jsonObject.getInt("am"));
            contentValues.put("Gender", jsonObject.getInt("gen"));
            contentValues.put("ApptTime", jsonObject.getString("ApptTime").replace('T',' ').substring(0,19));
            contentValues.put("Guid", jsonObject.getString("guid"));
            contentValues.put("JustIn", jsonObject.getString("JustIn"));

            if(!jsonObject.getString("InTime").equalsIgnoreCase("null"))
                contentValues.put("InTime", jsonObject.getString("InTime").replace('T',' ').substring(0,19));
            if(!jsonObject.getString("OutTime").equalsIgnoreCase("null"))
                contentValues.put("OutTime", jsonObject.getString("OutTime").replace('T',' ').substring(0,19));

            db.insertWithOnConflict("tbl_appointmentlist","ApptID",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        }
        catch (Exception e){logAppError(PageName, "addAppointmentList", "Exception", e.getMessage());}
        finally {db.close();}
    }*/

    public List<DoctorInfo> getDoctorList(int EntityID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from tbl_doctorlist where EntityID = "+EntityID, null );
        List<DoctorInfo> rows = new ArrayList<>();
        try {
            if (res.moveToFirst()) {
                do {
                    String Name = res.getString(res.getColumnIndex("Name"));
                    String Degree = res.getString(res.getColumnIndex("Degree"));
                    String Specialty = res.getString(res.getColumnIndex("Specialty"));
                    int ConsultationFee = res.getInt(res.getColumnIndex("ConsultationFee"));
                    int AvgConsultationTime = res.getInt(res.getColumnIndex("AvgConsultationTime"));
                    String Mobile = res.getString(res.getColumnIndex("Mobile"));
                    String Work = res.getString(res.getColumnIndex("Work"));
                    String Email = res.getString(res.getColumnIndex("Email"));
                    String remarks = res.getString(res.getColumnIndex("remarks"));
                    String Guid = res.getString(res.getColumnIndex("Guid"));
                    DoctorInfo hi = new DoctorInfo(Name,Degree,Specialty,ConsultationFee,AvgConsultationTime,Mobile,Work,Email,remarks,Guid);
                    rows.add(hi);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "addHospitalList", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return rows;
    }

    public String getDoctorName(String guid){

        SQLiteDatabase db = this.getReadableDatabase();
        String DoctorName = "";
        Cursor res =  db.rawQuery("Select Name from tbl_doctorlist where Guid = '"+guid+"'", null );
        if (res.moveToFirst()) {
            do {
                DoctorName =  res.getString(res.getColumnIndex("Name"));
            } while (res.moveToNext());
        }
        res.close();
        db.close();
        return DoctorName;
    }

    public List<String> getGuidList(int EntityID){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> GuidList =  new ArrayList<>();
        Cursor res =  db.rawQuery("Select distinct Guid from tbl_doctorlist where EntityID = "+EntityID, null );
        if (res.moveToFirst()) {
            do {
                String Guid =  res.getString(res.getColumnIndex("Guid"));
                GuidList.add(Guid);
            } while (res.moveToNext());
        }
        res.close();
        db.close();
        return GuidList;
    }


    //Common Tables
    public List<EntityInfo> getEntityList(String tablename){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+tablename, null );
        List<EntityInfo> rows = new ArrayList<>();
        try {
            if (res.moveToFirst()) {
                do {
                    int EEID = res.getInt(res.getColumnIndex("EEID"));
                    String Ename = res.getString(res.getColumnIndex("Ename"));
                    String Add1 = res.getString(res.getColumnIndex("Add1"));
                    String Add2 = res.getString(res.getColumnIndex("Add2"));
                    String City = res.getString(res.getColumnIndex("City"));
                    String State = res.getString(res.getColumnIndex("State"));
                    String Country = res.getString(res.getColumnIndex("Country"));
                    String Zip = res.getString(res.getColumnIndex("Zip"));
                    String ContactNo1 = res.getString(res.getColumnIndex("ContactNo1"));
                    String ContactNo2 = res.getString(res.getColumnIndex("ContactNo2"));
                    String EmailID = res.getString(res.getColumnIndex("EmailID"));
                    String WebSite = res.getString(res.getColumnIndex("WebSite"));
                    double Lat = res.getDouble(res.getColumnIndex("Lat"));
                    double Long = res.getDouble(res.getColumnIndex("Long"));
                    int PD = res.getInt(res.getColumnIndex("PD"));
                    EntityInfo hi = new EntityInfo(EEID,Ename,Add1,Add2,City,State,Country,Zip,ContactNo1,ContactNo2,EmailID,WebSite,Lat,Long,PD);
                    rows.add(hi);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError(PageName, "getEntityList", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return rows;
    }

    public void addEntityList(String inputstring, String tablename){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            JSONArray jsonArray = new JSONArray(inputstring);
            db.delete(tablename, null, null);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("EEID", jsonObject.getInt("EEID"));
                contentValues.put("Ename", jsonObject.getString("Ename"));
                contentValues.put("Add1", jsonObject.getString("Add1"));
                contentValues.put("Add2", jsonObject.getString("Add2"));
                contentValues.put("City", jsonObject.getString("City"));
                contentValues.put("State", jsonObject.getString("State"));
                contentValues.put("Country", jsonObject.getString("Country"));
                contentValues.put("Zip", jsonObject.getString("Zip"));
                contentValues.put("ContactNo1", jsonObject.getString("ContactNo1"));
                contentValues.put("ContactNo2", jsonObject.getString("ContactNo2"));
                contentValues.put("EmailID", jsonObject.getString("EmailID"));
                contentValues.put("WebSite", jsonObject.getString("WebSite"));
                contentValues.put("Lat", jsonObject.getDouble("Lat"));
                contentValues.put("Long", jsonObject.getDouble("Long"));
                contentValues.put("PD", jsonObject.getInt("PD"));
                db.insert(tablename, null, contentValues);
            }
        }
        catch (Exception e){logAppError(PageName, "addEntityList", "Exception", e.getMessage());}
        finally {db.close();}
    }

    public long getRecordCount(String tablename){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select count(*) from "+tablename;
        SQLiteStatement s = db.compileStatement(Query);
        long cnt =  s.simpleQueryForLong();
        s.close();
        db.close();
        return cnt;
    }

    public JSONObject getDeviceInfo(){
        SQLiteDatabase db = this.getReadableDatabase();
        JSONObject jobj = new JSONObject();
        Cursor res =  db.rawQuery("Select * from tbl_deviceinfo", null );
        try {
            if (res.moveToFirst()) {
                do {
                    jobj.put("DeviceID", res.getString(res.getColumnIndex("DeviceID")));
                    jobj.put("DeviceType", res.getString(res.getColumnIndex("DeviceType")));
                    jobj.put("DeviceVersion", res.getString(res.getColumnIndex("DeviceVersion")));
                    jobj.put("AppVersion", res.getString(res.getColumnIndex("AppVersion")));
                    jobj.put("DeviceToken", res.getString(res.getColumnIndex("DeviceToken")));
                    jobj.put("ServerMapID", res.getInt(res.getColumnIndex("ServerMapID")));
                    jobj.put("ModTime", res.getString(res.getColumnIndex("ModTime")));

                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getDeviceInfo", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return jobj;
    }

    public boolean setDeviceInfo(String column, String value){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String ts = new TimeStamp().getCurrentTimeStamp();

            if(column.equalsIgnoreCase("ServerMapID"))
                db.execSQL("update tbl_deviceinfo set "+column+" = "+value+", ModTime = '"+ts+"'");
            else
                db.execSQL("update tbl_deviceinfo set "+column+" = '"+value+"', ModTime = '"+ts+"'");
        }
        catch (Exception e){logAppError("DBHelper", "setDeviceInfo", "Exception", e.getMessage());}
        finally {db.close();}
        return true;
    }

    public boolean setSystemParameter(String ParamName, String ParamValue){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ParamName", ParamName);
        contentValues.put("ParamValue", ParamValue);
        db.insertWithOnConflict("tbl_SystemParameters","ParamName",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return true;
    }

    public String getSystemParameter(String ParamName){
        SQLiteDatabase db = this.getReadableDatabase();
        String ParamValue = "";
        Cursor res =  db.rawQuery("Select ParamValue from tbl_SystemParameters where ParamName='"+ParamName+"'", null );
        if(res.moveToFirst()) {
            do{
                ParamValue = res.getString(res.getColumnIndex("ParamValue"));
            }while (res.moveToNext());
        }
        db.close();
        return ParamValue;
    }

    public String getDeviceID(){

        SQLiteDatabase db = this.getReadableDatabase();
        String DeviceID = "";
        int ServerMapID = 0;
        Cursor res =  db.rawQuery("Select * from tbl_deviceinfo", null );
        if (res.moveToFirst()) {
            do {
                DeviceID =  res.getString(res.getColumnIndex("DeviceID"));
                ServerMapID = res.getInt(res.getColumnIndex("ServerMapID"));
            } while (res.moveToNext());
        }
        res.close();
        db.close();
        if(ServerMapID != 0)
            DeviceID = ServerMapID+"";
        return DeviceID;
    }

    public List<ErrorLog> getErrorLog(int limit){
        SQLiteDatabase db = this.getReadableDatabase();
        List<ErrorLog> rows = new ArrayList<ErrorLog>();
        Cursor res =  db.rawQuery( "select * from tbl_errorLog order by id limit "+limit, null );
        if(res.moveToFirst()) {
            do {
                String PageName = res.getString(res.getColumnIndex("PageName"));
                String MethodName = res.getString(res.getColumnIndex("MethodName"));
                String ExceptionType = res.getString(res.getColumnIndex("ExceptionType"));
                String ExceptionText = res.getString(res.getColumnIndex("ExceptionText"));
                String OcrDate = res.getString(res.getColumnIndex("OcrTime"));
                ErrorLog row = new ErrorLog(PageName, MethodName, ExceptionType, ExceptionText, OcrDate);
                rows.add(row);
            }while (res.moveToNext());
        }
        res.close();
        db.close();
        return rows;
    }

    public void deleteErrorLog(int limit){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = " id in (select id from tbl_errorLog order by id limit "+limit+")";
        db.delete("tbl_errorLog", where,  null);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        /*db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);*/
    }
}
