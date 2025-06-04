package com.historical.voting.user.service;

public interface VideoProcessService {
    public String processVideo(String inputPath);
    public String extractThumbnail(String videoPath);

}
