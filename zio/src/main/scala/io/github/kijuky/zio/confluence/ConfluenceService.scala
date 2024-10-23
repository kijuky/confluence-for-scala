package io.github.kijuky.zio.confluence

import zio.*

object ConfluenceService {
  def layer(
    baseUrl: String = "",
    accessToken: String = ""
  ): TaskLayer[Confluence] =
    ZLayer.scoped {
      ZIO.acquireRelease {
        for {
          optBaseUrl <- System.env("CONFLUENCE_BASE_URL")
          baseUrl <- ZIO.succeed(optBaseUrl.getOrElse(baseUrl))
          optAccessToken <- System.env("CONFLUENCE_ACCESS_TOKEN")
          accessToken <- ZIO.succeed(optAccessToken.getOrElse(accessToken))
        } yield Confluence(baseUrl, accessToken)
      } { confluence =>
        ZIO.succeed(confluence.close())
      }
    }
}
