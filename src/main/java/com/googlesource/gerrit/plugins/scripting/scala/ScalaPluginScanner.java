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
import com.google.common.collect.Lists;
import com.google.gerrit.server.plugins.AbstractPreloadedPluginScanner;
import com.google.gerrit.server.plugins.InvalidPluginException;
import com.google.gerrit.server.plugins.Plugin;
import com.google.gerrit.server.plugins.PluginEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

public class ScalaPluginScanner extends AbstractPreloadedPluginScanner {

  private final File staticResourcesPath;

  public ScalaPluginScanner(String pluginName, File srcFile,
      ScalaPluginScriptEngine scriptEngine) throws InvalidPluginException {
    super(pluginName, getPluginVersion(srcFile), loadScriptClasses(srcFile,
        scriptEngine), Plugin.ApiType.PLUGIN);

    this.staticResourcesPath = srcFile;
  }

  private static String getPluginVersion(File srcFile) {
    String srcFileName = srcFile.getName();
    int startPos = srcFileName.lastIndexOf('-');
    if (startPos == -1) {
      return "0";
    }
    int endPos = srcFileName.lastIndexOf('.');
    return srcFileName.substring(startPos + 1, endPos);
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
  public Optional<PluginEntry> getEntry(String resourcePath) {
    File resourceFile = getResourceFile(resourcePath);
    if (resourceFile.exists() && resourceFile.length() > 0) {
      return resourceOf(resourcePath);
    } else {
      return Optional.absent();
    }
  }

  private Optional<PluginEntry> resourceOf(String resourcePath) {
    File file = getResourceFile(resourcePath);
    if (file.exists() && file.length() > 0) {
      return Optional.of(new PluginEntry(resourcePath, file.lastModified(), file
          .length()));
    } else {
      return Optional.absent();
    }
  }

  private File getResourceFile(String resourcePath) {
    File resourceFile = new File(staticResourcesPath, resourcePath);
    return resourceFile;
  }

  @Override
  public InputStream getInputStream(PluginEntry entry)
      throws IOException {
    return new FileInputStream(getResourceFile(entry.getName()));
  }

  @Override
  public Enumeration<PluginEntry> entries() {
    final List<PluginEntry> resourcesList = Lists.newArrayList();
    try {
      Files.walkFileTree(staticResourcesPath.toPath(),
          EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
          new SimpleFileVisitor<Path>() {
            private int basicPathLength = staticResourcesPath.getAbsolutePath()
                .length();

            @Override
            public FileVisitResult visitFile(Path path,
                BasicFileAttributes attrs) throws IOException {
              Optional<PluginEntry> resource = resourceOf(relativePathOf(path));
              if (resource.isPresent()) {
                resourcesList.add(resource.get());
              }
              return FileVisitResult.CONTINUE;
            }

            private String relativePathOf(Path path) {
              return path.toFile().getAbsolutePath().substring(basicPathLength);
            }
          });
    } catch (IOException e) {
      new IllegalArgumentException("Cannot scan resource files in plugin", e);
    }
    return Collections.enumeration(resourcesList);
  }
}
