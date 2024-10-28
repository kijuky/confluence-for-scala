package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.content.Content
import com.atlassian.confluence.api.model.pagination.PageRequest
import com.atlassian.confluence.api.model.search.{
  SearchContext,
  SearchOptions,
  SearchPageResponse,
  SearchResult
}
import com.atlassian.confluence.api.model.{Expansion, Expansions}
import com.atlassian.confluence.rest.client.RemoteCQLSearchService

import java.util.concurrent.{CompletableFuture, CompletionStage}

case class FakeSearchService(
  doSearch: (
    String,
    SearchContext,
    PageRequest,
    Seq[Expansion]
  ) => SearchPageResponse[Content] = null,
  doSearch2: (
    String,
    PageRequest,
    Seq[Expansion]
  ) => SearchPageResponse[Content] = null,
  doSearch3: (String, Seq[Expansion]) => SearchPageResponse[Content] = null,
  doCountContent: String => Int = null,
  doCountContent2: (String, SearchContext) => Int = null,
  doSearch4: (
    String,
    SearchOptions,
    PageRequest,
    Seq[Expansion]
  ) => SearchPageResponse[SearchResult[? <: AnyRef]] = null
) extends RemoteCQLSearchService:
  override def searchContentCompletionStage(
    cqlInput: String,
    searchContext: SearchContext,
    pageRequest: PageRequest,
    expansions: Array[? <: Expansion]
  ): CompletionStage[SearchPageResponse[Content]] =
    CompletableFuture.supplyAsync(() =>
      doSearch(cqlInput, searchContext, pageRequest, expansions.toIndexedSeq)
    )

  override def searchContentCompletionStage(
    cql: String,
    request: PageRequest,
    expansions: Array[? <: Expansion]
  ): CompletionStage[SearchPageResponse[Content]] =
    CompletableFuture.supplyAsync(() =>
      doSearch2(cql, request, expansions.toIndexedSeq)
    )

  override def searchContentCompletionStage(
    cql: String,
    expansions: Array[? <: Expansion]
  ): CompletionStage[SearchPageResponse[Content]] =
    CompletableFuture.supplyAsync(() => doSearch3(cql, expansions.toIndexedSeq))

  override def countContent(cql: String): Int =
    doCountContent(cql)

  override def countContent(cql: String, searchContext: SearchContext): Int =
    doCountContent2(cql, searchContext)

  override def countContentCompletionStage(
    cql: String
  ): CompletionStage[Integer] =
    CompletableFuture.supplyAsync(() => Integer.valueOf(doCountContent(cql)))

  override def countContentCompletionStage(
    cql: String,
    searchContext: SearchContext
  ): CompletionStage[Integer] =
    CompletableFuture.supplyAsync(() =>
      Integer.valueOf(doCountContent2(cql, searchContext))
    )

  override def searchCompletionStage(
    cql: String,
    searchOptions: SearchOptions,
    pageRequest: PageRequest,
    expansions: Array[? <: Expansion]
  ): CompletionStage[SearchPageResponse[SearchResult[?]]] =
    CompletableFuture.supplyAsync(() =>
      doSearch4(cql, searchOptions, pageRequest, expansions.toIndexedSeq)
    )
