package com.han.nexttools.ftp;

/**
 * Created by Han on 2017/6/9.
 */


import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class FTP {

    public static final String FTP_CONNECT_SUCCESSS = "FTP Connect Success";
    public static final String FTP_CONNECT_FAIL = "FTP Connect Fail";
    public static final String FTP_DISCONNECT_SUCCESS = "FTP Disconnect Success";
    public static final String FTP_FILE_NOTEXISTS = "FTP File Not Exists";

    public static final String FTP_UPLOAD_SUCCESS = "FTP Upload Success";
    public static final String FTP_UPLOAD_FAIL = "FTP Upload Fail";
    public static final String FTP_UPLOAD_LOADING = "FTP Upload Loading";

    public static final String FTP_DOWN_LOADING = "FTP Down Loading";
    public static final String FTP_DOWN_SUCCESS = "ftp Down Success";
    public static final String FTP_DOWN_FAIL = "FTP Down Fail";

    public static final String FTP_DELETE_FILE_SUCCESS = "FTP Delete File Success";
    public static final String FTP_DELETE_FILE_FAIL = "ftp Delete File Fail";


    /**
     * 服务器名.
     */
    private String hostName;

    /**
     * 端口号
     */
    private int serverPort;

    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;

    /**
     * FTP连接.
     */
    private FTPClient ftpClient;

    public FTP(String hostName, String userName, String password) {
        this(hostName, 21, userName, password);
    }

    public FTP() {
        hostName = "192.168.0.92";
        serverPort = 21;
        userName = "Han";
        password = "111111";
        this.ftpClient = new FTPClient();
    }

    public FTP(String hostName, int serverPort, String userName, String password) {
        this.hostName = hostName;
        this.serverPort = serverPort;
        this.userName = userName;
        this.password = password;
        this.ftpClient = new FTPClient();
    }

    // -------------------------------------------------------文件上传方法------------------------------------------------

    public void uploadSingleFile(File singleFile, String remotePath,
                                 UploadProgressListener listener) throws IOException {

        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;
        flag = uploadingSingle(singleFile, listener);
        if (flag) {
            listener.onUploadProgress(FTP_UPLOAD_SUCCESS, 0,
                    singleFile);
        } else {
            listener.onUploadProgress(FTP_UPLOAD_FAIL, 0,
                    singleFile);
        }

        // 上传完成之后关闭连接
        this.uploadAfterOperate(listener);
    }

    /**
     * 上传多个文件.
     *
     * @param remotePath FTP目录
     * @param listener   监听器
     * @throws IOException
     */
    public void uploadMultiFile(LinkedList<File> fileList, String remotePath,
                                UploadProgressListener listener) throws IOException {

        // 上传之前初始化
        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;

        for (File singleFile : fileList) {
            flag = uploadingSingle(singleFile, listener);
            if (flag) {
                listener.onUploadProgress(FTP_UPLOAD_SUCCESS, 0,
                        singleFile);
            } else {
                listener.onUploadProgress(FTP_UPLOAD_FAIL, 0,
                        singleFile);
            }
        }

        // 上传完成之后关闭连接
        this.uploadAfterOperate(listener);
    }

    /**
     * 上传单个文件.
     *
     * @param localFile 本地文件
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    private boolean uploadingSingle(File localFile,
                                    UploadProgressListener listener) throws IOException {
        boolean flag = true;
        // 不带进度的方式
        // // 创建输入流
        // InputStream inputStream = new FileInputStream(localFile);
        // // 上传单个文件
        // flag = ftpClient.storeFile(localFile.getName(), inputStream);
        // // 关闭文件流
        // inputStream.close();

        // 带有进度的方式
        BufferedInputStream buffIn = new BufferedInputStream(
                new FileInputStream(localFile));
        ProgressInputStream progressInput = new ProgressInputStream(buffIn,
                listener, localFile);
        flag = ftpClient.storeFile(localFile.getName(), progressInput);
        buffIn.close();

        return flag;
    }

    /**
     * 上传文件之前初始化相关参数
     *
     * @param remotePath FTP目录
     * @param listener   监听器
     * @throws IOException
     */
    private void uploadBeforeOperate(String remotePath,
                                     UploadProgressListener listener) throws IOException {

        // 打开FTP服务
        try {
            this.openConnect();
            listener.onUploadProgress(FTP_CONNECT_SUCCESSS, 0,
                    null);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onUploadProgress(FTP_CONNECT_FAIL, 0, null);
            return;
        }

        // 设置模式
        ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
        // FTP下创建文件夹
        ftpClient.makeDirectory(remotePath);
        // 改变FTP目录
        ftpClient.changeWorkingDirectory(remotePath);
        // 上传单个文件

    }

    /**
     * 上传完成之后关闭连接
     *
     * @param listener
     * @throws IOException
     */
    private void uploadAfterOperate(UploadProgressListener listener)
            throws IOException {
        this.closeConnect();
        listener.onUploadProgress(FTP_DISCONNECT_SUCCESS, 0, null);
    }

    // -------------------------------------------------------文件下载方法------------------------------------------------

    /**
     * 下载单个文件，可实现断点下载.
     *
     * @param serverFileName Ftp目录及文件路径
     * @param localPath      本地目录
     * @param fileName       下载之后的文件名称
     * @param listener       监听器
     * @throws IOException
     */
    public void downloadSingleFile(String serverFileName, String localPath, String fileName, DownLoadProgressListener listener)
            throws Exception {

        ftpClient.enterLocalPassiveMode();
        // 打开FTP服务
        try {
            this.openConnect();
            listener.onDownLoadProgress(FTP_CONNECT_SUCCESSS, 0, null);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onDownLoadProgress(FTP_CONNECT_FAIL, 0, null);
            return;
        }

        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles();
        if (files == null || files.length == 0) {
            listener.onDownLoadProgress(FTP_FILE_NOTEXISTS, 0, null);
            return;
        }

        long totalSize = 0;
        for (FTPFile file : files) {
            if (file.getName().equals(serverFileName)) {
                totalSize = file.getSize();
                Log.d("Han", String.valueOf(totalSize));
                break;
            }
        }

        File mkFile = new File(localPath);
        if (!mkFile.exists()) {
            mkFile.mkdirs();
        }

        localPath = localPath + fileName;
        File localFile = new File(localPath);
        localFile.delete();


        OutputStream out = new FileOutputStream(localFile);
//        ftpClient.retrieveFile(serverFileName,out);
//        out.close();
//
//
        InputStream input = ftpClient.retrieveFileStream(serverFileName);
        byte[] b = new byte[1024];
        int bytesWritten = 0;
        int byteCount = 0;

        while ((byteCount = input.read(b)) != -1) {
            out.write(b, 0, byteCount);
            bytesWritten += byteCount;
            int process = (int) (bytesWritten * 100 / totalSize);
            Log.d("Han", process + " = " + process);
            listener.onDownLoadProgress(FTP_DOWN_LOADING, process, null);
        }
        out.flush();
        out.close();
        input.close();

        Log.d("Han", String.valueOf(new File(localPath).length()));
        listener.onDownLoadProgress(FTP_DOWN_SUCCESS, 0, new File(localPath));
        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
        if (ftpClient.completePendingCommand()) {
            listener.onDownLoadProgress(FTP_DOWN_SUCCESS, 0, new File(localPath));
        } else {
            listener.onDownLoadProgress(FTP_DOWN_FAIL, 0, null);
        }

        // 下载完成之后关闭连接
        this.closeConnect();
        listener.onDownLoadProgress(FTP_DISCONNECT_SUCCESS, 0, null);

        return;
    }


    public List<FTPFile> getAPKFileList()
            throws Exception {

        List<FTPFile> fileList = new ArrayList<>();
        // 打开FTP服务
        try {
            this.openConnect();
        } catch (IOException e1) {
            e1.printStackTrace();
            return fileList;
        }

        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles();
        for (FTPFile file : files) {
            if (file.getName().endsWith(".apk")) {
                fileList.add(file);
            }
            Log.d("Han", file.getName());
        }

        Collections.sort(fileList, new Comparator<FTPFile>() {
            @Override
            public int compare(FTPFile o1, FTPFile o2) {
                return -o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });


        this.closeConnect();
        return fileList;
    }

    // -------------------------------------------------------文件删除方法------------------------------------------------

    /**
     * 删除Ftp下的文件.
     *
     * @param serverPath Ftp目录及文件路径
     * @param listener   监听器
     * @throws IOException
     */
    public void deleteSingleFile(String serverPath, DeleteFileProgressListener listener)
            throws Exception {

        // 打开FTP服务
        try {
            this.openConnect();
            listener.onDeleteProgress(FTP_CONNECT_SUCCESSS);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onDeleteProgress(FTP_CONNECT_FAIL);
            return;
        }

        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            listener.onDeleteProgress(FTP_FILE_NOTEXISTS);
            return;
        }

        //进行删除操作
        boolean flag = true;
        flag = ftpClient.deleteFile(serverPath);
        if (flag) {
            listener.onDeleteProgress(FTP_DELETE_FILE_SUCCESS);
        } else {
            listener.onDeleteProgress(FTP_DELETE_FILE_FAIL);
        }

        // 删除完成之后关闭连接
        this.closeConnect();
        listener.onDeleteProgress(FTP_DISCONNECT_SUCCESS);

        return;
    }

    // -------------------------------------------------------打开关闭连接------------------------------------------------

    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public void openConnect() throws IOException {
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        int reply; // 服务器响应值
        // 连接至服务器
        ftpClient.connect(hostName, serverPort);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        }
        // 登录到服务器
        ftpClient.login(userName, password);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            // 获取登录信息
            FTPClientConfig config = new FTPClientConfig(ftpClient
                    .getSystemType().split(" ")[0]);
            config.setServerLanguageCode("en");
            ftpClient.configure(config);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient
                    .setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        }
    }

    /**
     * 关闭FTP服务.
     *
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            // 退出FTP
            ftpClient.logout();
            // 断开连接
            ftpClient.disconnect();
        }
    }

    // ---------------------------------------------------上传、下载、删除监听---------------------------------------------

    /*
     * 上传进度监听
     */
    public interface UploadProgressListener {
        public void onUploadProgress(String currentStep, long uploadSize, File file);
    }

    /*
     * 下载进度监听
     */
    public interface DownLoadProgressListener {
        public void onDownLoadProgress(String currentStep, long downProcess, File file);
    }

    /*
     * 文件删除监听
     */
    public interface DeleteFileProgressListener {
        public void onDeleteProgress(String currentStep);
    }

}
