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
  id = 'org.scala-lang:scala-compiler:2.11.6',
  sha1 = 'fa7d8a52d53c6571bce334b2c1d3d9546314dde3',
  license = 'Apache2.0',
  deps = [
    ':scala-reflect',
  ],
)

maven_jar(
  name = 'scala-reflect',
  id = 'org.scala-lang:scala-reflect:2.11.6',
  sha1 = 'f539c9c9cf312472b3a7771ac85ecda859657a1d',
  license = 'Apache2.0',
  deps = [
    ':scala-library',
  ],
)

maven_jar(
  name = 'scala-library',
  id = 'org.scala-lang:scala-library:2.11.6',
  sha1 = 'be3457b4b748df35bffba675d8cddf44e9df4f7b',
  license = 'Apache2.0',
)
