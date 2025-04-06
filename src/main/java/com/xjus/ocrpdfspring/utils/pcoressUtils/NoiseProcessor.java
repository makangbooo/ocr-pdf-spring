package com.xjus.ocrpdfspring.utils.pcoressUtils;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

public class NoiseProcessor extends BaseImageProcessor{

    // 自动模式参数
    private Map<String, Object> autoParams = new HashMap<>();

    // 手动模式参数
    private Map<String, Object> manualParams = new HashMap<>();

    public NoiseProcessor() {
        super(null);
        // 初始化自动模式参数
        autoParams.put("method", "bilateral");
        autoParams.put("gaussian_kernel_size", 5);
        autoParams.put("median_kernel_size", 5);
        autoParams.put("bilateral_d", 9);
        autoParams.put("bilateral_sigma_color", 75);
        autoParams.put("bilateral_sigma_space", 75);

        // 初始化手动模式参数
        manualParams.put("method", "bilateral");
        manualParams.put("gaussian_kernel_size", 5);
        manualParams.put("median_kernel_size", 5);
        manualParams.put("bilateral_d", 9);
        manualParams.put("bilateral_sigma_color", 75);
        manualParams.put("bilateral_sigma_space", 75);
    }

    public Mat processAuto(Mat img) {
        String method = (String) autoParams.get("method");

        try {
            if ("gaussian".equals(method)) {
                int kernelSize = (int) autoParams.get("gaussian_kernel_size");
                return applyGaussianBlur(img, kernelSize);
            } else if ("median".equals(method)) {
                int kernelSize = (int) autoParams.get("median_kernel_size");
                return applyMedianBlur(img, kernelSize);
            } else if ("bilateral".equals(method)) {
                int d = (int) autoParams.get("bilateral_d");
                int sigmaColor = (int) autoParams.get("bilateral_sigma_color");
                int sigmaSpace = (int) autoParams.get("bilateral_sigma_space");
                return applyBilateralFilter(img, d, sigmaColor, sigmaSpace);
            } else {
                throw new IllegalArgumentException("不支持的去噪方法: " + method);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return img; // 返回原图作为默认值
        }
    }

    public Mat processManual(Mat img) {
        String method = (String) manualParams.get("method");

        try {
            if ("gaussian".equals(method)) {
                int kernelSize = (int) manualParams.get("gaussian_kernel_size");
                return applyGaussianBlur(img, kernelSize);
            } else if ("median".equals(method)) {
                int kernelSize = (int) manualParams.get("median_kernel_size");
                return applyMedianBlur(img, kernelSize);
            } else if ("bilateral".equals(method)) {
                int d = (int) manualParams.get("bilateral_d");
                int sigmaColor = (int) manualParams.get("bilateral_sigma_color");
                int sigmaSpace = (int) manualParams.get("bilateral_sigma_space");
                return applyBilateralFilter(img, d, sigmaColor, sigmaSpace);
            } else {
                throw new IllegalArgumentException("不支持的去噪方法: " + method);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return img; // 返回原图作为默认值
        }
    }

    private Mat applyGaussianBlur(Mat img, int kernelSize) {
        Mat result = new Mat();
        Imgproc.GaussianBlur(img, result, new org.opencv.core.Size(kernelSize, kernelSize), 0);
        return result;
    }

    private Mat applyMedianBlur(Mat img, int kernelSize) {
        Mat result = new Mat();
        Imgproc.medianBlur(img, result, kernelSize);
        return result;
    }

    private Mat applyBilateralFilter(Mat img, double d, double sigmaColor, double sigmaSpace) {
        Mat result = new Mat();
        Imgproc.bilateralFilter(img, result, (int) d, sigmaColor, sigmaSpace);
        return result;
    }

    public static void main(String[] args) {
        // 加载 OpenCV 库
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

        // 示例：加载图像并处理
        Mat img = org.opencv.imgcodecs.Imgcodecs.imread("path/to/image.jpg");
        NoiseProcessor processor = new NoiseProcessor();

        // 自动模式
        Mat autoResult = processor.processAuto(img);
        org.opencv.imgcodecs.Imgcodecs.imwrite("path/to/auto_result.jpg", autoResult);

        // 手动模式
        processor.manualParams.put("method", "median"); // 修改手动模式参数
        Mat manualResult = processor.processManual(img);
        org.opencv.imgcodecs.Imgcodecs.imwrite("path/to/manual_result.jpg", manualResult);
    }
}