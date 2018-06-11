package com.travel.loadimage;

/**
 * Created by zhxtn on 2017/6/28.
 */

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.travel.lib.TravelApp;
import com.travel.lib.utils.OSUtil;


public class AsynImageLoader {
    private static final String TAG = "AsynImageLoader";
    public static final String CACHE_DIR ="/xblx" ;
    // 缓存下载过的图片的Map
    private Map<String, SoftReference<Bitmap>> caches;
    // 任务队列
    private List<Task> taskQueue;
    private boolean isRunning = false;

    public AsynImageLoader(){
        // 初始化变量
        caches = new HashMap<String, SoftReference<Bitmap>>();
        taskQueue = new ArrayList<AsynImageLoader.Task>();
        // 启动图片下载线程
        isRunning = true;
        new Thread(runnable).start();
    }

    /**
     *
     * @param imageView 需要延迟加载图片的对象
     * @param url 图片的URL地址
     * @param resId 图片加载过程中显示的图片资源
     */
    public void showImageAsyn( LinearLayout linearLayout,ImageView imageView, String url, int resId){
        imageView.setTag(url);
        Bitmap bitmap = loadImageAsyn(url, getImageCallback(linearLayout,imageView, resId));
        if(bitmap == null){
            imageView.setImageResource(resId);
        }else{
           // Bitmap tarBitmap = setLinearLayout(linearLayout, imageView, bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }

    @NonNull
    private Bitmap setLinearLayout(LinearLayout linearLayout, ImageView imageView, Bitmap bitmap) {
        Bitmap tarBitmap=adaptive(bitmap);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, tarBitmap.getHeight()));
        linearLayout.setBackgroundColor(Color.BLUE);

        imageView.setLayoutParams(new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT, tarBitmap.getHeight()));
        return tarBitmap;
    }

    //等比例缩放
    public static Bitmap adaptive(Bitmap bitmap) {
        //背景缩放
        final float scalX = OSUtil.getScreenWidth()-OSUtil.dp2px(TravelApp.appContext, 30);//屏宽
        final float scalY = OSUtil.getScreenHeight();//屏高

        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();// 获取资源位图的宽
        int height = bitmap.getHeight();// 获取资源位图的高
        float w = scalX / bitmap.getWidth();
        float h = scalY / bitmap.getHeight();
        matrix.postScale(w, w);// 获取缩放比例
        // 根据缩放比例获取新的位图
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,matrix, true);
        return newbmp;
    }

    public Bitmap loadImageAsyn(String path, ImageCallback callback){
        // 判断缓存中是否已经存在该图片
        if(caches.containsKey(path)){
            // 取出软引用
            SoftReference<Bitmap> rf = caches.get(path);
            // 通过软引用，获取图片
            Bitmap bitmap = rf.get();
            // 如果该图片已经被释放，则将该path对应的键从Map中移除掉
            if(bitmap == null){
                caches.remove(path);
            }else{
                // 如果图片未被释放，直接返回该图片
                Log.i(TAG, "return image in cache" + path);
                return bitmap;
            }
        }else{
            // 如果缓存中不常在该图片，则创建图片下载任务
            Task task = new Task();
            task.path = path;
            task.callback = callback;
            Log.i(TAG, "new Task ," + path);
            if(!taskQueue.contains(task)){
                taskQueue.add(task);
                // 唤醒任务下载队列
                synchronized (runnable) {
                    runnable.notify();
                }
            }
        }

        // 缓存中没有图片则返回null
        return null;
    }

    /**
     *
     * @param imageView
     * @param resId 图片加载完成前显示的图片资源ID
     * @return
     */
    private ImageCallback getImageCallback(final LinearLayout linearLayout,final ImageView imageView, final int resId){
        return new ImageCallback() {

            @Override
            public void loadImage(String path, Bitmap bitmap) {
                if(path.equals(imageView.getTag().toString())){
                   // Bitmap tarBitmap = setLinearLayout(linearLayout, imageView, bitmap);
                    imageView.setImageBitmap(bitmap);
                }else{
                    imageView.setImageResource(resId);
                }
            }
        };
    }

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // 子线程中返回的下载完成的任务
            Task task = (Task)msg.obj;
            // 调用callback对象的loadImage方法，并将图片路径和图片回传给adapter
            task.callback.loadImage(task.path, task.bitmap);
        }

    };

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            while(isRunning){
                // 当队列中还有未处理的任务时，执行下载任务
                while(taskQueue.size() > 0){
                    // 获取第一个任务，并将之从任务队列中删除
                    Task task = taskQueue.remove(0);
                    // 将下载的图片添加到缓存
                    task.bitmap = PicUtil.getbitmap(task.path);
                    caches.put(task.path, new SoftReference<Bitmap>(task.bitmap));
                    if(handler != null){
                        // 创建消息对象，并将完成的任务添加到消息对象中
                        Message msg = handler.obtainMessage();
                        msg.obj = task;
                        // 发送消息回主线程
                        handler.sendMessage(msg);
                    }
                }

                //如果队列为空,则令线程等待
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    //回调接口
    public interface ImageCallback{
        void loadImage(String path, Bitmap bitmap);
    }

    class Task{
        // 下载任务的下载路径
        String path;
        // 下载的图片
        Bitmap bitmap;
        // 回调对象
        ImageCallback callback;

        @Override
        public boolean equals(Object o) {
            Task task = (Task)o;
            return task.path.equals(path);
        }
    }
}