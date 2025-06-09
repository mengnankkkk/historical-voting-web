package com.historical.voting.user.strategy;

public interface TranscodingStrategy {
    /** 转码入口 */
    void transcode(String inputPath, String outputDir) throws Exception;
    /** 标识 */
    String getName();
}
