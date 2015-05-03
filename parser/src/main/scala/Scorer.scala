import scalikejdbc._
import scalikejdbc.config._
import org.joda.time._
 
object Scorer {
	
	// Make Article class and companion object with constructor
	case class Article(id: Long, stock: String, date: DateTime, importance: Int, sentiment: Int)
	object Article extends SQLSyntaxSupport[Article] {
	  override val tableName = "articles"
	  def apply(rs: WrappedResultSet): Article = new Article(
	    rs.long("id"), rs.string("stock"), rs.jodaDateTime("date"), rs.int("importance"), rs.int("sentiment"))
	}
	
	// Recalculate all daily scores for a given stock and update the database
	def doStock(stock: String)(implicit s: DBSession = AutoSession) = {
		sql"delete from scores where stock = ${stock}".update.apply()
		val articles = sql"select * from articles where stock = ${stock} order by date".map(rs => Article(rs)).list.apply()
		if (!articles.isEmpty) {
			var date = articles.head.date
			var sentimenttotal = 0
			var volume = 0
			for (article <- articles) {
				if (DateTimeComparator.getDateOnlyInstance().compare(date, article.date) != 0) {
					val sentiment = sentimenttotal/volume
					sql"insert into scores(stock,date,sentiment,volume) values (${stock}, ${date}, ${sentiment}, ${volume})".update.apply()
					date = article.date
					sentimenttotal = 0
					volume = 0
				}
				volume += article.importance
				sentimenttotal += article.importance*article.sentiment
			}
			val sentiment = sentimenttotal/volume
			sql"insert into scores(stock,date,sentiment,volume) values (${stock}, ${date}, ${sentiment}, ${volume})".update.apply()
		}
	}
	
  def main(args : Array[String]) {
		DBs.setupAll()
		implicit val session = AutoSession
		val stocks = sql"select stock from stocks".map(rs => rs.string("stock")).list.apply()
		for (stock <- stocks) doStock(stock)
		DBs.closeAll()
	}
	
}