import sbt._

object Dependencies {
  object Versions {
    val zio             = "1.0.5"
    val cats            = "2.4.2"
    val zioLog          = "0.5.7"
    val tapir           = "0.17.16"
    val doobie          = "0.12.1"
    val circe           = "0.13.0"
    val circeDerivation = "0.13.0-M5"
    val chimney         = "0.6.1"
    val enumeratum      = "1.6.0"
  }

  /* ZIO */
  lazy val zioCore    = "dev.zio"  %% "zio"               % Versions.zio
  lazy val zioTest    = "dev.zio"  %% "zio-test"          % Versions.zio % Test
  lazy val zioLog     = "dev.zio"  %% "zio-logging"       % Versions.zioLog
  lazy val zioSlf4j   = "dev.zio"  %% "zio-logging-slf4j" % Versions.zioLog
  lazy val zioDeps = Seq(zioCore, zioTest, zioLog, zioSlf4j)

  /* CATS */
  lazy val cats = "org.typelevel" %% "cats-core" % Versions.cats

  /* TAPIR */
  lazy val tapirZio         = "com.softwaremill.sttp.tapir"  %% "tapir-zio"                 % Versions.tapir
  lazy val tapirHttp4s      = "com.softwaremill.sttp.tapir"  %% "tapir-zio-http4s-server"   % Versions.tapir
  lazy val tapirCirce       = "com.softwaremill.sttp.tapir"  %% "tapir-json-circe"          % Versions.tapir
  lazy val tapirOpenApi     = "com.softwaremill.sttp.tapir"  %% "tapir-openapi-circe-yaml"  % Versions.tapir
  lazy val tapirDocs        = "com.softwaremill.sttp.tapir"  %% "tapir-openapi-docs"        % Versions.tapir
  lazy val tapirSwagger     = "com.softwaremill.sttp.tapir"  %% "tapir-swagger-ui-http4s"   % Versions.tapir
  lazy val tapirEnumeratum  = "com.softwaremill.sttp.tapir"  %% "tapir-enumeratum"          % Versions.tapir
  lazy val tapirDeps        = Seq(tapirZio, tapirHttp4s, tapirCirce, tapirOpenApi, tapirDocs, tapirSwagger, tapirEnumeratum)

  /* DOOBIE */
  lazy val doobieCore       = "org.tpolecat"                 %% "doobie-core"                % Versions.doobie
  lazy val doobieHikari     = "org.tpolecat"                 %% "doobie-hikari"              % Versions.doobie
  lazy val doobiePostgres   = "org.tpolecat"                 %% "doobie-postgres"            % Versions.doobie
  lazy val doobieCirce      = "org.tpolecat"                 %% "doobie-postgres-circe"      % Versions.doobie
  lazy val doobieDeps       = Seq(doobieCore, doobieHikari, doobiePostgres, doobieCirce)

  /* ENUMERATUM */
  lazy val enumeratum       = "com.beachape"                 %% "enumeratum"                    % Versions.enumeratum
  lazy val enumeratumCirce  = "com.beachape"                 %% "enumeratum-circe"              % Versions.enumeratum
  lazy val enumeratumDoobie = "com.beachape"                 %% "enumeratum-doobie"             % Versions.enumeratum
  lazy val enumeratumDeps   = Seq(enumeratum, enumeratumCirce, enumeratumDoobie)

  /* CIRCE */
  lazy val circeCore        = "io.circe"        %% "circe-core"                    % Versions.circe
  lazy val circeGeneric     = "io.circe"        %% "circe-generic"                 % Versions.circe
  lazy val circeExtras      = "io.circe"        %% "circe-generic-extras"          % Versions.circe
  lazy val circeLiteral     = "io.circe"        %% "circe-literal"                 % Versions.circe
  lazy val circeDerivation  = "io.circe"        %% "circe-derivation"              % Versions.circeDerivation
  lazy val circeAnnotations = "io.circe"        %% "circe-derivation-annotations"  % Versions.circeDerivation
  lazy val circeDeps        = Seq(circeCore, circeGeneric, circeExtras, circeLiteral, circeDerivation, circeAnnotations)

  /* OTHER */
  lazy val chimney          = "io.scalaland"    %% "chimney"                       % Versions.chimney

  lazy val rootDependencies: Seq[ModuleID] =
    zioDeps ++
    tapirDeps ++
    doobieDeps ++
    enumeratumDeps ++
    circeDeps ++
    Seq(cats, chimney)
}
