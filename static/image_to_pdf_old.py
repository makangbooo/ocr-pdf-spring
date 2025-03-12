import json
import time
import requests
import img2pdf
import os
import random
import string
import sys

def generate_random_filename(length=18):
    random_str = ''.join(random.choices(string.ascii_letters + string.digits, k=length))
    return random_str + '.pdf'

def convert_to_pdf(image_paths, pdf_path):
    with open(pdf_path, "wb") as f:
        f.write(img2pdf.convert(image_paths))

# def process_images_to_pdf(image_paths, output_pdf_path, server_url="http://1.117.68.241:1224"):
def process_images_to_pdf(image_paths, output_pdf_path, server_url="http://1.95.55.32:1224"):
    # 创建 PDF
    temp_pdf = output_pdf_path + "_temp.pdf"
    convert_to_pdf(image_paths, temp_pdf)
    print(f"已转换 {len(image_paths)} 张图片为临时 PDF: {temp_pdf}")

    # 上传到服务器获取双层 PDF
    url = f"{server_url}/api/doc/upload"
    options_json = json.dumps({"doc.extractionMode": "mixed"})

    with open(temp_pdf, "rb") as file:
        response = requests.post(url, files={"file": file}, data={"json": options_json})
    response.raise_for_status()
    res_data = json.loads(response.text)

    if res_data["code"] == 101:
        file_name = os.path.basename(temp_pdf)
        file_prefix, file_suffix = os.path.splitext(file_name)
        temp_name = "temp" + file_suffix
        print(f"[Warning] Upload failed, retrying with temp_name: {temp_name}")
        with open(temp_pdf, "rb") as file:
            response = requests.post(url, files={"file": (temp_name, file)}, data={"json": options_json})
        response.raise_for_status()
        res_data = json.loads(response.text)

    assert res_data["code"] == 100, f"Task submission failed: {res_data}"
    task_id = res_data["data"]
    print(f"Task ID: {task_id}")

    # 轮询任务状态
    url = f"{server_url}/api/doc/result"
    headers = {"Content-Type": "application/json"}
    data_str = json.dumps({"id": task_id, "is_data": True, "format": "text", "is_unread": True})

    while True:
        time.sleep(1)
        response = requests.post(url, data=data_str, headers=headers)
        res_data = json.loads(response.text)
        assert res_data["code"] == 100, f"Failed to get task status: {res_data}"
        print(f"Progress: {res_data['processed_count']}/{res_data['pages_count']}")
        if res_data["is_done"]:
            assert res_data["state"] == "success", f"Task execution failed: {res_data['message']}"
            print("OCR task completed.")
            break

    # 下载双层 PDF
    url = f"{server_url}/api/doc/download"
    download_options = {"id": task_id, "file_types": ["pdfLayered"], "ingore_blank": False}
    response = requests.post(url, data=json.dumps(download_options), headers=headers)
    res_data = json.loads(response.text)
    assert res_data["code"] == 100, f"Failed to get download URL: {res_data}"

    download_url = res_data["data"]


    with open(output_pdf_path, "wb") as file:
        response = requests.get(download_url, stream=True)
        for chunk in response.iter_content(chunk_size=8192):
            if chunk:
                file.write(chunk)
    print(f"Target file downloaded successfully: {output_pdf_path}")

    # 清理任务
    url = f"{server_url}/api/doc/clear/{task_id}"
    response = requests.get(url)
    res_data = json.loads(response.text)
    assert res_data["code"] == 100, f"Task cleanup failed: {res_data}"
    print("Task cleaned up successfully.")

    # 删除临时文件
    os.remove(temp_pdf)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python script.py <image_paths> <output_pdf_path>")
        sys.exit(1)

    image_paths = sys.argv[1].split(",")  # 假设图片路径以逗号分隔
    output_pdf_path = sys.argv[2]
    process_images_to_pdf(image_paths, output_pdf_path)