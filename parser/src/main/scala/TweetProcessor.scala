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

/** tweets are passed to the AlchemyAPI by text */

object TweetProcessor {
	val conf = ConfigFactory.load();	
	val alchemyKey = conf.getString("apikeys.alchemy");
	val alchemyObj = AlchemyAPI.GetInstanceFromString(alchemyKey);
	val sentimentParams = new AlchemyAPI_TargetedSentimentParams(); // a parameter object 
	var tweet : Status = null.asInstanceOf[Status]
	var query : String = ""

	/** returns the URL of a tweet
	  * https://twitter.com/[screen name of user]/status/[id of status] 
	  */
	def getTweetUrl(tweet : Status) : String = {
		return "http://twitter.com/"+ tweet.getUser().getScreenName() + "/status/" + tweet.getId()
	}

	/** returns the creation date for a tweet */
	def getTweetDate(tweet : Status) : Date = {
		return tweet.getCreatedAt()
	}

	/* returns (sentiment score, popularity score) */
	def processTweet(tweet : Status, query : String) : (Int,Int) = {
		this.tweet = tweet; this.query = query;
		return (getSentimentScore(), getPopularityScore())
	}

	private def getSentimentScore() : Int = {
		val url = getTweetUrl(tweet)
		val text = tweet.getText()
		try {
			val alchemyResult = ArticleProcessor.asXml(alchemyObj.TextGetTargetedSentiment(text,query,sentimentParams));
			val importance = SiteRank.getPopularity(url);
			return Math.round((1+ArticleProcessor.getSentimentScore(alchemyResult))*50);
		} catch {
			case e : Exception => throw new IllegalArgumentException(query+" couldn't be found in :"+ url);
		}
	}

	//////////////////////////////////////////////////
	/******************** TO DO *********************/
	//////////////////////////////////////////////////

	private def getPopularityScore() : Int = {
		val retweets = tweet.getRetweetCount()
		val favorites = tweet.getFavoriteCount()
		// to do ...
		return 1 // temporary
	}
}