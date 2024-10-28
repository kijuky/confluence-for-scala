package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.pagination.PageRequest
import com.atlassian.confluence.api.model.search.{
  SearchContext,
  SearchOptions,
  SearchPageResponse,
  SearchResult
}

import java.util.Optional
import scala.jdk.CollectionConverters.*

object FakeSearchPageResponse:
  def apply[T](
    results: Seq[T] = Nil,
    hasMore: Boolean = false,
    cqlQuery: String = null,
    pageRequest: PageRequest = null,
    totalSize: Int = 0,
    searchDuration: Int = 0,
    archivedResultCount: Option[Int] = None
  ): SearchPageResponse[T] =
    SearchPageResponse[T](
      results.asJava,
      hasMore,
      cqlQuery,
      pageRequest,
      totalSize,
      searchDuration,
      archivedResultCount
        .map(i => Optional.of(Integer.valueOf(i)))
        .getOrElse(Optional.empty())
    )
