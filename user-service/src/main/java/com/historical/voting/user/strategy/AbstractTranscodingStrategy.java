package com.historical.voting.user.strategy;

import com.historical.voting.user.config.FfmpegConfig;

public abstract class AbstractTranscodingStrategy implements TranscodingStrategy{

    protected final FfmpegConfig props;


    protected AbstractTranscodingStrategy(FfmpegConfig props) {
        this.props = props;
    }
    @Override
    public final void transcode(String inputPath, String outputDir) throws Exception{
        prepareDirectory(outputDir);
        String [] cmd = buildCommand(inputPath,outputDir);
        ProcessBuilder pd = new ProcessBuilder(cmd);
        Process p = pd.start();
        int exit = p.waitFor();
        if (exit!=0){
            throw new RuntimeException(getName()+"转码失败"+exit);

        }
        postProcess(outputDir);

    }
    private void prepareDirectory(String outputDir){
        new java.io.File(outputDir).mkdirs();
    }
    public abstract String getName();
    protected abstract String[] buildCommand(String inputPath, String outputDir);
    protected void postProcess(String outputDir) { }

}
