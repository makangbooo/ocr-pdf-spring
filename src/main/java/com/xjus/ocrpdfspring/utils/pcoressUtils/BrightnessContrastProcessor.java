package com.xjus.ocrpdfspring.utils.pcoressUtils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class BrightnessContrastProcessor extends BaseImageProcessor {

    // 自动模式参数
    private double autoClipLimit = 2.0; // CLAHE 的对比度限制
    private Size autoTileGridSize = new Size(8, 8); // CLAHE 的网格大小

    // 手动模式参数
    private int manualBrightness = 0; // 亮度调整，范围 [-100, 100]
    private double manualContrast = 1.0; // 对比度调整，范围 [0.0, 3.0]

    public BrightnessContrastProcessor(String processorName) {
        super(processorName);
    }

    public Mat processAuto(Mat img) {
        try {
            // 转换为 LAB 颜色空间
            Mat lab = new Mat();
            Imgproc.cvtColor(img, lab, Imgproc.COLOR_BGR2Lab);

            // 分离通道
            List<Mat> channels = new ArrayList<>();
            Core.split(lab, channels);
            Mat l = channels.get(0);
            Mat a = channels.get(1);
            Mat b = channels.get(2);

            // 对亮度通道应用 CLAHE
            CLAHE clahe = Imgproc.createCLAHE();
            clahe.setClipLimit(autoClipLimit);
            clahe.setTilesGridSize(new Size(autoTileGridSize.width, autoTileGridSize.height));
            clahe.apply(l, l);

            // 合并通道
            Core.merge(channels, lab);

            // 转回 BGR 颜色空间
            Mat result = new Mat();
            Imgproc.cvtColor(lab, result, 56);

            return result;
        } catch (Exception e) {
            System.err.println("自动亮度对比度处理失败: " + e.getMessage());
            return img; // 返回原图作为默认值
        }
    }

    public Mat processManual(Mat img) {
        try {
            // 获取参数
            double alpha = manualContrast; // 对比度
            double beta = manualBrightness; // 亮度

            // 应用亮度和对比度调整
            Mat result = new Mat();
            img.convertTo(result, -1, alpha, beta); // 公式: output = alpha * input + beta

            return result;
        } catch (Exception e) {
            System.err.println("手动亮度对比度处理失败: " + e.getMessage());
            return img; // 返回原图作为默认值
        }
    }

    public static void main(String[] args) {
        // 加载 OpenCV 库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 示例：加载图像并处理
        Mat img = Imgcodecs.imread("path/to/image.jpg");
        BrightnessContrastProcessor processor = new BrightnessContrastProcessor(null);

        // 自动模式
        Mat autoResult = processor.processAuto(img);
        Imgcodecs.imwrite("path/to/auto_result.jpg", autoResult);

        // 手动模式
        processor.manualBrightness = 50; // 修改手动模式参数
        processor.manualContrast = 1.5;
        Mat manualResult = processor.processManual(img);
        Imgcodecs.imwrite("path/to/manual_result.jpg", manualResult);
    }
}