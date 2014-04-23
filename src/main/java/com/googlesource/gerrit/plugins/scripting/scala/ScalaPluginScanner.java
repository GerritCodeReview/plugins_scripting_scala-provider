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

import com.google.gerrit.server.plugins.AbstractPreloadedPluginScanner;
import com.google.gerrit.server.plugins.InvalidPluginException;
import com.google.gerrit.server.plugins.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

public class ScalaPluginScanner extends AbstractPreloadedPluginScanner {

  public ScalaPluginScanner(String pluginName, File srcFile,
      ScalaPluginScriptEngine scriptEngine)
      throws InvalidPluginException {
    super(pluginName, getPluginVersion(srcFile), loadScriptClasses(srcFile, scriptEngine), Plugin.ApiType.PLUGIN);
  }

  private static String getPluginVersion(File srcFile) {
    String srcFileName = srcFile.getName();
    int startPos = srcFileName.lastIndexOf('-');
    if(startPos == -1) {
      return "0";
    }
    int endPos = srcFileName.lastIndexOf('.');
    return srcFileName.substring(startPos+1, endPos);
  }

  private static Set<Class<?>> loadScriptClasses(File srcFile,
      ScalaPluginScriptEngine scriptEngine) throws InvalidPluginException {
    try {
      return scriptEngine.eval(srcFile);
    } catch (ClassNotFoundException | IOException e) {
      throw new InvalidPluginException(
          "Cannot evaluate script file " + srcFile, e);
    }
  }

  @Override
  public <T> T getResource(String resourcePath, Class<? extends T> resourceClass) {
    return null;
  }

  @Override
  public InputStream getResourceInputStream(String resourcePath)
      throws IOException {
    return null;
  }

  @Override
  public <T> Enumeration<T> resources(Class<? extends T> resourceClass) {
    return Collections.emptyEnumeration();
  }
}
