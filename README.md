# MapWriteHelper
* 自动导入百度地图离线包（非在线下载离线包后离线使用）
* 对assets文件夹内文件不能超过1024KB的处理
* 将assets文件夹中的文件复制到手机存储卡
* 大文件分割成小文件及小文件恢复为大文件

*************************************
### 前言
　　由于涉密部门使用的手机都是加密并且无法连接外部网络的，正好我负责的项目要给一个涉密公司使用，于是这个任务就交给了我。（哈哈，觉得Google上一堆答案）
，于是我先来到了[百度地图官网](http://lbsyun.baidu.com/index.php?title=androidsdk/guide/offlinemap)离线地图模块，然后看见鲜红的几个字 “自v3.6.0起，官网不再支持地图离线包下载，所以SDK去掉手动导入离线包接口，SDK在线下载离线包接口仍维持不变。” v3.6.1版本的我内心只有两个字，呵呵。
******
### 快速使用
##### 1.使用3.6.0以前的百度地图sdk([部分历史sdk](http://111.13.120.16/forum.php?mod=viewthread&tid=3979&extra=page%3D1))
##### 2.先调用split()方法，把目标大文件切割成小文件
```Java
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
```
由于我这里处理的是百度地图的离线包，所以后缀就写死了为 .dat,如果需要的话可以通过代码自动处理后缀名或者外部传入,</br>
　　再调用
##### 3.将切割好的小文件放到assets文件夹下，建议新建一个文件夹保存
##### 4.在线程调用write()方法，修改getMergeFile（）中的参数为自己的文件名，如果不是针对百度地图使用，需要使用mergeFile()的四个参数的重载方法，自己传入合并后文件的保存路径
```Java
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
```
这样就实现了自动导入百度地图离线包



