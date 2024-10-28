package io.github.kijuky.zio.confluence

import zio.*

trait ConfluenceServiceProvider:
  def get: Task[ConfluenceService]

object ConfluenceServiceProvider:
  def get: RIO[ConfluenceServiceProvider, ConfluenceService] =
    ZIO.serviceWithZIO(_.get)
