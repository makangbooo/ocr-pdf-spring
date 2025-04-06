package com.xjus.ocrpdfspring.utils.pcoressUtils;

import lombok.Data;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.*;

@Data
public abstract class BaseImageProcessor {

    // 处理器名称
    protected String processorName;

    // 是否使用自动模式
    protected boolean isAutoMode = true;

    public BaseImageProcessor(String processorName) {
        this.processorName = processorName;
    }

    /**
     * 处理图像，根据当前模式选择自动或手动处理
     *
     * @param img 输入图像（OpenCV 格式）
     * @return 处理后的图像和处理是否成功的标志
     */
    public Mat process(Mat img) {
        if (isAutoMode) {
            return processAuto(img);
        } else {
            return processManual(img);
        }
    }

    /**
     * 自动处理图像（子类必须实现）
     *
     * @param img 输入图像（OpenCV 格式）
     * @return 处理后的图像
     */
    public abstract Mat processAuto(Mat img);

    /**
     * 手动处理图像（子类必须实现）
     *
     * @param img 输入图像（OpenCV 格式）
     * @return 处理后的图像
     */
    public abstract Mat processManual(Mat img);

    /**
     * 设置处理模式
     *
     * @param isAuto 是否使用自动模式
     */
    public void setAutoMode(boolean isAuto) {
        this.isAutoMode = isAuto;
    }

    /**
     * 处理图像文件
     *
     * @param inputPath 输入图像路径
     * @param outputDir 输出目录路径，如果为 null 则使用输入文件所在目录下的 processed 文件夹
     * @return 处理后图像的保存路径
     */
    public String processFile(String inputPath, String outputDir) {
        try {
            // 确定输出目录
            if (outputDir == null) {
                outputDir = new File(inputPath).getParent() + "/processed";
            }

            // 确保输出目录存在
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs();
            }

            // 生成输出文件名
            String inputFilename = new File(inputPath).getName();
            String name = inputFilename.substring(0, inputFilename.lastIndexOf('.'));
            String ext = inputFilename.substring(inputFilename.lastIndexOf('.'));
            String stepPrefix = "";
            if (this instanceof StepNumberedProcessor) {
                int stepNum = ((StepNumberedProcessor) this).getStepNum();
                stepPrefix = "step" + stepNum + "_";
            }

            // 使用英文处理器名称
            Map<String, String> processorNameMap = new HashMap<>();
            processorNameMap.put("透视校正", "perspective");
            processorNameMap.put("去噪", "denoise");
            processorNameMap.put("白平衡", "white_balance");
            processorNameMap.put("边缘抹白", "edge_whitening");
            processorNameMap.put("裁剪", "crop");
            processorNameMap.put("锐化", "sharpen");
            processorNameMap.put("亮度对比度", "brightness_contrast");
            processorNameMap.put("彩色平衡", "color_balance");
            processorNameMap.put("二值化", "binarize");

            String engProcessorName = processorNameMap.getOrDefault(processorName, processorName);
            String outputFilename = name + "_" + stepPrefix + engProcessorName + ext;
            String outputPath = outputDir + "/" + outputFilename;

            // 读取图像
            Mat img = Imgcodecs.imread(inputPath);
            if (img.empty()) {
                throw new IllegalArgumentException("无法读取图像: " + inputPath);
            }

            // 处理图像
            Mat result = process(img);

            // 保存结果
            Imgcodecs.imwrite(outputPath, result);
            return outputPath;
        } catch (Exception e) {
            System.err.println(processorName + "处理失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 处理目录中的所有图像文件
     *
     * @param inputDir 输入目录路径
     * @param outputDir 输出目录路径，如果为 null 则使用输入目录下的 processed 文件夹
     * @return 处理后图像的保存路径列表
     */
    public List<String> processDirectory(String inputDir, String outputDir) {
        try {
            // 确定输出目录
            if (outputDir == null) {
                outputDir = inputDir + "/processed";
            }

            // 确保输出目录存在
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs();
            }

            // 获取所有图像文件
            File[] imageFiles = new File(inputDir).listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp)$"));
            if (imageFiles == null || imageFiles.length == 0) {
                throw new IllegalArgumentException("未找到任何图像文件: " + inputDir);
            }

            // 处理所有图像
            List<String> outputPaths = new ArrayList<>();
            for (File imageFile : imageFiles) {
                String outputPath = processFile(imageFile.getAbsolutePath(), outputDir);
                if (outputPath != null) {
                    outputPaths.add(outputPath);
                }
            }
            return outputPaths;
        } catch (Exception e) {
            System.err.println(processorName + "目录处理失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取处理器名称
     *
     * @return 处理器名称
     */
    public String getProcessorName() {
        return processorName;
    }

    /**
     * 获取自动模式参数（如果有）
     *
     * @return 参数映射
     */
    public Map<String, Object> getAutoParams() {
        return new HashMap<>();
    }

    /**
     * 获取手动模式参数（如果有）
     *
     * @return 参数映射
     */
    public Map<String, Object> getManualParams() {
        return new HashMap<>();
    }

    /**
     * 设置自动模式参数
     *
     * @param params 参数映射
     */
    public void setAutoParams(Map<String, Object> params) {
        // 子类需要实现具体逻辑
    }

    /**
     * 设置手动模式参数
     *
     * @param params 参数映射
     */
    public void setManualParams(Map<String, Object> params) {
        // 子类需要实现具体逻辑
    }
}

/**
 * 如果需要支持步骤编号，可以实现此接口
 */
interface StepNumberedProcessor {
    int getStepNum();
}