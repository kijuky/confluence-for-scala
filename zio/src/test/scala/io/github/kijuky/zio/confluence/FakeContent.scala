package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.content.*
import com.atlassian.confluence.api.model.link.Link
import com.atlassian.confluence.api.model.pagination.PageResponse
import com.atlassian.confluence.api.model.permissions.{
  ContentRestriction,
  OperationCheckResult,
  OperationKey
}

import java.util.Collections
import scala.jdk.CollectionConverters.*

object FakeContent:
  def apply(
    contentType: ContentType = ContentType.PAGE,
    id: Long = 0,
    status: ContentStatus = null,
    title: String = null,
    links: Seq[Link] = null,
    history: History = null,
    space: Space = null,
    container: Container = null,
    parent: Content = null,
    ancestors: Seq[Content] = null,
    operations: Seq[OperationCheckResult] = null,
    children: Map[ContentType, PageResponse[Content]] = null,
    descendants: Map[ContentType, PageResponse[Content]] = null,
    body: Map[ContentRepresentation, ContentBody] = null,
    metadata: Map[String, AnyRef] = null,
    extensions: Map[String, AnyRef] = null,
    version: Version = null,
    restrictions: Map[OperationKey, ContentRestriction] = null,
    position: Int = 0
  ): Content =
    val builder = Content.builder(contentType, id)
    builder.status(status)
    builder.title(title)
    Option(links).foreach(_.foreach(builder.addLink))
    Option(history).foreach(builder.history)
    Option(space).foreach(builder.space)
    Option(container).foreach(builder.container)
    builder.parent(parent)
    builder.ancestors(Option(ancestors).map(_.asJava).orNull)
    builder.operations(Option(operations).map(_.asJava).orNull)
    builder
      .children(Option(children).map(_.asJava).getOrElse(Collections.emptyMap))
    builder.descendants(
      Option(descendants).map(_.asJava).getOrElse(Collections.emptyMap)
    )
    builder.body(Option(body).map(_.asJava).getOrElse(Collections.emptyMap))
    builder
      .metadata(Option(metadata).map(_.asJava).getOrElse(Collections.emptyMap))
    builder.extensions(
      Option(extensions).map(_.asJava).getOrElse(Collections.emptyMap)
    )
    Option(version).foreach(builder.version)
    builder.restrictions(
      Option(restrictions).map(_.asJava).getOrElse(Collections.emptyMap)
    )
    builder.position(position)
    builder.build()
