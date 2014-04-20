include_defs('//lib/maven.defs')

define_license(name = 'scala')

gerrit_plugin(
  name = 'scala-provider',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/**/*']),
  manifest_entries = [
    'Implementation-Title: Scala Provider',
    'Implementation-URL: https://gerrit.googlesource.com/plugins/scripting/scala-provider',
    'Gerrit-PluginName: scala-provider',
    'Gerrit-Module: com.googlesource.gerrit.plugins.scripting.scala.Module'
  ],
  deps = [
    ':scala-compiler',
    ':scala-reflect',
    ':scala-library',
  ],
)

maven_jar(
  name = 'scala-compiler',
  id = 'org.scala-lang:scala-compiler:2.10.3',
  sha1 = 'fc9759d060ae131a73c020d477e25a14534cbedd',
  license = 'Apache2.0',
  deps = [
    ':scala-reflect',
  ],
)

maven_jar(
  name = 'scala-reflect',
  id = 'org.scala-lang:scala-reflect:2.10.3',
  sha1 = '16dc45094c2d8919d21ee16a46a7ff7fa2aa3c88',
  license = 'Apache2.0',
  deps = [
    ':scala-library',
  ],
)

maven_jar(
  name = 'scala-library',
  id = 'org.scala-lang:scala-library:2.10.3',
  sha1 = '21d99cee3d3e478255ef9fcc90b571fb2ab074fc',
  license = 'Apache2.0',
)
