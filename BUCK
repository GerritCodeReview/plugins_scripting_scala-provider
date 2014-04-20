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
  id = 'org.scala-lang:scala-compiler:2.11.0',
  sha1 = '75d94f0ddcabb626832a992607acb6e2d6bc2a4a',
  license = 'Apache2.0',
  deps = [
    ':scala-reflect',
  ],
)

maven_jar(
  name = 'scala-reflect',
  id = 'org.scala-lang:scala-reflect:2.11.0',
  sha1 = '90be1e4ead42b3e9ac53044bfe1b032cd9d78229',
  license = 'Apache2.0',
  deps = [
    ':scala-library',
  ],
)

maven_jar(
  name = 'scala-library',
  id = 'org.scala-lang:scala-library:2.11.0',
  sha1 = 'aa8f7553253318c468f90ce58a85f94bd1a224eb',
  license = 'Apache2.0',
)
