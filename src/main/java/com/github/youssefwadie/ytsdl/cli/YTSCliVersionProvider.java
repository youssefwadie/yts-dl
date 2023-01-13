package com.github.youssefwadie.ytsdl.cli;

import picocli.CommandLine;

public class YTSCliVersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        return new String[]{"${COMMAND-FULL-NAME} v" + getClass().getPackage().getImplementationVersion()};
    }
}
