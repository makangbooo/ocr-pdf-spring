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
    temp_pdf = output_pdf_path
    convert_to_pdf(image_paths, temp_pdf)
    print(f"已转换 {len(image_paths)} 张图片为临时 PDF: {temp_pdf}")


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python script.py <image_paths> <output_pdf_path>")
        sys.exit(1)

    image_paths = sys.argv[1].split(",")  # 假设图片路径以逗号分隔
    output_pdf_path = sys.argv[2]
    print("{image_paths}",image_paths)
    print("{output_pdf_path}",output_pdf_path)
    process_images_to_pdf(image_paths, output_pdf_path)
