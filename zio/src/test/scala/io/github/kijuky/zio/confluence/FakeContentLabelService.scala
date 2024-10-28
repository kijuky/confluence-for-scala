package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.content.id.ContentId
import com.atlassian.confluence.api.model.content.*
import com.atlassian.confluence.api.model.pagination.PageRequest
import com.atlassian.confluence.rest.client.RemoteContentLabelService

import java.util.concurrent.{CompletableFuture, CompletionStage}
import scala.jdk.CollectionConverters.*

case class FakeContentLabelService(
  doGetLabels: (ContentId, Seq[Label.Prefix], PageRequest) => Seq[Label] = null,
  doAddLabels: (ContentId, Seq[Label]) => Seq[Label] = null,
  doRemoveLabel: (ContentId, Label) => Unit = null
) extends RemoteContentLabelService:
  override def getLabelsCompletionStage(
    contentId: ContentId,
    prefixes: java.util.Collection[Label.Prefix],
    pageRequest: PageRequest
  ): CompletionStage[? <: java.lang.Iterable[Label]] =
    CompletableFuture.supplyAsync(() =>
      doGetLabels(contentId, prefixes.asScala.toSeq, pageRequest).asJava
    )

  override def addLabelsCompletionStage(
    contentId: ContentId,
    labels: java.lang.Iterable[Label]
  ): CompletionStage[? <: java.lang.Iterable[Label]] =
    CompletableFuture.supplyAsync(() =>
      doAddLabels(contentId, labels.asScala.toSeq).asJava
    )

  override def removeLabelCompletionStage(
    contentId: ContentId,
    label: Label
  ): CompletionStage[Void] =
    CompletableFuture.runAsync(() => doRemoveLabel(contentId, label))
