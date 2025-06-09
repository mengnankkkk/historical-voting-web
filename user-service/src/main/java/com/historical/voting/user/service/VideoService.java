package com.historical.voting.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.historical.voting.user.entity.Video;

public interface VideoService extends IService<Video> {
    public void syncViewCounts();
    public String processVideo(String inputPath);
    public void extractThumbnail(String videoPath);
    public void recordView(Long videoId, String userIp);
    public Page<Video> pageByTags(int pageNum, int pageSize, String tagKeyword);

    public void likeVideo(Long videoId, String userId);
    public void dislikeVideo(Long videoId, String userId);
    public int getLikeCount(Long videoId);
    public int getDislikeCount(Long videoId);
    public void recordShare(Long videoId);
    public int getShareCount(Long videoId);
    public void submitTranscoding(Long videoId, String filename);





}
