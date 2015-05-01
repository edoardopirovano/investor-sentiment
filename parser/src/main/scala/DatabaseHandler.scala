import scalikejdbc._
import scalikejdbc.config._
 
object DatabaseHandler { 
  def main(args : Array[String]) {
		DBs.setupAll()
		implicit val session = AutoSession
		sql"insert into scores(stock,date,sentiment,volume) values ('GOOG', '2015-04-04', 60, 60)".update.apply()
		DBs.closeAll()
	}
}