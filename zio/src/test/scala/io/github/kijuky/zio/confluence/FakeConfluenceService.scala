package io.github.kijuky.zio.confluence

import com.atlassian.confluence.rest.client.authentication.AuthenticatedWebResourceProvider
import com.atlassian.confluence.rest.client.{
  RemoteCQLSearchService,
  RemoteContentLabelService,
  RemoteContentService
}

import java.util.concurrent.{ExecutorService, Executors}

class FakeConfluenceService(
  baseUrl: String,
  fakeSearchService: RemoteCQLSearchService,
  fakeContentService: RemoteContentService,
  fakeContentLabelService: RemoteContentLabelService,
  doClose: () => Unit
) extends ConfluenceService(baseUrl, null, null):
  override lazy val searchService: RemoteCQLSearchService =
    fakeSearchService
  override lazy val contentService: RemoteContentService =
    fakeContentService
  override lazy val contentLabelService: RemoteContentLabelService =
    fakeContentLabelService
  override def close(): Unit =
    doClose()

object FakeConfluenceService:
  def apply(
    baseUrl: String = null,
    searchService: RemoteCQLSearchService = null,
    contentService: RemoteContentService = null,
    contentLabelService: RemoteContentLabelService = null,
    doClose: () => Unit = null
  ): ConfluenceService =
    new FakeConfluenceService(
      baseUrl,
      searchService,
      contentService,
      contentLabelService,
      doClose
    )
