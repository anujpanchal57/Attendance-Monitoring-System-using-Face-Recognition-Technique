/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FaceRegognizer;

import Attendence.StudentsEntry;
import Database.DatabaseConnection;
import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.PointerPointer;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import static com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_legacy.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 *
 * @author test
 */
public class FaceRecognizer1 {

    int numberOfTrainingFaces = 30;

    private static final Logger LOGGER = Logger.getLogger(FaceRecognizer1.class.getName());
    private int nTrainFaces = 0;
    /**
     * the training face image array
     */
    IplImage[] trainingFaceImgArr;
    /**
     * the test face image array
     */
    IplImage[] FaceImgArr;
    /**
     * the person number array
     */
    IplImage[] testFaceImgArr;
    CvMat personNumTruthMat;
    /**
     * the number of persons
     */
    int nPersons;
    /**
     * the person names
     */
    final List<String> personNames = new ArrayList<String>();
    /**
     * the number of eigenvalues
     */
    int nEigens = 0;
    /**
     * eigenvectors
     */
    IplImage[] eigenVectArr;
    /**
     * eigenvalues
     */
    CvMat eigenValMat;
    /**
     * the average image
     */
    IplImage pAvgTrainImg;
    /**
     * the projected training faces
     */
    CvMat projectedTrainFaceMat;
    CvMat trainPersonNumMat;
    //Cascade File Name
    String faceCascadeFilename = "./HarrClassiifator/haarcascade_frontalface_alt2.xml";
    //face width faceHeight, faceWidth
    int faceWidth = 120;	// Default dimensions for faces in the face recognition database. Added by Shervin.
    int faceHeight = 90;
    //===========
    FrameGrabber grabber;
    CvMemStorage storage;
    public static CanvasFrame input;
    boolean stopflag = false;
    char actionChar = '0';
    char action = '0';

    waitScreen waitscreen;

    public boolean isstart = false;

    public FaceRecognizer1() {
        try {
            storage = cvCreateMemStorage(0);
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("static-access")
    public void recognizeFromCam1() throws InterruptedException {
        try {
            int i;
            waitscreen = new waitScreen(null, false);
            CvHaarClassifierCascade faceCascade;
            String cstr;
            boolean saveNextFaces = false;
            String newPersonName = "";
            int newPersonFaces;
            
            final FrameGrabber grabber = new OpenCVFrameGrabber(0);
            grabber.setImageWidth(210);
            grabber.setImageHeight(210);
            grabber.start();

            saveNextFaces = false;
            newPersonFaces = 0;

            if (loadTrainingData1() == 1) {
                faceWidth = pAvgTrainImg.width();
                faceHeight = pAvgTrainImg.height();
            } else {
            }
            final CanvasFrame input = new CanvasFrame("Face Training");
            input.setLayout(new FlowLayout());
            JButton save = new JButton("SAVE");
            JButton refresh = new JButton("CANCEL");
            MyOwnListener listener = new MyOwnListener(this);
            save.addActionListener(listener);
            input.add(save);
            input.add(refresh);
            refresh.setEnabled(false);
            input.setLocation(277, 165);
            input.setAlwaysOnTop(true);
            input.setDefaultCloseOperation(CanvasFrame.DO_NOTHING_ON_CLOSE);

            refresh.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!isstart) {
                        isstart = true;
                        input.dispose();
                        waitscreen.setVisible(false);
                        try {
                            grabber.stop();
                            JOptionPane.showMessageDialog(null, "User will not Register" + "\n" + "Try again");
                        } catch (FrameGrabber.Exception ex) {
                            Logger.getLogger(FaceRecognizer1.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            faceCascade = new CvHaarClassifierCascade(cvLoad(faceCascadeFilename));

            while (!isstart) {
                IplImage camImg;
                IplImage greyImg;
                IplImage faceImg;
                IplImage sizedImg;
                IplImage equalizedImg;
                IplImage processedFaceImg;
                CvRect faceRect;
                IplImage shownImg;

                char ch = actionChar;
                if (ch == KeyEvent.VK_ESCAPE) {
                    break;
                }
                switch (ch) {
                    case 'n':
                        newPersonName = StudentsEntry.NAME+"&"+StudentsEntry.STD;
                        if (newPersonName.equals("")) {
                            setActionChar('0');
                            continue;
                        }
                        newPersonName = modifyName(newPersonName);
                        if (newPersonName.length() > 0) {
                            System.out.println("Collecting all images until you hit 't', to start Training the images as " + newPersonName);
                            waitscreen.setVisible(true);
                            newPersonFaces = 0;	// restart training a new person
                            saveNextFaces = true;
                            setActionChar('0');

                            //===================== Disable Buttons ================
                            save.setEnabled(false);
                            refresh.setEnabled(false);

                        } else {
                            System.out.println("Did not get a valid name from you, so will ignore it. Hit 'n' to retry.\n");
                        }

                        break;

                    case 't':
                        //Strat training
                        refresh.setEnabled(true);
                        saveNextFaces = false;
                        System.out.println("Storing the training data for new person " + newPersonName);
                        for (i = 0; i < newPersonFaces; i++) {
                            cstr = "data/" + (nPersons + 1) + "_" + newPersonName + "_" + (i + 1) + ".jpg";
                            String svData = (nPersons + 1) + " " + cstr + " " + newPersonName + "\n";
                            saveFile(svData);
                        }
                        saveNextFaces = false;
                        newPersonFaces = 0;

                        if (!(trainPersonNumMat == null)) {
                            cvFree(trainPersonNumMat);// Free the previous data before getting new data
                        }
                        trainPersonNumMat = retrainOnline();
                        setActionChar('0');
                        waitscreen.setVisible(false);

                        DatabaseConnection dbcon = new DatabaseConnection();
                        dbcon.dbconnection();

                        String studentId = StudentsEntry.ID;
                        String student_name = StudentsEntry.NAME;
                        String studentStd = StudentsEntry.STD;
                        String phoneNumber = StudentsEntry.PHNO;
                        String email = StudentsEntry.EMAIL;

                        String query = "INSERT INTO student VALUES(" + studentId + ",'" + student_name + "','" + studentStd + "','" + phoneNumber + "','"+email+"')";
                        dbcon.getUpdate(query);
                        isstart = true;
                        continue;
                }
                // Get the camera frame
                try {
                    camImg = grabber.grab();
                } catch (Exception e) {
                    continue;
                }
                // Make sure the image is greyscale, since the Eigenfaces is only done on greyscale image.
                greyImg = convertImageToGreyscale(camImg);

                // Perform face detection on the input image, using the given Haar cascade classifier.  
                faceRect = detectFaceInImage(greyImg, faceCascade);
                // Make sure a valid face was detected.
                if (faceRect.width() > 0) {
                    faceImg = cropImage(greyImg, faceRect);
                    // Make sure the image is the same dimensions as the training images.
                    sizedImg = resizeImage(faceImg, faceWidth, faceHeight);
//                  Give the image a standard brightness and contrast, in case it was too dark or low contrast.
                    equalizedImg = IplImage.create(new CvSize(sizedImg.width(), sizedImg.height()), 8, 1);	// Create an empty greyscale image
                    cvEqualizeHist(sizedImg, equalizedImg);
                    processedFaceImg = equalizedImg;

                    if (saveNextFaces) {
                        if (newPersonFaces < (numberOfTrainingFaces)) {
                            cstr = "data/" + (nPersons + 1) + "_" + newPersonName + "_" + (newPersonFaces + 1) + ".jpg";
                            cvSaveImage(cstr, processedFaceImg);
                            newPersonFaces++;
                        } else {
                            saveNextFaces = false;
                            setActionChar('t');
                        }
                    }
                }
                // Show the data on the screen.
                shownImg = cvCloneImage(camImg);

                if (faceRect.width() > 60)// Check if a face was detected.
                {
                    // Show the detected face region.
                    cvRectangle(shownImg, cvPoint(faceRect.x(), faceRect.y()), cvPoint(faceRect.x() + faceRect.width() - 1, faceRect.y() + faceRect.height() - 1), CV_RGB(0, 255, 0), 1, 8, 0);
                }
                // Display the image.
                input.showImage(shownImg);
            }
            cvReleaseMemStorage(storage);
            grabber.stop();
            cvReleaseHaarClassifierCascade(faceCascade);
            input.dispose();
            JOptionPane.showMessageDialog(null, "Student is successfully Register");
            new StudentsEntry().setVisible(true);
        } catch (FrameGrabber.Exception e) {
        }
    }

    public IplImage resizeImage(IplImage origImg, int newWidth, int newHeight) {
        try {
            IplImage outImage;
            int origWidth = 0;
            int origHeight = 0;
            if (!(origImg == null)) {
                origWidth = origImg.width();
                origHeight = origImg.height();
            }
            if (newWidth <= 0 || newHeight <= 0 || origWidth <= 0 || origHeight <= 0) {
                return origImg;
            }
            // Scale the image to the new dimensions, even if the aspect ratio will be changed.
            outImage = IplImage.create(cvSize(newWidth, newHeight), origImg.depth(), origImg.nChannels());
            if (newWidth > origImg.width() && newHeight > origImg.height()) {
                // Make the image larger
                cvResetImageROI(origImg);
                cvResize(origImg, outImage, CV_INTER_LINEAR);	// CV_INTER_CUBIC or CV_INTER_LINEAR is good for enlarging
            } else {
                // Make the image smaller
                cvResetImageROI(origImg);
                cvResize(origImg, outImage, CV_INTER_AREA);	// CV_INTER_AREA is good for shrinking / decimation, but bad at enlarging.
            }
            return outImage;
        } catch (Exception e) {
        }
        return null;
    }

    //=====================================================================================
    public IplImage cropImage(IplImage img, CvRect region) {
        try {
            IplImage imageTmp;
            IplImage imageRGB;

            CvSize size = new CvSize(img.width(), img.height());

            if (img.depth() != IPL_DEPTH_8U) {
                return img;
            }

            // First create a new (color or greyscale) IPL Image and copy contents of img into it.
            imageTmp = IplImage.create(size, IPL_DEPTH_8U, img.nChannels());
            cvCopy(img, imageTmp);
            // Create a new image of the detected region
            // Set region of interest to that surrounding the face
            cvSetImageROI(imageTmp, region);
            // Copy region of interest (i.e. face) into a new iplImage (imageRGB) and return it
            size = size.width(region.width());
            size = size.height(region.height());
            imageRGB = IplImage.create(size, IPL_DEPTH_8U, img.nChannels());
            cvCopy(imageTmp, imageRGB);
            return imageRGB;
        } catch (Exception e) {
        }
        return null;
    }

    public CvRect detectFaceInImage(IplImage inputImg, CvHaarClassifierCascade cascade) {
        try {
            IplImage detectImg;
            IplImage greyImg = null;
            CvRect rc;
            double t;
            CvSeq rects;
            int i;
            if (storage == null) {
                storage = cvCreateMemStorage(0);
            }
            cvClearMemStorage(storage);

            detectImg = inputImg;
            if (inputImg.nChannels() > 1) {
                greyImg = IplImage.create(cvGetSize(inputImg), IPL_DEPTH_8U, 1);
                cvCvtColor(inputImg, greyImg, CV_BGR2GRAY);
                detectImg = greyImg;	// Use the greyscale version as the input.
            }
            // Detect all the faces.
            rects = cvHaarDetectObjects(detectImg, cascade, storage, 1.1, 1, 0);

            if (rects.total() > 0) {
                rc = new CvRect(cvGetSeqElem(rects, 0));
            } else {
                rc = new CvRect(-1, -1, -1, -1);
            }
            if (!(greyImg == null)) {
                cvReleaseImage(greyImg);
            }
            return rc;
        } catch (Exception e) {
        }
        return null;
    }

    //======================================================================================
    public IplImage convertImageToGreyscale(IplImage imageSrc) {
        try {
            IplImage imageGrey;
            // Either convert the image to greyscale, or make a copy of the existing greyscale image.
            // This is to make sure that the user can always call cvReleaseImage() on the output, whether it was greyscale or not.
            if (imageSrc.nChannels() == 3) {
                imageGrey = cvCreateImage(cvGetSize(imageSrc), IPL_DEPTH_8U, 1);
                cvCvtColor(imageSrc, imageGrey, CV_BGR2GRAY);
            } else {
                imageGrey = cvCloneImage(imageSrc);
            }
            return imageGrey;
        } catch (Exception e) {
        }
        return null;
    }
    //==================================================================================================================

    public CvMat retrainOnline() {
        try {
            CvMat trainPersonNumMat;
            int i = 0;
            if (FaceImgArr != null) {
                if (FaceImgArr.length > 0) {
                    for (i = 0; i < nTrainFaces; i++) {
                        if (!FaceImgArr[i].isNull()) {
                            cvReleaseImage(FaceImgArr[i]);
                        }
                    }
                }
            }
            //      cvFree(FaceImgArr); // array of face images

            if (personNumTruthMat != null) {
                cvFree(personNumTruthMat);
            } // array of person numbers
            personNames.clear();

            nPersons = 0; // the number of people in the training set. .
            nTrainFaces = 0; // the number of training images
            nEigens = 0; // the number of eigenvalues
            if (pAvgTrainImg != null) {
                cvReleaseImage(pAvgTrainImg); // the average image
            }
            for (i = 0; i < nTrainFaces; i++) {
                if (!eigenVectArr[i].isNull()) {
                    cvReleaseImage(eigenVectArr[i]);
                }
            }

            if (eigenValMat != null) {
                cvFree(eigenValMat);
            }
            if (projectedTrainFaceMat != null)// eigenvalues
            {
                cvFree(projectedTrainFaceMat); // projected training faces
            }
            // Retrain from the data in the files
            System.out.println("Retraining with the new person ...\n");
            String path = new File(".").getCanonicalPath();

            learn(path + "/train.txt");

            System.out.println("Done retraining.\n");
        } catch (IOException ex) {
            Logger.getLogger(FaceRecognizer1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    //==================================================================================================================

    private int loadTrainingData1() {
        LOGGER.info("loading training data");
        trainPersonNumMat = null; // the person numbers during training
        CvFileStorage fileStorage;
        int i;

        // create a file-storage interface
        fileStorage = cvOpenFileStorage(
                "data/facedata.xml", // filename
                null, // memstorage
                CV_STORAGE_READ, // flags
                null); // encoding
        if (fileStorage == null) {
            LOGGER.severe("Can't open training database file 'data/facedata.xml'.");
            return 0;
        }

        // Load the person names.
        personNames.clear();        // Make sure it starts as empty.
        nPersons = cvReadIntByName(
                fileStorage, // fs
                null, // map
                "nPersons", // name
                0); // default_value
        if (nPersons == 0) {
            LOGGER.severe("No people found in the training database 'data/facedata.xml'.");
            return 0;
        } else {
            LOGGER.info(nPersons + " persons read from the training database");
        }

        // Load each person's name.
        for (i = 0; i < nPersons; i++) {
            String sPersonName;
            String varname = "personName_" + (i + 1);
            sPersonName = cvReadStringByName(
                    fileStorage, // fs
                    null, // map
                    varname,
                    "");
            personNames.add(sPersonName);
        }
        LOGGER.info("person names: " + personNames);

        // Load the data
        nEigens = cvReadIntByName(
                fileStorage, // fs
                null, // map
                "nEigens",
                0); // default_value
        nTrainFaces = cvReadIntByName(
                fileStorage,
                null, // map
                "nTrainFaces",
                0); // default_value
        Pointer pointer = cvReadByName(
                fileStorage, // fs
                null, // map
                "trainPersonNumMat"); // name
        trainPersonNumMat = new CvMat(pointer);

        pointer = cvReadByName(
                fileStorage, // fs
                null, // map
                "eigenValMat"); // name
        eigenValMat = new CvMat(pointer);

        pointer = cvReadByName(
                fileStorage, // fs
                null, // map
                "projectedTrainFaceMat"); // name
        projectedTrainFaceMat = new CvMat(pointer);

        pointer = cvReadByName(
                fileStorage,
                null, // map
                "avgTrainImg");
        pAvgTrainImg = new IplImage(pointer);

        eigenVectArr = new IplImage[nTrainFaces];
        for (i = 0; i < nEigens; i++) {
            String varname = "eigenVect_" + i;
            pointer = cvReadByName(
                    fileStorage,
                    null, // map
                    varname);
            eigenVectArr[i] = new IplImage(pointer);
        }
        // release the file-storage interface
        cvReleaseFileStorage(fileStorage);

        LOGGER.log(Level.INFO, "Training data loaded ({0} training images of {1} people)", new Object[]{nTrainFaces, nPersons});
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("People: ");
        if (nPersons > 0) {
            stringBuilder.append("<").append(personNames.get(0)).append(">");
        }
        for (i = 1; i < nPersons; i++) {
            stringBuilder.append(", <").append(personNames.get(i)).append(">");
        }
        LOGGER.info(stringBuilder.toString());

        return 1;
    }
    //=========================================================================

    public static void saveFile(String text) {
        try {
            String path = new File(".").getCanonicalPath();

            File file = new File(path + "/train.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            String trainData;
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buff = new byte[fis.available()];
                fis.read(buff);
                trainData = new String(buff);
                trainData = trainData + text;
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(trainData.getBytes());
            }
        } catch (Exception e) {
        }
    }
    //==============================================================================

    public void learn(final String trainingFileName) {
        int i;
        // load training data
        LOGGER.info("===========================================");
        LOGGER.info("Loading the training images in " + trainingFileName);
        trainingFaceImgArr = loadFaceImgArray(trainingFileName);
        nTrainFaces = trainingFaceImgArr.length;
        LOGGER.info("Got " + nTrainFaces + " training images");
        if (nTrainFaces < 3) {
            LOGGER.severe("Need 3 or more training faces\n"
                    + "Input file contains only " + nTrainFaces);
            return;
        }
        // do Principal Component Analysis on the training faces
        doPCA();

        LOGGER.info("projecting the training images onto the PCA subspace");
        // project the training images onto the PCA subspace
        projectedTrainFaceMat = cvCreateMat(
                nTrainFaces, // rows
                nEigens, // cols
                CV_32FC1); // type, 32-bit float, 1 channel 

        // initialize the training face matrix - for ease of debugging
        for (int i1 = 0; i1 < nTrainFaces; i1++) {
            for (int j1 = 0; j1 < nEigens; j1++) {
                projectedTrainFaceMat.put(i1, j1, 0.0);
            }
        }

        LOGGER.info("created projectedTrainFaceMat with " + nTrainFaces + " (nTrainFaces) rows and " + nEigens + " (nEigens) columns");

        final FloatPointer floatPointer = new FloatPointer(nEigens);
        for (i = 0; i < nTrainFaces; i++) {
            cvEigenDecomposite(
                    trainingFaceImgArr[i], // obj
                    nEigens, // nEigObjs
                    new PointerPointer(eigenVectArr), // eigInput (Pointer)
                    0, // ioFlags
                    null, // userData (Pointer)
                    pAvgTrainImg, // avg
                    floatPointer); // coeffs (FloatPointer)

            for (int j1 = 0; j1 < nEigens; j1++) {
                projectedTrainFaceMat.put(i, j1, floatPointer.get(j1));
            }
        }
        if (nTrainFaces < 5) {
            LOGGER.log(Level.INFO, "projectedTrainFaceMat after cvEigenDecomposite:\n{0}", projectedTrainFaceMat);
        }
        // store the recognition data as an xml file
        storeTrainingData();
    }

    private IplImage[] loadFaceImgArray(final String filename) {
        IplImage[] faceImgArr;
        BufferedReader imgListFile;
        String imgFilename;
        int iFace = 0;
        int nFaces = 0;
        int i;
        try {
            // open the input file
            imgListFile = new BufferedReader(new FileReader(filename));

            // count the number of faces
            while (true) {
                final String line = imgListFile.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                nFaces++;
            }
            LOGGER.info("nFaces: " + nFaces);
            imgListFile = new BufferedReader(new FileReader(filename));

            // allocate the face-image array and person number matrix
            faceImgArr = new IplImage[nFaces];
            personNumTruthMat = cvCreateMat(
                    1, // rows
                    nFaces, // cols
                    CV_32SC1); // type, 32-bit unsigned, one channel

            // initialize the person number matrix - for ease of debugging
            for (int j1 = 0; j1 < nFaces; j1++) {
                personNumTruthMat.put(0, j1, 0);
            }

            personNames.clear();        // Make sure it starts as empty.
            nPersons = 0;

            // store the face images in an array
            for (iFace = 0; iFace < nFaces; iFace++) {
                String personName;
                String sPersonName;
                int personNumber;

                // read person number (beginning with 1), their name and the image filename.
                final String line = imgListFile.readLine();
                //   JOptionPane.showMessageDialog(null,"Line:"+line);
                if (line.isEmpty()) {
                    break;
                }
                final String[] tokens = line.split(" ");
                //       JOptionPane.showMessageDialog(null, tokens[0]+" and "+tokens[1]);
                personNumber = Integer.parseInt(tokens[0]);
                personName = tokens[2];
                imgFilename = tokens[1];
                sPersonName = personName;
                System.out.println(tokens[0] + "=> " + tokens[1] + " =>" + tokens[2]);
                LOGGER.info("Got " + iFace + " " + personNumber + " " + personName + " " + imgFilename);

                // Check if a new person is being loaded.
                if (personNumber > nPersons) {
                    // Allocate memory for the extra person (or possibly multiple), using this new person's name.
                    personNames.add(sPersonName);
                    nPersons = personNumber;
                    LOGGER.info("Got new person " + sPersonName + " -> nPersons = " + nPersons + " [" + personNames.size() + "]");
                }

                // Keep the data
                personNumTruthMat.put(
                        0, // i
                        iFace, // j
                        personNumber); // v

                // load the face image
                faceImgArr[iFace] = cvLoadImage(
                        imgFilename, // filename
                        CV_LOAD_IMAGE_GRAYSCALE); // isColor

                if (faceImgArr[iFace] == null) {
                    throw new RuntimeException("Can't load image from " + imgFilename);
                }
            }
            imgListFile.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        LOGGER.info("Data loaded from '" + filename + "': (" + nFaces + " images of " + nPersons + " people).");
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("People: ");
        if (nPersons > 0) {
            stringBuilder.append("<").append(personNames.get(0)).append(">");
        }
        for (i = 1; i < nPersons && i < personNames.size(); i++) {
            stringBuilder.append(", <").append(personNames.get(i)).append(">");
        }
        LOGGER.info(stringBuilder.toString());
        return faceImgArr;
    }

    /**
     * Does the Principal Component Analysis, finding the average image and the
     * eigenfaces that represent any image in the given dataset.
     */
    private void doPCA() {
        int i;
        CvTermCriteria calcLimit;
        CvSize faceImgSize = new CvSize();

        // set the number of eigenvalues to use
        nEigens = nTrainFaces - 1;

        LOGGER.info("allocating images for principal component analysis, using " + nEigens + (nEigens == 1 ? " eigenvalue" : " eigenvalues"));

        // allocate the eigenvector images
        faceImgSize.width(trainingFaceImgArr[0].width());
        faceImgSize.height(trainingFaceImgArr[0].height());
        eigenVectArr = new IplImage[nEigens];
        for (i = 0; i < nEigens; i++) {
            eigenVectArr[i] = cvCreateImage(
                    faceImgSize, // size
                    IPL_DEPTH_32F, // depth
                    1); // channels
        }

        // allocate the eigenvalue array
        eigenValMat = cvCreateMat(
                1, // rows
                nEigens, // cols
                CV_32FC1); // type, 32-bit float, 1 channel

        // allocate the averaged image
        pAvgTrainImg = cvCreateImage(
                faceImgSize, // size
                IPL_DEPTH_32F, // depth
                1); // channels

        // set the PCA termination criterion
        calcLimit = cvTermCriteria(
                CV_TERMCRIT_ITER, // type
                nEigens, // max_iter
                1); // epsilon

        LOGGER.info("computing average image, eigenvalues and eigenvectors");
        // compute average image, eigenvalues, and eigenvectors
        cvCalcEigenObjects(
                nTrainFaces, // nObjects
                new PointerPointer(trainingFaceImgArr), // input
                new PointerPointer(eigenVectArr), // output
                CV_EIGOBJ_NO_CALLBACK, // ioFlags
                0, // ioBufSize
                null, // userData
                calcLimit,
                pAvgTrainImg, // avg
                eigenValMat.data_fl()); // eigVals

        LOGGER.info("normalizing the eigenvectors");
        cvNormalize(
                eigenValMat, // src (CvArr)
                eigenValMat, // dst (CvArr)
                1, // a
                0, // b
                CV_L1, // norm_type
                null); // mask
    }

    /**
     * Stores the training data to the file 'data/facedata.xml'.
     */
    private void storeTrainingData() {
        CvFileStorage fileStorage;
        int i;
        LOGGER.info("writing data/facedata.xml");
        // create a file-storage interface
        fileStorage = cvOpenFileStorage(
                "data/facedata.xml", // filename
                null, // memstorage
                CV_STORAGE_WRITE, // flags
                null); // encoding

        // Store the person names. Added by hemant.
        cvWriteInt(
                fileStorage, // fs
                "nPersons", // name
                nPersons); // value

        for (i = 0; i < nPersons; i++) {
            String varname = "personName_" + (i + 1);
            cvWriteString(
                    fileStorage, // fs
                    varname, // name
                    personNames.get(i), // string
                    0); // quote
        }
        // store all the data
        cvWriteInt(
                fileStorage, // fs
                "nEigens", // name
                nEigens); // value

        cvWriteInt(
                fileStorage, // fs
                "nTrainFaces", // name
                nTrainFaces); // value

        cvWrite(
                fileStorage, // fs
                "trainPersonNumMat", // name
                personNumTruthMat); // value

        cvWrite(
                fileStorage, // fs
                "eigenValMat", // name
                eigenValMat); // value

        cvWrite(
                fileStorage, // fs
                "projectedTrainFaceMat", // name
                projectedTrainFaceMat);

        cvWrite(fileStorage, // fs
                "avgTrainImg", // name
                pAvgTrainImg); // value

        for (i = 0; i < nEigens; i++) {
            String varname = "eigenVect_" + i;
            cvWrite(
                    fileStorage, // fs
                    varname, // name
                    eigenVectArr[i]); // value
        }
        cvReleaseFileStorage(fileStorage);
    }

    public void setActionChar(char ch) {
        this.actionChar = ch;
    }

    public String modifyName(String name) {
        try {
            name = name.trim();
            name = name.replaceAll("[ ]+", "_");
            return name;
        } catch (Exception e) {
        }
        return name;
    }

    public static void main(String[] args) throws InterruptedException {
        new FaceRecognizer1().recognizeFromCam1();
    }
}

/**
 *
 */
class MyOwnListener implements ActionListener {

    FaceRecognizer1 recognizer1;

    public MyOwnListener(FaceRecognizer1 recognizer1) {
        this.recognizer1 = recognizer1;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("SAVE")) {
            recognizer1.setActionChar('n');
        }
    }
}
