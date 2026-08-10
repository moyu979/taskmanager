package utils.cvFunc;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class OCRUtils {

    private static final Tesseract tesseract = new Tesseract();

    static {
        // tesseract 数据路径（macOS Homebrew 默认安装路径）
        tesseract.setDatapath("/opt/homebrew/share/tessdata");
        tesseract.setLanguage("chi_sim"); // 英文
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    /**
     * 纯文字 OCR（展示用）
     */
    public static String extractText(Mat mat) {
        BufferedImage image = matToBufferedImage(mat);
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            e.printStackTrace();
            return null;
        }
    }
}
