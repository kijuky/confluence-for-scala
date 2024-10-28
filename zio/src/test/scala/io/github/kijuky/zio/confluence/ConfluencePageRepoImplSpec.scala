package io.github.kijuky.zio.confluence

import com.atlassian.confluence.api.model.*
import com.atlassian.confluence.api.model.content.*
import zio.*
import zio.test.*

object ConfluencePageRepoImplSpec extends ZIOSpecDefault:
  def spec: Spec[TestEnvironment, Any] = suiteAll("ConfluencePageRepoImplSpec"):
    suiteAll("ref"):
      suite("サービスが存在する場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          actual <- sut.ref(123456)
        yield Chunk(test("参照を返すこと"):
          assertTrue:
            actual.id.asLong == 123456
        )
      .provide(ZLayer:
        for service <- ZIO.succeed(FakeConfluenceService())
        yield ConfluencePageRepoImpl(service)
      )

    suiteAll("fetch(space,title)"):
      suite("searchContentCompletionStageがページを返す場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          actual <- sut.fetch("space", "title")
          Some(page) = actual: @unchecked
        yield Chunk(test("ページを返すこと"):
          assertTrue:
            page.title == "title"
        )
      .provide(ZLayer:
        val searchService = FakeSearchService(doSearch3 =
          (_, _) =>
            FakeSearchPageResponse(results = Seq(FakeContent(title = "title")))
        )
        for service <-
            ZIO.succeed(FakeConfluenceService(searchService = searchService))
        yield ConfluencePageRepoImpl(service)
      )

      suite("searchContentCompletionStageがページを返さない場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          actual <- sut.fetch("space", "title")
        yield Chunk(test("ページを返さないこと"):
          assertTrue:
            actual.isEmpty
        )
      .provide(ZLayer:
        val searchService = FakeSearchService(doSearch3 =
          (_, _) => FakeSearchPageResponse(results = Nil)
        )
        for service <-
            ZIO.succeed(FakeConfluenceService(searchService = searchService))
        yield ConfluencePageRepoImpl(service)
      )

      suite("searchContentCompletionStageが異常終了した場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          isFailure <- sut.fetch("space", "title").isFailure
        yield Chunk(test("失敗すること"):
          assertTrue:
            isFailure
        )
      .provide(ZLayer:
        val searchService =
          FakeSearchService(doSearch3 = (_, _) => throw RuntimeException())
        for service <-
            ZIO.succeed(FakeConfluenceService(searchService = searchService))
        yield ConfluencePageRepoImpl(service)
      )

    suiteAll("fetch(space,title,expansions)"):
      suite("fetchCompletionStageがページを返す場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          actual <- sut.fetch(
            "space",
            "title",
            Seq(Expansion(Content.Expansions.BODY))
          )
          Some(page) = actual: @unchecked
        yield Chunk(test("ページを返すこと"):
          assertTrue:
            page.title == "title"
        )
      .provide(ZLayer:
        val searchService = FakeSearchService(doSearch3 =
          (_, _) => FakeSearchPageResponse(results = Seq(FakeContent()))
        )
        val contentService = FakeContentService(
          doFind = _ => (),
          doWithId = _ => (),
          doFetch = _ => Some(FakeContent(title = "title"))
        )
        for service <- ZIO.succeed(
            FakeConfluenceService(
              searchService = searchService,
              contentService = contentService
            )
          )
        yield ConfluencePageRepoImpl(service)
      )

      suite("fetchCompletionStageがページを返さない場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          actual <- sut.fetch(
            "space",
            "title",
            Seq(Expansion(Content.Expansions.BODY))
          )
        yield Chunk(test("ページを返さないこと"):
          assertTrue:
            actual.isEmpty
        )
      .provide(ZLayer:
        val searchService = FakeSearchService(doSearch3 =
          (_, _) => FakeSearchPageResponse(results = Seq(FakeContent()))
        )
        val contentService = FakeContentService(
          doFind = _ => (),
          doWithId = _ => (),
          doFetch = _ => None
        )
        for service <- ZIO.succeed(
            FakeConfluenceService(
              searchService = searchService,
              contentService = contentService
            )
          )
        yield ConfluencePageRepoImpl(service)
      )

      suite("fetchCompletionStageが異常終了した場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          isFailure <- sut
            .fetch("space", "title", Seq(Expansion(Content.Expansions.BODY)))
            .isFailure
        yield Chunk(test("失敗すること"):
          assertTrue:
            isFailure
        )
      .provide(ZLayer:
        val searchService = FakeSearchService(doSearch3 =
          (_, _) => FakeSearchPageResponse(results = Seq(FakeContent()))
        )
        val contentService = FakeContentService(
          doFind = _ => (),
          doWithId = _ => (),
          doFetch = _ => throw RuntimeException()
        )
        for service <- ZIO.succeed(
            FakeConfluenceService(
              searchService = searchService,
              contentService = contentService
            )
          )
        yield ConfluencePageRepoImpl(service)
      )

      suite("searchContentCompletionStageがページを返さない場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          actual <- sut.fetch(
            "space",
            "title",
            Seq(Expansion(Content.Expansions.BODY))
          )
        yield Chunk(test("ページを返さないこと"):
          assertTrue:
            actual.isEmpty
        )
      .provide(ZLayer:
        val searchService = FakeSearchService(doSearch3 =
          (_, _) => FakeSearchPageResponse(results = Nil)
        )
        for service <-
            ZIO.succeed(FakeConfluenceService(searchService = searchService))
        yield ConfluencePageRepoImpl(service)
      )

      suite("searchContentCompletionStageが異常終了した場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          isFailure <- sut
            .fetch("space", "title", Seq(Expansion(Content.Expansions.BODY)))
            .isFailure
        yield Chunk(test("失敗すること"):
          assertTrue:
            isFailure
        )
      .provide(ZLayer:
        val searchService =
          FakeSearchService(doSearch3 = (_, _) => throw RuntimeException())
        for service <-
            ZIO.succeed(FakeConfluenceService(searchService = searchService))
        yield ConfluencePageRepoImpl(service)
      )

    suiteAll("create"):
      suite("createCompletionStageがページを返す場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          actual <- sut.create("space", "title", "body", None, Nil)
        yield Chunk(test("ページを返すこと"):
          assertTrue:
            actual.title == "title"
        )
      .provide(ZLayer:
        val contentService =
          FakeContentService(doCreate = _ => FakeContent(title = "title"))
        for service <-
            ZIO.succeed(FakeConfluenceService(contentService = contentService))
        yield ConfluencePageRepoImpl(service)
      )

      suite("createCompletionStageが異常終了する場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          isFailure <- sut.create("space", "title", "body", None, Nil).isFailure
        yield Chunk(test("失敗すること"):
          assertTrue:
            isFailure
        )
      .provide(ZLayer:
        val contentService =
          FakeContentService(doCreate = _ => throw RuntimeException())
        for service <-
            ZIO.succeed(FakeConfluenceService(contentService = contentService))
        yield ConfluencePageRepoImpl(service)
      )

      suite("ラベルがある場合"):
        for
          sut <- ZIO.service[ConfluencePageRepoImpl]
          actual <- sut.create("space", "title", "body", None, Seq("label"))
        yield Chunk(test("ページを返すこと"):
          assertTrue:
            actual.title == "title"
        )
      .provide(ZLayer:
        val contentService =
          FakeContentService(doCreate = _ => FakeContent(title = "title"))
        val contentLabelService =
          FakeContentLabelService(doAddLabels =
            (_, _) => Seq(FakeLabel(name = "label"))
          )
        for service <- ZIO.succeed(
            FakeConfluenceService(
              contentService = contentService,
              contentLabelService = contentLabelService
            )
          )
        yield ConfluencePageRepoImpl(service)
      )
