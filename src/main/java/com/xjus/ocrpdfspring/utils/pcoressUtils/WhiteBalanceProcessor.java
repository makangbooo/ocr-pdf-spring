package com.xjus.ocrpdfspring.utils.pcoressUtils;

import org.opencv.core.*;
import org.opencv.core.CvType;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhiteBalanceProcessor extends BaseImageProcessor {

    // 自动模式参数
    private Map<String, Object> autoParams = new HashMap<>();

    // 手动模式参数
    private Map<String, Object> manualParams = new HashMap<>();

    public WhiteBalanceProcessor() {
        super(null);
        // 初始化自动模式参数
        autoParams.put("method", "grayworld");

        // 初始化手动模式参数
        manualParams.put("method", "manual");
        manualParams.put("r_scale", 1.0);
        manualParams.put("g_scale", 1.0);
        manualParams.put("b_scale", 1.0);
    }

    public Mat processAuto(Mat img) {
        String method = (String) autoParams.get("method");

        try {
            if ("simple".equals(method)) {
                return applySimpleWhiteBalance(img);
            } else if ("grayworld".equals(method)) {
                return applyGrayworldWhiteBalance(img);
            } else {
                throw new IllegalArgumentException("不支持的白平衡方法: " + method);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return img; // 返回原图作为默认值
        }
    }

    public Mat processManual(Mat img) {
        // 分离通道
        List<Mat> channels = new ArrayList<>();
        Core.split(img, channels);

        Mat b = channels.get(0);
        Mat g = channels.get(1);
        Mat r = channels.get(2);

        // 应用用户指定的缩放因子
        double rScale = (double) manualParams.get("r_scale");
        double gScale = (double) manualParams.get("g_scale");
        double bScale = (double) manualParams.get("b_scale");

        scaleChannel(r, rScale);
        scaleChannel(g, gScale);
        scaleChannel(b, bScale);

        // 合并通道
        Mat result = new Mat();
        Core.merge(channels, result);
        return result;
    }

    private void scaleChannel(Mat channel, double scale) {
        channel.convertTo(channel, CvType.CV_32F); // 转换为浮点类型以进行计算
        Core.multiply(channel, new Scalar(scale), channel); // 缩放
        Core.min(channel, new Scalar(255), channel); // 限制最大值为 255
        Core.max(channel, new Scalar(0), channel); // 限制最小值为 0
        channel.convertTo(channel, CvType.CV_8U); // 转换回 8 位无符号整数
    }

    private Mat applySimpleWhiteBalance(Mat img) {
        // 分离通道
        List<Mat> channels = new ArrayList<>();
        Core.split(img, channels);

        Mat b = channels.get(0);
        Mat g = channels.get(1);
        Mat r = channels.get(2);

        // 计算每个通道的最大值
        double rMax = getMaxValue(r);
        double gMax = getMaxValue(g);
        double bMax = getMaxValue(b);

        // 计算缩放因子
        if (rMax > 0 && gMax > 0 && bMax > 0) {
            scaleChannel(r, 255.0 / rMax);
            scaleChannel(g, 255.0 / gMax);
            scaleChannel(b, 255.0 / bMax);
        }

        // 合并通道
        Mat result = new Mat();
        Core.merge(channels, result);
        return result;
    }

    private Mat applyGrayworldWhiteBalance(Mat img) {
        // 分离通道
        List<Mat> channels = new ArrayList<>();
        Core.split(img, channels);

        Mat b = channels.get(0);
        Mat g = channels.get(1);
        Mat r = channels.get(2);

        // 计算每个通道的平均值
        double rAvg = getMeanValue(r);
        double gAvg = getMeanValue(g);
        double bAvg = getMeanValue(b);

        // 计算整体平均值
        double avg = (rAvg + gAvg + bAvg) / 3;

        // 计算缩放因子
        if (rAvg > 0 && gAvg > 0 && bAvg > 0) {
            scaleChannel(r, avg / rAvg);
            scaleChannel(g, avg / gAvg);
            scaleChannel(b, avg / bAvg);
        }

        // 合并通道
        Mat result = new Mat();
        Core.merge(channels, result);
        return result;
    }

    private double getMaxValue(Mat channel) {
        MatOfDouble maxVal = new MatOfDouble();
        Core.minMaxLoc(channel, null);
        return maxVal.get(0, 0)[0];
    }

    private double getMeanValue(Mat channel) {
        MatOfDouble mean = new MatOfDouble();
        Core.meanStdDev(channel, mean, mean);
        return mean.get(0, 0)[0];
    }

    public static void main(String[] args) {
        // 加载 OpenCV 库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 示例：加载图像并处理
        Mat img = Imgcodecs.imread("path/to/image.jpg");
        WhiteBalanceProcessor processor = new WhiteBalanceProcessor();

        // 自动模式
        Mat autoResult = processor.processAuto(img);
        Imgcodecs.imwrite("path/to/auto_result.jpg", autoResult);

        // 手动模式
        processor.manualParams.put("r_scale", 1.2); // 修改手动模式参数
        processor.manualParams.put("g_scale", 1.0);
        processor.manualParams.put("b_scale", 0.8);
        Mat manualResult = processor.processManual(img);
        Imgcodecs.imwrite("path/to/manual_result.jpg", manualResult);
    }
}