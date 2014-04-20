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

import com.google.gerrit.server.plugins.PluginLoader;
import com.google.inject.Inject;

import scala.Option;
import scala.reflect.io.VirtualDirectory;
import scala.tools.nsc.Settings;

import java.net.URL;
import java.net.URLClassLoader;

public class ScalaSettings {
  private static final String CLASSPATH_DEBUG = "false";
  private static final String VERBOSE_COMPILE_OUTPUT = "false";

  private VirtualDirectory virtualDirectory;
  private final Settings settings;

  @Inject
  public ScalaSettings() {
    settings = new Settings();
    settings.usejavacp().tryToSetFromPropertyValue("true");
    settings.exposeEmptyPackage().tryToSetFromPropertyValue("true");
    settings.Ylogcp().tryToSetFromPropertyValue(CLASSPATH_DEBUG);
    settings.verbose().tryToSetFromPropertyValue(VERBOSE_COMPILE_OUTPUT);

    settings.outputDirs().setSingleOutput(initVirtualDirectory());
    settings.classpath().tryToSetFromPropertyValue(
        classPathOf(PluginLoader.class) + ":" + classPathOf(this.getClass()));
  }

  private VirtualDirectory initVirtualDirectory() {
    virtualDirectory =
        new VirtualDirectory("(memory)", Option.apply((VirtualDirectory) null));
    return virtualDirectory;
  }

  private String classPathOf(Class<?> clazz) {
    StringBuilder currentClassPath = new StringBuilder();
    for (URL url : ((URLClassLoader) (clazz.getClassLoader())).getURLs()) {
      if (url.getProtocol().equals("file")) {
        if (currentClassPath.length() > 0) {
          currentClassPath.append(":");
        }
        currentClassPath.append(url.getPath());
      }
    }
    String currentClassPathString = currentClassPath.toString();
    return currentClassPathString;
  }

  public Settings getSettings() {
    return settings;
  }

  public VirtualDirectory getVirtualDirectory() {
    return virtualDirectory;
  }

}
