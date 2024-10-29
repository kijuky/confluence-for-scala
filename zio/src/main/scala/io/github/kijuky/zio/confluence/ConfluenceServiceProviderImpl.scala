package io.github.kijuky.zio.confluence

import zio.*

private case class ConfluenceServiceProviderImpl(confluence: ConfluenceService)
    extends ConfluenceServiceProvider:
  def get: Task[ConfluenceService] =
    ZIO.succeed(confluence)

object ConfluenceServiceProviderImpl:
  def layer(
    baseUrl: String = "",
    accessToken: String = ""
  ): TaskLayer[ConfluenceService] =
    ZLayer.scoped:
      ZIO.fromAutoCloseable:
        for
          optBaseUrl <- System.env("CONFLUENCE_BASE_URL")
          baseUrl <- ZIO.succeed(optBaseUrl.getOrElse(baseUrl))
          optAccessToken <- System.env("CONFLUENCE_ACCESS_TOKEN")
          accessToken <- ZIO.succeed(optAccessToken.getOrElse(accessToken))
        yield ConfluenceService(baseUrl, accessToken)
