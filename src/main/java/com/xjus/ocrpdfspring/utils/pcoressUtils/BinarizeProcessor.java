package com.xjus.ocrpdfspring.utils.pcoressUtils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class BinarizeProcessor extends BaseImageProcessor {

    // 自动模式参数
    private boolean autoAdaptive = true; // 是否使用自适应阈值
    private int autoThreshold = 127; // 固定阈值（仅在 adaptive=False 时使用）
    private int autoBlockSize = 11; // 自适应阈值的块大小
    private int autoC = 2; // 自适应阈值的常数

    // 手动模式参数
    private boolean manualAdaptive = true;
    private int manualThreshold = 127;
    private int manualBlockSize = 11;
    private int manualC = 2;

    public BinarizeProcessor(String processorName) {
        super(processorName);
    }

    public Mat processAuto(Mat img) {
        try {
            // 转换为灰度图
            Mat gray = new Mat();
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

            // 根据参数选择二值化方法
            Mat binary = new Mat();
            if (autoAdaptive) {
                // 自适应阈值二值化
                Imgproc.adaptiveThreshold(
                        gray, binary, 255,
                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                        Imgproc.THRESH_BINARY,
                        autoBlockSize, autoC
                );
            } else {
                // 简单阈值二值化
                Imgproc.threshold(gray, binary, autoThreshold, 255, Imgproc.THRESH_BINARY);
            }

            // 转回彩色图像格式（保持一致性）
            Mat result = new Mat();
            Imgproc.cvtColor(binary, result, Imgproc.COLOR_GRAY2BGR);

            return result;
        } catch (Exception e) {
            System.err.println("自动二值化处理失败: " + e.getMessage());
            return img; // 返回原图作为默认值
        }
    }

    public Mat processManual(Mat img) {
        try {
            // 转换为灰度图
            Mat gray = new Mat();
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

            // 根据参数选择二值化方法
            Mat binary = new Mat();
            if (manualAdaptive) {
                // 自适应阈值二值化
                Imgproc.adaptiveThreshold(
                        gray, binary, 255,
                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                        Imgproc.THRESH_BINARY,
                        manualBlockSize, manualC
                );
            } else {
                // 简单阈值二值化
                Imgproc.threshold(gray, binary, manualThreshold, 255, Imgproc.THRESH_BINARY);
            }

            // 转回彩色图像格式（保持一致性）
            Mat result = new Mat();
            Imgproc.cvtColor(binary, result, Imgproc.COLOR_GRAY2BGR);

            return result;
        } catch (Exception e) {
            System.err.println("手动二值化处理失败: " + e.getMessage());
            return img; // 返回原图作为默认值
        }
    }

    public static void main(String[] args) {
        // 加载 OpenCV 库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 示例：加载图像并处理
        Mat img = Imgcodecs.imread("path/to/image.jpg");
        if (img.empty()) {
            System.err.println("无法加载图像！");
            return;
        }

        BinarizeProcessor processor = new BinarizeProcessor(null);

        // 自动模式
        Mat autoResult = processor.processAuto(img);
        Imgcodecs.imwrite("path/to/auto_result.jpg", autoResult);

        // 手动模式
        processor.manualAdaptive = false; // 修改手动模式参数
        processor.manualThreshold = 100;
        Mat manualResult = processor.processManual(img);
        Imgcodecs.imwrite("path/to/manual_result.jpg", manualResult);
    }
}