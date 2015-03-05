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

import com.google.common.base.Optional;
import com.google.gerrit.server.plugins.AbstractPreloadedPluginScanner;
import com.google.gerrit.server.plugins.InvalidPluginException;
import com.google.gerrit.server.plugins.Plugin;
import com.google.gerrit.server.plugins.PluginEntry;

import com.googlesource.gerrit.plugins.web.WebPluginScanner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Set;

public class ScalaPluginScanner extends AbstractPreloadedPluginScanner {
  private final WebPluginScanner webScanner;

  public ScalaPluginScanner(String pluginName, Path srcFile,
      ScalaPluginScriptEngine scriptEngine) throws InvalidPluginException {
    super(pluginName, getPluginVersion(srcFile), loadScriptClasses(srcFile,
        scriptEngine), Plugin.ApiType.PLUGIN);

    this.webScanner = new WebPluginScanner(srcFile);
  }

  private static String getPluginVersion(Path srcFile) {
    String srcFileName = srcFile.getFileName().toString();
    int startPos = srcFileName.lastIndexOf('-');
    if (startPos == -1) {
      return "0";
    }
    int endPos = srcFileName.lastIndexOf('.');
    return srcFileName.substring(startPos + 1, endPos);
  }

  private static Set<Class<?>> loadScriptClasses(Path srcFile,
      ScalaPluginScriptEngine scriptEngine) throws InvalidPluginException {
    try {
      return scriptEngine.eval(srcFile);
    } catch (ClassNotFoundException | IOException e) {
      throw new InvalidPluginException(
          "Cannot evaluate script file " + srcFile, e);
    }
  }

  @Override
  public Optional<PluginEntry> getEntry(String resourcePath) throws IOException {
    return webScanner.getEntry(resourcePath);
  }

  @Override
  public InputStream getInputStream(PluginEntry entry) throws IOException {
    return webScanner.getInputStream(entry);
  }

  @Override
  public Enumeration<PluginEntry> entries() {
    return webScanner.entries();
  }
}
