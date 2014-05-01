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
package com.googlesource.gerrit.plugins.scripting.scala;

import com.google.inject.Inject;

import scala.tools.nsc.reporters.ConsoleReporter;
import scala.tools.nsc.reporters.Reporter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ScalaReporter {

  private final ConsoleReporter consoleReporter;
  private final ByteArrayOutputStream buffer;
  private int bufferPos;

  @Inject
  public ScalaReporter(ScalaSettings settings) {
    buffer = new ByteArrayOutputStream();
    consoleReporter =
        new ConsoleReporter(settings.getSettings(), new BufferedReader(
            new InputStreamReader(System.in)), new PrintWriter(buffer, true));
  }

  public String getOutput() {
    return new String(buffer.toByteArray()).substring(bufferPos);
  }

  public void reset() {
    consoleReporter.reset();
    bufferPos = new String(buffer.toByteArray()).length();
  }

  public boolean hasErrors() {
    return consoleReporter.hasErrors();
  }

  public Reporter getConsoleReporter() {
    return consoleReporter;
  }
}