package com.historical.voting.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.Table;
import java.sql.Date;

@Data
@TableName("video_metadata")
public class Video {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String url;
    private String coverUrl;
    private Integer duration;
    private String format;
    private Long size;
    private Date createTime;
    private Integer viewCount;
    private String description;
    private String tags;
}
