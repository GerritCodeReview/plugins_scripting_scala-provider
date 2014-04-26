// Copyright (C) 2013 The Android Open Source Project
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

import static scala.collection.JavaConversions.asScalaBuffer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Char;
import scala.Option;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.Seq;
import scala.collection.mutable.Map;
import scala.reflect.internal.util.BatchSourceFile;
import scala.reflect.internal.util.SourceFile;
import scala.reflect.io.AbstractFile;
import scala.reflect.io.VirtualDirectory;
import scala.tools.nsc.Global;
import scala.tools.nsc.Global.Run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ScalaPluginScriptEngine {
  private static final Logger LOG = LoggerFactory
      .getLogger(ScalaPluginScriptEngine.class);

  // private final IMain scalaEngine;
  private final ScalaClassLoader classLoader;
  private Global globalEngine;
  private ScalaReporter reporter;

  public class ScalaClassLoader extends ClassLoader {
    private static final String CLASS_EXTENSION = ".class";
    private Map<String, AbstractFile> scalaClasses;

    public ScalaClassLoader(ScalaSettings settings) {
      super(ScalaClassLoader.class.getClassLoader());
      scalaClasses =
          settings.getVirtualDirectory()
              .scala$reflect$io$VirtualDirectory$$files();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      AbstractFile classFile = getClassFile(name, scalaClasses);
      byte[] ba;
      try {
        ba = classFile.toByteArray();
        return defineClass(name, ba, 0, ba.length);
      } catch (IOException e) {
        throw new ClassNotFoundException("Cannot open Scala class file "
            + classFile, e);
      }
    }

    private AbstractFile getClassFile(String fullClassName,
        Map<String, AbstractFile> tree) throws ClassNotFoundException {
      String[] nameComponents = fullClassName.split("\\.");
      nameComponents[nameComponents.length-1] =
          nameComponents[nameComponents.length-1] + CLASS_EXTENSION;
      for (String component : nameComponents) {
        Option<AbstractFile> node = tree.get(component);
        if (node.isEmpty()) {
          throw new ClassNotFoundException(
              "Cannot find compiled Scala code for class " + fullClassName
                  + ": " + component + " is unknown");
        }

        AbstractFile abstractFile = node.get();
        if (component.endsWith(CLASS_EXTENSION)) {
          return abstractFile;
        } else {
          tree =
              ((VirtualDirectory) abstractFile)
                  .scala$reflect$io$VirtualDirectory$$files();
        }
      }
      throw new ClassNotFoundException(
          "Cannot find compiled Scala code for class " + fullClassName);
    }

    public Set<String> getAllLoadedClassNames() {
      return scanTree("", scalaClasses);
    }

    private Set<String> scanTree(String packageName,
        Map<String, AbstractFile> tree) {
      Set<String> classNames = Sets.newHashSet();
      for (Iterator<Tuple2<String, AbstractFile>> keysIter = tree.toIterator(); keysIter
          .hasNext();) {
        Tuple2<String, AbstractFile> node = keysIter.next();
        String fileName = node._1;
        AbstractFile fileContent = node._2;

        if (fileName.endsWith(CLASS_EXTENSION)) {
          classNames.add(nameWithPackage(
              packageName,
              fileName.substring(0,
                  fileName.length() - CLASS_EXTENSION.length())));

        } else if (VirtualDirectory.class.isAssignableFrom(fileContent
            .getClass())) {
          VirtualDirectory subNode = (VirtualDirectory) node._2;
          classNames.addAll(scanTree(nameWithPackage(packageName, fileName),
              subNode.scala$reflect$io$VirtualDirectory$$files()));
        }
      }
      return classNames;
    }

    private String nameWithPackage(String packageName, String packageMember) {
      if (packageName.length() <= 0) {
        return packageMember;
      } else {
        return packageName + "." + packageMember;
      }
    }
  }

  @Inject
  public ScalaPluginScriptEngine(ScalaSettings settings,
      ScalaReporter reporter) {
    this.classLoader = new ScalaClassLoader(settings);
    globalEngine =
        new Global(settings.getSettings(), reporter.getConsoleReporter());
    this.reporter = reporter;
  }

  public Set<Class<?>> eval(File scalaFile) throws IOException,
      ClassNotFoundException {
    if (scalaFile.isFile()) {
      return evalFiles(Arrays.asList(scalaFile));
    } else if (scalaFile.isDirectory()) {
      return evalDirectory(scalaFile);
    } else {
      throw new IOException("File " + scalaFile
          + " is not a supported for loading Scala scripts");
    }
  }

  private Set<Class<?>> evalDirectory(File scalaFile) throws IOException,
      ClassNotFoundException {
    final List<File> scalaFiles = Lists.newArrayList();

    Files.walkFileTree(scalaFile.toPath(),
        EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
              throws IOException {
            File file = path.toFile();
            String fileName = file.getName();
            if (file.isFile() && fileName.endsWith(ScalaPluginProvider.SCALA_EXTENSION)) {
              scalaFiles.add(file);
            }
            return FileVisitResult.CONTINUE;
          }
        });
    return evalFiles(scalaFiles);
  }

  private Set<Class<?>> evalFiles(List<File> scalaFiles) throws IOException,
      ClassNotFoundException {
    Set<Class<?>> classes = Sets.newHashSet();

    List<SourceFile> scalaSourceFiles = Lists.transform(scalaFiles, new Function<File,SourceFile>() {
      @Override
      public SourceFile apply(File scalaFile) {
        try {
          return new BatchSourceFile(scalaFile.getName(), readScalaFile(scalaFile));
        } catch (IOException e) {
          throw new IllegalArgumentException("Cannot load scala file " + scalaFile, e);
        }
      }
    });
    Run run = globalEngine.new Run();
    reporter.reset();
    run.compileSources(asScalaBuffer(scalaSourceFiles).toList());
    if (reporter.hasErrors()) {
      LOG.error("Error compiling scala files " + scalaFiles);
      LOG.error(reporter.getOutput());
      throw new IOException("Invalid Scala files " + scalaFiles);
    } else {
      String output = reporter.getOutput();
      if(output.length() > 0) {
        LOG.info("Scala files " + scalaFiles + " loaded successfully");
        LOG.info(output);
      }
    }

    for (String className : classLoader.getAllLoadedClassNames()) {
      Class<?> clazz = classLoader.loadClass(className);
      classes.add(clazz);
    }
    return classes;
  }

  private Seq<Object> readScalaFile(File scalaFile) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(scalaFile));
    StringBuilder scalaCode = new StringBuilder();
    try {
      String line;
      while (null != (line = reader.readLine())) {
        scalaCode.append(line);
        scalaCode.append("\n");
      }
    } finally {
      reader.close();
    }

    List<Object> chars = new ArrayList<Object>();
    for (char c : scalaCode.toString().toCharArray()) {
      chars.add(Char.unbox(c));
    }

    return asScalaBuffer(chars).toList();
  }

  public ScalaClassLoader getClassLoader() {
    return classLoader;
  }
}
