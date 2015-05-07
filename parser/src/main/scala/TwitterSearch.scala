import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import scala.collection.JavaConversions._
import com.github.nscala_time.time.Imports._
import com.typesafe.config.ConfigFactory
import java.util.Date
 
object TwitterSearch { 
	val conf = ConfigFactory.load()
	val consumerKey = conf.getString("apikeys.twitterConsumer")
	val consumerSecret = conf.getString("apikeys.twitterSecret")

	// Build a configuration to fetch the authentication token.
	val builder = new ConfigurationBuilder()
	  .setApplicationOnlyAuthEnabled(true)
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
    val token = new TwitterFactory(builder.build()).getInstance().getOAuth2Token()

	// Build a configuration and corresponding Twitter object to use.
    val cb = new ConfigurationBuilder()
	    cb.setDebugEnabled(true)
		.setApplicationOnlyAuthEnabled(true)
	    .setOAuthConsumerKey(consumerKey)
	    .setOAuthConsumerSecret(consumerSecret)
		.setOAuth2TokenType(token.getTokenType())
		.setOAuth2AccessToken(token.getAccessToken())
    val tf = new TwitterFactory(cb.build())
    val twitter = tf.getInstance()
    val tweetLimit = conf.getString("limits.tweets").toInt; // tweets per query

   // returns an array of tweet objects that are confirmed to be written in English (for the AlchemyAPI)
   def getTweets(entity : String) : Array[Status] = {
   		val data = new Array[Status](tweetLimit)
   		val query = new Query(entity); query.setCount(tweetLimit)
   		try {
   			val result = twitter.search(query)
   			val tweets = result.getTweets()
   			var i = 0
   			for (tweet <- tweets) {]
          if (tweet.getLang() == "en") data(i) = tweet; i += 1
   			}
   		} catch {
   			case e : Exception => throw new TwitterException("Couldn't connect to Twitter API")
   		}
   		return data
   }
   
}