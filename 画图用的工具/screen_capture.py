import os
import sys
import subprocess
import tkinter as tk
from tkinter import filedialog, messagebox
from PIL import Image, ImageTk
import cv2
import numpy as np

class ScreenCaptureApp:
    def __init__(self, root):
        self.root = root
        self.root.title("屏幕截图区域选择工具")
        self.root.geometry("1200x800")
        
        # 变量初始化
        self.source_image = None
        self.temp_image = None
        self.selection_start = None
        self.selection_end = None
        self.is_selecting = False
        self.canvas = None
        self.photo = None
        
        self.setup_ui()
    
    def setup_ui(self):
        # 主框架
        main_frame = tk.Frame(self.root)
        main_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # 顶部控制区域
        control_frame = tk.Frame(main_frame)
        control_frame.pack(fill=tk.X, pady=(0, 10))
        
        # 路径输入
        path_frame = tk.Frame(control_frame)
        path_frame.pack(fill=tk.X, pady=5)
        
        tk.Label(path_frame, text="保存路径:").pack(side=tk.LEFT)
        self.path_var = tk.StringVar()
        self.path_entry = tk.Entry(path_frame, textvariable=self.path_var, width=50)
        self.path_entry.pack(side=tk.LEFT, padx=(5, 5))
        
        tk.Button(path_frame, text="选择路径", command=self.select_path).pack(side=tk.LEFT, padx=(0, 5))
        tk.Button(path_frame, text="验证路径", command=self.validate_path).pack(side=tk.LEFT)
        
        # 按钮区域
        button_frame = tk.Frame(control_frame)
        button_frame.pack(fill=tk.X, pady=5)
        
        tk.Button(button_frame, text="ADB截图", command=self.capture_screen).pack(side=tk.LEFT, padx=(0, 10))
        tk.Button(button_frame, text="重新截图", command=self.recapture_screen).pack(side=tk.LEFT, padx=(0, 10))
        tk.Button(button_frame, text="保存图像", command=self.save_images).pack(side=tk.LEFT, padx=(0, 10))
        tk.Button(button_frame, text="清除选择", command=self.clear_selection).pack(side=tk.LEFT)
        
        # 状态标签
        self.status_label = tk.Label(control_frame, text="请输入或选择保存路径，支持自动创建目录和循环使用", fg="blue")
        self.status_label.pack(pady=5)
        
        # 图像显示区域
        self.canvas_frame = tk.Frame(main_frame, bg="white")
        self.canvas_frame.pack(fill=tk.BOTH, expand=True)
        
        # 创建画布
        self.canvas = tk.Canvas(self.canvas_frame, bg="white")
        self.canvas.pack(fill=tk.BOTH, expand=True)
        
        # 绑定鼠标事件
        self.canvas.bind("<Button-1>", self.on_mouse_down)
        self.canvas.bind("<B1-Motion>", self.on_mouse_drag)
        self.canvas.bind("<ButtonRelease-1>", self.on_mouse_up)
    
    def select_path(self):
        """选择保存路径"""
        path = filedialog.askdirectory(title="选择保存路径")
        if path:
            self.path_var.set(path)
            self.status_label.config(text=f"已选择路径: {path}", fg="green")
    
    def ensure_directory_exists(self, path):
        """确保目录存在，如果不存在则创建"""
        if not os.path.exists(path):
            try:
                os.makedirs(path)
                return True
            except Exception as e:
                messagebox.showerror("错误", f"创建目录失败: {str(e)}")
                return False
        return True
    
    def validate_path(self):
        """验证并创建路径"""
        path = self.path_var.get().strip()
        if not path:
            messagebox.showerror("错误", "请输入保存路径")
            return
        
        if self.ensure_directory_exists(path):
            self.status_label.config(text=f"路径验证成功: {path}", fg="green")
        else:
            self.status_label.config(text="路径验证失败", fg="red")
    
    def recapture_screen(self):
        """重新截图（清除当前图像和选择）"""
        # 清除当前图像和选择
        self.clear_selection()
        self.source_image = None
        self.canvas.delete("all")
        self.status_label.config(text="已清除当前图像，请点击ADB截图", fg="blue")
        
        # 执行截图
        self.capture_screen()
    
    def capture_screen(self):
        """使用ADB截图"""
        if not self.path_var.get():
            messagebox.showerror("错误", "请先选择保存路径")
            return
        
        # 确保目录存在
        if not self.ensure_directory_exists(self.path_var.get()):
            return
        
        try:
            self.status_label.config(text="正在截图...", fg="orange")
            self.root.update()
            
            # 执行ADB截图命令
            result = subprocess.run(
                ["adb", "exec-out", "screencap", "-p"],
                capture_output=True,
                text=False
            )
            
            if result.returncode != 0:
                raise Exception("ADB截图失败")
            
            # 保存截图到临时文件
            temp_path = os.path.join(self.path_var.get(), "temp_screenshot.png")
            with open(temp_path, "wb") as f:
                f.write(result.stdout)
            
            # 加载图像
            self.source_image = cv2.imread(temp_path)
            if self.source_image is None:
                raise Exception("无法加载截图")
            
            # 获取图像尺寸
            height, width = self.source_image.shape[:2]
            self.image_size = (width, height)
            
            # 显示图像
            self.display_image()
            
            self.status_label.config(text=f"截图成功! 尺寸: {width}x{height} - 请选择区域后保存", fg="green")
            
            # 删除临时文件
            os.remove(temp_path)
            
        except Exception as e:
            messagebox.showerror("错误", f"截图失败: {str(e)}")
            self.status_label.config(text="截图失败", fg="red")
    
    def display_image(self):
        """显示图像到画布"""
        if self.source_image is None:
            return
        
        # 转换为RGB格式
        rgb_image = cv2.cvtColor(self.source_image, cv2.COLOR_BGR2RGB)
        pil_image = Image.fromarray(rgb_image)
        
        # 获取画布尺寸
        canvas_width = self.canvas.winfo_width()
        canvas_height = self.canvas.winfo_height()
        
        if canvas_width <= 1 or canvas_height <= 1:
            # 如果画布还未初始化，使用默认尺寸
            canvas_width = 800
            canvas_height = 600
        
        # 计算缩放比例
        img_width, img_height = pil_image.size
        scale_x = canvas_width / img_width
        scale_y = canvas_height / img_height
        scale = min(scale_x, scale_y)
        
        # 缩放图像
        new_width = int(img_width * scale)
        new_height = int(img_height * scale)
        pil_image = pil_image.resize((new_width, new_height), Image.Resampling.LANCZOS)
        
        # 转换为PhotoImage
        self.photo = ImageTk.PhotoImage(pil_image)
        
        # 清除画布并显示图像
        self.canvas.delete("all")
        self.canvas.create_image(
            canvas_width // 2, canvas_height // 2,
            image=self.photo, anchor=tk.CENTER
        )
        
        # 保存缩放信息
        self.scale_factor = scale
        self.canvas_offset_x = (canvas_width - new_width) // 2
        self.canvas_offset_y = (canvas_height - new_height) // 2
    
    def on_mouse_down(self, event):
        """鼠标按下事件"""
        self.selection_start = (event.x, event.y)
        self.is_selecting = True
        self.selection_rect = None
    
    def on_mouse_drag(self, event):
        """鼠标拖拽事件"""
        if not self.is_selecting:
            return
        
        # 删除之前的选择框
        if self.selection_rect:
            self.canvas.delete(self.selection_rect)
        
        # 绘制新的选择框
        x1, y1 = self.selection_start
        x2, y2 = event.x, event.y
        self.selection_rect = self.canvas.create_rectangle(
            x1, y1, x2, y2,
            outline="red", width=2
        )
    
    def on_mouse_up(self, event):
        """鼠标释放事件"""
        if not self.is_selecting:
            return
        
        self.is_selecting = False
        self.selection_end = (event.x, event.y)
        
        # 计算选择区域
        x1, y1 = self.selection_start
        x2, y2 = self.selection_end
        
        # 确保坐标顺序正确
        x1, x2 = min(x1, x2), max(x1, x2)
        y1, y2 = min(y1, y2), max(y1, y2)
        
        # 转换为图像坐标
        img_x1 = int((x1 - self.canvas_offset_x) / self.scale_factor)
        img_y1 = int((y1 - self.canvas_offset_y) / self.scale_factor)
        img_x2 = int((x2 - self.canvas_offset_x) / self.scale_factor)
        img_y2 = int((y2 - self.canvas_offset_y) / self.scale_factor)
        
        # 确保坐标在图像范围内
        img_x1 = max(0, img_x1)
        img_y1 = max(0, img_y1)
        img_x2 = min(self.image_size[0], img_x2)
        img_y2 = min(self.image_size[1], img_y2)
        
        self.selection_coords = (img_x1, img_y1, img_x2, img_y2)
        
        # 显示选择区域信息
        width = img_x2 - img_x1
        height = img_y2 - img_y1
        self.status_label.config(
            text=f"已选择区域: ({img_x1}, {img_y1}) 到 ({img_x2}, {img_y2}), 尺寸: {width}x{height}",
            fg="green"
        )
    
    def clear_selection(self):
        """清除选择"""
        if hasattr(self, 'selection_rect') and self.selection_rect:
            self.canvas.delete(self.selection_rect)
            self.selection_rect = None
        self.selection_coords = None
        self.selection_start = None
        self.selection_end = None
        self.is_selecting = False
        self.status_label.config(text="选择已清除", fg="blue")
    
    def save_images(self):
        """保存图像"""
        if self.source_image is None:
            messagebox.showerror("错误", "没有可保存的图像")
            return
        
        if not hasattr(self, 'selection_coords') or self.selection_coords is None:
            messagebox.showerror("错误", "请先选择区域")
            return
        
        if not self.path_var.get():
            messagebox.showerror("错误", "请选择保存路径")
            return
        
        # 确保目录存在
        if not self.ensure_directory_exists(self.path_var.get()):
            return
        
        try:
            # 获取图像尺寸
            width, height = self.image_size
            
            # 保存源图像
            source_filename = f"{width}_{height}_source.png"
            source_path = os.path.join(self.path_var.get(), source_filename)
            cv2.imwrite(source_path, self.source_image)
            
            # 裁剪并保存模板图像
            x1, y1, x2, y2 = self.selection_coords
            temp_image = self.source_image[y1:y2, x1:x2]
            temp_width, temp_height = x2 - x1, y2 - y1
            temp_filename = f"{width}_{height}_template.png"
            temp_path = os.path.join(self.path_var.get(), temp_filename)
            cv2.imwrite(temp_path, temp_image)
            
            messagebox.showinfo("成功", f"图像已保存:\n源图像: {source_filename}\n模板图像: {temp_filename}")
            self.status_label.config(text="图像保存成功! 可以继续截图或选择新区域", fg="green")
            
            # 清除选择，准备下一次操作
            self.clear_selection()
            
        except Exception as e:
            messagebox.showerror("错误", f"保存失败: {str(e)}")
            self.status_label.config(text="保存失败", fg="red")

def main():
    # 检查ADB是否可用
    try:
        result = subprocess.run(["adb", "version"], capture_output=True, text=True)
        if result.returncode != 0:
            messagebox.showerror("错误", "ADB未安装或未配置到PATH中")
            return
    except FileNotFoundError:
        messagebox.showerror("错误", "ADB未安装或未配置到PATH中")
        return
    
    # 检查设备连接
    try:
        result = subprocess.run(["adb", "devices"], capture_output=True, text=True)
        if "device" not in result.stdout:
            messagebox.showerror("错误", "未检测到连接的Android设备")
            return
    except Exception as e:
        messagebox.showerror("错误", f"检查设备连接失败: {str(e)}")
        return
    
    # 创建主窗口
    root = tk.Tk()
    app = ScreenCaptureApp(root)
    
    # 启动应用
    root.mainloop()

if __name__ == "__main__":
    main() 