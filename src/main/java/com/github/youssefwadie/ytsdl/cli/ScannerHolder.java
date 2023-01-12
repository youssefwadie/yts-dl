package com.github.youssefwadie.ytsdl.cli;

import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.util.Scanner;

@Getter
public class ScannerHolder implements Closeable {

    private final Scanner scanner;

    private ScannerHolder() {
        this.scanner = new Scanner(System.in);
    }

    public static final ScannerHolder INSTANCE = new ScannerHolder();

    @Override
    public void close() throws IOException {
        this.scanner.close();
    }
}
