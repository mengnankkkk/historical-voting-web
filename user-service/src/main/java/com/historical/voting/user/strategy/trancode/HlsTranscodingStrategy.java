package com.historical.voting.user.strategy.trancode;

import com.historical.voting.user.config.FfmpegConfig;
import com.historical.voting.user.strategy.AbstractTranscodingStrategy;
import org.springframework.stereotype.Component;

@Component("hls")
public class HlsTranscodingStrategy extends AbstractTranscodingStrategy {

    protected HlsTranscodingStrategy(FfmpegConfig props) {
        super(props);
    }

    @Override
    public String getName() {
        return "HLS";
    }

    @Override
    protected String[] buildCommand(String in, String outDir) {
        return new String[]{
                props.getExecutable(),
                "-i", in,
                "-codec: copy",
                "-start_number", "0",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-f", "hls",
                outDir + "/index.m3u8"
        };
    }
}
