import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scalikejdbc._
import scalikejdbc.config._
 
object Controller {
	val sources = List[ArticleSearcher](GuardianSearch, YFSearch, BingSearch, FarooSearch)
  def main(args : Array[String]) {
		DBs.setupAll()
		implicit val session = AutoSession
		while(true) {
			println("Beginning refreshing of data.")
			SiteRank.purgeCache()
			val stocks = sql"select * from stocks".map(rs => (rs.string("stock"), rs.string("stockname"))).list.apply()
			for ((ticker, stockName) <- stocks) {
				var urls = sql"select source from articles where stock = ${ticker}".map(rs => rs.string("source")).list.apply()
				println("Searching for articles about " + stockName)
				for (source <- sources) {
					println("Searching using " + source.getClass)
					for ((date, title, url) <- source.getArticles(ticker, stockName) if !(urls contains url)) {
						urls = url :: urls
						try {
							val (importance, sentiment) = ArticleProcessor.processArticle(url, stockName)
							sql"insert into articles(stock,date,source,title,importance,sentiment) values (${ticker}, ${date}, ${url}, ${title}, ${importance}, ${sentiment})".update.apply()
							println("Article successfully added to database.")
						} catch {
							case e: IllegalArgumentException => {
								println("Article could not be parsed to extract a sentiment score.")
								sql"insert into articles(stock,date,source,title) values (${ticker}, ${date}, ${url}, ${title})".update.apply()
							}
						}
					}
				}
				println("Recalculating stock scores.")
				Scorer.doStock(ticker)
			}
			println("Data refresh complete.")
			wait(3600)
		}
		DBs.closeAll()
	}
	
	def wait(seconds: Int) {
			val f = Future { Thread.sleep(seconds*1000) }
			Await.result(f, Duration.Inf)
	}
	
}