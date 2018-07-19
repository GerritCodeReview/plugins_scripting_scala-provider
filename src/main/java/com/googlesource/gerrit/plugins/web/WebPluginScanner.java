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

import com.google.common.collect.Lists;
import com.google.gerrit.server.plugins.InvalidPluginException;
import com.google.gerrit.server.plugins.PluginContentScanner;
import com.google.gerrit.server.plugins.PluginEntry;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
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
import java.util.Map;
import java.util.Optional;
import java.util.jar.Manifest;

public class WebPluginScanner implements PluginContentScanner {
  private final Path staticResourcesPath;

  @Inject
  public WebPluginScanner(Path rootDir) {
    this.staticResourcesPath = rootDir;
  }

  @Override
  public Manifest getManifest() throws IOException {
    return new Manifest();
  }

  @Override
  public Map<Class<? extends Annotation>, Iterable<ExtensionMetaData>> scan(
      String pluginName, Iterable<Class<? extends Annotation>> annotations)
      throws InvalidPluginException {
    return Collections.emptyMap();
  }

  @Override
  public Optional<PluginEntry> getEntry(String resourcePath) throws IOException {
    Path resourceFile = getResourceFile(resourcePath);
    if (Files.exists(resourceFile) && Files.size(resourceFile) > 0) {
      return resourceOf(resourcePath);
    } else {
      return Optional.empty();
    }
  }

  private Optional<PluginEntry> resourceOf(String resourcePath) throws IOException {
    Path file = getResourceFile(resourcePath);
    long fileSize = Files.size(file);
    if (Files.exists(file) && fileSize > 0) {
      long fileLastModifiedTimeMillis = Files.getLastModifiedTime(file).toMillis();
      if (resourcePath.endsWith("html")) {
        return Optional.of(new PluginEntry(resourcePath, fileLastModifiedTimeMillis));
      } else {
        return Optional.of(
            new PluginEntry(resourcePath, fileLastModifiedTimeMillis, Optional.of(fileSize)));
      }
    } else {
      return Optional.empty();
    }
  }

  private Path getResourceFile(String resourcePath) {
    return staticResourcesPath.resolve(resourcePath);
  }

  @Override
  public InputStream getInputStream(PluginEntry entry) throws IOException {
    String name = entry.getName();
    if (name.endsWith("html")) {
      return new SSIPageInputStream(staticResourcesPath, name);
    } else {
      return Files.newInputStream(getResourceFile(name));
    }
  }

  @Override
  public Enumeration<PluginEntry> entries() {
    final List<PluginEntry> resourcesList = Lists.newArrayList();
    try {
      Files.walkFileTree(
          staticResourcesPath,
          EnumSet.of(FileVisitOption.FOLLOW_LINKS),
          Integer.MAX_VALUE,
          new SimpleFileVisitor<Path>() {
            private int basicPathLength = staticResourcesPath.toAbsolutePath().toString().length();

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                throws IOException {
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
      throw new IllegalArgumentException("Cannot scan resource files in plugin", e);
    }
    return Collections.enumeration(resourcesList);
  }
}
