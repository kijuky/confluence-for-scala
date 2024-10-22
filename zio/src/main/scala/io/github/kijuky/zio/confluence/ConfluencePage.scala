package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.content.{
  Content,
  ContentBody,
  ContentRepresentation
}
import com.atlassian.confluence.api.model.content.id.ContentId
import zio.*

import scala.jdk.CollectionConverters.*

class ConfluencePage(val content: Content, baseUrl: String) {
  def id: ContentId = content.getId
  def title: String = content.getTitle
  def body: Map[ContentRepresentation, ContentBody] =
    content.getBody.asScala.toMap
  def browseUrl = s"$baseUrl/pages/viewpage.action?pageId=${id.asLong}"
}

object ConfluencePage {
  def applyZIO(content: Content, baseUrl: String): UIO[ConfluencePage] =
    ZIO.succeed(new ConfluencePage(content, baseUrl))
}
