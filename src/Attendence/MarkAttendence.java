/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Attendence;

import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.PointerPointer;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_legacy.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import java.awt.FlowLayout;

/**
 *
 * @author test
 */
public class MarkAttendence {

    //==================================================
    float MaxConfidence = 0.800f;
    //==================================================

    private static final Logger LOGGER = Logger.getLogger(MarkAttendence.class.getName());
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

    //====== Additional classes==========================
    Attendence attendence;

    public MarkAttendence() {
        try {
            storage = cvCreateMemStorage(0);
            attendence = new Attendence();
        } catch (Exception e) {
        }
    }

    public void recognizeFromCam() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            float projectedTestFace[];
            CvHaarClassifierCascade faceCascade;
            
            FrameGrabber grabber = new OpenCVFrameGrabber(0);
            grabber.setImageWidth(210);
            grabber.setImageHeight(210);
            grabber.start();

            if (loadTrainingData1() == 1) {
                faceWidth = pAvgTrainImg.width();
                faceHeight = pAvgTrainImg.height();
            } else {
            }
            projectedTestFace = new float[nEigens];

            CanvasFrame input = new CanvasFrame("Mark Attendence frame");
            input.setLayout(new FlowLayout());
            input.setLocation(580, 150);
            input.setAlwaysOnTop(true);
            input.setDefaultCloseOperation(CanvasFrame.DO_NOTHING_ON_CLOSE);
            
            faceCascade = new CvHaarClassifierCascade(cvLoad(faceCascadeFilename));

            while (true) {
                int iNearest, nearest = 0;
                IplImage camImg;
                IplImage greyImg;
                IplImage faceImg;
                IplImage sizedImg;
                IplImage equalizedImg;
                IplImage processedFaceImg;
                CvRect faceRect;
                IplImage shownImg;
                float confidence = 0;

                try {
                    camImg = grabber.grab();
                } catch (Exception e) {
                    continue;
                }
                // Make sure the image is greyscale, since the Eigenfaces is only done on greyscale image.
                greyImg = convertImageToGreyscale(camImg);

                // Perform face detection on the input image, using the given Haar cascade classifier.  
                faceRect = detectFaceInImage(greyImg, faceCascade);
//                faceRect.width();
                // Make sure a valid face was detected.

                if (faceRect.width() > 0) {
                    faceImg = cropImage(greyImg, faceRect);
                    // Make sure the image is the same dimensions as the training images.
                    sizedImg = resizeImage(faceImg, faceWidth, faceHeight);
//                  Give the image a standard brightness and contrast, in case it was too dark or low contrast.
                    equalizedImg = IplImage.create(new CvSize(sizedImg.width(), sizedImg.height()), 8, 1);	// Create an empty greyscale image
                    cvEqualizeHist(sizedImg, equalizedImg);
                    processedFaceImg = equalizedImg;

                    if (processedFaceImg.isNull()) {
                        continue;
                    }

                    // If the face rec database has been loaded, then try to recognize the person currently detected.
                    if (nEigens > 0) {
                        // project the test image onto the PCA subspace

                        cvEigenDecomposite(
                                processedFaceImg,
                                nEigens,
                                new PointerPointer(eigenVectArr),
                                0, null,
                                pAvgTrainImg,
                                projectedTestFace);

                        // Check which person it is most likely to be.
                        final FloatPointer pConfidence = new FloatPointer(confidence);
                        iNearest = findNearestNeighbor(projectedTestFace, new FloatPointer(pConfidence));
                        confidence = pConfidence.get();
                        nearest = trainPersonNumMat.data_i().get(iNearest);
                    }
                }

                // Show the data on the screen.
                shownImg = cvCloneImage(camImg);

                if (faceRect.width() > 0)// Check if a face was detected.
                {
                    // Show the detected face region.
                    cvRectangle(shownImg, cvPoint(faceRect.x(), faceRect.y()), cvPoint(faceRect.x() + faceRect.width() - 1, faceRect.y() + faceRect.height() - 1), CV_RGB(0, 255, 0), 1, 8, 0);
                    if (nEigens > 0 && confidence >= MaxConfidence) // Check if the face recognition database is loaded and a person was recognized.
                    {
                        // Show the name of the recognized person, overlayed on the image below their face.
                        CvFont font = new CvFont();

                        cvInitFont(font, CV_FONT_HERSHEY_PLAIN, 1.0, 1.0, 0, 1, CV_AA);
                        CvScalar textColor = CV_RGB(255, 0, 0);	// light blue text

                        String text = "";

                        String arr[] = personNames.get(nearest - 1).toString().split("&");
                        String name = "" + arr[0];
                        text = name;
                        cvPutText(shownImg, text, cvPoint(faceRect.x(), faceRect.y() + faceRect.height() + 15), font, textColor);

                        String pname = getOrgnlName(name);
                        String std = arr[1];
                        attendence.MarkAttendence(attendence.getSid(pname, std));
                    }
                }
                // Display the image.
                input.showImage(shownImg);
//                KeyEvent key = input.waitKey(10); 
            }
        } catch (Exception e) {
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

//        size=size.height(img.height());
//        size=size.width(img.width());
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
//        cvReleaseImage(imageTmp);
            return imageRGB;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public CvRect detectFaceInImage(IplImage inputImg, CvHaarClassifierCascade cascade) {
        try {
            IplImage detectImg;
            IplImage greyImg = null;
//            CvMemStorage storage = null;
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
//              System.out.println(rc.width());
            } else {
                rc = new CvRect(-1, -1, -1, -1);
            }
            if (!(greyImg == null)) {
                cvReleaseImage(greyImg);
            }
//            System.out.println(rc.width());
//            cvReleaseMemStorage(storage);

            return rc;
        } catch (Exception e) {
            e.printStackTrace();

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
            e.printStackTrace();
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
                "trainPersonNumMat", null); // name
        trainPersonNumMat = new CvMat(pointer);

        pointer = cvReadByName(
                fileStorage, // fs
                null, // map
                "eigenValMat", null); // name
        eigenValMat = new CvMat(pointer);

        pointer = cvReadByName(
                fileStorage, // fs
                null, // map
                "projectedTrainFaceMat", null); // name
        projectedTrainFaceMat = new CvMat(pointer);

        pointer = cvReadByName(
                fileStorage,
                null, // map
                "avgTrainImg", null);
        pAvgTrainImg = new IplImage(pointer);

        eigenVectArr = new IplImage[nTrainFaces];
        for (i = 0; i < nEigens; i++) {
            String varname = "eigenVect_" + i;
            pointer = cvReadByName(
                    fileStorage,
                    null, // map
                    varname, null);
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

    /**
     * Find the most likely person based on a detection. Returns the index, and
     * stores the confidence value into pConfidence.
     *
     * @param projectedTestFace the projected test face
     * @param pConfidencePointer a pointer containing the confidence value
     * @param iTestFace the test face index
     * @return the index
     */
    private int findNearestNeighbor(float projectedTestFace[], FloatPointer pConfidencePointer) {
        double leastDistSq = Double.MAX_VALUE;
        int i = 0;
        int iTrain = 0;
        int iNearest = 0;

        LOGGER.info("................");
        LOGGER.info("find nearest neighbor from " + nTrainFaces + " training faces");
        for (iTrain = 0; iTrain < nTrainFaces; iTrain++) {
            //LOGGER.info("considering training face " + (iTrain + 1));
            double distSq = 0;

            for (i = 0; i < nEigens; i++) {
                //LOGGER.debug("  projected test face distance from eigenface " + (i + 1) + " is " + projectedTestFace[i]);

                float projectedTrainFaceDistance = (float) projectedTrainFaceMat.get(iTrain, i);
                float d_i = projectedTestFace[i] - projectedTrainFaceDistance;
                distSq += d_i * d_i; // / eigenValMat.data_fl().get(i);  // Mahalanobis distance (might give better results than Eucalidean distance)
//          if (iTrain < 5) {
//            LOGGER.info("    ** projected training face " + (iTrain + 1) + " distance from eigenface " + (i + 1) + " is " + projectedTrainFaceDistance);
//            LOGGER.info("    distance between them " + d_i);
//            LOGGER.info("    distance squared " + distSq);
//          }
            }

            if (distSq < leastDistSq) {
                leastDistSq = distSq;
                iNearest = iTrain;
                LOGGER.info("  training face " + (iTrain + 1) + " is the new best match, least squared distance: " + leastDistSq);
            }
        }

        // Return the confidence level based on the Euclidean distance,
        // so that similar images should give a confidence between 0.5 to 1.0,
        // and very different images should give a confidence between 0.0 to 0.5.
        float pConfidence = (float) (1.0f - Math.sqrt(leastDistSq / (float) (nTrainFaces * nEigens)) / 255.0f);
        pConfidencePointer.put(pConfidence);

        LOGGER.info("training face " + (iNearest + 1) + " is the final best match, confidence " + pConfidence);
        return iNearest;
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

    public String getOrgnlName(String name) {
        try {

            name = name.trim();
            name = name.replaceAll("[_]+", " ");
            return name;
        } catch (Exception e) {
        }
        return name;
    }

//    public static void main(String[] args) {
//        new MarkAttendence().recognizeFromCam();
//    }
}
