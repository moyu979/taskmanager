package infra.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * OCR 工具类
 * <p>
 * 提供静态无状态的图片文字识别功能。
 * 传入 OpenCV Mat 图片，返回识别到的文字。
 * 线程安全：内部 Tesseract 实例是线程安全的。
 */
public class OCRUtils {

    public static final Tesseract tesseract = new Tesseract();

    static {
        // 设置 tessdata 路径（macOS Homebrew 默认安装路径）
        tesseract.setDatapath("/opt/homebrew/share/tessdata");
        // 默认英文识别，可修改为 "chi_sim"（中文简体）或 "eng+chi_sim"（中英混合）
        tesseract.setLanguage("chi_sim");
        // 如需指定 tessdata 路径，取消下面注释并修改路径
        // tesseract.setDatapath("/path/to/tessdata");
    }

    /**
     * Mat 转 BufferedImage
     */
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
     * 对图像进行预处理（灰度化 + 二值化），提高识别率
     */
    private static Mat preprocess(Mat mat) {
        Mat gray = new Mat();
        if (mat.channels() == 3 || mat.channels() == 4) {
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = mat.clone();
        }

        Mat binary = new Mat();
        Imgproc.GaussianBlur(gray, gray, new org.opencv.core.Size(3, 3), 0);
        Imgproc.adaptiveThreshold(gray, binary, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 11, 2);

        gray.release();
        return binary;
    }

    /**
     * 识别图片中的文字（直接识别，无预处理）
     *
     * @param mat OpenCV Mat 格式的图片
     * @return 识别到的文字，失败返回空字符串
     */
    public static String extractText(Mat mat) {
        if (mat == null || mat.empty()) {
            return "";
        }
        BufferedImage image = matToBufferedImage(mat);
        try {
            String result = tesseract.doOCR(image);
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            System.err.println("OCR 识别失败: " + e.getMessage());
            return "";
        }
    }

    /**
     * 识别图片中的文字（带预处理，对较模糊或低对比度的文字效果更好）
     *
     * @param mat OpenCV Mat 格式的图片
     * @return 识别到的文字，失败返回空字符串
     */
    public static String extractTextWithPreprocess(Mat mat) {
        if (mat == null || mat.empty()) {
            return "";
        }
        Mat processed = preprocess(mat);
        try {
            BufferedImage image = matToBufferedImage(processed);
            String result = tesseract.doOCR(image);
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            System.err.println("OCR 识别失败: " + e.getMessage());
            return "";
        } finally {
            processed.release();
        }
    }

    /**
     * 设置识别语言
     *
     * @param language 语言代码，如 "eng"（英文）、"chi_sim"（中文简体）、"eng+chi_sim"（中英混合）
     */
    public static void setLanguage(String language) {
        tesseract.setLanguage(language);
    }
}
