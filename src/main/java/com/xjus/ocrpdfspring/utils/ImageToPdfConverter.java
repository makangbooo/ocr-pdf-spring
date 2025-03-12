package com.xjus.ocrpdfspring.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class ImageToPdfConverter {

    private static final String SERVER_URL = "http://1.95.55.32:1224";
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    // 生成随机文件名
    public static String generateRandomFilename(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.append(".pdf").toString();
    }

    // 模拟将图片转换为 PDF（需要外部库支持，这里仅作占位）
    public static void convertToPdf(String[] imagePaths, String pdfPath) throws IOException {
        // 假设使用第三方库完成转换，此处仅创建空文件模拟
        Files.write(Paths.get(pdfPath), new byte[0]);
    }

    // 处理图片到双层 PDF
    public static void processImagesToPdf(String[] imagePaths, String outputPdfPath) throws Exception {
        String tempPdf = outputPdfPath + "_temp.pdf";
        convertToPdf(imagePaths, tempPdf);
        System.out.println("Converted " + imagePaths.length + " images to temp PDF: " + tempPdf);

        // 上传到服务器
        String url = SERVER_URL + "/api/doc/upload";
        JSONObject options = new JSONObject().put("doc.extractionMode", "mixed");
        File file = new File(tempPdf);
        JSONObject resData = uploadFile(url, file, options);

        if (resData.getInt("code") == 101) {
            String fileName = file.getName();
            String tempName = "temp" + fileName.substring(fileName.lastIndexOf("."));
            System.out.println("[Warning] Upload failed, retrying with temp_name: " + tempName);
            resData = uploadFile(url, file, options, tempName);
        }

        if (resData.getInt("code") != 100) throw new Exception("Task submission failed: " + resData);
        String taskId = resData.getString("data");
        System.out.println("Task ID: " + taskId);

        // 轮询任务状态
        url = SERVER_URL + "/api/doc/result";
        JSONObject pollData = new JSONObject()
                .put("id", taskId)
                .put("is_data", true)
                .put("format", "text")
                .put("is_unread", true);

        while (true) {
            Thread.sleep(1000);
            resData = postJson(url, pollData);
            if (resData.getInt("code") != 100) throw new Exception("Failed to get task status: " + resData);
            System.out.println("Progress: " + resData.getInt("processed_count") + "/" + resData.getInt("pages_count"));
            if (resData.getBoolean("is_done")) {
                if (!"success".equals(resData.getString("state"))) {
                    throw new Exception("Task execution failed: " + resData.getString("message"));
                }
                System.out.println("OCR task completed.");
                break;
            }
        }

        // 下载双层 PDF
        url = SERVER_URL + "/api/doc/download";
        JSONObject downloadOptions = new JSONObject()
                .put("id", taskId)
                .put("file_types", new String[]{"pdfLayered"})
                .put("ingore_blank", false);
        resData = postJson(url, downloadOptions);
        if (resData.getInt("code") != 100) throw new Exception("Failed to get download URL: " + resData);

        String downloadUrl = resData.getString("data");
        downloadFile(downloadUrl, outputPdfPath);
        System.out.println("Target file downloaded successfully: " + outputPdfPath);

        // 清理任务
        url = SERVER_URL + "/api/doc/clear/" + taskId;
        resData = getJson(url);
        if (resData.getInt("code") != 100) throw new Exception("Task cleanup failed: " + resData);
        System.out.println("Task cleaned up successfully.");

        // 删除临时文件
        Files.delete(Paths.get(tempPdf));
    }

    // 上传文件辅助方法
    private static JSONObject uploadFile(String url, File file, JSONObject options) throws IOException {
        return uploadFile(url, file, options, file.getName());
    }

    private static JSONObject uploadFile(String url, File file, JSONObject options, String fileName) throws IOException {
        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, fileName)
                .addTextBody("json", options.toString(), ContentType.APPLICATION_JSON);
        post.setEntity(builder.build());

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String responseText = EntityUtils.toString(response.getEntity());
            return new JSONObject(responseText);
        }
    }

    // POST JSON 请求
    private static JSONObject postJson(String url, JSONObject data) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(data.toString()));
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String responseText = EntityUtils.toString(response.getEntity());
            return new JSONObject(responseText);
        }
    }

    // GET 请求
    private static JSONObject getJson(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            String responseText = EntityUtils.toString(response.getEntity());
            return new JSONObject(responseText);
        }
    }

    // 下载文件
    private static void downloadFile(String url, String outputPath) throws IOException {
        HttpGet get = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(get);
             FileOutputStream fos = new FileOutputStream(outputPath)) {
            response.getEntity().writeTo(fos);
        }
    }

    public static void main(String[] args) throws Exception {

        String[] imagePaths = "/Users/makangbo/mine/code/ocr_project/ocr-pdf-spring/filesTemp/uploadTemp/27.png,./Users/makangbo/mine/code/ocr_project/ocr-pdf-spring/filesTemp/uploadTemp/29.png".split(",");
        String outputPdfPath = "./filesTemp/uploadTemp/output.pdf";
        processImagesToPdf(imagePaths, outputPdfPath);
    }
}