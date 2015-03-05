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
  id = 'org.scala-lang:scala-compiler:2.11.4',
  sha1 = 'fa6eaeeecc9c6080d4b2f407f207339f2ff97ad7',
  license = 'Apache2.0',
  deps = [
    ':scala-reflect',
  ],
)

maven_jar(
  name = 'scala-reflect',
  id = 'org.scala-lang:scala-reflect:2.11.4',
  sha1 = 'a212daed62de3d47034ea171dd3168a4dd85d08c',
  license = 'Apache2.0',
  deps = [
    ':scala-library',
  ],
)

maven_jar(
  name = 'scala-library',
  id = 'org.scala-lang:scala-library:2.11.4',
  sha1 = '734ac63e470706015ea786f7780c80db49f001a4',
  license = 'Apache2.0',
)
