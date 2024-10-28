package io.github.kijuky.zio.confluence

import com.atlassian.confluence.rest.client.*
import com.atlassian.confluence.rest.client.authentication.AuthenticatedWebResourceProvider
import com.sun.jersey.api.client.{ClientRequest, ClientResponse}
import com.sun.jersey.api.client.filter.ClientFilter

import java.util.concurrent.{ExecutorService, Executors}

class ConfluenceService(
  val baseUrl: String,
  provider: AuthenticatedWebResourceProvider,
  executorService: ExecutorService
) extends AutoCloseable:
  lazy val searchService: RemoteCQLSearchService =
    RemoteCQLSearchServiceImpl(provider, executorService)
  lazy val contentService: RemoteContentService =
    RemoteContentServiceImpl(provider, executorService)
  lazy val contentLabelService: RemoteContentLabelService =
    RemoteContentLabelServiceImpl(provider, executorService)
  def close(): Unit =
    executorService.close()

object ConfluenceService:
  def apply(baseUrl: String, accessToken: String): ConfluenceService =
    new ConfluenceService(
      baseUrl,
      AuthenticatedWebResourceProvider(
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
      ),
      Executors.newSingleThreadExecutor()
    )
