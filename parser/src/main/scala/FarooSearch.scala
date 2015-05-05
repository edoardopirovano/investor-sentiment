import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model._
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import com.typesafe.config.ConfigFactory;
import search.web.FarooSearcher

object FarooSearch extends ArticleFetcher {
	val searcher = new FarooSearcher()
	
	def getArticles(tickerSymbol: String, stockName: String): List[Result] = {
		var resultList = List[Result]()
		var res = searcher.search(stockName)
		for (result <- res.results) {
			resultList = (new DateTime(result.date), result.title, result.url) :: resultList
		}
		return resultList
	}

}