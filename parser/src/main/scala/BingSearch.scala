import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import search.web.BingSearcher

object BingSearch extends ArticleSearcher {
	val conf = ConfigFactory.load();
	val bingKey = conf.getString("apikeys.bing");
	val limit = conf.getInt("limits.bing");
	val searcher = new BingSearcher(bingKey)
	
	def getArticles(tickerSymbol: String, stockName: String): List[Result] = {
		var resultList = List[Result]()
		var offset = 0
		var results = searcher.searchNews(stockName, 10, 0)
		var didFind = true
		while (didFind && offset < limit) {
			didFind = false
			for (result <- results.News) {
				didFind = true
				resultList = (new DateTime(result.Date), result.Title, result.Url) :: resultList
			}
			offset += 10
			results = searcher.searchNews(stockName, 10, offset)
		}
		return resultList
	}

}