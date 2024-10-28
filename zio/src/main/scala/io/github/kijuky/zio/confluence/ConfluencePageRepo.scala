package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.Expansion
import zio.*

trait ConfluencePageRepo:
  def ref(id: Long): Task[ConfluencePage]
  def fetch(space: String, title: String): Task[Option[ConfluencePage]]
  def fetch(
    space: String,
    title: String,
    expansions: Seq[Expansion]
  ): Task[Option[ConfluencePage]]
  def create(
    space: String,
    title: String,
    body: String,
    parent: Option[ConfluencePage],
    labels: Seq[String]
  ): Task[ConfluencePage]

object ConfluencePageRepo:
  def ref(id: Long): RIO[ConfluencePageRepo, ConfluencePage] =
    ZIO.serviceWithZIO(_.ref(id))
  def fetch(
    space: String,
    title: String
  ): RIO[ConfluencePageRepo, Option[ConfluencePage]] =
    ZIO.serviceWithZIO(_.fetch(space, title))
  def fetch(
    space: String,
    title: String,
    expansions: Seq[Expansion]
  ): RIO[ConfluencePageRepo, Option[ConfluencePage]] =
    ZIO.serviceWithZIO(_.fetch(space, title, expansions))
  def create(
    space: String,
    title: String,
    body: String,
    parent: Option[ConfluencePage],
    labels: Seq[String]
  ): RIO[ConfluencePageRepo, ConfluencePage] =
    ZIO.serviceWithZIO(_.create(space, title, body, parent, labels))
