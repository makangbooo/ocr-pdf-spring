package com.xjus.ocrpdfspring.utils.pcoressUtils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class EdgeWhiteningProcessor extends BaseImageProcessor{

    // 自动模式参数
    private int autoBorderSize = 20; // 边缘宽度（像素）
    private int autoFeatherSize = 10; // 羽化过渡区域宽度（像素）

    // 手动模式参数
    private int manualBorderSize = 20;
    private int manualFeatherSize = 10;

    public EdgeWhiteningProcessor(String processorName) {
        super(processorName);
    }

    public Mat processAuto(Mat img) {
        return applyEdgeWhitening(img, autoBorderSize, autoFeatherSize);
    }

    public Mat processManual(Mat img) {
        return applyEdgeWhitening(img, manualBorderSize, manualFeatherSize);
    }

    private Mat applyEdgeWhitening(Mat img, int borderSize, int featherSize) {
        int height = img.rows();
        int width = img.cols();

        // 创建白色图像（确保BGR顺序）
        Mat whiteImg = new Mat(img.size(), img.type());
        whiteImg.setTo(new Scalar(255, 255, 255)); // BGR: 255,255,255

        // 创建掩码（单通道浮点型）
        Mat mask = Mat.zeros(height, width, CvType.CV_32F);
        Point topLeft = new Point(borderSize, borderSize);
        Point bottomRight = new Point(width - borderSize, height - borderSize);
        Imgproc.rectangle(mask, topLeft, bottomRight, new Scalar(1.0), -1, Imgproc.LINE_8, 0);

        // 应用高斯模糊并归一化
        if (featherSize > 0) {
            int kernelSize = featherSize * 2 + 1;
            Imgproc.GaussianBlur(mask, mask, new Size(kernelSize, kernelSize), 0);
            Core.normalize(mask, mask, 0.0, 1.0, Core.NORM_MINMAX); // 确保数值范围正确
        }

        // 扩展掩码到三通道（BGR顺序）
        List<Mat> maskChannels = new ArrayList<>();
        maskChannels.add(mask.clone()); // B通道
        maskChannels.add(mask.clone()); // G通道
        maskChannels.add(mask.clone()); // R通道
        Mat mask3ch = new Mat();
        Core.merge(maskChannels, mask3ch);

        // 转换图像为浮点型
        Mat imgFloat = new Mat();
        img.convertTo(imgFloat, CvType.CV_32F);
        Mat whiteFloat = new Mat();
        whiteImg.convertTo(whiteFloat, CvType.CV_32F);

        // 混合计算
        Mat term1 = new Mat();
        Core.multiply(imgFloat, mask3ch, term1); // 原图部分

        Mat ones = new Mat(mask3ch.size(), mask3ch.type(), new Scalar(1.0));
        Mat oneMinusMask = new Mat();
        Core.subtract(ones, mask3ch, oneMinusMask); // 1 - mask

        Mat term2 = new Mat();
        Core.multiply(whiteFloat, oneMinusMask, term2); // 白色部分

        Mat resultFloat = new Mat();
        Core.add(term1, term2, resultFloat); // 合并结果

        // 转换回8UC3类型
        Mat result = new Mat();
        resultFloat.convertTo(result, CvType.CV_8U);

        return result;
    }

    public static void main(String[] args) {
        // 加载 OpenCV 库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 示例：加载图像并处理
        Mat img = Imgcodecs.imread("path/to/image.jpg");
        EdgeWhiteningProcessor processor = new EdgeWhiteningProcessor(null);

        // 自动模式
        Mat autoResult = processor.processAuto(img);
        Imgcodecs.imwrite("path/to/auto_result.jpg", autoResult);

        // 手动模式
        processor.manualBorderSize = 30; // 修改手动模式参数
        processor.manualFeatherSize = 15;
        Mat manualResult = processor.processManual(img);
        Imgcodecs.imwrite("path/to/manual_result.jpg", manualResult);
    }
}