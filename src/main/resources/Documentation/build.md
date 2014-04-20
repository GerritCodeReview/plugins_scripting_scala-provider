Build
=====

This plugin is built with Buck.

Clone or link this plugin to the plugins directory of Gerrit's source
tree, and issue the command:

```
  buck build plugins/scala-provider
```

The output is created in

```
  buck-out/gen/plugins/scala-provider/scala-provider.jar
```

This project can be imported into the Eclipse IDE:

```
  ./tools/eclipse/project.py
```
