package nr.king.familytracker.utils;

import nr.king.familytracker.exceptions.FailedResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
@Service
public class FileUtils {

    @Autowired
    private CommonUtils commonUtils;

    private final Logger logger = LogManager.getLogger(FileUtils.class);

    public File convertMultiPartToFile(MultipartFile file){
        return convertMultiPartToFile(file, "");
    }

    public File convertMultiPartToFile(MultipartFile file,String path) {
        File convFile = new File(path.equalsIgnoreCase("")?file.getName():path);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
            return convFile;
        } catch (IOException e) {
            logger.error("exception while converting multipart to file due to - " + e.getMessage(), e);
            throw new FailedResponseException("Internal Server Error, Kindly contact GoFrugal Support.!");
        }
    }

    public File resizeImage(MultipartFile image,int width,int height) {
        try {
            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(image.getBytes()));
            BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());
            // scales the input image to the output image
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, width, height, null);
            g2d.dispose();
            // writes to output file
            File outputFile = new File(image.getName());
            ImageIO.write(outputImage, "png", outputFile);
            return outputFile;
        } catch (Exception e) {
            logger.error("exception while resizing file due to - " + e.getMessage(), e);
           /* commonUtils.insertAudit(new AuditMaster(0,"vendor", -1,"url", "Resize", "", "Header",
                    String.format("Exception while resizing file due to - %s and its trace  - %s", e.getMessage(), Arrays.toString(e.getStackTrace())),8, "ReSize Image"));
 */
            throw new FailedResponseException("Please Try with another App Icon image..! Unable to Process this Image");
        }
    }


    public File resizeImageFromFiles(File image,int width,int height, String outputFilePath) {
        try {
            BufferedImage inputImage = ImageIO.read(image);
            Image resultingImage = inputImage.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
            BufferedImage outputImage = new BufferedImage(width,height, inputImage.getType());
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            File outputFile = new File(outputFilePath+"/"+System.currentTimeMillis()+".png");
            ImageIO.write(outputImage, "png", outputFile);
            return outputFile;
        } catch (Exception e) {
            logger.error("exception while resizing file due to - " + e.getMessage(), e);
            throw new FailedResponseException("Please Try with another App Icon image..! Unable to Process this Image");
        }
    }

    public File resizeImageFromFile(File image,int width,int height, String outputFilePath) {
        try {
            BufferedImage inputImage = ImageIO.read(image);
            BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, width, height, null);
            g2d.dispose();
            File outputFile = new File(outputFilePath+"/"+image.getName());
            ImageIO.write(outputImage, "png", outputFile);
            return outputFile;
        } catch (Exception e) {
            logger.error("exception while resizing file due to - " + e.getMessage(), e);
            throw new FailedResponseException("Please Try with another App Icon image..! Unable to Process this Image");
        }
    }

    public File writeOnFile(byte[] content){
        return writeOnFile(content, "name");
    }

    public File writeOnFile(byte[] content,String path) {
        File responseFile = new File(path);
        try (FileOutputStream fos = new FileOutputStream(responseFile)) {
            fos.write(content);
            return responseFile;
        } catch (IOException e) {
            logger.error("exception while converting multipart to file due to - " + e.getMessage(), e);
            throw new FailedResponseException("Internal Server Error, Kindly contact GoFrugal Support.!");
        }
    }
}
