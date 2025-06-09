package com.historical.voting.user.strategy.trancode;

import com.historical.voting.user.config.FfmpegConfig;
import com.historical.voting.user.strategy.AbstractTranscodingStrategy;
import org.springframework.stereotype.Component;

@Component("dash")
public class DashTranscodingStrategy extends AbstractTranscodingStrategy {


    protected DashTranscodingStrategy(FfmpegConfig props) {
        super(props);
    }

    @Override
    public String getName() { return "DASH"; }

    @Override
    protected String[] buildCommand(String in, String outDir) {
        return new String[]{
                props.getExecutable(),
                "-i", in,
                "-codec: copy",
                "-f", "dash",
                outDir + "/manifest.mpd"
        };
    }
}