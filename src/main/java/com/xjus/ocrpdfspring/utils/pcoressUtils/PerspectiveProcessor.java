package com.xjus.ocrpdfspring.utils.pcoressUtils;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class PerspectiveProcessor extends BaseImageProcessor{

    // 自动模式参数
    private static final int GAUSSIAN_KERNEL_SIZE = 5;
    private static final double GAUSSIAN_SIGMA = 0;
    private static final int ADAPTIVE_BLOCK_SIZE = 11;
    private static final int ADAPTIVE_C = 2;
    private static final int CANNY_LOW = 50;
    private static final int CANNY_HIGH = 150;
    private static final double MIN_CONTOUR_AREA = 1000;
    private static final double MIN_RECT_RATIO = 0.7;

    // 手动模式参数
    private List<Point> manualPoints = new ArrayList<>();

    public PerspectiveProcessor(String processorName) {
        super(processorName);
    }

    public Mat processAuto(Mat img) {
        // 转换为灰度图
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        // 应用高斯模糊
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(GAUSSIAN_KERNEL_SIZE, GAUSSIAN_KERNEL_SIZE), GAUSSIAN_SIGMA);

        // 应用自适应阈值处理
        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(blurred, binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, ADAPTIVE_BLOCK_SIZE, ADAPTIVE_C);

        // 边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(binary, edges, CANNY_LOW, CANNY_HIGH);

        // 寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            System.out.println("未检测到文档轮廓，返回原图");
            return img;
        }

        // 筛选合适的轮廓
        List<MatOfPoint> validContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area < MIN_CONTOUR_AREA) continue;

            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double peri = Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approxCurve, 0.02 * peri, true);

            if (approxCurve.toArray().length == 4) {
                RotatedRect rect = Imgproc.minAreaRect(contour2f);
                double rectArea = rect.size.width * rect.size.height;
                double rectRatio = area / (rectArea > 0 ? rectArea : 1);

                if (rectRatio >= MIN_RECT_RATIO) {
                    validContours.add(contour);
                }
            }
        }

        if (validContours.isEmpty()) {
            System.out.println("未找到合适的文档轮廓，返回原图");
            return img;
        }

        // 在有效轮廓中找到最大的
        MatOfPoint largestContour = validContours.stream()
                .max((c1, c2) -> Double.compare(Imgproc.contourArea(c1), Imgproc.contourArea(c2)))
                .orElse(null);

        if (largestContour == null) {
            return img;
        }

        // 获取最小外接矩形
        MatOfPoint2f largestContour2f = new MatOfPoint2f(largestContour.toArray());
        RotatedRect rect = Imgproc.minAreaRect(largestContour2f);
        Point[] boxPoints = new Point[4];
        rect.points(boxPoints);

        // 应用透视变换
        MatOfPoint2f srcPoints = new MatOfPoint2f(boxPoints);
        return applyPerspectiveTransform(img, srcPoints);
    }

    public Mat processManual(Mat img) {
        // 创建窗口并设置鼠标回调
        String windowName = "手动透视校正";
        HighGui.namedWindow(windowName);

        Mat imgCopy = img.clone();
        HighGui.imshow(windowName, imgCopy);

        while (true) {
            imgCopy = img.clone();
            for (int i = 0; i < manualPoints.size(); i++) {
                Point pt = manualPoints.get(i);
                Imgproc.circle(imgCopy, pt, 5, new Scalar(0, 255, 0), -1);
                Imgproc.putText(imgCopy, String.valueOf(i + 1), new Point(pt.x + 10, pt.y + 10),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 0), 2);
            }
            if (manualPoints.size() > 1) {
                for (int i = 0; i < manualPoints.size() - 1; i++) {
                    Imgproc.line(imgCopy, manualPoints.get(i), manualPoints.get(i + 1), new Scalar(0, 255, 0), 2);
                }
                if (manualPoints.size() == 4) {
                    Imgproc.line(imgCopy, manualPoints.get(3), manualPoints.get(0), new Scalar(0, 255, 0), 2);
                }
            }
            HighGui.imshow(windowName, imgCopy);

            char key = (char) HighGui.waitKey(0);
            if (key == 'r') {
                manualPoints.clear();
            } else if (key == 'q') {
                HighGui.destroyAllWindows();
                return img;
            } else if (key == '\n' && manualPoints.size() == 4) {
                break;
            }
        }

        HighGui.destroyAllWindows();

        // 将手动点转换为 MatOfPoint2f
        MatOfPoint2f srcPoints = new MatOfPoint2f();
        srcPoints.fromList(manualPoints);

        return applyPerspectiveTransform(img, srcPoints);
    }

    private Mat applyPerspectiveTransform(Mat img, MatOfPoint2f srcPoints) {
        // 对点进行排序
        Point[] pts = srcPoints.toArray();
        Point[] rect = new Point[4];

        double[] s = new double[pts.length];
        for (int i = 0; i < pts.length; i++) {
            s[i] = pts[i].x + pts[i].y;
        }
        rect[0] = pts[minIndex(s)]; // 左上
        rect[2] = pts[maxIndex(s)]; // 右下

        double[] diff = new double[pts.length];
        for (int i = 0; i < pts.length; i++) {
            diff[i] = pts[i].x - pts[i].y;
        }
        rect[1] = pts[minIndex(diff)]; // 右上
        rect[3] = pts[maxIndex(diff)]; // 左下

        // 计算新图像的宽度和高度
        double widthA = Math.sqrt(Math.pow(rect[2].x - rect[3].x, 2) + Math.pow(rect[2].y - rect[3].y, 2));
        double widthB = Math.sqrt(Math.pow(rect[1].x - rect[0].x, 2) + Math.pow(rect[1].y - rect[0].y, 2));
        int maxWidth = (int) Math.max(widthA, widthB);

        double heightA = Math.sqrt(Math.pow(rect[1].x - rect[2].x, 2) + Math.pow(rect[1].y - rect[2].y, 2));
        double heightB = Math.sqrt(Math.pow(rect[0].x - rect[3].x, 2) + Math.pow(rect[0].y - rect[3].y, 2));
        int maxHeight = (int) Math.max(heightA, heightB);

        // 设置目标点
        MatOfPoint2f dstPoints = new MatOfPoint2f(
                new Point(0, 0),
                new Point(maxWidth - 1, 0),
                new Point(maxWidth - 1, maxHeight - 1),
                new Point(0, maxHeight - 1)
        );

        // 计算透视变换矩阵并应用
        Mat perspectiveMatrix = Imgproc.getPerspectiveTransform(new MatOfPoint2f(rect), dstPoints);
        Mat result = new Mat();
        Imgproc.warpPerspective(img, result, perspectiveMatrix, new Size(maxWidth, maxHeight));

        return result;
    }

    private int minIndex(double[] arr) {
        int minIdx = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[minIdx]) minIdx = i;
        }
        return minIdx;
    }

    private int maxIndex(double[] arr) {
        int maxIdx = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[maxIdx]) maxIdx = i;
        }
        return maxIdx;
    }

    public static void main(String[] args) {
        // 加载 OpenCV 库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 示例：加载图像并处理
        Mat img = Imgcodecs.imread("path/to/image.jpg");
        PerspectiveProcessor processor = new PerspectiveProcessor(null);

        // 自动模式
        Mat autoResult = processor.processAuto(img);
        Imgcodecs.imwrite("path/to/auto_result.jpg", autoResult);

        // 手动模式
        Mat manualResult = processor.processManual(img);
        Imgcodecs.imwrite("path/to/manual_result.jpg", manualResult);
    }
}