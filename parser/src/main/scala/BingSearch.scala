import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model._
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import com.typesafe.config.ConfigFactory;
import search.web.BingSearcher

object BingSearch extends ArticleFetcher {
	val searcher = new BingSearcher()
	
	def getArticles(tickerSymbol: String, stockName: String): List[Result] = {
		var resultList = List[Result]()
		var results = searcher.searchNews(stockName, 50)
		for (result <- results.News) {
			resultList = (new DateTime(result.Date), result.Title, result.Url) :: resultList
		}
		return resultList
	}

}