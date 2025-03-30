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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

public class PDF2PDF {
    public static final String SERVER_URL = "http://1.95.55.32:1224";
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static void pdf2PdfConverter(String inputPdfPath, String outputPdfPath) throws Exception {
        String URL = SERVER_URL + "/api/doc/upload";
        JSONObject options = new JSONObject().put("doc.extractionMode", "mixed");
        File file = new File(outputPdfPath);
        JSONObject resData = uploadFile(URL, file, options);


        if (resData.getInt("code") == 101) {
            String fileName = file.getName();
            String tempName = "temp" + fileName.substring(fileName.lastIndexOf("."));
            System.out.println("[Warning] Upload failed, retrying with temp_name: " + tempName);
            resData = uploadFile(URL, file, options, tempName);
        }

        if (resData.getInt("code") != 100)
            throw new Exception("Task submission failed: " + resData);
        String taskId = resData.getString("data");
        System.out.println("Task ID: " + taskId);

        // 轮询任务状态
        URL = SERVER_URL + "/api/doc/result";
        JSONObject pollData = new JSONObject()
                .put("id", taskId)
                .put("is_data", true)
                .put("format", "text")
                .put("is_unread", true);

        while (true) {
            Thread.sleep(1000);
            resData = postJson(URL, pollData);
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
        URL = SERVER_URL + "/api/doc/download";
        JSONObject downloadOptions = new JSONObject()
                .put("id", taskId)
                .put("file_types", new String[]{"pdfLayered"})
                .put("ingore_blank", false);
        resData = postJson(URL, downloadOptions);
        if (resData.getInt("code") != 100) throw new Exception("Failed to get download URL: " + resData);

        String downloadUrl = resData.getString("data");
        //downloadUrl = downloadUrl.replaceAll("[?|#]", "");
        //System.out.println(downloadUrl.substring(0, 77));
        downloadUrl = downloadUrl.substring(0, 77) + URLEncoder.encode(downloadUrl.substring(77), "UTF-8");
        System.out.println(downloadUrl);
        downloadFile(downloadUrl, outputPdfPath);
        System.out.println("Target file downloaded successfully: " + outputPdfPath);

        // 清理任务
        URL = SERVER_URL + "/api/doc/clear/" + taskId;
        resData = getJson(URL);
        if (resData.getInt("code") != 100) throw new Exception("Task cleanup failed: " + resData);
        System.out.println("Task cleaned up successfully.");

        // 删除临时文件
        //Files.delete(Paths.get(outputPdfPath));
    }

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

    private static JSONObject postJson(String url, JSONObject data) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(data.toString()));
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String responseText = EntityUtils.toString(response.getEntity());
            return new JSONObject(responseText);
        }
    }

    private static JSONObject getJson(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            String responseText = EntityUtils.toString(response.getEntity());
            return new JSONObject(responseText);
        }
    }

    private static void downloadFile(String url, String outputPath) throws IOException {
        HttpGet get = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(get);
             FileOutputStream fos = new FileOutputStream(outputPath)) {
            response.getEntity().writeTo(fos);
        }
    }
}
