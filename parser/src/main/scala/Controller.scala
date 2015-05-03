import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scalikejdbc._
import scalikejdbc.config._
 
object Controller {
	val sources = List[ArticleFetcher](ArticleRetrieval, RSSReader)
  def main(args : Array[String]) {
		DBs.setupAll()
		implicit val session = AutoSession
		while(true) {
			println("Beginning refreshing of data.")
			val stocks = List[(String,String)](("GOOG", "Google"))
			for ((ticker, stockName) <- stocks) {
				println("Searching for articles about " + stocks)
				for (source <- sources) {
					println("Searching using " + source.getClass.getName)
					for ((date, title, url) <- source.getArticles(ticker, stockName)) {
						try {
							val (importance, sentiment) = ArticleProcesser.processArticle(url, stockName)
							sql"insert into articles(stock,date,source,title,importance,sentiment) values (${ticker}, ${date}, ${url}, ${title}, ${importance}, ${sentiment})".update.apply()
							println("Article successfully added to database.")
						} catch {
							case e: IllegalArgumentException => {
								println("Article could not be parsed to extract a sentiment score.")
							}
						}
					}
				}
				scorer.doStock(ticker)
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