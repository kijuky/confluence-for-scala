package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.content.Label

object FakeLabel:
  def apply(
    prefix: String = null,
    name: String = null,
    id: String = null
  ): Label =
    Label(prefix, name, id)
