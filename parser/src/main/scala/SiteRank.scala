import scala.xml.XML
 
object SiteRank { 
	private def rankToPopularity(rank: Int): Int = {
		val lower = 10
		val upper = 10000
		println(rank)
		if (rank < lower) return 100
		if (rank > upper) return 0
		return Math.round(((upper-rank).toFloat/(upper-lower))*100)
	}
  def getPopularity(url: String):Int = {
		val xml = XML.load("http://data.alexa.com/data?cli=10&url="+url)
		val rank = ((xml \\ "REACH").head) \ "@RANK"
		return rankToPopularity(rank.toString.toInt)
	}
  def main(args : Array[String]) {
		println(getPopularity("http://forbes.com"))
	}
}