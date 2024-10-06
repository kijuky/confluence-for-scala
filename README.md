# confluence-for-scala

## Example

```shell
sbt console
```

```
import io.github.kijuky.confluence.Implicits._
val confluence = createConfluenceClient("https://confluence.example.com", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
implicit val c = confluence
val content = confluence.pageContent("space", "title", Seq(Expansions.BODY_VIEW)).get
content.body(com.atlassian.confluence.api.model.content.ContentRepresentation.VIEW).getValue
```
