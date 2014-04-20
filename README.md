Gerrit Scala Provider Plugin
=============================

This plugin provides Scala runtime environment for Gerrit plugins in Scala.

To test this series must be applied on top of Gerrit master [1].

To build link this directory under Gerrit's tree plugins directory and run:

```
  buck build plugins/scala-provider
```

The resulting artifact can be found under:

```
 buck-out/gen/plugins/scala-provider/scala-provider.jar
```

A sample Scala hello world script is:
```
  import com.google.gerrit.sshd._

  @CommandMetaData(name = "scala", description = "My first Gerrit SSH command in Scala")
  class ScalaCommand extends SshCommand {
    override def run = stdout println "Hello Scala world!"
  }

  class ScalaModule extends CommandModule {
    override def configure = {
      val helloCmd = Commands.named("hello")
      command(helloCmd).toProvider(new DispatchCommandProvider(helloCmd));
      command(helloCmd, classOf[ScalaCommand])
    }
  }
```

Edit the file $GERRIT_SITE/plugin/hello-1.0.scala with the Scala hello world 
sample content provided.

The "hello" plugin version 1.0 will be loaded into Gerrit and will provide a new
SSH command "hello scala".

* [1] https://gerrit-review.googlesource.com/#/q/status:open+project:gerrit+branch:master+topic:scripting-reloaded
