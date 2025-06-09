package com.historical.voting.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.historical.voting.user.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    int incrementViewCount(@Param("videoId") Long videoId, @Param("count") Integer count);

}
