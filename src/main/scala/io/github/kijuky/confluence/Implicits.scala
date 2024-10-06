package io.github.kijuky.confluence

import com.atlassian.confluence.api.model.Expansion
import com.atlassian.confluence.api.model.Expansion.combine
import com.atlassian.confluence.api.model.content.id.ContentId
import com.atlassian.confluence.api.model.content._
import com.atlassian.confluence.rest.client._
import com.atlassian.confluence.rest.client.authentication.AuthenticatedWebResourceProvider
import com.sun.jersey.api.client.filter.ClientFilter
import com.sun.jersey.api.client.{ClientRequest, ClientResponse}

import java.io.File
import java.util.concurrent.{ExecutorService, Executors}
import scala.collection.JavaConverters._
import scala.language.implicitConversions
import scala.xml._

object Implicits {
  def createConfluenceClient(
    baseUrl: String,
    accessToken: String
  ): ConfluenceClient = {
    new ConfluenceClient(
      baseUrl,
      new AuthenticatedWebResourceProvider(
        {
          val client = RestClientFactory.newClient()
          client.addFilter(new ClientFilter {
            override def handle(clientRequest: ClientRequest): ClientResponse =
              super.getNext.handle {
                clientRequest.getHeaders
                  .add("Authorization", s"Bearer $accessToken")
                clientRequest
              }
          })
          client
        },
        baseUrl,
        ""
      )
    )
  }

  class ConfluenceClient(
    val baseUrl: String,
    provider: AuthenticatedWebResourceProvider
  ) {
    private implicit val implicitConfluence: ConfluenceClient = this
    private lazy val executor: ExecutorService =
      Executors.newSingleThreadExecutor()
    private lazy val searchService: RemoteCQLSearchService =
      new RemoteCQLSearchServiceImpl(provider, executor)
    private lazy val contentService: RemoteContentService =
      new RemoteContentServiceImpl(provider, executor)
    private lazy val contentLabelService: RemoteContentLabelService =
      new RemoteContentLabelServiceImpl(provider, executor)

    /** ローカルに保存されたConfluenceのテンプレートページのXMLファイルを読み出します。 */
    def templateFromXML(file: File): String =
      XML.loadFile(file).child.toString

    def pageRef(id: Long): Content =
      Content.builder(ContentType.PAGE).id(ContentId.of(id)).build()

    def page(
      space: String,
      title: String,
      bodyStorage: String,
      parent: Option[Content]
    ): Content =
      Content
        .builder(ContentType.PAGE)
        .space(space)
        .title(title)
        .body(bodyStorage, ContentRepresentation.STORAGE)
        .parent(parent.orNull)
        .build()

    def fetchPage(space: String, title: String): Option[Content] = {
      val cql = s"""space = "$space" and title = "$title""""
      searchService.search(cql).headOption
    }

    def pageContent(
      space: String,
      title: String,
      expansions: Seq[Expansion]
    ): Option[Content] = {
      for {
        result <- fetchPage(space, title)
        content <- contentService.fetch(result, expansions)
      } yield content
    }

    def createPage(
      space: String,
      title: String,
      bodyStorage: String,
      parent: Option[Content] = None,
      labels: Seq[String] = Nil
    ): Content = {
      val content = page(space, title, bodyStorage, parent)
      val result = contentService.createPage(content)

      // （あれば）ラベルをつける
      if (labels.nonEmpty) {
        contentLabelService.addLabels(result, labels)
      }

      result
    }
  }

  object Expansions {
    private lazy val body = Content.Expansions.BODY

    /** 編集画面の記事コンテンツ */
    lazy val BODY_STORAGE: Expansion =
      combine(body, ContentRepresentation.STORAGE.getValue)

    /** 表示される記事コンテンツ */
    lazy val BODY_VIEW: Expansion =
      combine(body, ContentRepresentation.VIEW.getValue)
  }

  implicit def expansionToContentRepresentation(
    expansion: Expansion
  ): ContentRepresentation =
    expansion match {
      case Expansions.BODY_STORAGE => ContentRepresentation.STORAGE
      case Expansions.BODY_VIEW    => ContentRepresentation.VIEW
    }

  implicit class RichRemoteCQLSearchService(
    remoteCQLSearchService: RemoteCQLSearchService
  ) {
    def search(cql: String): Seq[Content] =
      remoteCQLSearchService
        .searchContentCompletionStage(cql)
        .toCompletableFuture
        .get()
        .getResults
        .asScala
        .toSeq
  }

  implicit class RichRemoteContentService(
    remoteContentService: RemoteContentService
  )(implicit confluenceClient: ConfluenceClient) {
    def fetch(content: Content, expansions: Seq[Expansion]): Option[Content] =
      remoteContentService
        .find(expansions: _*)
        .withId(content.id)
        .fetchCompletionStage()
        .toCompletableFuture
        .get()
        .map[Option[Content]](Some(_))
        .orElseGet(() => None)

    def createPage(content: Content): Content =
      remoteContentService
        .createCompletionStage(content)
        .toCompletableFuture
        .get()
  }

  implicit class RichRemoteContentLabelService(
    remoteContentLabelService: RemoteContentLabelService
  )(implicit confluence: ConfluenceClient) {
    def addLabels(content: Content, labelNames: Seq[String]): Seq[Label] = {
      val labels = labelNames.map(Label.builder(_).build()).asJava
      remoteContentLabelService
        .addLabelsCompletionStage(content.id, labels)
        .toCompletableFuture
        .get()
        .asScala
        .toSeq
    }
  }

  implicit class RichContent(content: Content)(implicit
    confluence: ConfluenceClient
  ) {
    def id: ContentId = content.getId
    def title: String = content.getTitle
    def body: Map[ContentRepresentation, ContentBody] =
      content.getBody.asScala.toMap
    def browseUrl =
      s"${confluence.baseUrl}/pages/viewpage.action?pageId=${id.asLong}"
  }

  implicit class RichContentBody(contentBody: ContentBody) {
    def value: String = contentBody.getValue
    def valueWithRoot: String = s"<root>$value</root>"
    def toXml: Elem = XML.loadString(valueWithRoot)
  }
}
