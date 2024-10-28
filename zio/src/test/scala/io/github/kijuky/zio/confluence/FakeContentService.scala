package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.Expansion
import com.atlassian.confluence.api.model.content.id.ContentId
import com.atlassian.confluence.api.model.content.{
  Content,
  ContentStatus,
  ContentType,
  Space
}
import com.atlassian.confluence.api.model.locator.ContentLocator
import com.atlassian.confluence.api.model.pagination.{PageRequest, PageResponse}
import com.atlassian.confluence.rest.client.RemoteContentService

import java.time.LocalDate
import java.util
import java.util.Optional
import java.util.concurrent.{CompletableFuture, CompletionStage}
import scala.jdk.CollectionConverters.*

case class FakeContentService(
  doFind: Seq[Expansion] => Unit = null,
  doCreate: Content => Content = null,
  doCreateDetail: (Content, Seq[Expansion]) => Content = null,
  doUpdate: Content => Content = null,
  doTrash: Content => Content = null,
  doRestore: Content => Content = null,
  doPurge: Content => Unit = null,
  doDelete: Content => Unit = null,
  doGetChildren: (
    Content,
    PageRequest,
    Seq[Expansion]
  ) => PageResponse[Content] = null,
  // RemoteSingleContentFetcher
  doFetchMany: (ContentType, PageRequest) => PageResponse[Content] = null,
  doFetchMappedByContentType: PageRequest => Map[ContentType, PageResponse[
    Content
  ]] = null,
  // RemoteParameterContentFinder
  doWithSpace: Seq[Space] => Unit = null,
  doWithType: Seq[ContentType] => Unit = null,
  doWithCreatedDate: LocalDate => Unit = null,
  doWithTitle: String => Unit = null,
  // RemoteContentFinder
  doWithId: Seq[ContentId] => Unit = null,
  doWithIdAndVersion: (ContentId, Int) => Unit = null,
  doWithLocator: ContentLocator => Unit = null,
  doWithStatus: Seq[ContentStatus] => Unit = null,
  doWithAnyStatus: Unit => Unit = null,
  // RemoteSingleContentFetcher
  doFetch: Unit => Option[Content] = null
) extends RemoteContentService
    with RemoteContentService.RemoteContentFinder
    with RemoteContentService.RemoteSingleContentFetcher:
  override def find(
    expansions: Array[? <: Expansion]
  ): RemoteContentService.RemoteContentFinder =
    doFind(expansions.toIndexedSeq)
    this

  override def createCompletionStage(
    newContent: Content
  ): CompletionStage[Content] =
    CompletableFuture.supplyAsync(() => doCreate(newContent))

  override def createCompletionStage(
    newContent: Content,
    expansions: Array[? <: Expansion]
  ): CompletionStage[Content] =
    CompletableFuture.supplyAsync(() =>
      doCreateDetail(newContent, expansions.toIndexedSeq)
    )

  override def updateCompletionStage(
    content: Content
  ): CompletionStage[Content] =
    CompletableFuture.supplyAsync(() => doUpdate(content))

  override def trashCompletionStage(
    content: Content
  ): CompletionStage[Content] =
    CompletableFuture.supplyAsync(() => doTrash(content))

  override def restoreCompletionStage(
    content: Content
  ): CompletionStage[Content] =
    CompletableFuture.supplyAsync(() => doRestore(content))

  override def purgeCompletionStage(content: Content): CompletionStage[Void] =
    CompletableFuture.runAsync(() => doPurge(content))

  override def deleteCompletionStage(content: Content): CompletionStage[Void] =
    CompletableFuture.runAsync(() => doDelete(content))

  override def getChildrenCompletionStage(
    parent: Content,
    pageRequest: PageRequest,
    expansion: Array[? <: Expansion]
  ): CompletionStage[PageResponse[Content]] =
    CompletableFuture.supplyAsync(() =>
      doGetChildren(parent, pageRequest, expansion.toIndexedSeq)
    )

  override def fetchManyCompletionStage(
    contentType: ContentType,
    request: PageRequest
  ): CompletionStage[PageResponse[Content]] =
    CompletableFuture.supplyAsync(() => doFetchMany(contentType, request))

  override def fetchMappedByContentTypeCompletionStage(
    request: PageRequest
  ): CompletionStage[java.util.Map[ContentType, PageResponse[Content]]] =
    CompletableFuture.supplyAsync(() =>
      doFetchMappedByContentType(request).asJava
    )

  override def withSpace(
    space: Array[? <: Space]
  ): RemoteContentService.RemoteParameterContentFinder =
    doWithSpace(space.toIndexedSeq)
    this

  override def withType(
    contentType: Array[? <: ContentType]
  ): RemoteContentService.RemoteParameterContentFinder =
    doWithType(contentType.toIndexedSeq)
    this

  override def withCreatedDate(
    time: LocalDate
  ): RemoteContentService.RemoteParameterContentFinder =
    doWithCreatedDate(time)
    this

  override def withTitle(
    title: String
  ): RemoteContentService.RemoteParameterContentFinder =
    doWithTitle(title)
    this

  override def withId(id: ContentId): RemoteContentService.RemoteContentFinder =
    doWithId(Seq(id))
    this

  override def withIdAndVersion(
    id: ContentId,
    version: Int
  ): RemoteContentService.RemoteContentFinder =
    doWithIdAndVersion(id, version)
    this

  override def withId(
    first: ContentId,
    tail: Array[? <: ContentId]
  ): RemoteContentService.RemoteSingleContentFetcher =
    doWithId(first +: tail.toIndexedSeq)
    this

  override def withId(
    contentIds: java.lang.Iterable[ContentId]
  ): RemoteContentService.RemoteSingleContentFetcher =
    doWithId(contentIds.asScala.toIndexedSeq)
    this

  override def withLocator(
    locator: ContentLocator
  ): RemoteContentService.RemoteContentFinder =
    doWithLocator(locator)
    this

  override def withStatus(
    status: Array[? <: ContentStatus]
  ): RemoteContentService.RemoteContentFinder =
    doWithStatus(status.toIndexedSeq)
    this

  override def withAnyStatus(): RemoteContentService.RemoteContentFinder =
    doWithAnyStatus(())
    this

  override def fetchCompletionStage(): CompletionStage[Optional[Content]] =
    CompletableFuture.supplyAsync(() =>
      doFetch(()).map(Optional.of).getOrElse(Optional.empty())
    )
