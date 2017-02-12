load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "scala-provider",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Implementation-Title: Scala Provider",
        "Implementation-URL: https://gerrit.googlesource.com/plugins/scripting/scala-provider",
        "Gerrit-PluginName: scala-provider",
        "Gerrit-Module: com.googlesource.gerrit.plugins.scripting.scala.Module"
    ],
    deps = [
        "@scala_compiler//jar",
        "@scala_reflect//jar",
        "@scala_library//jar",
    ],
)
