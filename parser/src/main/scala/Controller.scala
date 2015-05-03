import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scalikejdbc._
import scalikejdbc.config._
 
object Controller {
	val sources = List[ArticleFetcher](ArticleRetrieval, RssReader)
  def main(args : Array[String]) {
		implicit val session = AutoSession
		while(true) {
			println("Beginning processing of articles.")
			val stocks = List[(String,String)](("GOOG", "Google"))
			for ((ticker, stockName) <- stocks) {
				for (source <- sources) {
					for ((date, title, url) <- source.getArticles(ticker, stockName)) {
						try {
							val (importance, sentiment) = ArticleProcesser.processArticle(url, stockName)
							sql"insert into articles(stock,date,source,title,importance,sentiment) values (${ticker}, ${date}, ${url}, ${title}, ${sentiment}, ${importance})".update.apply()
							println("Article successfully added to database.")
						} catch {
							case e: IllegalArgumentException => {
								println("Article could not be parsed to extract a sentiment score.")
							}
						}
					}
				}
			}
			println("Refreshing daily scores.")
			Scorer.main(Array[String]())
			println("Data refresh complete.")
			wait(3600)
		}
	}
	
	def wait(seconds: Int) {
			val f = Future { Thread.sleep(seconds*1000) }
			Await.result(f, Duration.Inf)
	}
	
}