Gerrit Scala Provider Plugin
=============================

This plugin provides Scala runtime environment for Gerrit plugins in Scala.

To build link this directory under Gerrit's tree plugins directory and run:

```
  buck build plugins/scala-provider:scala-provider
```

The resulting artifact can be found under:

```
 buck-out/gen/plugins/scala-provider/scala-provider.jar
```

A sample Scala hello world script is:
```
  import com.google.gerrit.sshd._
  import com.google.gerrit.extensions.annotations._

  @Export("scala")
  class ScalaCommand extends SshCommand {
    override def run = stdout println "Hello from Scala!"
  }
```

Copy and past the above sample Scala fragment into the file $GERRIT_SITE/plugin/hello-1.0.scala.

The "hello" plugin version 1.0 will be automatically loaded into Gerrit and will provide a new
SSH command "hello scala".
