import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model._
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import com.typesafe.config.ConfigFactory;

object GuardianReader extends ArticleFetcher {
	val conf = ConfigFactory.load();
	val guardianKey = conf.getString("apikeys.guardian");
	val alchemyKey = conf.getString("apikeys.alchemy");
	val numArticles = conf.getInt("guardian.numArticles");
	
	def getArticles(tickerSymbol: String, stockName: String): List[Result] = {
		var results = List[Result]()
		val client = new GuardianContentClient(guardianKey)
		val searchQuery = SearchQuery().q(stockName).pageSize(numArticles) // Build a search query of 50 articles about stock
		val response = Await.result(client.getResponse(searchQuery), Duration.Inf) // Wait for result to be returned
		for (result <- response.results) {
			results = (result.webPublicationDate, result.webTitle, result.webUrl) :: results
		}
		return results
	}

}