// Copyright (C) 2012 The Android Open Source Project
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

import com.google.gerrit.server.PluginUser;
import com.google.gerrit.server.plugins.InvalidPluginException;
import com.google.gerrit.server.plugins.ServerPlugin;
import com.google.gerrit.server.plugins.ServerPluginProvider;
import com.google.inject.Singleton;

import org.eclipse.jgit.internal.storage.file.FileSnapshot;

import java.io.File;

/**
 * Scala scripting plugins.
 *
 * Allows to define a Groovy class to implement any type of Gerrit plugin.
 *
 * Example of Scala SSH Plugin (hello-1.0.scala):
 * ------------------------------------------------ TBD
 *
 * The above example add a "hello scala" command to Gerrit SSH interface that
 * displays "Hello Gerrit from Scala !"
 *
 * import com.google.gerrit.sshd.SshCommand import
 * com.google.gerrit.extensions.annotations.Export
 *
 * @Export("scala") class MyClass extends SshCommand { override def run = stdout
 *                  println "Hello Gerrit from Scala !" }
 *
 */
class ScalaPlugin extends ServerPlugin {
  private static final String SCALA_EXTENSION = ".scala";


  @Singleton
  public static class Provider implements ServerPluginProvider {
    private final ScalaPluginScriptEngine scriptEngine =
        new ScalaPluginScriptEngine();

    @Override
    public ServerPlugin get(String name, File srcFile, PluginUser pluginUser,
        FileSnapshot snapshot) throws InvalidPluginException {
      return new ScalaPlugin(scriptEngine, name, pluginUser, srcFile, snapshot);
    }

    @Override
    public boolean handles(File srcFile) {
      return srcFile.getName().toLowerCase().endsWith(SCALA_EXTENSION);
    }

    @Override
    public String getPluginName(File srcFile) {
      String srcFileName = srcFile.getName();
      return srcFileName.substring(0, srcFileName.lastIndexOf("-"));
    }
  }


  public ScalaPlugin(ScalaPluginScriptEngine scriptEngine, String pluginName,
      PluginUser pluginUser, File srcFile, FileSnapshot snapshot)
      throws InvalidPluginException {
    super(pluginName, "", pluginUser, srcFile, snapshot,
        new ScalaPluginScanner(pluginName, srcFile, scriptEngine),
        new File("/"), scriptEngine.getClassLoader());
  }
}
