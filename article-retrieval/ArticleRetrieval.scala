import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model._
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

object ArticleRetrieval {
  def main(args: Array[String]) = {
		val client = new GuardianContentClient("5q4xyarnabx5jzxr92rch59v")
		val searchQuery = SearchQuery().q("Google").pageSize(20) // Build a search query of 20 articles about Google
		val response = Await.result(client.getResponse(searchQuery), Duration.Inf) // Wait for result to be returned
		for (result <- response.results) println(result.webPublicationDate + " - " + result.webTitle) // Print titles of articles
	}
}