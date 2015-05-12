import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scalikejdbc._
import scalikejdbc.config._
import org.joda.time.DateTime
import java.sql.Date
import java.io.File
import ox.CSO._
import twitter4j._
 
object Controller {
	DBs.setupAll()
	implicit val session = AutoSession
	val sources = List[ArticleSearcher](GuardianSearch, YFSearch, BingSearch, FarooSearch)
	
	def wait(seconds: Int) {
			val f = Future { Thread.sleep(seconds*1000) }
			Await.result(f, Duration.Inf)
	}

  	def main(args : Array[String]) {
			val stocks = sql"select * from stocks".map(rs => (rs.string("stock"), rs.string("stockname"))).list.apply()

			val dir = new File("tmp")
			assert(!dir.exists,"[ERROR] Delete the /tmp directory before running.") // if the Controller fails to clean up
			dir.mkdir()
			
			var companies : List[String] = List() ; for ((ticker,stockname) <- stocks) {companies = stockname :: companies}
			
			/** Article processing */
			val processArticles : PROC = proc {
				for ((ticker, stockName) <- stocks) {
				var urls = sql"select source from articles where stock = ${ticker}".map(rs => rs.string("source")).list.apply()
				println("\n[SYSTEM] Searching for articles about " + stockName)
				for (source <- sources) {
					println("\n[SYSTEM] Searching using " + source.getClass)
					var articles = List[(DateTime, String, String)]()
					try {
						articles = source.getArticles(ticker, stockName)
					} catch {
						case e: Exception => {
							println("Articles count not be retrieved at this time.")
						}
					}
					for ((date, title, url) <- articles if !(urls contains url)) {
						urls = url :: urls
						try {
							val (importance, sentiment) = ArticleProcessor.processArticle(url, stockName)
							sql"insert into articles(stock,date,source,title,importance,sentiment) values (${ticker}, ${date}, ${url}, ${title}, ${importance}, ${sentiment})".update.apply()
							println("\n[DATABASE] Article successfully added to database.")
						} catch {
							case e: IllegalArgumentException => {
								println("\n[ERROR] Article could not be parsed to extract a sentiment score.")
								sql"insert into articles(stock,date,source,title) values (${ticker}, ${date}, ${url}, ${title})".update.apply()
							}
						}
					}
				}
				println("\n[SYSTEM] Recalculating stock scores for "+stockName+".")
				Scorer.doStock(ticker)
				}
			}
			
			/** Twitter processing *
			  * sentiment scoring is based on the Sentiment140 API's scheme *
			  * 	0 -> negative, 1 -> neutral, 2 -> positive *
			  */
			val processTweets : PROC = proc {
				var results : List[(String,Array[(Status,Int)])] = TweetProcessor.getTwitterSentimentScores(companies)
				var tweetIds = sql"select tweetID from tweets".map(v => v.string("tweetID")).list.apply() // ids of tweets already in the DB
				for ((stock,scores) <- results) {
					for ((tweet,score) <- scores) {
						val id = tweet.getId()
						if (!(tweetIds contains id.toString())) {
							tweetIds = id.toString() :: tweetIds
							val url = TweetProcessor.getTweetUrl(tweet)
							val text = tweet.getText()
						    val popularity = TweetProcessor.getTweetPopularity(tweet)
						    val importance = TweetProcessor.getTweetImportance(tweet)
							val date : java.sql.Date = TweetProcessor.getTweetDate(tweet)
							try {
								sql"insert into tweets(tweetID, stock,date,source,text,importance,sentiment) values (${id}, ${stock}, ${date}, ${url}, ${text}, ${importance}, ${score})".update.apply()
								println("\n[DATABASE] Tweet "+id+" successfully added to database.")
							} catch {
								case e : Exception => println("\n[ERROR] Tweet couldn't be added to the database")
							}
						}
					}
				}
			}

			/** Article and Twitter processing in parallel */
			val System : PROC = {
				repeat {
					println("[SYSTEM] Beginning data refresh.")
					SiteRank.purgeCache()

					(processArticles || processTweets)()
					
					// clean up directory
					val entries = dir.list();
						for (s <- entries) {
    					var currentFile = new File(dir.getPath(),s);
    					currentFile.delete();
					}

					dir.delete()
					
					println("\n\[SYSTEM] Finished data refresh.")
					println("\n[SYSTEM] System thread sleeping for 60 mins")
					wait(3600) 
				}
			}
		System()
		DBs.closeAll()
	} 
}