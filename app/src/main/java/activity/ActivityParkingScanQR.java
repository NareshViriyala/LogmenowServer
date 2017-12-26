package activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.logmenowserver.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adapter.AdapterParkingInfo;
import database.DBHelper;
import dialog.DialogParkingUsage;
import dialog.DialogParkingVehicle;
import gcm.PushNotification;
import shared.CommonClasses;
import dialog.DialogParkingContext;
import shared.Models;
import shared.Models.ParkingInfo;
import shared.NetworkDetector;
import shared.WebServiceCall;
import zxing.BinaryBitmap;
import zxing.ChecksumException;
import zxing.DecodeHintType;
import zxing.FormatException;
import zxing.NotFoundException;
import zxing.PlanarYUVLuminanceSource;
import zxing.Reader;
import zxing.Result;
import zxing.common.HybridBinarizer;
import zxing.qrcode.QRCodeReader;

/**
 * Created by nviriyala on 26-08-2016.
 */
public class ActivityParkingScanQR extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback{

    private DBHelper mydb;
    private String PageName = "ActivityParkingScanQR";
    private TextView tv_title;
    private TextView tv_scanStatus;
    private ListView lv_scelst;
    private ImageView img_back;
    private ImageView img_home;
    private ImageView img_more;
    private View vw_footer;
    private TextView tv_sync;
    private TextView tv_clear;
    private TextView tv_context;
    private ImageView img_sync;
    private CommonClasses cc;
    private NetworkDetector nd;
    public AdapterParkingInfo adapterSCE;

    public boolean isCamOn = false;
    private boolean progress = true;
    private boolean screenChanging = false;
    public String lastQRContent = "";

    public int shotInterval = 100;

    public SurfaceView sfv_camview;
    public Camera mCamera;
    public SurfaceHolder surfaceHolder;
    public Camera.PreviewCallback previewCallback;

    public TakeShots takeshots;
    public Timer timer;
    public boolean decodingQR = false;
    public Vibrator v;
    public int vibrateMilli = 400;
    public int EntityID;
    public String Usage;
    public String TariffTypeCode;
    public String VehicleType;
    public DialogParkingContext dpc;
    public DialogParkingVehicle dpv;

    public Gson gson = new Gson();
    public Type ParkingInfotypelist = new TypeToken<List<ParkingInfo>>(){}.getType();
    public Type ParkingInfotype = new TypeToken<ParkingInfo>(){}.getType();

    public Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_parkingscanqr);
            mydb = new DBHelper(this);
            cc = new CommonClasses(this);
            nd = new NetworkDetector(this);
            EntityID = Integer.parseInt(mydb.getSystemParameter("EntityID"));

            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);

            tv_context = (TextView) findViewById(R.id.tv_context);

            tv_scanStatus = (TextView) findViewById(R.id.tv_scanStatus);
            lv_scelst = (ListView) findViewById(R.id.lv_scelst);

            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_home = (ImageView) findViewById(R.id.img_home);
            img_home.setOnClickListener(this);
            img_more = (ImageView) findViewById(R.id.img_more);
            img_more.setOnClickListener(this);

            dpc = new DialogParkingContext(this);
            dpv = new DialogParkingVehicle(this);
            adapterSCE = new AdapterParkingInfo(this);
            lv_scelst.setAdapter(adapterSCE);

            vw_footer = findViewById(R.id.vw_footer);
            inflateFooter();
            //img_processing = (ImageView) findViewById(R.id.img_processing);
            //Glide.with(this).load(R.drawable.loading).into(img_processing);
            lv_scelst.setAdapter(adapterSCE);
            sfv_camview = (SurfaceView) findViewById(R.id.sfv_camview);
            sfv_camview.setOnClickListener(this);
            surfaceHolder = sfv_camview.getHolder();
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

            previewCallback = new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    try {
                        if(!decodingQR && !screenChanging) {
                            decodingQR = true;
                            Object boxdata = data;
                            new decodeQR().execute(boxdata);
                        }
                    } catch (Exception e) {
                        mydb.logAppError(PageName, "onPreviewFrame", "Exception", e.getMessage());
                    }
                }
            };
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try{
            switch (v.getId()){
                case R.id.img_back:
                    onBackPressed();
                    break;
                case R.id.tv_title:
                    onBackPressed();
                    break;
                case R.id.img_home:
                    break;
                case R.id.img_more:
                    break;
                case R.id.sfv_camview:
                    camOperation(!isCamOn);
                    break;
                case R.id.ll_camera:
                    lastQRContent="";
                    camOperation(!isCamOn);
                    break;
                case R.id.ll_search:
                    Intent intent = new Intent(this, ActivityParkingLog.class);
                    startActivity(intent);
                    break;
                case R.id.ll_context:
                    dpc.show();
                    break;
                case R.id.ll_clear:
                    mydb.clearParkingTable(EntityID);
                    if(adapterSCE != null)
                        adapterSCE.clearList();
                    setFooterTextCounters();
                    break;
                case R.id.ll_sync:
                    if(nd.isInternetAvailable()){
                        List<ParkingInfo> unsyncedList = mydb.getParkingEntryRows(EntityID);
                        if(unsyncedList.size() > 0){
                            String input = "<entry>";
                            for(ParkingInfo entry : unsyncedList){
                                input = input + "<ServerID>0</ServerID><EntityID>"+entry.getEntityID()+"</EntityID>";
                                input = input + "<ServerDeviceID>"+mydb.getDeviceID()+"</ServerDeviceID>";
                                input = input + "<LocalDBID>"+entry.getDBID()+"</LocalDBID>";
                                input = input + "<LocalDBTime>"+entry.getLocalTime()+"</LocalDBTime>";
                                input = input + "<DeviceID>"+entry.getDeviceID()+"</DeviceID>";
                                input = input + "<VehicleNo>"+entry.getVehicleNo()+"</VehicleNo>";
                                input = input + "<VehicleType>"+entry.getVehicleType()+"</VehicleType>";
                                input = input + "<TCTID>"+entry.getTariffType()+"</TCTID>";
                                input = input + "<AddType>"+entry.getUsage()+"</AddType>";
                                input = input + "<OverRide>"+entry.getOverRide()+"</OverRide>";
                            }
                            input = input + "</entry>";
                            String lst = gson.toJson(unsyncedList, ParkingInfotypelist).replace(" ", "%20");
                            new pushToserver().execute(input);
                        }
                    }
                    else
                        Toast.makeText(ActivityParkingScanQR.this, getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            img_back.setVisibility(View.VISIBLE);
            tv_title.setText("Scan QR");
            screenChanging = false;
            camOperation(true);
            Usage = mydb.getSystemParameter("ParkingUsage");
            VehicleType = mydb.getSystemParameter("ParkingVehicleType");
            TariffTypeCode = mydb.getSystemParameter("ParkingTariffType");
            if(Usage.equalsIgnoreCase("") || VehicleType.equalsIgnoreCase("") || TariffTypeCode.equalsIgnoreCase(""))
                dpc.show();
            else
                setParkingContext();
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }

    @Override
    protected void onPause() {
        super.onPause();
        screenChanging = true;
        while (decodingQR){
            try{Thread.sleep(100);}
            catch (Exception e){mydb.logAppError(PageName, "onPause", "Exception", e.getMessage());}
        }
        camOperation(false);
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            initiateCameraPreview();
        }
        catch (RuntimeException ex){
            requestCameraPermissions();
        }
        catch (Exception e){
            mydb.logAppError(PageName, "surfaceCreated", "Exception", e.getMessage());
        }
    }

    public void requestCameraPermissions(){
        try{
            int hasCameraAccess = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (hasCameraAccess != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setMessage(getResources().getString(R.string.app_name)+ " " +getResources().getString(R.string.campermission))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ActivityParkingScanQR.this, new String[] {Manifest.permission.CAMERA}, 500);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "requestPermissions", "Exception", e.getMessage());}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case 100: {
                    if (grantResults.length > 0 && grantResults[0] == 500) {
                        initiateCameraPreview();
                    } else {
                        Toast.makeText(this, "We can not scan QR without CAMERA ACCESS \n Go to Settings and allow access.", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onRequestPermissionsResult", "Exception", e.getMessage());}
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try{
            if(mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "surfaceDestroyed", "Exception", e.getMessage());
        }
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters){
        int wid = sfv_camview.getWidth();
        int hig = sfv_camview.getHeight();

        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        int diff = Math.abs((wid * hig)-(sizeList.get(0).width * sizeList.get(0).height));
        for(int i = 0; i < sizeList.size(); i++){
            int slw = sizeList.get(i).width;
            int slh = sizeList.get(i).height;
            if(diff > Math.abs((wid * hig)-(sizeList.get(i).width * sizeList.get(i).height))){
                bestSize = sizeList.get(i);
                diff = Math.abs((wid * hig)-(sizeList.get(i).width * sizeList.get(i).height));
            }
        }
        return bestSize;
    }

    public void requestLocationPermission(){
        try{
            int hasCoarseLocationAccess = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int hasFineLocationAccess = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasCoarseLocationAccess != PackageManager.PERMISSION_GRANTED || hasFineLocationAccess != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ActivityParkingScanQR.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 500);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "requestPermissions", "Exception", e.getMessage());}
    }

    public void initiateCameraPreview(){
        try{
            /*if(mCamera != null)
                return;*/
            mCamera = Camera.open();
            Camera.Parameters params = mCamera.getParameters();
            params.setRotation(90);
            Camera.Size size = getBestPreviewSize(params);
            params.setPreviewSize(size.width, size.height);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            if(takeshots == null)
                takeshots = new TakeShots();
            if(timer == null)
                timer = new Timer();
            timer.schedule(takeshots, 100, shotInterval);
        }
        catch (Exception e){mydb.logAppError(PageName, "initiateCameraPreview", "Exception", e.getMessage());}
    }

    public class TakeShots extends TimerTask {
        public void run(){
            try {
                mCamera.setOneShotPreviewCallback(previewCallback);
            }
            catch (Exception e){
                mydb.logAppError(PageName, "TakeShots", "Exception", e.getMessage());
            }
        }
    }

    public void camOperation(boolean start){
        try{
            lastQRContent = "";
            if(start){
                isCamOn = true;
                if(mCamera != null) {
                    mCamera.startPreview();
                    timer = new Timer();
                    takeshots = new TakeShots();
                    timer.schedule(takeshots, 100, shotInterval);
                }
            }
            else{
                isCamOn = false;
                if(mCamera != null)
                    mCamera.stopPreview();
                if(timer != null){
                    timer.cancel();
                    timer = null;
                }
                if(takeshots != null){
                    takeshots.cancel();
                    takeshots = null;
                }
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "camOperation", "Exception", e.getMessage());}
    }

    public class decodeQR extends AsyncTask<Object, Integer, String> {

        @Override
        protected String doInBackground(Object[] params) {
            //android.os.Debug.waitForDebugger();
            String decodedText = null;
            try {
                byte[] data = (byte[])params[0];
                Camera.Parameters parameters = mCamera.getParameters();
                int previewHeight = parameters.getPreviewSize().width;
                int previewWidth = parameters.getPreviewSize().height;
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, previewHeight, previewWidth, 0, 0, previewHeight, previewWidth, false);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Reader reader = new QRCodeReader();
                Result result = reader.decode(bitmap, decodeHints);
                decodedText = result.getText();
            }
            catch (NotFoundException e) {
                decodingQR = false;
            }
            catch (ChecksumException e) {
                decodingQR = false;
            }
            catch (FormatException e) {
                decodingQR = false;
            }
            return decodedText;
        }

        @Override
        protected void onPostExecute(String decodedText){
            super.onPostExecute(decodedText);
            if(decodedText != null) {
                decodingQR = false;

                if(lastQRContent.equalsIgnoreCase(decodedText))
                    return;
                lastQRContent = decodedText;
                validatedQR(decodedText);
            }
            else
                new scanAnimation().execute();
        }
    }

    public class scanAnimation extends AsyncTask<Object,Integer,Object>{

        @Override
        protected Object doInBackground(Object[] params) {
            return null;
        }

        @Override
        protected void onPostExecute(Object input){
            super.onPostExecute(input);
            try{
                if(progress){
                    if(tv_scanStatus.getText().toString().length() > 40) {
                        progress = false;
                    }
                    else
                        tv_scanStatus.setText("*"+tv_scanStatus.getText().toString()+"*");
                }
                else{
                    if(tv_scanStatus.getText().toString().length() <= 12){
                        progress = true;
                        tv_scanStatus.setText("Searching QR");
                    }
                    else{
                        tv_scanStatus.setText(tv_scanStatus.getText().toString().substring(1, tv_scanStatus.getText().toString().length()-1));
                    }
                }
            }
            catch (Exception e){}
        }
    }

    public void saveEntry(ParkingInfo entry){
        try{
            long id = mydb.addParkingEntry(entry);
            entry.setDBID(id);
            if(id > 0) {
                adapterSCE.addEntry(entry);
                String lst = "["+gson.toJson(entry, ParkingInfotype).replace(" ", "%20")+"]";
                if(nd.isInternetAvailable()) {
                    String input = "<entry><ServerID>0</ServerID><EntityID>"+entry.getEntityID()+"</EntityID>";
                    input = input + "<ServerDeviceID>"+mydb.getDeviceID()+"</ServerDeviceID>";
                    input = input + "<LocalDBID>"+entry.getDBID()+"</LocalDBID>";
                    input = input + "<LocalDBTime>"+entry.getLocalTime()+"</LocalDBTime>";
                    input = input + "<DeviceID>"+entry.getDeviceID()+"</DeviceID>";
                    input = input + "<VehicleNo>"+entry.getVehicleNo()+"</VehicleNo>";
                    input = input + "<VehicleType>"+entry.getVehicleType()+"</VehicleType>";
                    input = input + "<TCTID>"+entry.getTariffType()+"</TCTID>";
                    input = input + "<AddType>"+entry.getUsage()+"</AddType>";
                    input = input + "<OverRide>"+entry.getOverRide()+"</OverRide>";
                    input = input + "</entry>";
                    new pushToserver().execute(input);
                }
                else
                    Toast.makeText(ActivityParkingScanQR.this, getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
            }
            setFooterTextCounters();
        }
        catch (Exception e){mydb.logAppError(PageName, "sendtoServer", "Exception", e.getMessage());}
    }

    public void validatedQR(String QRContent){
        ParkingInfo entry = new ParkingInfo();
        try{
            String[] content = QRContent.split("\n");
            if (content.length != 3) {
                v.vibrate(vibrateMilli);
                Toast.makeText(ActivityParkingScanQR.this, "Invalid QR", Toast.LENGTH_SHORT).show();
                return;
            }

            String Vno = cc.ValidateVehicleNo(content[1]);
            if(Vno == null) {
                v.vibrate(vibrateMilli);
                Toast.makeText(ActivityParkingScanQR.this, "Invalid vehicle no", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!cc.validateVehicleType(content[2])) {
                v.vibrate(vibrateMilli);
                Toast.makeText(ActivityParkingScanQR.this, "Invalid vehicle type", Toast.LENGTH_SHORT).show();
                return;
            }

            entry.setDeviceID(content[0]);
            entry.setVehicleNo(Vno);
            entry.setVehicleType(Integer.parseInt(content[2]));
            entry.setEntityID(EntityID);
            entry.setUsage(Integer.parseInt(Usage));
            if(entry.getUsage() == 1) {
                entry.setLocalEntryDate(new Models.TimeStamp().getCurrentTimeStamp());
                entry.setLocalTime(new Models.TimeStamp().getCurrentTimeStamp());
            }
            else {
                entry.setLocalExitDate(new Models.TimeStamp().getCurrentTimeStamp());
                entry.setLocalTime(new Models.TimeStamp().getCurrentTimeStamp());
            }
            entry.setTariffType(Integer.parseInt(TariffTypeCode));
            entry.setServerSync(0);
            entry.setOverRide(0);

            if(!VehicleType.equalsIgnoreCase("0") && !content[2].equalsIgnoreCase(VehicleType)){
                v.vibrate(vibrateMilli);
                dpv.setVehicleTypes(entry);
                dpv.show();
            }else{
                v.vibrate(vibrateMilli/4);
                saveEntry(entry);
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "validatedQR", "Exception", e.getMessage());
        }
    }

    public class pushToserver extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            img_sync.setImageResource(R.drawable.ic_syncing);
        }

        @Override
        protected String doInBackground(String[] params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            try {
                response = new WebServiceCall(ActivityParkingScanQR.this).Get("AddParkingVehicleEntry", "?json="+params[0]);
            }
            catch (Exception e){mydb.logAppError(PageName, "pushToserver--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response){
            super.onPostExecute(response);
            try {
                img_sync.setImageResource(R.drawable.ic_sync);
                new PushNotification(ActivityParkingScanQR.this).execute();
                checkServerRecordStatus(response);
            }
            catch (Exception e){mydb.logAppError(PageName, "pushToserver--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public void checkServerRecordStatus(String response){
        try{
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i<jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ParkingInfo row = mydb.getParkingEntryRow(jsonObject.getInt("DBID"));

                if(row.getUsage() == 1){
                    if(!jsonObject.getString("IEntryDate").equalsIgnoreCase("") && jsonObject.getString("DEntryDate").equalsIgnoreCase("")) { //Normal Entry
                        mydb.updateParkingRows(jsonObject);
                        row.setServerSync(1);
                        row.setServerEntryDate(jsonObject.getString("IEntryDate").replace('T',' ').substring(0,19));
                        adapterSCE.refreshList(row);
                    }else if(jsonObject.getString("IEntryDate").equalsIgnoreCase(jsonObject.getString("DEntryDate"))){//Entry already exists
                        //show popup of already existing entry
                        //if cancel, delete data from DB and reset adapter
                        //if override, push to server with over ride info
                        row.setServerEntryDate(jsonObject.getString("IEntryDate").replace('T',' ').substring(0,19));
                        row.setOverRide(1);
                        DialogParkingUsage sdpu = new DialogParkingUsage(this);
                        sdpu.pushRow(row);
                        sdpu.show();
                        v.vibrate(vibrateMilli);
                    }else if(!jsonObject.getString("IEntryDate").equalsIgnoreCase(jsonObject.getString("DEntryDate"))) {//Over entry
                        mydb.updateParkingRows(jsonObject);
                        row.setServerSync(1);
                        row.setServerEntryDate(jsonObject.getString("IEntryDate").replace('T',' ').substring(0,19));
                        adapterSCE.refreshList(row);
                    }
                }else{
                    if(!jsonObject.getString("IExitDate").equalsIgnoreCase("") && jsonObject.getString("DExitDate").equalsIgnoreCase("")) { //Normal Exit
                        mydb.updateParkingRows(jsonObject);
                        row.setServerSync(1);
                        row.setLocalEntryDate(jsonObject.getString("LocalEnterDate").replace('T',' ').substring(0,19));
                        row.setServerEntryDate(jsonObject.getString("IEntryDate").replace('T',' ').substring(0,19));
                        row.setServerExitDate(jsonObject.getString("IExitDate").replace('T',' ').substring(0,19));
                        adapterSCE.refreshList(row);
                    }else if(jsonObject.getString("IEntryDate").equalsIgnoreCase("") && jsonObject.getString("IEntryDate").equalsIgnoreCase(jsonObject.getString("DEntryDate"))){//Exit already exists
                        row.setOverRide(1);
                        DialogParkingUsage sdpu = new DialogParkingUsage(this);
                        sdpu.pushRow(row);
                        sdpu.show();
                        v.vibrate(vibrateMilli);
                    }else if(!jsonObject.getString("IEntryDate").equalsIgnoreCase("") && jsonObject.getString("IExitDate").equalsIgnoreCase(jsonObject.getString("DEntryDate"))) {//Override Exit
                        mydb.updateParkingRows(jsonObject);
                        row.setServerSync(1);
                        row.setLocalEntryDate(jsonObject.getString("LocalEnterDate").replace('T',' ').substring(0,19));
                        row.setServerEntryDate(jsonObject.getString("IEntryDate").replace('T',' ').substring(0,19));
                        row.setServerExitDate(jsonObject.getString("IExitDate").replace('T',' ').substring(0,19));
                        adapterSCE.refreshList(row);
                    }
                }
            }
            setFooterTextCounters();
        }
        catch (Exception e){mydb.logAppError(PageName, "checkServerRecordStatus", "Exception", e.getMessage());}
    }

    public void inflateFooter(){
        try{
            LinearLayout ll_camera = (LinearLayout) vw_footer.findViewById(R.id.ll_camera);
            LinearLayout ll_search = (LinearLayout) vw_footer.findViewById(R.id.ll_search);
            LinearLayout ll_clear = (LinearLayout) vw_footer.findViewById(R.id.ll_clear);
            LinearLayout ll_sync = (LinearLayout) vw_footer.findViewById(R.id.ll_sync);
            LinearLayout ll_context = (LinearLayout) vw_footer.findViewById(R.id.ll_context);
            tv_sync = (TextView) vw_footer.findViewById(R.id.tv_sync);
            tv_clear = (TextView) vw_footer.findViewById(R.id.tv_clear);
            img_sync = (ImageView) vw_footer.findViewById(R.id.img_sync);
            ll_camera.setOnClickListener(this);
            ll_search.setOnClickListener(this);
            ll_clear.setOnClickListener(this);
            ll_sync.setOnClickListener(this);
            ll_context.setOnClickListener(this);

            setFooterTextCounters();
        }
        catch (Exception e){mydb.logAppError(PageName, "inflateFooter", "Exception", e.getMessage());}
    }

    public void setFooterTextCounters(){
        try{
            tv_sync.setText("Sync: "+mydb.getParkingEntryCount(EntityID, "Unsynced"));
            tv_clear.setText("Clear: "+mydb.getParkingEntryCount(EntityID, "All"));
        }
        catch (Exception e){mydb.logAppError(PageName, "setFooterTextValues", "Exception", e.getMessage());}
    }

    public void setParkingContext(){
        try{
            String text = "";
            lastQRContent = "";
            Usage = mydb.getSystemParameter("ParkingUsage");
            VehicleType = mydb.getSystemParameter("ParkingVehicleType");
            TariffTypeCode = mydb.getSystemParameter("ParkingTariffType");

            text = (Usage.equalsIgnoreCase("1"))?"Enter":"Exit";
            text = text+"\n"+((VehicleType.equalsIgnoreCase("0"))?"None":(VehicleType+" Wheeler"));
            switch (TariffTypeCode){
                case "1":
                    text = text+"\nHoliday";
                    break;
                case "2":
                    text = text+"\nNormal";
                    break;
                case "3":
                    text = text+"\nPeaktime";
                    break;
                case "4":
                    text = text+"\nWeekend";
                    break;
                default:
                    text = text+"\nError";
                    break;
            }
            tv_context.setText(text);
        }
        catch (Exception e){mydb.logAppError(PageName, "setParkingContext", "Exception", e.getMessage());}
    }
}