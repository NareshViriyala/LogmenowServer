package shared;

import android.support.v4.app.INotificationSideChannel;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by nviriyala on 05-07-2016.
 */
public class Models {
    public static class TimeStamp{
        private String ts;

        public TimeStamp(){
            //this.ts = new java.sql.Timestamp(new java.util.Date().getTime());
            setTimeStamp();
        }

        public void setTimeStamp(){
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.ts = df.format(c.getTime());
        }

        public String getCurrentTimeStamp(){
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return df.format(c.getTime());
        }
    }

    public static class ErrorLog{
        private String PageName;
        private String MethodName;
        private String ExceptionType;
        private String ExceptionText;
        private String OcrTime;

        public ErrorLog(String PageName, String MethodName, String ExceptionType, String ExceptionText, String OcrTime){
            this.PageName = PageName;
            this.MethodName = MethodName;
            this.ExceptionType = ExceptionType;
            this.ExceptionText = ExceptionText;
            this.OcrTime = OcrTime;
        }

        public String getPageName(){return this.PageName;}
        public String getMethodName(){return this.MethodName;}
        public String getExceptionType(){return this.ExceptionType;}
        public String getExceptionText(){
            if(this.ExceptionText.length() > 500)
                return this.ExceptionText.substring(1,499);
            else
                return this.ExceptionText;
        }
        public String getOcrTime(){return this.OcrTime;}
    }

    public static class BoxErrorLog{
        private String DeviceID;
        private List<ErrorLog> rows;
        public BoxErrorLog(String DeviceID, List<ErrorLog> rows){
            this.DeviceID = DeviceID;
            this.rows = rows;
        }
    }

    public static class EntityInfo{
        private int EEID;
        private String Ename;
        private String Add1;
        private String Add2;
        private String City;
        private String State;
        private String Country;
        private String Zip;
        private String ContactNo1;
        private String ContactNo2;
        private String EmailID;
        private String WebSite;
        private double Lat;
        private double Long;
        private int PD;

        public EntityInfo(int EEID,
                            String Ename,
                            String Add1,
                            String Add2,
                            String City,
                            String State,
                            String Country,
                            String Zip,
                            String ContactNo1,
                            String ContactNo2,
                            String EmailID,
                            String WebSite,
                            double Lat,
                            double Long,
                            int PD){
            this.EEID = EEID;
            this.Ename = Ename;
            this.Add1 = Add1;
            this.Add2 = Add2;
            this.City = City;
            this.State = State;
            this.Country = Country;
            this.Zip = Zip;
            this.ContactNo1 = ContactNo1;
            this.ContactNo2 = ContactNo2;
            this.EmailID = EmailID;
            this.WebSite = WebSite;
            this.Lat = Lat;
            this.Long = Long;
            this.PD = PD;
        }

        public int getEEID(){return this.EEID;}
        public String getEname(){return this.Ename;}
        public String getAdd1(){return this.Add1;}
        public String getAdd2(){return this.Add2;}
        public String getCity(){return this.City;}
        public String getState(){return this.State;}
        public String getCountry(){return this.Country;}
        public String getZip(){return this.Zip;}
        public String getContactNo1(){return this.ContactNo1;}
        public String getContactNo2(){return this.ContactNo2;}
        public String getEmailID(){return this.EmailID;}
        public String getWebSite(){return this.WebSite;}
        public double getLat(){return this.Lat;}
        public double getLong(){return this.Long;}
        public int getPD(){return this.PD;}
    }

    public static class DoctorInfo{
        private String Name;
        private String Degree;
        private String Specialty;
        private int ConsultationFee;
        private int AvgConsultationTime;
        private String Mobile;
        private String Work;
        private String Email;
        private String remarks;
        private String Guid;

        public DoctorInfo(String Name,
                          String Degree,
                          String Specialty,
                          int ConsultationFee,
                          int AvgConsultationTime,
                          String Mobile,
                          String Work,
                          String Email,
                          String remarks,
                          String Guid){
            this.Name = Name;
            this.Degree = Degree;
            this.Specialty = Specialty;
            this.ConsultationFee = ConsultationFee;
            this.AvgConsultationTime = AvgConsultationTime;
            this.Mobile = Mobile;
            this.Work = Work;
            this.Email = Email;
            this.remarks = remarks;
            this.Guid = Guid;
        }
        public String getName(){return this.Name;}
        public String getDegree(){return this.Degree;}
        public String getSpecialty(){return this.Specialty;}
        public int getConsultationFee(){return this.ConsultationFee;}
        public int getAvgConsultationTime(){return this.AvgConsultationTime;}
        public String getMobile(){return this.Mobile;}
        public String getWork(){return this.Work;}
        public String getEmail(){return this.Email;}
        public String getremarks(){return this.remarks;}
        public String getGuid(){return this.Guid;}
    }

    public static class AppointmentInfo{
        private int ApptID;
        private String PatientName;
        private int AgeYear;
        private int AgeMonth;
        private int Gender;
        private String ApptTime;
        private String Guid;
        private String InTime;
        private String OutTime;
        private int JustIn;
        private int UserCancelled;
        private String PatientPhone;

        public AppointmentInfo(int ApptID, String PatientName, int AgeYear, int AgeMonth, int Gender, String ApptTime, String Guid, String InTime, String OutTime, int JustIn, int UserCancelled, String PatientPhone){
            this.ApptID = ApptID;
            this.PatientName = PatientName;
            this.AgeYear = AgeYear;
            this.AgeMonth = AgeMonth;
            this.Gender = Gender;
            this.ApptTime = ApptTime;
            this.Guid = Guid;
            this.InTime = InTime;
            this.OutTime = OutTime;
            this.JustIn = JustIn;
            this.UserCancelled = UserCancelled;
            this.PatientPhone = PatientPhone;
        }

        public int getApptID(){return this.ApptID;}
        public String getPatientName(){return this.PatientName;}
        public int getAgeYear(){return this.AgeYear;}
        public int getAgeMonth(){return this.AgeMonth;}
        public int getGender(){return this.Gender;}
        public String getGuid(){return this.Guid;}
        public String getApptTime(){return this.ApptTime;}
        public String getInTime(){return this.InTime;}
        public String getOutTime(){return this.OutTime;}
        public int getJustIn(){return this.JustIn;}
        public int getUserCancelled(){return this.UserCancelled;}
        public void setJustIn(int JustIn){this.JustIn = JustIn;}
        public String getPatientPhone(){return this.PatientPhone;}
    }

    public static class RestaurantTable{
        private String Guid;
        private int EntityID;
        private String TableNo;
        private int Call;
        private int OrderPlaced;
        private int TblStatus;

        public RestaurantTable(String Guid,int EntityID,String TableNo, int Call, int OrderPlaced, int TblStatus){
            this.Guid = Guid;
            this.EntityID = EntityID;
            this.TableNo = TableNo;
            this.Call = Call;
            this.OrderPlaced = OrderPlaced;
            this.TblStatus = TblStatus;
        }

        public String getGuid(){return this.Guid;}
        public int getEntityID(){return this.EntityID;}
        public String getTableNo(){return this.TableNo;}
        public int getCall(){return this.Call;}
        public int getOrderPlaced(){return this.OrderPlaced;}
        public int getTblStatus(){return this.TblStatus;}
    }

    public static class RestaurantMenuItem {
        private int ItemID;
        private String ItemGroup;
        private String ItemName;
        private int ItemType;
        private int ItemPrice;
        private int Quantity;
        private String Person;
        private int DeviceID;
        private int MasterID;
        private int OrderID;

        public RestaurantMenuItem (int ItemID,String ItemGroup,String ItemName,int ItemPrice,int Quantity,String Person,int DeviceID,int MasterID,int OrderID,int ItemType){
            this.ItemID = ItemID;
            this.ItemGroup = ItemGroup;
            this.ItemName = ItemName;
            this.ItemPrice = ItemPrice;
            this.Quantity = Quantity;
            this.Person = Person;
            this.DeviceID = DeviceID;
            this.MasterID = MasterID;
            this.OrderID = OrderID;
            this.ItemType = ItemType;
        }

        public int getItemID(){return this.ItemID;}
        public String getItemGroup(){return this.ItemGroup;}
        public String getItemName(){return this.ItemName;}
        public int getItemPrice(){return this.ItemPrice;}
        public int getQuantity(){return this.Quantity;}
        public void setQuantity(int cnt){this.Quantity = cnt;}
        public String getPerson(){return this.Person;}
        public int getDeviceID(){return this.DeviceID;}
        public int getMasterID(){return this.MasterID;}
        public int getItemType(){return this.ItemType;}
        public int getOrderID(){return this.OrderID;}
    }

    public static class RestaurantMenu{
        private int TID; //Table ID
        private int OID; //Order
        private boolean IT; //Item Type - Veg/NonVeg
        private String IG; //Item Group
        private String IN; //Item Name
        private int IP; //Item Price
        private String ID; //Item Description
        private int SI; //Spice Index
        private boolean CR; //Chef Recommended
        private int Usr; //Users
        private int RT; //Rating
        private int QTY; //Quantity
        private boolean Selected;


        public RestaurantMenu(int TID, int OID, boolean IT, String IG, String IN, int IP, String ID, int SI, boolean CR, int Usr, int RT, int QTY, boolean Selected){
            this.TID = TID;
            this.OID = OID;
            this.IT = IT;
            this.IG = IG;
            this.IN = IN;
            this.IP = IP;
            this.ID = ID;
            this.SI = SI;
            this.CR = CR;
            this.Usr = Usr;
            this.RT = RT;
            this.QTY = QTY;
            this.Selected = Selected;
        }

        public int getTID(){return this.TID;}
        public int getOID(){return this.OID;}
        public boolean getIT(){return this.IT;}
        public String getIG(){return this.IG;}
        public String getIN(){return this.IN;}
        public int getIP(){return this.IP;}
        public String getID(){return this.ID;}
        public int getSI(){return this.SI;}
        public boolean getCR(){return this.CR;}
        public int getUsr(){return this.Usr;}
        public int getRT(){return this.RT;}
        public int getQTY(){return this.QTY;}
        public boolean getSelected(){return  this.Selected;}

        public void setSelected(boolean Selected){this.Selected = Selected;}
        public void setQTY(int cnt){this.QTY = cnt;}
    }

    public static class SecurityCheckEntry{
        private long DBID;
        private int EntityID;
        private String DeviceID;
        private String Name;
        private String Age;
        private String DOB;
        private String Vehicle;
        private String VehicleType;
        private String ComingFrom;
        private String VisitingCompany;
        private String HomeAddress;
        private String Email;
        private String Phone;
        private String Gender;
        private String Pov;
        private String ContactPerson;
        private String OfficeAddress;
        private String Block;
        private String Flat;
        private boolean synced = false;
        private boolean isDeleted = false;
        private String EnterTime;
        private int ServerID;

        /*public SecurityCheckEntry(long DBID,
                                  String DeviceID,
                                  String Name,
                                  String Age,
                                  String DOB,
                                  String Vehicle,
                                  String ComingFrom,
                                  String VisitingCompany,
                                  String HomeAddress,
                                  String Email,
                                  String Phone,
                                  String Gender,
                                  String Pov,
                                  String ContactPerson,
                                  String OfficeAddress,
                                  boolean synced,
                                  boolean isDeleted){
            this.DBID = DBID;
            this.DeviceID = DeviceID;
            this.Name = Name;
            this.Age = Age;
            this.DOB = DOB;
            this.Vehicle = Vehicle;
            this.ComingFrom = ComingFrom;
            this.VisitingCompany = VisitingCompany;
            this.HomeAddress = HomeAddress;
            this.Email = Email;
            this.Phone = Phone;
            this.Gender = Gender;
            this.Pov = Pov;
            this.ContactPerson = ContactPerson;
            this.OfficeAddress = OfficeAddress;
            this.synced = synced;
            this.isDeleted = isDeleted;
        }*/

        public long getDBID(){return this.DBID;}
        public int getEntityID(){return this.EntityID;}
        public int getServerID(){return this.ServerID;}
        public String getDeviceID(){return this.DeviceID;}
        public String getName(){return this.Name;}
        public String getAge(){return this.Age;}
        public String getDOB(){return this.DOB;}
        public String getVehicle(){return this.Vehicle;}
        public String getVehicleType(){return this.VehicleType;}
        public String getComingFrom(){return this.ComingFrom;}
        public String getVisitingCompany(){return this.VisitingCompany;}
        public String getHomeAddress(){return this.HomeAddress;}
        public String getEmail(){return this.Email;}
        public String getPhone(){return this.Phone;}
        public String getGender(){return this.Gender;}
        public String getPov(){return this.Pov;}
        public String getContactPerson(){return this.ContactPerson;}
        public String getOfficeAddress(){return this.OfficeAddress;}
        public String getBlock(){return this.Block;}
        public String getFlat(){return this.Flat;}
        public boolean getsynced(){return this.synced;}
        public String getEnterTime(){return this.EnterTime;}
        public boolean getisDeleted(){return this.isDeleted;}

        public void setDBID(long id){this.DBID = id;}
        public void setEntityID(int EntityID){this.EntityID = EntityID;}
        public void setServerID(int id){this.ServerID = id;}
        public void setDeviceID(String DeviceID){this.DeviceID = DeviceID;}
        public void setName(String Name){this.Name = Name;}
        public void setAge(String Age){this.Age = Age;}
        public void setDOB(String DOB){this.DOB = DOB;}
        public void setVehicle(String Vehicle){this.Vehicle = Vehicle;}
        public void setVehicleType(String VehicleType){this.VehicleType = VehicleType;}
        public void setComingFrom(String ComingFrom){this.ComingFrom = ComingFrom;}
        public void setVisitingCompany(String VisitingCompany){this.VisitingCompany = VisitingCompany;}
        public void setHomeAddress(String HomeAddress){this.HomeAddress = HomeAddress;}
        public void setPhone(String Phone){this.Phone = Phone;}
        public void setEmail(String Email){this.Email = Email;}
        public void setGender(String Gender){this.Gender = Gender;}
        public void setPov(String Pov){this.Pov = Pov;}
        public void setContactPerson(String ContactPerson){this.ContactPerson = ContactPerson;}
        public void setOfficeAddress(String OfficeAddress){this.OfficeAddress = OfficeAddress;}
        public void setBlock(String Block){this.Block = Block;}
        public void setFlat(String Flat){this.Flat = Flat;}
        public void setisDeleted(boolean isDeleted){this.isDeleted = isDeleted;}
        public void setEnterTime(String EnterTime){this.EnterTime = EnterTime;}
        public void setsynced(boolean synced){this.synced = synced;}

    }

    public static class ParkingInfo{
        private long DBID;
        private int ServerID;
        private int EntityID;
        private String DeviceID;
        private String VehicleNo;
        private int VehicleType;
        private int Usage;
        private int TariffType;
        private int TariffAmt;
        private String LocalTime;
        private String LocalEntryDate;
        private String LocalExitDate;
        private String ServerEntryDate;
        private String ServerExitDate;
        private String ServerParkTime;
        private String LocalParkTime;
        private int ServerSync;
        private int OverRide = 0;


        /*public ParkingInfo(long id,
                           int ServerID,
                           String DeviceID,
                           String VehicleNo,
                           int VehicleType,
                           int Usage,
                           int TariffType,
                           int TariffAmt,
                           String LocalEntryDate,
                           String LocalExitDate,
                           String ServerEntryDate,
                           String ServerExitDate,
                           int ServerSync){
            this.id = id;
            this.ServerID = ServerID;
            this.DeviceID = DeviceID;
            this.VehicleNo = VehicleNo;
            this.VehicleType = VehicleType;
            this.Usage = Usage;
            this.TariffType = TariffType;
            this.TariffAmt = TariffAmt;
            this.LocalEntryDate = LocalEntryDate;
            this.LocalExitDate = LocalExitDate;
            this.ServerEntryDate = ServerEntryDate;
            this.ServerExitDate = ServerExitDate;
            this.ServerSync = ServerSync;
        }*/

        public long getDBID(){return this.DBID;}
        public int getServerID(){return this.ServerID;}
        public int getEntityID(){return this.EntityID;}
        public String getDeviceID(){return this.DeviceID;}
        public String getVehicleNo(){return this.VehicleNo;}
        public int getVehicleType(){return this.VehicleType;}
        public int getUsage(){return this.Usage;}
        public int getTariffType(){return this.TariffType;}
        public int getTariffAmt(){return this.TariffAmt;}
        public String getLocalTime(){return this.LocalTime;}
        public String getLocalEntryDate(){return this.LocalEntryDate;}
        public String getLocalExitDate(){return this.LocalExitDate;}
        public String getServerEntryDate(){return this.ServerEntryDate;}
        public String getServerExitDate(){return this.ServerExitDate;}
        public String getLocalParkTime(){return this.LocalParkTime;}
        public String getServerParkTime(){return this.ServerParkTime;}
        public int getServerSync(){return this.ServerSync;}
        public int getOverRide(){return this.OverRide;}

        public void setDBID(long DBID){this.DBID = DBID;}
        public void setServerID(int ServerID){this.ServerID = ServerID;}
        public void setEntityID(int EntityID){this.EntityID = EntityID;}
        public void setDeviceID(String DeviceID){this.DeviceID = DeviceID;}
        public void setVehicleNo(String VehicleNo){this.VehicleNo = VehicleNo;}
        public void setVehicleType(int VehicleType){this.VehicleType = VehicleType;}
        public void setUsage(int Usage){this.Usage = Usage;}
        public void setTariffType(int TariffType){this.TariffType = TariffType;}
        public void setTariffAmt(int TariffAmt){this.TariffAmt = TariffAmt;}
        public void setLocalTime(String LocalTime){this.LocalTime = LocalTime;}
        public void setLocalEntryDate(String LocalEntryDate){this.LocalEntryDate = LocalEntryDate;}
        public void setLocalExitDate(String LocalExitDate){this.LocalExitDate = LocalExitDate;}
        public void setServerEntryDate(String ServerEntryDate){this.ServerEntryDate = ServerEntryDate;}
        public void setServerExitDate(String ServerExitDate){this.ServerExitDate = ServerExitDate;}
        public void setLocalParkTime(String LocalParkTime){this.LocalParkTime = LocalParkTime;}
        public void setServerParkTime(String ServerParkTime){this.ServerParkTime = ServerParkTime;}
        public void setServerSync(int ServerSync){this.ServerSync = ServerSync;}
        public void setOverRide(int OverRide){this.OverRide = OverRide;}

    }

    public static class RestaurantTableLog{
        private int MasterID;
        private int TotalItems;
        private int TotalBill;
        private String PlacedTime;
        private String Items;
        private int OID;

        public RestaurantTableLog(int MasterID,int TotalItems,int TotalBill,String PlacedTime,String Items,int OID){
            this.MasterID = MasterID;
            this.TotalItems = TotalItems;
            this.TotalBill = TotalBill;
            this.PlacedTime = PlacedTime;
            this.Items = Items;
            this.OID = OID;
        }

        public int getMasterID(){return this.MasterID;}
        public int getTotalItems(){return this.TotalItems;}
        public int getTotalBill(){return this.TotalBill;}
        public String getPlacedTime(){return this.PlacedTime;}
        public String getItems(){return this.Items;}
        public int getOID(){return this.OID;}

        public void setMasterID(int MasterID){this.MasterID = MasterID;}
        public void setTotalItems(int TotalItems){this.TotalItems = TotalItems;}
        public void setTotalBill(int TotalBill){this.TotalBill = TotalBill;}
        public void setPlacedTime(String PlacedTime){this.PlacedTime = PlacedTime;}
        public void setItems(String Items){this.Items = Items;}
        public void setOID(int OID){this.OID = OID;}
    }
}
