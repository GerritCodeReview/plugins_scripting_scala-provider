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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gerrit.extensions.annotations.Export;
import com.google.gerrit.server.plugins.InvalidPluginException;
import com.google.gerrit.server.plugins.PluginScanner;
import com.google.gerrit.sshd.CommandModule;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

public class ScalaPluginScanner implements PluginScanner {
  private final String pluginName;
  private final String pluginVersion;
  private final Set<Class<?>> scriptClasses;
  private Class<? extends Module> sshModule;
  private Class<? extends Module> httpModule;
  private Class<? extends Module> sysModule;

  public ScalaPluginScanner(String pluginName, File srcFile,
      ScalaPluginScriptEngine scriptEngine)
      throws InvalidPluginException {
    this.pluginName = pluginName;
    this.pluginVersion = getPluginVersion(srcFile, pluginName);
    this.scriptClasses = loadScriptClasses(srcFile, scriptEngine);
    loadGuiceModules(this.scriptClasses);
  }

  private String getPluginVersion(File srcFile, String pluginName) {
    String srcFileName = srcFile.getName();
    int startPos = pluginName.length()+1;
    int endPos = srcFileName.lastIndexOf('.');
    return srcFileName.substring(startPos, endPos);
  }

  private Set<Class<?>> loadScriptClasses(File srcFile,
      ScalaPluginScriptEngine scriptEngine) throws InvalidPluginException {
    try {
      return scriptEngine.eval(srcFile);
    } catch (ClassNotFoundException | IOException e) {
      throw new InvalidPluginException(
          "Cannot evaluate script file " + srcFile, e);
    }
  }

  @SuppressWarnings("unchecked")
  private void loadGuiceModules(Set<Class<?>> scriptClasses) {
    for (Class<?> scriptClass : scriptClasses) {
      if (CommandModule.class.isAssignableFrom(scriptClass)) {
        sshModule = ((Class<? extends Module>) scriptClass);
      } else if (ServletModule.class.isAssignableFrom(scriptClass)) {
        httpModule = ((Class<? extends Module>) scriptClass);
      } else if (Module.class.isAssignableFrom(scriptClass)) {
        sysModule = ((Class<? extends Module>) scriptClass);
      }
    }
  }

  @Override
  public Manifest getManifest() throws IOException {
    String manifestString =
            "PluginName: " + pluginName + "\n" +
            "Implementation-Version: " + pluginVersion + "\n" +
            "Gerrit-ReloadMode: reload\n" +
            (sysModule == null ? "":("Gerrit-Module: " + sysModule.getName() + "\n")) +
            (httpModule == null ? "":("Gerrit-HttpModule: " + httpModule.getName() + " \n")) +
            (sshModule == null ? "":("Gerrit-SshModule: " + sshModule.getName() + "\n")) +
            "Gerrit-ApiType: PLUGIN\n";
    return new Manifest(new ByteArrayInputStream(manifestString.getBytes()));
  }

  @Override
  public Map<Class<? extends Annotation>, Iterable<ExtensionMetaData>> scan(
      String pluginName, Iterable<Class<? extends Annotation>> annotations)
      throws InvalidPluginException {
    ImmutableMap.Builder<Class<? extends Annotation>, Iterable<ExtensionMetaData>> result =
        ImmutableMap.builder();

    for (Class<? extends Annotation> annotation : annotations) {
      Set<ExtensionMetaData> classMetaDataSet = Sets.newHashSet();
      result.put(annotation, classMetaDataSet);

      for (Class<?> scriptClass : scriptClasses) {
        if (!Modifier.isAbstract(scriptClass.getModifiers())
            && scriptClass.getAnnotation(annotation) != null) {
          classMetaDataSet.add(new ExtensionMetaData(scriptClass.getName(),
              getExportAnnotationValue(scriptClass, annotation)));
        }
      }
    }
    return result.build();
  }

  private String getExportAnnotationValue(Class<?> scriptClass,
      Class<? extends Annotation> annotation) {
    return annotation == Export.class ? scriptClass.getAnnotation(Export.class)
        .value() : "";
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
