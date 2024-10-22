package io.github.kijuky.zio.confluence

import com.atlassian.confluence.rest.client.*
import com.atlassian.confluence.rest.client.authentication.AuthenticatedWebResourceProvider
import com.sun.jersey.api.client.{ClientRequest, ClientResponse}
import com.sun.jersey.api.client.filter.ClientFilter

import java.util.concurrent.{ExecutorService, Executors}

final class Confluence(
  val baseUrl: String,
  provider: AuthenticatedWebResourceProvider,
  executorService: ExecutorService
) {
  lazy val searchService: RemoteCQLSearchService =
    new RemoteCQLSearchServiceImpl(provider, executorService)
  lazy val contentService: RemoteContentService =
    new RemoteContentServiceImpl(provider, executorService)
  lazy val contentLabelService: RemoteContentLabelService =
    new RemoteContentLabelServiceImpl(provider, executorService)
}

object Confluence {
  def apply(baseUrl: String, accessToken: String): Confluence =
    new Confluence(
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
      ),
      Executors.newSingleThreadExecutor()
    )
}
