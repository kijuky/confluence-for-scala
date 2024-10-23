package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.Expansion
import com.atlassian.confluence.api.model.content.id.ContentId
import com.atlassian.confluence.api.model.content.{
  Content,
  ContentRepresentation,
  ContentType,
  Label
}
import zio.*

import scala.concurrent.Future
import scala.jdk.CollectionConverters.*
import scala.util.Try

private case class ConfluencePageRepoImpl(confluence: Confluence)
    extends ConfluencePageRepo {
  override def ref(id: Long): Task[ConfluencePage] =
    ConfluencePage.applyZIO(
      Content.builder(ContentType.PAGE).id(ContentId.of(id)).build(),
      confluence.baseUrl
    )

  override def fetch(
    space: String,
    title: String
  ): Task[Option[ConfluencePage]] =
    val cql = s"""space = "$space" and title = "$title""""
    for {
      response <- ZIO.fromFuture(implicit ex =>
        Future(
          confluence.searchService
            .searchContentCompletionStage(cql)
            .toCompletableFuture
            .get()
        )
      )
    } yield Try(response.getResults.getFirst).toOption
      .map(new ConfluencePage(_, confluence.baseUrl))

  override def fetch(
    space: String,
    title: String,
    expansions: Seq[Expansion]
  ): Task[Option[ConfluencePage]] =
    for {
      result <- fetch(space, title)
      content <- result match {
        case Some(content) =>
          ZIO.fromFuture(implicit ec =>
            Future(
              confluence.contentService
                .find(expansions: _*)
                .withId(content.id)
                .fetchCompletionStage()
                .toCompletableFuture
                .get()
                .map[Option[Content]](Some(_))
                .orElseGet(() => None)
            )
          )
        case None =>
          ZIO.none
      }
    } yield content.map(new ConfluencePage(_, confluence.baseUrl))

  override def create(
    space: String,
    title: String,
    body: String,
    parent: Option[ConfluencePage],
    labels: Seq[String]
  ): Task[ConfluencePage] = {
    for {
      content <- createContent(space, title, body, parent)
      result <- ZIO.fromFuture(implicit ec =>
        Future(
          confluence.contentService
            .createCompletionStage(content)
            .toCompletableFuture
            .get()
        )
      )

      // （あれば）ラベルをつける
      _ <- ZIO.when(labels.nonEmpty)(
        ZIO.fromFuture(implicit ec =>
          Future(
            confluence.contentLabelService
              .addLabelsCompletionStage(
                result.getId,
                labels.map(Label.builder(_).build()).asJava
              )
              .toCompletableFuture
              .get()
          )
        )
      )
    } yield new ConfluencePage(result, confluence.baseUrl)
  }

  private def createContent(
    space: String,
    title: String,
    body: String,
    parent: Option[ConfluencePage]
  ): UIO[Content] = {
    ZIO.succeed(
      Content
        .builder(ContentType.PAGE)
        .space(space)
        .title(title)
        .body(body, ContentRepresentation.STORAGE)
        .parent(parent.map(_.content).orNull)
        .build()
    )
  }
}

object ConfluencePageRepoImpl {
  def layer: URLayer[Confluence, ConfluencePageRepo] =
    ZLayer(
      for confluence <- ZIO.service[Confluence]
      yield ConfluencePageRepoImpl(confluence)
    )
}
