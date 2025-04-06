package com.xjus.ocrpdfspring.utils.pcoressUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.*;

public class ImageProcessorApp {

    // 初始化所有处理器
    Map<String, Object> processors = new HashMap<>();
    private List<String> processFlow = Arrays.asList(
            "perspective",
            "noise",
            "white_balance",
            "brightness_contrast",
            "edge_whitening",
            "binarize"
    );

    public ImageProcessorApp() {
        // 初始化处理器实例
        processors.put("perspective", new PerspectiveProcessor(null));
        processors.put("noise", new NoiseProcessor());
        processors.put("white_balance", new WhiteBalanceProcessor());
        processors.put("edge_whitening", new EdgeWhiteningProcessor(null));
        processors.put("brightness_contrast", new BrightnessContrastProcessor(null));
        processors.put("binarize", new BinarizeProcessor(null));

        // 设置各处理器的最小处理强度
        ((NoiseProcessor) processors.get("noise")).setAutoParams(Map.of(
                "bilateral_d", 5,
                "bilateral_sigma_color", 10,
                "bilateral_sigma_space", 10
        ));
        ((WhiteBalanceProcessor) processors.get("white_balance")).setAutoParams(Map.of(
                "method", "simple"
        ));
        ((EdgeWhiteningProcessor) processors.get("edge_whitening")).setAutoParams(Map.of(
                "border_size", 5,
                "feather_size", 3
        ));
        ((BrightnessContrastProcessor) processors.get("brightness_contrast")).setAutoParams(Map.of(
                "clip_limit", 1.5,
                "tile_grid_size", new Size(4, 4)
        ));
        ((BinarizeProcessor) processors.get("binarize")).setAutoParams(Map.of(
                "adaptive", true,
                "block_size", 15,
                "c", 5
        ));
    }

    public String processSingleStep(String inputPath, String processorName, String outputDir, boolean isAuto, Map<String, Object> params) {
        // 检查处理器是否存在
        if (!processors.containsKey(processorName)) {
            throw new IllegalArgumentException("不支持的处理器: " + processorName);
        }

        // 获取处理器
        BaseImageProcessor processor = (BaseImageProcessor) processors.get(processorName);
        processor.setProcessorName(processorName);
        
        
        // 设置处理模式
        processor.setAutoMode(isAuto);

        // 设置处理参数（如果有）
        if (!isAuto && params != null) {
            processor.setManualParams(params);
        }

        // 处理图像或目录
        File inputFile = new File(inputPath);
        if (inputFile.isDirectory()) {
            return processor.processDirectory(inputPath, outputDir).toString();
        } else {
            return processor.processFile(inputPath, outputDir);
        }
    }

    public List<String> processComplete(String inputPath, String outputDir, boolean isAuto) {
        // 确定输出目录
        if (outputDir == null) {
            File inputFile = new File(inputPath);
            if (inputFile.isDirectory()) {
                outputDir = new File(inputFile, "processed").getPath();
            } else {
                outputDir = new File(inputFile.getParentFile(), "processed").getPath();
            }
        }

        // 确保输出目录存在
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        // 处理单个文件或目录
        List<String> finalOutputs = new ArrayList<>();
        File inputFile = new File(inputPath);
        if (inputFile.isDirectory()) {
            // 获取所有图像文件
            File[] imageFiles = inputFile.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp)$"));
            if (imageFiles != null) {
                for (File imageFile : imageFiles) {
                    String result = processSingleFile(imageFile.getPath(), outputDir, isAuto);
                    if (result != null) {
                        finalOutputs.add(result);
                    }
                }
            }
        } else {
            // 处理单个文件
            System.out.println(outputDir);
            String result = processSingleFile(inputPath, outputDir, isAuto);
            if (result != null) {
                finalOutputs.add(result);
            }
        }
        return finalOutputs;
    }

    private String processSingleFile(String inputPath, String outputDir, boolean isAuto) {
        String currentInput = inputPath;
        String lastSuccessInput = inputPath;
        outputDir += "\\processed";

        // 按顺序执行处理流程
        for (int stepNum = 1; stepNum <= processFlow.size(); stepNum++) {
            String processorName = processFlow.get(stepNum - 1);
            BaseImageProcessor processor = (BaseImageProcessor) processors.get(processorName);
            processor.setAutoMode(isAuto);

            try {
                // 读取当前输入图像
                Mat img = Imgcodecs.imread(currentInput);
                if (img.empty()) {
                    throw new IllegalArgumentException("无法读取图像: " + currentInput);
                }

                // 直接处理图像
                Mat result = processor.process(img);
                if (result != null) {
                    // 生成输出文件名
                    String inputFilename = new File(inputPath).getName();
                    String name = inputFilename.substring(0, inputFilename.lastIndexOf('.'));
                    String ext = inputFilename.substring(inputFilename.lastIndexOf('.'));
                    String safeProcessorName = processorName.replaceAll("[^\\x00-\\x7F]", "_");
                    String outputFilename = String.format("%s_step%d_%s%s", name, stepNum, safeProcessorName, ext);
                    String outputPath = new File(outputDir , outputFilename).getPath();
                    //System.out.println(outputPath);
                    // 保存结果
                    Imgcodecs.imwrite(outputPath, result);
                    currentInput = outputPath;
                    lastSuccessInput = outputPath;
                } else {
                    System.out.println(processorName + "处理失败，使用上一步结果继续处理");
                    currentInput = lastSuccessInput;
                }
            } catch (Exception e) {
                System.err.println(processorName + "处理出错：" + e.getMessage());
                currentInput = lastSuccessInput;
            }
        }
        return currentInput;
    }

    public static void main(String[] args) {
        // 加载 OpenCV 库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 创建应用实例
        ImageProcessorApp app = new ImageProcessorApp();

        // 示例图像和目录路径（用户需要替换为实际路径）
        String imagePath = "G:\\hp\\Scan_20250316_013023.jpg";
        String imageDir = new File(imagePath).getParent();

        // 示例1：单步处理单个图像（自动模式）
        System.out.println("1. 处理单个图像 - 透视校正（自动模式）...");
        String resultPath = app.processSingleStep(imagePath, "perspective", null, true, null);
        System.out.println("处理结果保存在: " + resultPath);

        // 示例2：单步处理单个图像（手动模式）
        System.out.println("2. 处理单个图像 - 去噪（手动模式）...");
        Map<String, Object> noiseParams = Map.of(
                "method", "bilateral",
                "bilateral_d", 15,
                "bilateral_sigma_color", 80,
                "bilateral_sigma_space", 80
        );
        resultPath = app.processSingleStep(imagePath, "noise", null, false, noiseParams);
        System.out.println("处理结果保存在: " + resultPath);

        // 示例3：完整流程处理单个图像
        System.out.println("3. 完整流程处理单个图像...");
        String finalPath = app.processComplete(imagePath, null, true).get(0);
        System.out.println("最终处理结果保存在: " + finalPath);
    }
}