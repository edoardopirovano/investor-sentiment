import scalikejdbc._
import scalikejdbc.config._
import org.joda.time._
import com.typesafe.config.ConfigFactory

object Scorer {
	val conf = ConfigFactory.load();
	val baseImportance = conf.getInt("scorer.baseImportance");
	
	// Make Article class and companion object with constructor
	case class Article(id: Long, date: DateTime, importance: Int, sentiment: Int)
	object Article extends SQLSyntaxSupport[Article] {
	  override val tableName = "articles"
	  def apply(rs: WrappedResultSet): Article = new Article(
	    rs.long("id"), rs.jodaDateTime("date"), rs.int("importance"), rs.int("sentiment"))
	}
	
	// Recalculate all daily scores for a given stock and update the database
	def doStock(stock: String)(implicit s: DBSession = AutoSession) = {
		sql"delete from scores where stock = ${stock}".update.apply()
		val articles = sql"select * from articles where stock = ${stock} where sentiment is not null order by date".map(rs => Article(rs)).list.apply()
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
				volume += article.importance+baseImportance
				sentimenttotal += (article.importance+baseImportance)*article.sentiment
			}
			val sentiment = sentimenttotal/volume
			sql"insert into scores(stock,date,sentiment,volume) values (${stock}, ${date}, ${sentiment}, ${volume})".update.apply()
		}
	}
	
}