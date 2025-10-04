package common;

import java.awt.*;
import java.util.Random;

public class Area {
    private final Rectangle rect;
    private final Random random;

    public Area(int x, int y, int width, int height) {
        this.rect = new Rectangle(x, y, width, height);
        this.random = new Random();
    }

    public Area(Rectangle rect) {
        this.rect = rect;
        this.random = new Random();
    }

    /**
     * 在矩形区域内随机选择一个点
     */
    public Point randomPoint() {
        int rx = rect.x + random.nextInt(rect.width);
        int ry = rect.y + random.nextInt(rect.height);
        return new Point(rx, ry);
    }

    /**
     * 判断一个点是否在矩形区域内
     */
    public boolean contains(Point p) {
        return rect.contains(p);
    }

    /**
     * 获取内部的Rectangle对象（只读）
     */
    public Rectangle getRect() {
        return new Rectangle(rect);
    }
}
