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

import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

public class SSIPageInputStream extends FilterInputStream {
  private static final String INCLUDE_VIRTUAL_PREFIX =
      "<!--#include virtual=\"";
  private static final String INCLUDE_VIRTUAL_SUFFIX = " -->";

  private LookAheadFileInputStream currentIs;
  private Stack<LookAheadFileInputStream> fileInputStreamStack;
  private final Path basePath;

  public SSIPageInputStream(Path basePath, String filePath)
      throws IOException {
    super(new LookAheadFileInputStream(basePath.resolve(filePath)));

    this.basePath = basePath;
    this.fileInputStreamStack = new Stack<>();
    this.currentIs = (LookAheadFileInputStream) this.in;
  }

  @Override
  public int read() throws IOException {
    int character;
    if (currentIs.isNewLine() && currentIs.startsWith(INCLUDE_VIRTUAL_PREFIX)) {
      processInclude();
      character = read();
    } else {
      character = currentIs.read();
    }

    if (character < 0 && !fileInputStreamStack.isEmpty()) {
      pop();
      return read();
    } else {
      return character;
    }
  }

  private void processInclude() throws IOException {
    push(getIncludeFileName());
  }

  private void push(String includeFileName) throws IOException {
    fileInputStreamStack.push(currentIs);
    Path inputFile = getFile(includeFileName);
    if (!Files.exists(inputFile)) {
      throw new IOException("Cannot find file '" + includeFileName
          + "' included in " + currentIs.getFileName() + ":"
          + currentIs.getLineNr());
    }
    currentIs = new LookAheadFileInputStream(inputFile);
    in = currentIs;
  }

  private Path getFile(String includeFileName) {
    if (includeFileName.startsWith("/")) {
      return basePath.resolve(includeFileName);
    } else {
      return currentIs.getCurrentDir().resolve(includeFileName);
    }
  }

  private void pop() {
    currentIs = fileInputStreamStack.pop();
    in = currentIs;
  }

  private String getIncludeFileName() throws IOException {
    skipAll(INCLUDE_VIRTUAL_PREFIX.length());

    StringBuilder includeFileName = new StringBuilder();
    char last = '\0';
    last = (char) currentIs.read();
    while (last != '\"' && !currentIs.isNewLine() && last > 0) {
      includeFileName.append(last);
      last = (char) currentIs.read();
    }
    if (!currentIs.startsWith(INCLUDE_VIRTUAL_SUFFIX)) {
      throw new IOException("Invalid SHTML include directive at line "
          + currentIs.getLineNr());
    }

    skipAll(INCLUDE_VIRTUAL_SUFFIX.length());
    return includeFileName.toString();
  }

  private void skipAll(int length) throws IOException {
    for (long skipped = skip(length);
        length > 0 && skipped > 0;
        skipped = skip(length)) {
      length -= skipped;
    }
  }

  @Override
  public int read(byte b[]) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    int c = read();
    if (c == -1) {
      return -1;
    }
    b[off] = (byte) c;

    int i = 1;
    try {
      for (; i < len; i++) {
        c = read();
        if (c == -1) {
          break;
        }
        b[off + i] = (byte) c;
      }
    } catch (IOException ee) {
    }
    return i;
  }
}
