import scala.xml.XML
import scala.collection.mutable.HashMap
import java.net.URLEncoder
import com.github.john_kurkowski.tldextract._

object SiteRank {
	val cache = HashMap(
		"yahoo.com" -> 5000
	)
	private def rankToPopularity(rank: Int): Int = {
		val lower = 10
		val upper = 10000
		if (rank < lower) return 100
		if (rank > upper) return 0
		return Math.round(((upper-rank).toFloat/(upper-lower))*100)
	}
  def getPopularity(url: String):Int = {
		var root = url
		SplitHost.fromURL(url) match {
			case SplitHost(subdomain, domain, tld) => root = domain+"."+tld
		}
		if (!cache.contains(root)) {
			val xml = XML.load("http://data.alexa.com/data?cli=10&url="+URLEncoder.encode(root, "UTF-8"))
			val rank = ((xml \\ "REACH").head) \ "@RANK"
			cache += ((root, rank.toString.toInt))
		}
		return rankToPopularity(cache(root))
	}
}