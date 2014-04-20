Gerrit Scala Provider Plugin.

This plugin provides Scala runtime environment for Gerrit plugins in Scala.
Scala language level and library is Ver. 2.11.0 and does not require any
external installation as everything is provided by this plugin as embedded
engine.

To test this series must be applied on top of Gerrit master [1].

To test deploy the review plugin [2] and copy this Scala Provider plugin
under `$gerrit_site/plugins` directory.

* [1] https://gerrit-review.googlesource.com/#/q/status:open+project:gerrit+branch:master+topic:scripting-reloaded
* [2] https://github.com/davido/gerrit-scala-plugin
