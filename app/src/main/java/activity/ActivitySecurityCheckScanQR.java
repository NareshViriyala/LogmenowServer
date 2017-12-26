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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adapter.AdapterSecurityCheckEntry;
import database.DBHelper;
import gcm.PushNotification;
import shared.CommonClasses;
import shared.Models;
import shared.Models.*;
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
 * Created by nviriyala on 23-08-2016.
 */
public class ActivitySecurityCheckScanQR extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, View.OnLongClickListener{

    private DBHelper mydb;
    private String PageName = "ActivitySecurityCheckScanQR";
    private TextView tv_title;
    private TextView tv_scanStatus;
    private ListView lv_scelst;
    private ImageView img_back;
    private ImageView img_home;
    private ImageView img_more;
    private View vw_footer;
    private TextView tv_sync;
    private TextView tv_clear;
    private ImageView img_sync;
    private CommonClasses cc;
    private NetworkDetector nd;
    private AdapterSecurityCheckEntry adapterSCE;

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
    private int EntityID;

    public Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_securitycheckscanqr);
            mydb = new DBHelper(this);
            cc = new CommonClasses(this);
            nd = new NetworkDetector(this);
            EntityID = Integer.parseInt(mydb.getSystemParameter("EntityID"));

            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);

            tv_scanStatus = (TextView) findViewById(R.id.tv_scanStatus);
            lv_scelst = (ListView) findViewById(R.id.lv_scelst);

            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_home = (ImageView) findViewById(R.id.img_home);
            img_home.setOnClickListener(this);
            img_more = (ImageView) findViewById(R.id.img_more);
            img_more.setOnClickListener(this);

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
                    Intent intent = new Intent(this, ActivitySecurityCheckLog.class);
                    startActivity(intent);
                    break;
                case R.id.ll_clear:
                    mydb.clearSecurityCheckTable(EntityID, false);
                    if(adapterSCE != null)
                        adapterSCE.clearList();
                    setFooterTextCounters();
                    break;
                case R.id.ll_sync:
                    if(nd.isInternetAvailable()){
                        List<SecurityCheckEntry> unsyncedList = mydb.getSecurityCheckEntryRows(EntityID);
                        if(unsyncedList.size() > 0){
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<SecurityCheckEntry>>(){}.getType();
                            String lst = gson.toJson(unsyncedList, type).replace(" ", "%20");
                            String input = "<Entry>";
                            for(SecurityCheckEntry entry : unsyncedList){
                                input = input + "<ServerID>0</ServerID><LocalDBID>"+entry.getDBID()+"</LocalDBID><EntityID>"+entry.getEntityID()+"</EntityID><ServerDeviceID>"+mydb.getDeviceID()+"</ServerDeviceID>";
                                input = input + "<DeviceID>"+entry.getDeviceID()+"</DeviceID><isDeleted>"+entry.getisDeleted()+"</isDeleted>";
                                input = input + ((entry.getName() == null)?"":"<Name>"+entry.getName()+"</Name>");
                                input = input + ((entry.getEmail() == null)?"":"<Email>"+entry.getEmail()+"</Email>");
                                input = input + ((entry.getPhone() == null)?"":"<Phone>"+entry.getPhone()+"</Phone>");
                                input = input + ((entry.getDOB() == null)?"":"<DOB>"+entry.getDOB()+"</DOB>");
                                input = input + ((entry.getAge() == null)?"":"<Age>"+entry.getAge()+"</Age>");
                                input = input + ((entry.getGender() == null)?"":"<Sex>"+entry.getGender()+"</Sex>");
                                input = input + ((entry.getVehicle() == null)?"":"<Vehicle>"+entry.getVehicle()+"</Vehicle>");
                                input = input + ((entry.getVehicleType() == null)?"":"<VehicleType>"+entry.getVehicleType()+"</VehicleType>");
                                input = input + ((entry.getComingFrom() == null)?"":"<ComingFrom>"+entry.getComingFrom()+"</ComingFrom>");
                                input = input + ((entry.getPov() == null)?"":"<Purpose>"+entry.getPov()+"</Purpose>");
                                input = input + ((entry.getVisitingCompany() == null)?"":"<VisitingCompany>"+entry.getVisitingCompany()+"</VisitingCompany>");
                                input = input + ((entry.getContactPerson() == null)?"":"<ContactPerson>"+entry.getContactPerson()+"</ContactPerson>");
                                input = input + ((entry.getBlock() == null)?"":"<Block>"+entry.getBlock()+"</Block>");
                                input = input + ((entry.getFlat() == null)?"":"<Flat>"+entry.getFlat()+"</Flat>");
                                input = input + ((entry.getHomeAddress() == null)?"":"<HomeAdd>"+entry.getHomeAddress()+"</HomeAdd>");
                                input = input + ((entry.getOfficeAddress() == null)?"":"<OfcAdd>"+entry.getOfficeAddress()+"</OfcAdd>");
                            }
                            input = input + "</Entry>";
                            new pushToserver().execute(input);
                        }
                    }
                    else
                        Toast.makeText(ActivitySecurityCheckScanQR.this, getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(ActivitySecurityCheckScanQR.this, "Long Clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        img_back.setVisibility(View.VISIBLE);
        tv_title.setText("Scan QR");
        screenChanging = false;
        camOperation(true);
        //this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
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
                                ActivityCompat.requestPermissions(ActivitySecurityCheckScanQR.this, new String[] {Manifest.permission.CAMERA}, 500);
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
                ActivityCompat.requestPermissions(ActivitySecurityCheckScanQR.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 500);
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
                doOperation(decodedText);
                //v.vibrate(vibrateMilli);
                //decodingQR = false;
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

    public void doOperation(String QRContent){
        try{
            if(lastQRContent.equalsIgnoreCase(QRContent))
                return;
            lastQRContent = QRContent;
            v.vibrate(vibrateMilli/4);
            SecurityCheckEntry entry = validatedQR(QRContent);
            if(entry == null)
                Toast.makeText(ActivitySecurityCheckScanQR.this, "Invalid QR", Toast.LENGTH_SHORT).show();
            else{
                if(adapterSCE == null) {
                    List<SecurityCheckEntry> sce = new ArrayList<>();
                    sce.add(entry);
                    adapterSCE = new AdapterSecurityCheckEntry(this, sce);
                    lv_scelst.setAdapter(adapterSCE);
                }
                else {
                    adapterSCE.addEntry(entry);
                }

                String input = "<Entry><ServerID>0</ServerID><LocalDBID>"+entry.getDBID()+"</LocalDBID><EntityID>"+entry.getEntityID()+"</EntityID><ServerDeviceID>"+mydb.getDeviceID()+"</ServerDeviceID>";
                input = input + "<DeviceID>"+entry.getDeviceID()+"</DeviceID><isDeleted>"+entry.getisDeleted()+"</isDeleted>";
                input = input + ((entry.getName() == null)?"":"<Name>"+entry.getName()+"</Name>");
                input = input + ((entry.getEmail() == null)?"":"<Email>"+entry.getEmail()+"</Email>");
                input = input + ((entry.getPhone() == null)?"":"<Phone>"+entry.getPhone()+"</Phone>");
                input = input + ((entry.getDOB() == null)?"":"<DOB>"+entry.getDOB()+"</DOB>");
                input = input + ((entry.getAge() == null)?"":"<Age>"+entry.getAge()+"</Age>");
                input = input + ((entry.getGender() == null)?"":"<Sex>"+entry.getGender()+"</Sex>");
                input = input + ((entry.getVehicle() == null)?"":"<Vehicle>"+entry.getVehicle()+"</Vehicle>");
                input = input + ((entry.getVehicleType() == null)?"":"<VehicleType>"+entry.getVehicleType()+"</VehicleType>");
                input = input + ((entry.getComingFrom() == null)?"":"<ComingFrom>"+entry.getComingFrom()+"</ComingFrom>");
                input = input + ((entry.getPov() == null)?"":"<Purpose>"+entry.getPov()+"</Purpose>");
                input = input + ((entry.getVisitingCompany() == null)?"":"<VisitingCompany>"+entry.getVisitingCompany()+"</VisitingCompany>");
                input = input + ((entry.getContactPerson() == null)?"":"<ContactPerson>"+entry.getContactPerson()+"</ContactPerson>");
                input = input + ((entry.getBlock() == null)?"":"<Block>"+entry.getBlock()+"</Block>");
                input = input + ((entry.getFlat() == null)?"":"<Flat>"+entry.getFlat()+"</Flat>");
                input = input + ((entry.getHomeAddress() == null)?"":"<HomeAdd>"+entry.getHomeAddress()+"</HomeAdd>");
                input = input + ((entry.getOfficeAddress() == null)?"":"<OfcAdd>"+entry.getOfficeAddress()+"</OfcAdd>");
                input = input + "</Entry>";
                if(nd.isInternetAvailable())
                    new pushToserver().execute(input);
                else
                    Toast.makeText(ActivitySecurityCheckScanQR.this, getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
            }
            //tv_scanInfo.setText(QRContent);
            //Toast.makeText(ActivityScanQR.this, QRContent, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){mydb.logAppError(PageName, "doOperation", "Exception", e.getMessage());}
    }

    public SecurityCheckEntry validatedQR(String QRContent){
        boolean valid=true;
        SecurityCheckEntry entry = new SecurityCheckEntry();
        try{
            String[] content = QRContent.split("\n");
            for (String item:content) {
                if(cc.isInteger(item.substring(0, item.indexOf('.')))){
                    int num = Integer.parseInt(item.substring(0, item.indexOf('.')));
                    switch (num){
                        case 0:
                            entry.setDeviceID(item.substring(item.indexOf('.')+1));
                            break;
                        case 1:
                            entry.setName(item.substring(item.indexOf('.')+1));
                            break;
                        case 2:
                            entry.setEmail(item.substring(item.indexOf('.')+1));
                            break;
                        case 3:
                            entry.setPhone(item.substring(item.indexOf('.')+1));
                            break;
                        case 4:
                            entry.setDOB(item.substring(item.indexOf('.')+1));
                            break;
                        case 5:
                            entry.setAge(item.substring(item.indexOf('.')+1));
                            break;
                        case 6:
                            entry.setGender(item.substring(item.indexOf('.')+1));
                            break;
                        case 7:
                            entry.setVehicle(item.substring(item.indexOf('.')+1));
                            break;
                        case 8:
                            entry.setVehicleType(item.substring(item.indexOf('.')+1));
                            break;
                        case 9:
                            entry.setComingFrom(item.substring(item.indexOf('.')+1));
                            break;
                        case 10:
                            entry.setPov(item.substring(item.indexOf('.')+1));
                            break;
                        case 11:
                            entry.setVisitingCompany(item.substring(item.indexOf('.')+1));
                            break;
                        case 12:
                            entry.setContactPerson(item.substring(item.indexOf('.')+1));
                            break;
                        case 13:
                            entry.setBlock(item.substring(item.indexOf('.')+1));
                            break;
                        case 14:
                            entry.setFlat(item.substring(item.indexOf('.')+1));
                            break;
                        case 15:
                            entry.setHomeAddress(item.substring(item.indexOf('.')+1).replace("||","\n"));
                            break;
                        case 16:
                            entry.setOfficeAddress(item.substring(item.indexOf('.')+1).replace("||","\n"));
                            break;
                    }
                }
                else {
                    valid = false;
                    break;
                }
            }
            if(valid) {
                //entry.setsynced(false);
                //entry.setisDeleted(false);
                entry.setEnterTime(new TimeStamp().getCurrentTimeStamp());
                entry.setEntityID(EntityID);
                long id = mydb.addSecurityCheckEntry(entry);
                entry.setDBID(id);
                if(id <= 0)
                    valid = false;
                setFooterTextCounters();
            }
        }
        catch (Exception e){
            //mydb.logAppError(PageName, "validatedQR", "Exception", e.getMessage());
            valid = false;
        }
        return valid?entry:null;
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
                response = new WebServiceCall(ActivitySecurityCheckScanQR.this).Get("AddSecurityCheckEntry", "?json="+params[0]);
            }
            catch (Exception e){mydb.logAppError(PageName, "pushToserver--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response){
            super.onPostExecute(response);
            try {
                JSONArray jsonArray = new JSONArray(response);
                for(int i = 0; i<jsonArray.length(); i++) {
                    mydb.updateSecurityCheck(jsonArray.getJSONObject(i));
                    if(adapterSCE != null)
                        adapterSCE.refreshList(jsonArray.getJSONObject(i).getInt("DBID"), true);
                }
                setFooterTextCounters();
                new PushNotification(ActivitySecurityCheckScanQR.this).execute();
            }
            catch (Exception e){mydb.logAppError(PageName, "pushToserver--onPostExecute", "Exception", e.getMessage());}
            finally {img_sync.setImageResource(R.drawable.ic_sync);}
        }
    }

    public void inflateFooter(){
        try{
            LinearLayout ll_camera = (LinearLayout) vw_footer.findViewById(R.id.ll_camera);
            LinearLayout ll_search = (LinearLayout) vw_footer.findViewById(R.id.ll_search);
            LinearLayout ll_clear = (LinearLayout) vw_footer.findViewById(R.id.ll_clear);
            LinearLayout ll_sync = (LinearLayout) vw_footer.findViewById(R.id.ll_sync);
            tv_sync = (TextView) vw_footer.findViewById(R.id.tv_sync);
            tv_clear = (TextView) vw_footer.findViewById(R.id.tv_clear);
            img_sync = (ImageView) vw_footer.findViewById(R.id.img_sync);
            ll_camera.setOnClickListener(this);
            ll_search.setOnClickListener(this);
            ll_clear.setOnClickListener(this);
            ll_sync.setOnClickListener(this);

            ll_clear.setOnLongClickListener(this);

            setFooterTextCounters();
        }
        catch (Exception e){mydb.logAppError(PageName, "inflateFooter", "Exception", e.getMessage());}
    }

    public void setFooterTextCounters(){
        try{
            tv_sync.setText("Sync: "+mydb.getSecurityCheckEntryCount(EntityID, "Unsynced"));
            tv_clear.setText("Clear: "+mydb.getSecurityCheckEntryCount(EntityID, "All"));
        }
        catch (Exception e){mydb.logAppError(PageName, "setFooterTextValues", "Exception", e.getMessage());}
    }
}