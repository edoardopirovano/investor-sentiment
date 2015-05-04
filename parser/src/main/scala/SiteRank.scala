import scala.xml.XML
import scala.collection.mutable.HashMap
import java.net.URLEncoder
import com.github.john_kurkowski.tldextract._
import com.typesafe.config.ConfigFactory

object SiteRank {
	val conf = ConfigFactory.load()
	val lowerBound = conf.getInt("siterank.lowerBound")
	val upperBound = conf.getInt("siterank.upperBound")
	val cache = HashMap(
		"yahoo.com" -> 5000
	)
	private def rankToPopularity(rank: Int): Int = {
		if (rank < lowerBound) return 100
		if (rank > upperBound) return 0
		return Math.round(((upperBound-rank).toFloat/(upperBound-lowerBound))*100)
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