// Copyright (C) 2014 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.googlesource.gerrit.plugins.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

class LookAheadFileInputStream extends BufferedInputStream {
  private static final int NEWLINE_CR = '\r';
  private static final int NEWLINE_NL = '\n';
  private static final int BUFFER_SIZE = 1024;

  private int lineNr = 1;
  private int lastChar = NEWLINE_NL;

  private final String fileExtension;
  private final String fileName;
  private final Path currentDir;

  public LookAheadFileInputStream(Path inputFile) throws IOException {
    super(Files.newInputStream(inputFile), BUFFER_SIZE);

    fileName = inputFile.getFileName().toString();
    fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
    currentDir = inputFile.getParent();
  }

  @Override
  public String toString() {
    return "pos=" + pos + " count=" + count + " lineNr=" + lineNr
        + " buffer=\'" + new String(buf, pos, count - pos) + "'";
  }

  @Override
  public synchronized int read() throws IOException {
    lastChar = super.read();
    if (isNewLine()) {
      lineNr++;
    }
    return lastChar;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public synchronized int read(byte[] b, int off, int len) throws IOException {
    int numBytes = super.read(b, off, len);
    if (numBytes > 0) {
      lastChar = b[off + numBytes - 1];
    }
    return numBytes;
  }

  public synchronized boolean startsWith(String includeVirtualPrefix)
      throws IOException {
    mark(includeVirtualPrefix.length());
    try {
      byte[] cmp = new byte[includeVirtualPrefix.length()];
      super.read(cmp);
      return Arrays.equals(includeVirtualPrefix.getBytes(), cmp);
    } finally {
      reset();
    }
  }

  public boolean isNewLine() {
    return isNewLine(lastChar);
  }

  public boolean isNewLine(int last) {
    return last == NEWLINE_CR || last == NEWLINE_NL;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public int getLineNr() {
    return lineNr;
  }

  public String getFileName() {
    return fileName;
  }

  public Path getCurrentDir() {
    return currentDir;
  }

}
