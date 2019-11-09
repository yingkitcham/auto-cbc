package finalyearproject.autocbc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class ImageViewActivity extends AppCompatActivity {
    private String fileName;
    public MyDBHandler db;
    private ViewList imgInfo;
    private String fldrName;
    private static final String mainDir = "Auto-CBC";
    ImageView imageView;
    private String dirFileOri;
    private String dirFileRes;
    private int countWBC = 0;
    private int countRBC = 0;
    //private int countPLT = 0;
    Bitmap processBitmap;
    double elapsedSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        // > 23API permission issue, must have code segment below.
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // call string from another activity
        Bundle bundle = getIntent().getExtras();
        fileName = bundle.getString("_fileName");

        // call SQLite Db
        dbCall();

        // set title
        setTitle();

        // Display Images
        showImages();

        // Text View Display
        showResult();

    }

    public void dbCall(){
        db = new MyDBHandler(this);
        imgInfo = db.getImgView(fileName);
    }

    public void setTitle(){
        TextView bloodsampleview_name = (TextView)findViewById(R.id.fileName);
        bloodsampleview_name.setText("Image View of " + imgInfo.get_viewName());
    }

    public void showImages(){
        fldrName = imgInfo.get_sampleName();
        dirFileOri = Environment.getExternalStorageDirectory()+ File.separator + mainDir
                + File.separator + fldrName + File.separator + fileName + ".jpg";
        dirFileRes = Environment.getExternalStorageDirectory()+ File.separator + mainDir
                + File.separator + fldrName + File.separator + fileName + "_R.jpg";
        File imgFile = new File(dirFileOri);
        File imgFileRes = new File(dirFileRes);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            int nh = (int) (myBitmap.getHeight() * (512.0 / myBitmap.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 512, nh, true);
            PhotoView myImage = (PhotoView) findViewById(R.id.imageView);
            myImage.setImageBitmap(scaled);
        } else {
            Toast.makeText(this,"Unable to obtain image at directory: " + dirFileOri ,Toast.LENGTH_SHORT).show();
        }
        if(imgFileRes.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFileRes.getAbsolutePath());
            PhotoView myImage = (PhotoView) findViewById(R.id.imageView2);
            myImage.setImageBitmap(myBitmap);
        }
        else {
            ImageView myImage = (ImageView) findViewById(R.id.imageView2);
            myImage.setImageResource(R.drawable.defaultdis);
        }
    }

    public void showResult(){
        EditText rbcCountDis = (EditText)findViewById(R.id.rbcCount);
        rbcCountDis.setText(String.valueOf(imgInfo.get_rbcCount()));
        rbcCountDis.setEnabled(false);
        EditText wbcCountDis = (EditText)findViewById(R.id.wbcCount);
        wbcCountDis.setText(String.valueOf(imgInfo.get_wbcCount()));
        wbcCountDis.setEnabled(false);
//        EditText pltCountDis = (EditText)findViewById(R.id.pltCount);
//        pltCountDis.setText(String.valueOf(imgInfo.get_pltCount()));
//        pltCountDis.setEnabled(false);
        EditText pltCountDis = (EditText)findViewById(R.id.timeElapsed);
        pltCountDis.setText(String.valueOf(imgInfo.get_time_elapsed())+" seconds");
        pltCountDis.setEnabled(false);
        EditText dateCreatedDis = (EditText)findViewById(R.id.dateCreated);
        dateCreatedDis.setText(String.valueOf(imgInfo.get_date_created()));
        dateCreatedDis.setEnabled(false);
        EditText dateAnalysedDis = (EditText)findViewById(R.id.dateAnalysed);
        if (imgInfo.get_date_analysed() !=null)
            dateAnalysedDis.setText(String.valueOf(imgInfo.get_date_analysed()));
        else
            dateAnalysedDis.setText("-");
        dateAnalysedDis.setEnabled(false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem analyseButton = menu.findItem(R.id.analyse);
        if (imgInfo.get_rbcCount() != 0) {
            analyseButton.setVisible(false);
        } else {
            analyseButton.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.imgviewmenulist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.analyse:
                analyseImage();
                return true;
            case R.id.about:
                aboutUs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void analyseImage(){
        File imgFile = new File(dirFileOri);
        if(imgFile.exists()){
            processBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            analysedAlgo();
        } else {
            Toast.makeText(this,"Unable to obtain image at directory: " + dirFileOri ,Toast.LENGTH_SHORT).show();
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void analysedAlgo(){
        final ProgressDialog progress = ProgressDialog.show(this, "In Progress",
                "The process for segmentation and counting is in progress. Please Wait.", true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                long tStart = System.currentTimeMillis();
                countWBC = 0;
                countRBC = 0;
                /**
                 *  Called the image from the panel
                 */
                Mat imgOri = new Mat();
                Utils.bitmapToMat(processBitmap, imgOri);
                Imgproc.cvtColor(imgOri,imgOri, Imgproc.COLOR_RGBA2RGB);

                /**
                 * Grayscale Image
                 */
                Mat imgGray = new Mat();
                Imgproc.cvtColor(imgOri,imgGray, Imgproc.COLOR_RGB2GRAY);

                /**
                 * Binary Image
                 */
                Mat imgBin = new Mat();
                Imgproc.threshold(imgGray, imgBin ,0,255, Imgproc.THRESH_OTSU);

                /**
                 * Creating
                 */
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                List<Double> areasList = new ArrayList<Double>();
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                Imgproc.findContours(imgBin, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
                Mat drawing = new Mat(imgBin.size(), CvType.CV_8UC3);
                Mat hierarchy = new Mat();
                Rect rect = new Rect();
                int xBound =0;
                int yBound =0;
                int xWidth =0;
                int yHeight =0;
                int radius = 0;
                for (int idx = 0; idx < contours.size(); idx++) {
                    Mat contour = contours.get(idx);
                    double contourarea = Imgproc.contourArea(contour);
                    areasList.add(contourarea);
                    MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(idx).toArray());
                    double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                    Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
                    MatOfPoint points = new MatOfPoint(approxCurve.toArray());
                    rect = Imgproc.boundingRect(points);
                    if (contourarea > 10000) {
                        /**
                         * Contour Drawing + Bounding Box
                         */
                        xBound = rect.x;
                        yBound = rect.y;
                        xWidth = rect.width;
                        yHeight = rect.height;
                        Imgproc.drawContours(imgOri, contours, idx, new Scalar(0, 0, 255), 2, 8, hierarchy, 0, new Point());
                        Imgproc.rectangle(imgOri, new Point(rect.x, rect.y),
                                new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0), 3);
                        if (rect.height > rect.width)
                            radius = rect.width/2;
                        else
                            radius = rect.height/2;
                    }
                }
                radius = (int)(radius*0.70711);
                int xPos = xBound+(xWidth/2)-radius;
                int yPos = yBound+(yHeight/2)-radius;
                Rect reg_info = new Rect(xPos,yPos,radius*2,radius*2);
                Mat region = imgOri.submat(reg_info);

                /**
                 * Transform RGB to L*a*b*
                 */
                Mat imgLab = new Mat();
                Imgproc.cvtColor(region, imgLab, Imgproc.COLOR_RGB2Lab);
                /**
                 *  Split L*a*b* to individual channels
                 */
                List<Mat> lLab = new ArrayList<Mat>(3);
                Core.split(imgLab, lLab);
                Mat mLabL = lLab.get(0);
                Mat mLaba = lLab.get(1);
                Mat mLabb = lLab.get(2);

                /**
                 * Image Otsu from Labb channel image
                 */
                Mat imgWBC = new Mat();
                Imgproc.threshold(mLabb, imgWBC, 110, 255, Imgproc.THRESH_BINARY_INV);

                Mat imgRBCmask = imgWBC.clone();
                Core.bitwise_not(imgWBC,imgRBCmask);

                Mat imgRBC = new Mat();
                Imgproc.threshold(mLaba, imgRBC, 0, 255, Imgproc.THRESH_OTSU);

                Core.bitwise_and(imgRBC,imgRBCmask,imgRBC);

                Mat imgRBCnew = imgRBC.clone();

                /**
                 * Contour Finding for WBC
                 */
                List<MatOfPoint> contoursWBC = new ArrayList<MatOfPoint>();
                MatOfPoint2f approxCurveWBC = new MatOfPoint2f();
                Imgproc.findContours(imgWBC, contoursWBC, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
                Mat drawingWBC = new Mat(imgWBC.size(), CvType.CV_8UC3);
                Mat hierarchyWBC = new Mat();
                for (int idx = 0; idx < contoursWBC.size(); idx++) {
                    Mat contour = contoursWBC.get(idx);
                    double contourarea = Imgproc.contourArea(contour);
                    MatOfPoint2f contour2f = new MatOfPoint2f(contoursWBC.get(idx).toArray());
                    double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                    Imgproc.approxPolyDP(contour2f, approxCurveWBC, approxDistance, true);
                    MatOfPoint points = new MatOfPoint(approxCurveWBC.toArray());
                    Rect rectWBC = Imgproc.boundingRect(points);
                    if (contourarea >2000) {
                        /**
                         * Contour Drawing + Bounding Box
                         */
                        countWBC++;
                        Imgproc.drawContours(region, contoursWBC, idx, new Scalar(0, 0, 255), 2, 8, hierarchyWBC, 0, new Point());
                        Imgproc.putText(region,String.valueOf(countWBC), new Point(rectWBC.x+rectWBC.width/2,rectWBC.y+rectWBC.height/2),
                                Core.FONT_HERSHEY_SIMPLEX,1.5,new Scalar(255, 0, 0),5);
                    }
                }

                /**
                 * Contour Filling for RBC (which the bounded size lesser than 70x70 pixels)
                 */
                List<MatOfPoint> contoursRBC = new ArrayList<MatOfPoint>();
                List<Double> areasListRBC = new ArrayList<Double>();
                MatOfPoint2f approxCurveRBC = new MatOfPoint2f();
                Imgproc.findContours(imgRBC, contoursRBC, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
                Mat drawingRBC = new Mat(imgRBC.size(), CvType.CV_8U);
                Mat hierarchyRBC = new Mat();
                for (int idx = 0; idx < contoursRBC.size(); idx++) {
                    Mat contourRBC = contoursRBC.get(idx);
                    double contourareaRBC = Imgproc.contourArea(contourRBC);
                    areasListRBC.add(contourareaRBC);
                    MatOfPoint2f contour2f = new MatOfPoint2f(contoursRBC.get(idx).toArray());
                    double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                    Imgproc.approxPolyDP(contour2f, approxCurveRBC, approxDistance, true);
                    MatOfPoint points = new MatOfPoint(approxCurveRBC.toArray());
                    Rect rectRBC = Imgproc.boundingRect(points);
                    if (rectRBC.height <= 70 && rectRBC.width <= 70 ){
                        Imgproc.drawContours(imgRBCnew,contoursRBC,idx, new Scalar(255),-1);
                    }
                    if  (rectRBC.height > 70 && rectRBC.width > 70 && rectRBC.height < 150 && rectRBC.width < 150 ) {
                        countRBC++;
                        Imgproc.drawContours(region, contoursRBC, idx, new Scalar(0, 255, 0), 6, 8, hierarchyRBC, 0, new Point());
                        Imgproc.drawContours(imgRBCnew,contoursRBC,idx, new Scalar(0),-1);
                        Imgproc.putText(region,String.valueOf(countRBC), new Point(rectRBC.x+rectRBC.width/2,rectRBC.y+rectRBC.height/2),
                                Core.FONT_HERSHEY_SIMPLEX,1.5,new Scalar(0, 255, 255),5);
                    }
                }

                Mat imgRBCnewCircle = imgRBCnew.clone();

                List<MatOfPoint> contoursRBCdTnew = new ArrayList<MatOfPoint>();
                List<Double> areasListRBCdTnew = new ArrayList<Double>();
                MatOfPoint2f approxCurveRBCdTnew = new MatOfPoint2f();
                Imgproc.findContours(imgRBCnew, contoursRBCdTnew, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
                Mat hierarchyRBCdTnew = new Mat();
                for (int idx = 0; idx < contoursRBCdTnew.size(); idx++) {
                    Mat contourRBC = contoursRBCdTnew.get(idx);
                    double contourareaRBC = Imgproc.contourArea(contourRBC);
                    areasListRBCdTnew.add(contourareaRBC);
                    MatOfPoint2f contour2f = new MatOfPoint2f(contoursRBCdTnew.get(idx).toArray());
                    double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                    Imgproc.approxPolyDP(contour2f, approxCurveRBCdTnew, approxDistance, true);
                    MatOfPoint points = new MatOfPoint(approxCurveRBCdTnew.toArray());
                    Rect rectRBC = Imgproc.boundingRect(points);
                    if  ((rectRBC.width >= 130) || (rectRBC.height >= 130)) {
                        Imgproc.rectangle(region, new Point(rectRBC.x, rectRBC.y),
                                new Point(rectRBC.x + rectRBC.width, rectRBC.y + rectRBC.height), new Scalar(255, 0, 0), 3);
                        Mat cropped = imgRBCnewCircle.submat(rectRBC);
                        Mat circles = new Mat();
                        /**
                         * Hough Cirle Transform
                         *
                         */
                        double minDist = 75;
                        double cannyHighThreshold = 50;
                        double accumlatorThreshold = 5.5;
                        Imgproc.HoughCircles(cropped, circles, Imgproc.CV_HOUGH_GRADIENT, 1, minDist, cannyHighThreshold, accumlatorThreshold, 50, 90);
                        for (int count = 0; count < circles.cols(); count++) {
                            countRBC++;
                            double[] circleCoordinates = circles.get(0, count);
                            int xI = (int) circleCoordinates[0];
                            int yI = (int) circleCoordinates[1];
                            Point center = new Point(xI, yI);
                            int radiusRBC = (int) circleCoordinates[2];
                            Imgproc.circle(region.submat(rectRBC), center, radiusRBC, new Scalar(0, 255, 0), 3);
                            Imgproc.circle(region.submat(rectRBC), center, 10, new Scalar(0, 255, 255), -1);
                            Imgproc.putText(region.submat(rectRBC), String.valueOf(countRBC), new Point(xI - 30, yI - 30),
                                    Core.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 255, 255), 6);
                        }
                    }
                }
                long tEnd = System.currentTimeMillis();
                long tDelta = tEnd - tStart;
                elapsedSeconds = tDelta/1000.0 ;
                /**
                 * Transform Mat into Bitmap
                 */
                try {
                    processBitmap = Bitmap.createBitmap(region.cols(), region.rows(), Bitmap.Config.RGB_565);
                    Utils.matToBitmap(region,processBitmap);
                } catch (CvException e) {
                    Log.d(TAG, e.getMessage());
                }
                /**
                 * ImageView Result
                 */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        invalidateOptionsMenu();
                        PhotoView myImage = (PhotoView) findViewById(R.id.imageView2);
                        myImage.setImageBitmap(processBitmap);
                        db.updateResult(new ViewList(fileName,fldrName,countRBC,countWBC,String.valueOf(elapsedSeconds),getDate()));
                        dbCall();
                        showResult();
                        try{
                            FileOutputStream fOut = new FileOutputStream(dirFileRes);
                            processBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                            try{
                                fOut.flush();
                                fOut.close();
                            } catch (IOException E){
                                E.printStackTrace();
                            }
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }

                    }
                });
            }
        }).start();
    }

    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void aboutUs(){
        Intent intent = new Intent(ImageViewActivity.this,About.class);
        startActivity(intent);
    }
}
