import twitter4j._
import scala.xml._;
import scala.xml.Elem._;
import scala.xml.Node._;
import org.joda.time.DateTime
import javax.xml.transform.sax.SAXResult;
import scala.xml.parsing.NoBindingFactoryAdapter;
import com.typesafe.config.ConfigFactory;
import com.alchemyapi.api.AlchemyAPI
import com.alchemyapi.api._
import org.w3c.dom.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Date

object TweetProcessor {
	val conf = ConfigFactory.load();	
	val alchemyKey = conf.getString("apikeys.alchemy");
	val alchemyObj = AlchemyAPI.GetInstanceFromString(alchemyKey);
	val sentimentParams = new AlchemyAPI_TargetedSentimentParams(); // a parameter object 

	// https://twitter.com/[screen name of user]/status/[id of status] 
	
	// returns (sentiment score, popularity score)
	def processTweet(tweet : Status, query : String) : (Int,Int) = {
		val url = "http://twitter.com/"+ tweet.getUser().getScreenName() + "/status/" + tweet.getId()
		val retweets = tweet.getRetweetCount()
		val favorites = tweet.getFavoriteCount()
		return (getSentimentScore(url,query),getPopularityScore(retweets,favorites))
	}

	/////////////////////////////////////////////////////////
	/******************** NEEDS FIXING *********************/
	/****  i.e. different AlchemyObj method and params  ****/
	/////////////////////////////////////////////////////////

	private def getSentimentScore(url : String, query : String) : Int = {
		try {
			val alchemyResult = ArticleProcessor.asXml(alchemyObj.URLGetTargetedSentiment(url,query,sentimentParams));
			val importance = SiteRank.getPopularity(url);
			return Math.round((1+ArticleProcessor.getSentimentScore(alchemyResult))*50);
		} catch {
			case e : Exception => throw new IllegalArgumentException(query+" couldn't be found in "+url);
		}
	}

	//////////////////////////////////////////////////
	/******************** TO DO *********************/
	//////////////////////////////////////////////////

	private def getPopularityScore(retweets : Int, favorites : Int) : Int = {
		return 1 // temporary def
	}
}