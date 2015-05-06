import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import search.web.FarooSearcher

object FarooSearch extends ArticleFetcher {
	val conf = ConfigFactory.load();
	val farooKey = conf.getString("apikeys.faroo");
	val limit = conf.getInt("limits.faroo");
	val searcher = new FarooSearcher(farooKey)

	def getArticles(tickerSymbol: String, stockName: String): List[Result] = {
		var resultList = List[Result]()
		var offset = 0
		var res = searcher.search(stockName)
		var didFind = true
		while (didFind && offset < limit) {
			didFind = false
			for (result <- res.results) {
				didFind = true
				resultList = (new DateTime(result.date), result.title, result.url) :: resultList
			}
			offset += 10
			res = searcher.search(stockName, 10, offset)
		}
		return resultList
	}

}