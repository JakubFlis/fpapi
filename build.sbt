import sbt.Keys._
import Dependencies._

lazy val root =
  (project in file ("."))
    .settings(Seq(
      name := "fpapi",
      version := "0.0.9",
      scalaVersion := "2.13.5",
      organization := "Jakub Flis",
      libraryDependencies ++= rootDependencies
    ))