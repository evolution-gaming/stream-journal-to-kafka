addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

externalResolvers += Resolver.bintrayIvyRepo("evolutiongaming", "sbt-plugins")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.5")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")

addSbtPlugin("com.evolution" % "sbt-scalac-opts-plugin" % "0.0.9")