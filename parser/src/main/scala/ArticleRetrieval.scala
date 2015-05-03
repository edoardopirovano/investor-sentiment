import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model._
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.xml._;
import scala.xml.Elem._;
import scala.xml.Node._;
import javax.xml.transform.sax.SAXResult;
import scala.xml.parsing.NoBindingFactoryAdapter;
import org.xml.sax.SAXException;
import com.typesafe.config.ConfigFactory;

object ArticleRetrieval extends ArticleFetcher {
	val conf = ConfigFactory.load();
	val guardianKey = conf.getString("apikeys.guardian");
	val alchemyKey = conf.getString("apikeys.alchemy");
	
	def getArticles(tickerSymbol: String, stockName: String): List[Result] = {
		var results = List[Result]()
		val client = new GuardianContentClient(guardianKey)
		val searchQuery = SearchQuery().q(stockName).pageSize(1) // Build a search query of 1 article about Google
		val response = Await.result(client.getResponse(searchQuery), Duration.Inf) // Wait for result to be returned
		for (result <- response.results) {
			results = (result.webPublicationDate, result.webTitle, result.webUrl) :: results
		}
		return results
	}

}