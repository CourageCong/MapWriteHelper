package com.guoxin.im.map;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.guoxin.im.ZApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 针对百度地图sdk版本低于3.6.0的版本的前提下进行的手动导入离线包处理
 * <p>
 * 百度地图sdk版本高于3.6.0时，取消了手动导入离线包接口，使用无效，所以应使用低于该版本的历史版本
 * 由于官网不在离线包下载，所以使用的离线包都是使用百度地图在线下载接口下载到本地的，为了不再额外
 * 提供离线包让用户手动导入，采取的是自动从assets文件夹中读取的方法，由于assets文件夹下的文件最大
 * 不能超过1024KB,下面提供了大文件分割的方法split（），分割后复制到assets文件夹下，然后在程序中自动调用
 * mergeFile()方法，即可在程序启动的时候先把地图加载到本地，实现自动导入离线包
 *
 * @author fucong
 * @version 1.0.0
 * @see ""
 */

public class MapWriteHelper {

    private static final String TAG = "MapWriteHelper";

    private static MapWriteHelper help;

    public static MapWriteHelper getInstance() {
        if (help == null) {
            help = new MapWriteHelper();
        }
        return help;
    }

    /**
     * 分割大文件，如果有地图更新或者新的城市，调用该方法
     * 还需要在assets文件夹中新建以指定城市命名的文件夹
     *
     * @param fromFilePath 外部存储卡内的路径(包括文件名) , 百度地图下载的地图存放位置 eg: /BaiduMapSDK/vmp/h/beijing_131.dat
     * @param outFilePath  存放小文件文件夹路径  eg: "/com.couragecong.im/"
     * @param outFileName  存放小文件文件夹名称，建议为城市名称 eg: "beijing"
     */
    public void split(String fromFilePath, String outFilePath, String outFileName) {
        File fromFile = getOriginalFile(fromFilePath);
        if (fromFile == null) {
            Log.e(TAG, "write: formFile为空");
            return;
        }

        File toFile = getOutFile(outFilePath, outFileName);
        if (toFile == null) {
            Log.e(TAG, "write: toFile为空");
            return;
        }

        split(fromFile, toFile, outFileName, ".dat");

    }


    /**
     * 将文件从assets复制合并到本地
     */
    public void write() {

        File mergetF = getMergeFile("beijing_131.dat");

        if (mergetF == null) {
            Log.e(TAG, "write: mergetF为空");
            return;
        }

        try {
            mergeFile("beijing", mergetF, ".dat");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获得原大文件File对象文件，如果不存在，返回null,吐司一次
     *
     * @path /BaiduMapSDK/vmp/h/beijing_131.dat   百度地图手机sdk上的位置
     */
    private File getOriginalFile(String path) {
        String originalPath = getSDCardPath() + path;
        File f = new File(originalPath);
        if (f.exists()) {
            Toast.makeText(ZApp.getInstance(), "成功", Toast.LENGTH_SHORT).show();
            return f;
        }
        Toast.makeText(ZApp.getInstance(), "第一步原文件不存在", Toast.LENGTH_SHORT).show();
        return null;
    }

    /**
     * 获取输出目标文件夹,在指定文件夹中建相应城市名称的文件夹用来存放分割后的文件
     *
     * @param path 指定外部路径 eg: "/com.couragecong.im/"
     * @param city 城市名称
     */
    private File getOutFile(String path, String city) {
        String outPath = getSDCardPath() + path + city;
        File file = new File(outPath);
        if (file.exists() && file.isDirectory()) {
            return file;
        } else {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获得文件合并后保存到百度地图指定路径下文件的File对象
     * <p>
     * 用在百度地图
     *
     * @param fileName 文件名称，注意后缀名
     */
    private File getMergeFile(String fileName) {
        return this.getMergeFile("", fileName);
    }

    /**
     * 获得文件合并后保存到指定路径下文件的File对象
     *
     * @param path     如果不为""  表示保存在外部指定路径 格式eg:/com.couragecong.im/music/
     *                 否则就表示保存默认路径，即百度地图路径
     * @param fileName 必须是文件，不要忘记后缀名
     */
    private File getMergeFile(String path, String fileName) {
        String outPath = "";

        if (!path.equals("")) {
            //保存到外部
            outPath = getSDCardPath() + fileName;
        } else {
            //保存到百度地图文件夹中
            outPath = getSDCardPath() + "/BaiduMapSDK/vmp/h" + File.separator + fileName;
        }

        File file = new File(outPath);

        if (file.exists() && file.isFile()) {
            return file;
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 获取SDCard的目录路径功能
     *
     * @return sd卡路径
     */
    private String getSDCardPath() {
        File sdcardDir = null;
        //判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        return sdcardDir.toString();
    }


    /**
     * 把大件拆分，放到指定目录文件夹下
     * <p>
     * 如果有地图更新或者新的城市，调用该方法，然后把分割后的文件从目标文件夹复制到assets目录下，
     * 配置mergeFile方法路径
     *
     * @param splitFile 需要分割的文件
     * @param outFile   分割后输出的文件夹
     * @param city      城市名称 "beijing"
     * @param lastName  后缀 ".dat"  ".cfg"
     */
    public void split(File splitFile, File outFile, String city, String lastName) {

        //以每个小文件1024*1024字节即1M的标准来分割
        int split = 1024 * 1024;
        byte[] buf = new byte[1024];
        int num = 1;
        //建立输入流
        File inFile = splitFile;
        try {
            FileInputStream fis = new FileInputStream(inFile);
            while (true) {
                //以"demo"+num+".db"方式来命名小文件即分割后为demo1.db，demo2.db，。。。。。。
                FileOutputStream fos = new FileOutputStream(new File(outFile, city + num + lastName));//path + base + num + ext
                for (int i = 0; i < split / buf.length; i++) {
                    int read = fis.read(buf);
                    fos.write(buf, 0, read);
                    // 判断大文件读取是否结束
                    if (read < buf.length) {
                        fis.close();
                        fos.close();
                        return;
                    }
                }
                fos.close();
                num++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "split: 分割完成 ");
    }


    /**
     * 从assets文件夹中合并文件写到外部sd卡
     * <p>
     * 内部小文件命名规则 fileName+index ， index从1开始，注意for循环，按文件名获取有序文件进行有序操作
     * 还原大文件
     *
     * @param fileName       assets文件夹中需要整合的文件夹名字 eg:beijing 同时也是内部小文件开头的名字，
     *                       尾部名字为序号
     * @param mergeTagetFile sd卡目标文件路径 ，整合后的文件File对象，是个文件
     * @param last           文件后缀名
     * @throws IOException
     */
    public void mergeFile(String fileName, File mergeTagetFile, String last)
            throws IOException {

        String fileN[] = ZApp.getInstance().getAssets().list(fileName);
        int fileSize = fileN.length;//小文件数量

        if (!mergeTagetFile.exists()) {
            mergeTagetFile.mkdirs();
        }

        if (mergeTagetFile.exists()) {
            FileOutputStream outStream = new FileOutputStream(mergeTagetFile);
            byte[] buffer = new byte[1024];
            InputStream in;
            int readLen = 0;

            for (int i = 1; i <= fileSize; i++) {
                // 获得输入流 ,注意文件的路径
                Log.e(TAG, "mergeFile: " + i);
                in = ZApp.getInstance().getAssets().open(fileName + File.separator + fileName + i + last);
                while ((readLen = in.read(buffer)) != -1) {
                    outStream.write(buffer, 0, readLen);
                }
                outStream.flush();
                in.close();
            }

            // 把所有小文件都进行写操作后才关闭输出流，这样就会合并为一个文件了    
            outStream.close();
        }
    }

    /**
     * 将指定路径的文件中的小文件合并成一个大文件
     *
     * @param splitFile         存放分割后文件的地方
     * @param splitFileNameList 小文件名集合
     * @param mergeTagetFile    目标文件路径
     */
    public void mergeFile(File splitFile, ArrayList<String> splitFileNameList, File mergeTagetFile)
            throws IOException {

        if (!mergeTagetFile.exists()) {
            mergeTagetFile.mkdirs();
        }

        if (mergeTagetFile.exists()) {
            FileOutputStream outStream = new FileOutputStream(mergeTagetFile);
            byte[] buffer = new byte[1024];
            InputStream in;
            int readLen = 0;

            for (int i = 0; i < splitFileNameList.size(); i++) {
                // 获得输入流 ,注意文件的路径
                in = new FileInputStream(splitFile + File.separator + splitFileNameList.get(i));
                while ((readLen = in.read(buffer)) != -1) {
                    outStream.write(buffer, 0, readLen);
                }
                outStream.flush();
                in.close();
            }

            // 把所有小文件都进行写操作后才关闭输出流，这样就会合并为一个文件了
            outStream.close();
        }
    }

    /**
     * 分割后保存文件的file
     * <p>
     * 文件尾号从1开始
     *
     * @return 返回文件夹中所有分割后文件的有序列表
     */
    private ArrayList<String> getFileNameArray(File afterSplitFile, String city, String lastName) {

        File[] files = afterSplitFile.listFiles();
        int size = files.length;
        ArrayList<String> fileNames = new ArrayList<String>();

        for (int i = 1; i <= size; i++) {
            fileNames.add(city + i + lastName);

        }
        return fileNames;

    }

}
