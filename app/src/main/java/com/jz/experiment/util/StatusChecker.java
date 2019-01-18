package com.jz.experiment.util;

public class StatusChecker {
    public static final int ACK = 0;
    public static final int ERROR_LENGTH = 1;
    public static final int ERROR_CMD = 2;
    public static final int ERROR_INDEX_OUT = 3;
    public static final int ERROR_CHECKSUM = 4;
    public static final int ERROR_TYPE = 5;
    public static final int ERROR_DATA = 6;
    public static final int ERROR_PARAM = 7;
    public static final int ERROR_UNKNOWN = 8;
    public static final int ERROR_TEMP_TIMEOUT = 9;
    public static final int ERROR_IMG_TIMEOUT = 10;

    /**
     * 0
     * ACK
     * 1
     * 数据包长度错误
     * 2
     * CMD错误
     * 3
     * 图像行号越界
     * 4
     * checksum错误
     * 5
     * TYPE错误
     * 6
     * 数据段错误
     * 7
     * 参数超出范围
     * 8
     * 未定义错误
     * 9
     * 温控命令执行超时
     * 10
     * 图像命令执行超时
     *
     * @param status
     * @return
     */
    public static boolean checkStatus(int status) {

        boolean succ = false;
        switch (status) {
            case ACK:
                succ = true;
                break;
        }
        return false;
    }

    public static String getStatusDesc(int status) {
        String desc;
        switch (status) {
            case ACK:
                desc="成功";
                break;
            case ERROR_LENGTH:
                desc="数据包长度错误";
                break;
            case ERROR_CMD:
                desc="CMD错误";
                break;
            case ERROR_INDEX_OUT:
                desc="图像行号越界错误";
                break;
            case ERROR_CHECKSUM:
                desc="checksum错误";
                break;
            case ERROR_TYPE:
                desc="type错误";
                break;
            case ERROR_DATA:
                desc="数据段错误";
                break;
            case ERROR_PARAM:
                desc="参数超出范围错误";
                break;
            case ERROR_UNKNOWN:
                desc="未定义错误";
                break;
            case ERROR_TEMP_TIMEOUT:
                desc="温控命令执行超时";
                break;
            case ERROR_IMG_TIMEOUT:
                desc="图像命令执行超时";
                break;
            default:
                desc = "未知错误";
                break;
        }
        return desc;
    }
}
