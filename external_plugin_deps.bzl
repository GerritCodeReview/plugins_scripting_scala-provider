load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
  maven_jar(
    name = 'scala_compiler',
    artifact = 'org.scala-lang:scala-compiler:2.11.6',
    sha1 = 'fa7d8a52d53c6571bce334b2c1d3d9546314dde3',
    exports = [
      '@scala_reflect//jar',
    ],
  )

  maven_jar(
    name = 'scala_reflect',
    artifact = 'org.scala-lang:scala-reflect:2.11.6',
    sha1 = 'f539c9c9cf312472b3a7771ac85ecda859657a1d',
    exports = [
      '@scala_library//jar',
    ],
  )

  maven_jar(
    name = 'scala_library',
    artifact = 'org.scala-lang:scala-library:2.11.6',
    sha1 = 'be3457b4b748df35bffba675d8cddf44e9df4f7b',
  )
